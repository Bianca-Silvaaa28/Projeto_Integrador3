package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class BoasVindasActivity : AppCompatActivity() {

    private val REQUEST_ALL_PERMISSIONS = 1001

    private val PREF_NAME = "permissoes"
    private val KEY_PERMISSOES_SOLICITADAS = "permissoes_solicitadas"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_boas_vindas)

        if (jaSolicitouPermissoes()) {
            iniciarLoginComDelay()
        } else {
            verificarOuSolicitarPermissoes()
        }
    }

    private fun verificarOuSolicitarPermissoes() {
        val permissoesNecessarias = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }

        val permissoesNegadas = permissoesNecessarias.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissoesNegadas.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissoesNegadas.toTypedArray(), REQUEST_ALL_PERMISSIONS)
        } else {
            iniciarLoginComDelay()
        }
    }

    private fun iniciarLoginComDelay() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 2000)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_ALL_PERMISSIONS) {
            salvarQueJaSolicitou()

            val todasConcedidas = grantResults.all { it == PackageManager.PERMISSION_GRANTED }

            if (todasConcedidas) {
                Toast.makeText(this, "Todas as permissões concedidas", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Algumas permissões foram negadas. O app pode não funcionar corretamente.", Toast.LENGTH_LONG).show()
            }

            iniciarLoginComDelay()
        }
    }

    private fun salvarQueJaSolicitou() {
        val prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_PERMISSOES_SOLICITADAS, true).apply()
    }

    private fun jaSolicitouPermissoes(): Boolean {
        val prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        return prefs.getBoolean(KEY_PERMISSOES_SOLICITADAS, false)
    }
}