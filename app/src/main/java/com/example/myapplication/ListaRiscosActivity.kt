package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class ListaRiscosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_riscos)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_riscos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val botaoAdicionar = findViewById<Button>(R.id.buttonAdd)
        val botaoDeslogar = findViewById<FrameLayout>(R.id.back_container)

        // Lista de riscos
        val listaRiscos = listOf(
            Risco("Risco 1", "Informação 1", "01/05/2025", "Local A"),
            Risco("Risco 2", "Informação 2", "02/05/2025", "Local B"),
            Risco("Risco 3", "Informação 3", "03/05/2025", "Local C"),
            Risco("Risco 4", "Informação 4", "04/05/2025", "Local D"),
            Risco("Risco 5", "Informação 5", "05/05/2025", "Local E"),
            Risco("Risco 6", "Informação 6", "06/05/2025", "Local F")
        )

        recyclerView.adapter = MeuAdapter(listaRiscos)

        botaoAdicionar.setOnClickListener {
            val intent = Intent(this, RegistrarRiscoActivity::class.java)
            startActivity(intent)
        }

        botaoDeslogar.setOnClickListener {
            // Lógica que elimina autenticação
            Firebase.auth.signOut()
            finish()
        }
    }
}