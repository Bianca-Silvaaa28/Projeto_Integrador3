package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ListaRiscosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_riscos)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_riscos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val botaoAdicionar = findViewById<Button>(R.id.buttonAdd)

        // Lista de riscos
        val listaRiscos = listOf(
            Risco("Risco 1", "Informação 1", "01/05/2025", "Local A"),
            Risco("Risco 2", "Informação 2", "02/05/2025", "Local B"),
            Risco("Risco 3", "Informação 3", "03/05/2025", "Local C")
        )

        recyclerView.adapter = MeuAdapter(listaRiscos)

        botaoAdicionar.setOnClickListener {
            val intent = Intent(this, RegistrarRiscoActivity::class.java)
            startActivity(intent)
        }
    }
}