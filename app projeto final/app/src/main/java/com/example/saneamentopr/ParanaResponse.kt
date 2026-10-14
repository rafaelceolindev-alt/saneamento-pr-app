package com.example.saneamentopr

data class ParanaResponse(
    val features: List<Feature>?
)

data class Feature(
    val attributes: Atributos?
)

data class Atributos(
    val NmMunicipio: String?,
    val IDHM2010: Double?,
    val IDHM2010Classes: String?,
    val P_ComRedeAbstDeAguaUrbSede: Double?,
    val P_SemRedeAbstDeAguaUrbSede: Double?,
    val P_SemInfoRedeAbstDeAguaUrbSede: Double?,
    val P_ComRedeColetaEsgUrbSede: Double?,
    val P_SemRedeColetaEsgUrbSede: Double?,
    val P_SemInfoRedeColetaEsgUrbSede: Double?
)
