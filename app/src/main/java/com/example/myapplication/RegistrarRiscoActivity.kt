package com.example.myapplication

import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class RegistrarRiscoActivity: AppCompatActivity()  {

    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_risco)

        val botaoRetornar = findViewById<ImageButton>(R.id.button_back)

        val botaoRegistrarRisco = findViewById<Button>(R.id.button_register_risk)

        val descricaoRisco = findViewById<EditText>(R.id.edit_description)
        val localReferenciaRisco = findViewById<EditText>(R.id.edit_local_referencia)

        botaoRetornar.setOnClickListener {
            finish()
        }

        botaoRegistrarRisco.setOnClickListener {
            val dataRegistroRisco = Calendar.getInstance().time
            val emailUsuario = FirebaseAuth.getInstance().currentUser?.email

            val registroRisco = Risco(
                descricaoRisco.text.toString(),
                dataRegistroRisco.toString(),
                localReferenciaRisco.text.toString(),
                emailUsuario.toString())

            db.collection("riscos")
            .add(registroRisco)
            .addOnSuccessListener { _ ->
                Toast.makeText(
                    baseContext,
                    "Risco registrado com sucesso!",
                    Toast.LENGTH_SHORT,
                ).show()
            }
            .addOnFailureListener { _ ->
                Toast.makeText(
                    baseContext,
                    "Falha no registro de risco. Por favor, tente novamente!",
                    Toast.LENGTH_SHORT,
                ).show()
            }

            finish()
        }
    }
}