package com.example.myapplication

import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ListaRiscosActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_riscos)

        recyclerView = findViewById<RecyclerView>(R.id.recycler_riscos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val botaoAdicionar = findViewById<Button>(R.id.buttonAdd)
        val botaoDeslogar = findViewById<FrameLayout>(R.id.back_container)

        obterDadosBD()

        botaoAdicionar.setOnClickListener {
            val intent = Intent(this, RegistrarRiscoActivity::class.java)
            startActivity(intent)
        }

        botaoDeslogar.setOnClickListener {
            Firebase.auth.signOut()
            finish()
        }
    }

    private fun obterDadosBD()
    {
        val emailUsuario = FirebaseAuth.getInstance().currentUser?.email

        db.collection("riscos")
            .whereEqualTo("emailUsuario", emailUsuario)
            .get()
            .addOnSuccessListener { documents ->
                listarRiscosRegistrados(documents)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Não foi possível obter a lista de riscos registrados.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun listarRiscosRegistrados(documents: QuerySnapshot){
        var listaRiscos = mutableListOf<Risco>()

        for (document in documents) {
            val descricao = document.data.get("descricao").toString()
            val localReferencia = document.data.get("localReferencia").toString()
            val emailUsuario = document.data.get("localReferencia").toString()
            val latitude = document.data.get("latitude").toString()
            val longitude = document.data.get("longitude").toString()
            val imagemBase64 = document.data.get("imagemBase64").toString()

            val timestamp = document.getTimestamp("data")
            val dataFormatada = timestamp?.toDate()?.let { formatarData(it) }

            val risco = Risco(
                descricao,
                dataFormatada ?: "",
                localReferencia,
                emailUsuario,
                latitude,
                longitude,
                imagemBase64)
            listaRiscos.add(risco)
        }

        if (listaRiscos.size > 0)
            findViewById<TextView>(R.id.tv_sem_risco).visibility = View.GONE

        recyclerView.adapter = MeuAdapter(listaRiscos)
    }

    private fun formatarData(data: Date): String {
        val formatoDesejado = SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault())
        return formatoDesejado.format(data)
    }

    override fun onResume() {
        super.onResume()

        obterDadosBD()
    }
}