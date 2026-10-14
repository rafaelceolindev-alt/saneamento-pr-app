package com.example.saneamentopr

// Modelo de resposta principal da API Paraná Interativo
data class ViaCepResponse(
    val cep: String?,
    val municipio: String?,
    val ibge: String?,
    val logradouro: String?,
    val coordenadas: Coordenadas?,
    val dados: DadosSaneamento?
)

// Informações de cobertura de saneamento
data class DadosSaneamento(
    val P_ComRedeAbstDeAguaUrbSede: Float?,
    val P_ComRedeColetaEsgUrbSede: Float?
)
