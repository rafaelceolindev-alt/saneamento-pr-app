package com.example.saneamentopr

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    val backend: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5000/") // ✅ endereço especial do host no emulador
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

