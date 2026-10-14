package com.example.saneamentopr

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("infra/{cep}")
    fun consultarCep(@Path("cep") cep: String): Call<ViaCepResponse>
}
