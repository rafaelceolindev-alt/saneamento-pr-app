from flask import Flask, jsonify
from flask_cors import CORS
import requests
import sqlite3
import urllib3
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)
from datetime import datetime
from functools import lru_cache

app = Flask(__name__)
CORS(app, resources={r"/*": {"origins": "*"}})

# === DB - Banco de dados ===
def init_db():
    conn = sqlite3.connect("consultas.db")
    c = conn.cursor()
    c.execute("""
        CREATE TABLE IF NOT EXISTS consultas (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            cep TEXT,
            municipio TEXT,
            ibge TEXT,
            data_consulta TEXT,
            dados_json TEXT
        )
    """)
    conn.commit()
    conn.close()

init_db()

# === Funções e coleta de dados das APIS ===
@lru_cache(maxsize=32)
def consultar_cep(cep):
    if len(cep) != 8 or not cep.isdigit():
        return None
    try:
        r = requests.get(f"https://viacep.com.br/ws/{cep}/json/", timeout=10)
        r.raise_for_status()
        return r.json()
    except Exception:
        return None

@lru_cache(maxsize=32)
def consultar_parana_interativo(cod_ibge):
    url = "https://paranainterativo.pr.gov.br/interativo/rest/services/BaseDadosParanaInterativo/MapServer/6/query"
    params = {
        "where": f"CdMunicipioIBGE='{cod_ibge}'",
        "outFields": "*",
        "returnGeometry": "false",
        "f": "json"
    }
    try:
        r = requests.get(url, params=params, timeout=30, verify=False)
        r.raise_for_status()
        data = r.json()
        if 'features' in data and data['features']:
            return data['features'][0]['attributes']
        return None
    except Exception as e:
        print(f"Erro Paraná Interativo: {e}")
        return None

# === Rotas ===
@app.route('/consulta/<cep>', methods=['GET'])
def consulta(cep):
    dados_cep = consultar_cep(cep)
    if not dados_cep or 'erro' in dados_cep:
        return jsonify({'erro': 'CEP inválido'}), 400

    if dados_cep.get('uf') != 'PR':
        return jsonify({'erro': 'Fora do Paraná'}), 400

    cod_ibge = dados_cep.get('ibge')
    dados_saneamento = consultar_parana_interativo(cod_ibge)

    if not dados_saneamento:
        return jsonify({
            "cep": cep,
            "municipio": dados_cep.get('localidade'),
            "ibge": cod_ibge,
            "erro": "Falha ao consultar dados de saneamento"
        }), 502

    # Salvar histórico
    conn = sqlite3.connect("consultas.db")
    c = conn.cursor()
    c.execute("""
        INSERT INTO consultas (cep, municipio, ibge, data_consulta, dados_json)
        VALUES (?, ?, ?, ?, ?)
    """, (
        cep,
        dados_cep.get('localidade'),
        cod_ibge,
        datetime.now().isoformat(),
        str(dados_saneamento)
    ))
    conn.commit()
    conn.close()

    return jsonify({
        "cep": cep,
        "municipio": dados_cep.get('localidade'),
        "ibge": cod_ibge,
        "dados": dados_saneamento or {}
    })

@app.route('/historico', methods=['GET'])
def historico():
    conn = sqlite3.connect("consultas.db")
    c = conn.cursor()
    c.execute("SELECT cep, municipio, ibge, data_consulta FROM consultas ORDER BY id DESC LIMIT 10")
    rows = c.fetchall()
    conn.close()
    return jsonify([
        {"cep": r[0], "municipio": r[1], "ibge": r[2], "data_consulta": r[3]} for r in rows
    ])

import urllib.parse

# ============================================
# Geocodificação via IBGE API - para os mapas
# ============================================
def geocodificar_endereco(logradouro, municipio, uf):
    """
    Usa o Nominatim (OpenStreetMap) para obter latitude e longitude do endereço.
    Retorna (lat, lon) ou None se falhar.
    """
    try:
        # Se vier logradouro vazio, tenta só município + UF
        if logradouro:
            endereco = f"{logradouro}, {municipio}, {uf}"
        else:
            endereco = f"{municipio}, {uf}"

        params = {
            "q": endereco,
            "format": "json",
            "limit": 1
        }

        # Nominatim exige User-Agent identificando o app
        headers = {
            "User-Agent": "SaneamentoPR/1.0 (uso acadêmico)"
        }

        url = "https://nominatim.openstreetmap.org/search"
        r = requests.get(url, params=params, headers=headers, timeout=10)
        r.raise_for_status()
        data = r.json()

        if data and len(data) > 0:
            lat = float(data[0]["lat"])
            lon = float(data[0]["lon"])
            return lat, lon

        print("Geocodificação: nenhum resultado encontrado para:", endereco)
        return None

    except Exception as e:
        print(f"Erro na geocodificação: {e}")
        return None



# ============================================
# Consulta lat, lon API - Paraná Interativo
# ============================================
def consultar_infraestrutura(lat, lon, layer_id):
    """
    Consulta se o ponto informado (lat, lon) está dentro da área de cobertura
    do layer informado (1 = água, 2 = esgoto).
    Retorna True se houver interseção, False caso contrário.
    """
    try:
        url = f"https://paranainterativo.pr.gov.br/interativo/rest/services/BaseDadosParanaInterativo/MapServer/{layer_id}/query"
        params = {
            "geometry": f"{lon},{lat}",  # ordem correta: longitude,latitude
            "geometryType": "esriGeometryPoint",
            "inSR": 4326,
            "spatialRel": "esriSpatialRelIntersects",
            "returnGeometry": "false",
            "outFields": "*",
            "f": "json"
        }

        r = requests.get(url, params=params, timeout=20, verify=False)
        r.raise_for_status()
        data = r.json()
        return bool(data.get("features"))
    except Exception as e:
        print(f"Erro na consulta ao layer {layer_id}: {e}")
        return False


# ============================================
# /infra/<cep>
# ============================================
@app.route("/infra/<cep>", methods=["GET"])
def infra_por_cep(cep):
    """
    Retorna informações de infraestrutura de água e esgoto
    para o endereço correspondente ao CEP.
    """
    dados_cep = consultar_cep(cep)
    if not dados_cep or "erro" in dados_cep:
        return jsonify({"erro": "CEP inválido"}), 400

    logradouro = dados_cep.get("logradouro", "")
    municipio = dados_cep.get("localidade", "")
    uf = dados_cep.get("uf", "")

    if uf != "PR":
        return jsonify({"erro": "Fora do Paraná"}), 400

    coordenadas = geocodificar_endereco(logradouro, municipio, uf)
    if not coordenadas:
        return jsonify({"erro": "Não foi possível geocodificar o endereço"}), 500

    lat, lon = coordenadas

    # Consulta cobertura nos layers do Paraná Interativo
    agua = consultar_infraestrutura(lat, lon, layer_id=1)   # Água
    esgoto = consultar_infraestrutura(lat, lon, layer_id=2) # Esgoto

    resposta = {
        "cep": cep,
        "logradouro": logradouro,
        "municipio": municipio,
        "uf": uf,
        "coordenadas": {"lat": lat, "lon": lon},
        "agua_atendida": agua,
        "esgoto_atendido": esgoto
    }

    return jsonify(resposta)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)

