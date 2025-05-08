package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class BoasVindasActivity : AppCompatActivity() {

    // Código usado para identificar a solicitação de permissão
    private val REQUEST_LOCATION_PERMISSION = 1001

    // Nome do arquivo SharedPreferences e chave usada para armazenar o status
    private val PREF_NAME = "permissoes"
    private val KEY_LOCALIZACAO_SOLICITADA = "localizacao_solicitada"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_boas_vindas)

        // Verifica se já foi solicitada a permissão anteriormente
        if (jaSolicitouPermissao()) {
            // Se sim, apenas aguarda 2 segundos e vai para a próxima tela
            iniciarLoginComDelay()
        } else {
            // Se não, solicita a permissão de localização
            verificarOuSolicitarPermissaoLocalizacao()
        }
    }

    // Função que verifica se a permissão de localização já foi concedida
    private fun verificarOuSolicitarPermissaoLocalizacao() {
        val permissao = Manifest.permission.ACCESS_FINE_LOCATION

        // Verifica se a permissão já foi concedida
        if (ContextCompat.checkSelfPermission(this, permissao) != PackageManager.PERMISSION_GRANTED) {
            // Se não, solicita a permissão
            ActivityCompat.requestPermissions(this, arrayOf(permissao), REQUEST_LOCATION_PERMISSION)
        } else {
            // Se já tem permissão, segue normalmente para o login com delay
            iniciarLoginComDelay()
        }
    }

    // Inicia a próxima tela com um atraso de 2 segundos (splash)
    private fun iniciarLoginComDelay() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 2000)
    }

    // Callback que é chamado quando o usuário responde à solicitação de permissão
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Verifica se a resposta é da permissão de localização
        if (requestCode == REQUEST_LOCATION_PERMISSION) {

            // Salva que a permissão já foi solicitada (independente da resposta)
            salvarQueJaSolicitou()

            // Se a permissão foi concedida
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissão de localização concedida", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissão negada. O app pode não funcionar corretamente.", Toast.LENGTH_LONG).show()
            }

            // Depois da resposta (sim ou não), segue normalmente para o login
            iniciarLoginComDelay()
        }
    }

    // Armazena no SharedPreferences que a permissão já foi solicitada
    private fun salvarQueJaSolicitou() {
        val prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_LOCALIZACAO_SOLICITADA, true).apply()
    }

    // Verifica se a permissão já foi solicitada anteriormente
    private fun jaSolicitouPermissao(): Boolean {
        val prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        return prefs.getBoolean(KEY_LOCALIZACAO_SOLICITADA, false)
    }
}
