package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class RegistrarRiscoActivity : AppCompatActivity() {

    private val db = Firebase.firestore

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private lateinit var botaoRegistrarRisco: Button
    private lateinit var descricaoRisco: EditText
    private lateinit var localReferenciaRisco: EditText
    private lateinit var botaoCamera: ImageButton
    private lateinit var botaoAnexo: ImageButton
    private lateinit var imagePreview: ImageView

    private var currentPhotoUri: Uri? = null
    private var anexoPhotoUri: Uri? = null

    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_risco)

        botaoRegistrarRisco = findViewById(R.id.button_register_risk)
        descricaoRisco = findViewById(R.id.edit_description)
        localReferenciaRisco = findViewById(R.id.edit_local_referencia)
        botaoCamera = findViewById(R.id.button_camera)
        botaoAnexo = findViewById(R.id.button_attachment)
        imagePreview = findViewById(R.id.image_preview)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupImageLaunchers()

        botaoCamera.setOnClickListener { openCamera() }
        botaoAnexo.setOnClickListener { openGallery() }

        botaoRegistrarRisco.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1001
                )
                return@setOnClickListener
            }
            initiateLocationAndSaveRisk()
        }
    }

    private fun setupImageLaunchers() {
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                currentPhotoUri?.let {
                    anexoPhotoUri = it
                    imagePreview.setImageURI(it)
                    imagePreview.visibility = View.VISIBLE
                }
            }
        }

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                anexoPhotoUri = it
                imagePreview.setImageURI(it)
                imagePreview.visibility = View.VISIBLE
            }
        }
    }

    private fun openCamera() {
        val photoFile = createImageFile()
        val photoUri = FileProvider.getUriForFile(
            this,
            "$packageName.provider",
            photoFile
        )
        currentPhotoUri = photoUri
        takePictureLauncher.launch(photoUri)
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun initiateLocationAndSaveRisk() {
        botaoRegistrarRisco.isEnabled = false

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000).setMaxUpdates(1).build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation
                if (location != null) {
                    salvarRiscoComLocalizacao(
                        descricaoRisco.text.toString(),
                        localReferenciaRisco.text.toString(),
                        location.latitude,
                        location.longitude
                    )
                } else {
                    Toast.makeText(this@RegistrarRiscoActivity, "Erro ao obter localização", Toast.LENGTH_SHORT).show()
                    botaoRegistrarRisco.isEnabled = true
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }

    private fun salvarRiscoComLocalizacao(descricao: String, localReferencia: String, latitude: Double, longitude: Double) {
        val dataRegistro = Date()
        val emailUsuario = FirebaseAuth.getInstance().currentUser?.email

        if (descricao.isEmpty() || localReferencia.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
            botaoRegistrarRisco.isEnabled = true
            return
        }

        if (anexoPhotoUri == null) {
            Toast.makeText(this, "Selecione uma imagem.", Toast.LENGTH_SHORT).show()
            botaoRegistrarRisco.isEnabled = true
            return
        }

        val imagemBase64 = uriToBase64(anexoPhotoUri!!)
        if (imagemBase64 == null) {
            Toast.makeText(this, "Erro ao processar imagem.", Toast.LENGTH_SHORT).show()
            botaoRegistrarRisco.isEnabled = true
            return
        }

        val risco = hashMapOf(
            "descricao" to descricao,
            "data" to dataRegistro.toString(),
            "localReferencia" to localReferencia,
            "emailUsuario" to emailUsuario,
            "latitude" to latitude,
            "longitude" to longitude,
            "imagemBase64" to imagemBase64
        )

        db.collection("riscos").add(risco)
            .addOnSuccessListener {
                Toast.makeText(this, "Risco salvo com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao salvar risco.", Toast.LENGTH_SHORT).show()
                botaoRegistrarRisco.isEnabled = true
            }
    }

    private fun uriToBase64(uri: Uri): String? {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val resized = resizeBitmap(bitmap, 1024)
        val baos = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val bytes = baos.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val ratio: Float = width.toFloat() / height.toFloat()
        return if (ratio > 1) {
            Bitmap.createScaledBitmap(bitmap, maxSize, (maxSize / ratio).toInt(), true)
        } else {
            Bitmap.createScaledBitmap(bitmap, (maxSize * ratio).toInt(), maxSize, true)
        }
    }
}