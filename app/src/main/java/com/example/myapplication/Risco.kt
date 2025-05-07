package com.example.myapplication

data class Risco(
    val descricao: String,
    val data: String,
    val localReferencia: String,
    // TODO: adicionar geolocalização
    val usuario: String?
)