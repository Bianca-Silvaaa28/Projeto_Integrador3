package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        var layoutEmail = findViewById<EditText>(R.id.userEmail)
        val layoutSenha = findViewById<EditText>(R.id.userPassword)

        val linkResetSenha = findViewById<TextView>(R.id.tvRecuperarAcesso)

        val botaoEntrar = findViewById<Button>(R.id.btnLogin)

        val linkCadastrar = findViewById<TextView>(R.id.tvCadastrar)

        botaoEntrar.setOnClickListener {
            val emailDigitado = layoutEmail.text.toString()
            val senhaDigitada = layoutSenha.text.toString()

            login(emailDigitado, senhaDigitada)
        }

        linkResetSenha.setOnClickListener {
            val intent = Intent(this, ResetSenhaActivity::class.java)
            startActivity(intent)
        }

        linkCadastrar.setOnClickListener {
            val intent = Intent(this, CadastrarUsuarioActivity::class.java)
            startActivity(intent)
        }
    }

    private fun login(email: String, psw: String) {
        auth.signInWithEmailAndPassword(email, psw)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // daqui dá pra direcionar pra outras atividades já logado
                    // por exemplo:
                     val intent = Intent(this, ListaRiscosActivity::class.java)
                     startActivity(intent)
                } else {
                    Toast.makeText(
                        baseContext,
                        "A autenticação falhou! Por favor, cheque o email e senha.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }
}
