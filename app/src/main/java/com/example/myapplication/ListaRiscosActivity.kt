package com.example.myapplication

import android.content.Intent
import android.os.Bundle
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

        botaoAdicionar.setOnClickListener {
            val intent = Intent(this, RegistrarRiscoActivity::class.java)
            startActivity(intent)
        }

        botaoDeslogar.setOnClickListener {
            Firebase.auth.signOut()
            finish()
        }
    }

    private fun listarRiscosRegistrados(documents: QuerySnapshot){
        var listaRiscos = mutableListOf<Risco>()

        for (document in documents) {
            val descricao = document.data.get("descricao").toString()
            val data = document.data.get("data").toString()
            val localReferencia = document.data.get("localReferencia").toString()
            val emailUsuario = document.data.get("localReferencia").toString()
            val latitude = document.data.get("latitude").toString()
            val longitude = document.data.get("longitude").toString()

            val dataFormatada = formatarData(data)

            val risco = Risco(
                descricao,
                dataFormatada ?: "",
                localReferencia,
                emailUsuario,
                latitude,
                longitude)
            listaRiscos.add(risco)
        }

        recyclerView.adapter = MeuAdapter(listaRiscos)
    }

    private fun formatarData(data: String): String?{

        val formatoOriginal = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val date: Date? = formatoOriginal.parse(data)
        val formatoDesejado = SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault())
        val dataFormatada = date?.let { formatoDesejado.format(it) }

        return dataFormatada
    }
}