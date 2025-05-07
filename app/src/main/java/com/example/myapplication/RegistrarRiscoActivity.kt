package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class RegistrarRiscoActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private val REQUEST_LOCATION_PERMISSION = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_risco)

        val botaoRetornar = findViewById<ImageButton>(R.id.button_back)
        val botaoRegistrarRisco = findViewById<Button>(R.id.button_register_risk)
        val descricaoRisco = findViewById<EditText>(R.id.edit_description)
        val localReferenciaRisco = findViewById<EditText>(R.id.edit_local_referencia)

        // Inicializa o cliente de localização
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        botaoRetornar.setOnClickListener {
            finish()
        }

        botaoRegistrarRisco.setOnClickListener {
            // Verifica se a permissão foi concedida
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
                return@setOnClickListener
            }

            // Cria uma requisição de localização mais precisa e ativa
            locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 3000 // tempo em ms
            ).setMaxUpdates(1).build()

            // Define o callback que será chamado quando a localização for recebida
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location: Location? = locationResult.lastLocation
                    if (location != null) {
                        salvarRiscoComLocalizacao(
                            descricaoRisco.text.toString(),
                            localReferenciaRisco.text.toString(),
                            location.latitude,
                            location.longitude
                        )
                    } else {
                        Toast.makeText(
                            this@RegistrarRiscoActivity,
                            "Não foi possível obter a localização.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            // Solicita a localização
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                mainLooper
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun salvarRiscoComLocalizacao(
        descricao: String,
        localReferencia: String,
        latitude: Double,
        longitude: Double
    ) {
        val dataRegistro = Calendar.getInstance().time
        val emailUsuario = FirebaseAuth.getInstance().currentUser?.email

        val risco = hashMapOf(
            "descricao" to descricao,
            "data" to dataRegistro.toString(),
            "localReferencia" to localReferencia,
            "emailUsuario" to emailUsuario,
            "latitude" to latitude,
            "longitude" to longitude
        )

        db.collection("riscos")
            .add(risco)
            .addOnSuccessListener {
                Toast.makeText(this, "Risco registrado com localização!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao registrar risco.", Toast.LENGTH_SHORT).show()
            }
    }
}
