package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

//        val layoutEmail = findViewById<TextInputLayout>(R.id.editEmail)
//        val layoutSenha = findViewById<TextInputLayout>(R.id.editSenha)
//        val botaoEntrar = findViewById<Button>(R.id.button)

//        botaoEntrar.setOnClickListener {
////            val emailDigitado = layoutEmail.editText?.text.toString()
////            val senhaDigitada = layoutSenha.editText?.text.toString()
//
//            val emailCorreto = "bvds@aulakotlin.com"
//            val senhaCorreta = "123456"
//
////            if (emailDigitado == emailCorreto && senhaDigitada == senhaCorreta) {
////                val intent = Intent(this, BoasVindasActivity::class.java)
////                startActivity(intent)
////            } else {
////                Toast.makeText(this, "Email ou senha incorretos", Toast.LENGTH_SHORT).show()
////            }
//        }
    }
}
