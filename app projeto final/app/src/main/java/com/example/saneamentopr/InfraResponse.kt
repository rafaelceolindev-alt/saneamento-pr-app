package com.example.saneamentopr

data class InfraResponse(
    val cep: String?,
    val logradouro: String?,
    val municipio: String?,
    val uf: String?,
    val coordenadas: Coordenadas?,
    val agua_atendida: Boolean?,
    val esgoto_atendido: Boolean?
)
