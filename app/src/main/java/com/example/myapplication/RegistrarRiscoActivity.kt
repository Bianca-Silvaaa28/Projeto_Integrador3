package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class RegistrarRiscoActivity: AppCompatActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_risco)

        val botaoRetornar = findViewById<ImageButton>(R.id.button_back)

        val botaoRegistrarRisco = findViewById<Button>(R.id.button_register_risk)

        botaoRetornar.setOnClickListener {
            finish()
        }

        botaoRegistrarRisco.setOnClickListener {
            // implementar lógica de salvamento de dados no BD
            finish()
        }
    }
}