package com.example.saneamentopr

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

// Serviço para o endpoint /consulta/{cep} (Paraná Interativo)
interface ConsultaService{
    @GET("consulta/{cep}")
    fun consultarPercentuais(@Path("cep") cep: String): Call<ViaCepResponse>
}