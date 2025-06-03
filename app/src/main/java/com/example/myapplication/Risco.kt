package com.example.myapplication

data class Risco(
    val descricao: String,
    val data: String,
    val localReferencia: String,
    val emailUsuario: String,
    val latitude: String,
    val longitude: String,
    val imagemBase64: String?
)