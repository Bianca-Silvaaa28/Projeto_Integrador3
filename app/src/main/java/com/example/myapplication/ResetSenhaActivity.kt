package com.example.myapplication

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class ResetSenhaActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_senha)

        auth = Firebase.auth

        var layoutEmail = findViewById<EditText>(R.id.userEmail)
        val botaoResetSenha = findViewById<TextView>(R.id.btnResetSenha)

        botaoResetSenha.setOnClickListener {
            val emailDigitado = layoutEmail.text.toString()

            resetarSenha(emailDigitado)
        }
    }

    private fun resetarSenha(email: String) {
        Firebase.auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.isSuccessful) {
                        Toast.makeText(
                            baseContext,
                            "Email com instruções de recuperação de acesso enviado!",
                            Toast.LENGTH_SHORT,
                        ).show()
                        finish()
                    }
                }
            }
    }

    override fun onResume() {
        super.onResume()

        var layoutEmail = findViewById<EditText>(R.id.userEmail)

        layoutEmail.text.clear()
    }
}