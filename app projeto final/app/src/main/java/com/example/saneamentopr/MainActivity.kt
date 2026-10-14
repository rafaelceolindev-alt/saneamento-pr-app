package com.example.saneamentopr

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var cepInput: EditText
    private lateinit var buscarButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cepInput = findViewById(R.id.inputCep)
        buscarButton = findViewById(R.id.btnBuscar)

        buscarButton.setOnClickListener {
            val cep = cepInput.text.toString().trim()

            if (cep.length == 8 && cep.all { it.isDigit() }) {
                buscarCep(cep)
            } else {
                cepInput.error = "Digite um CEP válido (8 números)"
            }
        }
    }

    private fun buscarCep(cep: String) {
        val infraService = RetrofitClient.backend.create(InfraService::class.java)
        val consultaService = RetrofitClient.backend.create(ConsultaService::class.java)

        // Busca dados básicos e coordenadas
        infraService.getInfra(cep).enqueue(object : Callback<InfraResponse> {
            override fun onResponse(call: Call<InfraResponse>, response: Response<InfraResponse>) {
                if (response.isSuccessful) {
                    val infra = response.body()
                    if (infra != null) {

                        //Busca os percentuais de saneamento (Paraná Interativo)
                        consultaService.consultarPercentuais(cep).enqueue(object : Callback<ViaCepResponse> {
                            override fun onResponse(call: Call<ViaCepResponse>, response: Response<ViaCepResponse>) {
                                val consulta = response.body()

                                // Cria intent e envia tudo pra tela de resultado
                                val intent = Intent(this@MainActivity, ResultadoActivity::class.java)
                                intent.putExtra("cep", infra.cep)
                                intent.putExtra("municipio", infra.municipio)
                                intent.putExtra("logradouro", infra.logradouro)
                                intent.putExtra("uf", infra.uf)
                                intent.putExtra("lat", infra.coordenadas?.lat ?: -25.4284)
                                intent.putExtra("lon", infra.coordenadas?.lon ?: -49.2733)
                                intent.putExtra("agua_atendida", infra.agua_atendida)
                                intent.putExtra("esgoto_atendido", infra.esgoto_atendido)

                                // Percentuais reais vindos da API /consulta/
                                intent.putExtra("agua_percent", consulta?.dados?.P_ComRedeAbstDeAguaUrbSede ?: 0f)
                                intent.putExtra("esgoto_percent", consulta?.dados?.P_ComRedeColetaEsgUrbSede ?: 0f)

                                startActivity(intent)
                            }

                            override fun onFailure(call: Call<ViaCepResponse>, t: Throwable) {
                                Toast.makeText(applicationContext, "Erro ao obter percentuais: ${t.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                    } else {
                        Toast.makeText(applicationContext, "Nenhum dado retornado do backend.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(applicationContext, "Erro na consulta: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<InfraResponse>, t: Throwable) {
                Toast.makeText(applicationContext, "Falha na conexão: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


}

