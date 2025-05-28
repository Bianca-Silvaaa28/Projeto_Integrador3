package com.example.myapplication

import com.google.firebase.Timestamp

data class Risco(
    val descricao: String,
    val data: Timestamp,
    val localReferencia: String,
    val emailUsuario: String,
    val latitude: String,
    val longitude: String,
    val imagemBase64: String?
)