# Saneamento PR

App Android que, a partir de um CEP do Paraná, mostra os dados de saneamento do município e se o endereço tem rede de água e de esgoto.

Projeto final do curso de Tecnólogo em Banco de Dados (Uninter, 2025).

## Como funciona

```
App Android (Kotlin) ──► API Flask (Python) ──► ViaCEP · Paraná Interativo · OpenStreetMap
                                │
                                ▼
                     SQLite (histórico de consultas)
```

1. O usuário digita o CEP no app.
2. A API busca o endereço e o código IBGE do município no ViaCEP.
3. Com o código IBGE, consulta os indicadores de saneamento do município na base do Paraná Interativo (Governo do Paraná).
4. Para o mapa, converte o endereço em latitude e longitude pelo OpenStreetMap (Nominatim) e verifica se o ponto está dentro das áreas atendidas por água e por esgoto.
5. Cada consulta fica registrada no SQLite.

## Endpoints da API

| Rota | O que devolve |
|---|---|
| `GET /consulta/<cep>` | município, código IBGE e indicadores de saneamento |
| `GET /infra/<cep>` | coordenadas do endereço e se há rede de água e de esgoto |
| `GET /historico` | as 10 consultas mais recentes |

CEPs fora do Paraná ou inválidos devolvem erro 400.

## Tecnologias

- **App:** Kotlin, Retrofit, Google Maps SDK, MPAndroidChart
- **API:** Python, Flask, requests
- **Banco:** SQLite

## Como rodar

**API**

```bash
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
python app.py
```

A API sobe na porta 5000.

**App**

1. Abra a pasta `app projeto final` no Android Studio.
2. No arquivo `local.properties`, acrescente a linha `MAPS_API_KEY=sua_chave` com uma chave do Google Maps SDK for Android.
3. Rode no emulador. O app chama a API em `10.0.2.2:5000`, que é o endereço do computador visto de dentro do emulador.
