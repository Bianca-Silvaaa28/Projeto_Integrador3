package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class CadastrarUsuarioActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        auth = Firebase.auth

        var layoutEmail = findViewById<EditText>(R.id.userEmail)
        val layoutSenha = findViewById<EditText>(R.id.userPassword)
        val botaoCadastrar = findViewById<TextView>(R.id.btnCadastrar)

        botaoCadastrar.setOnClickListener {
            val emailDigitado = layoutEmail.text.toString()
            val senhaDigitada = layoutSenha.text.toString()

            cadastrar(emailDigitado, senhaDigitada)
        }
    }

    private fun cadastrar(email: String, psw: String) {
        auth.createUserWithEmailAndPassword(email, psw)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        baseContext,
                        "Cadastro realizado com sucesso!",
                        Toast.LENGTH_SHORT,
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        baseContext,
                        "O cadastro falhou! Por favor, tente mais tarde.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

    override fun onResume() {
        super.onResume()

        var layoutEmail = findViewById<EditText>(R.id.userEmail)
        val layoutSenha = findViewById<EditText>(R.id.userPassword)

        layoutEmail.text.clear()
        layoutSenha.text.clear()
    }
}