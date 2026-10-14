package com.example.saneamentopr

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface InfraService {
    @GET("infra/{cep}")
    fun getInfra(@Path("cep") cep: String): Call<InfraResponse>
}
