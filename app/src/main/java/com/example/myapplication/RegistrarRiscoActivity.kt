package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegistrarRiscoActivity : AppCompatActivity() {

    private val db = Firebase.firestore

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private lateinit var botaoRetornar: ImageButton
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
    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestStoragePermissionLauncher: ActivityResultLauncher<Array<String>>

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1001
        private const val TAG = "RegistrarRiscoActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_risco)

        botaoRetornar = findViewById(R.id.button_back)
        botaoRegistrarRisco = findViewById(R.id.button_register_risk)
        descricaoRisco = findViewById(R.id.edit_description)
        localReferenciaRisco = findViewById(R.id.edit_local_referencia)
        botaoCamera = findViewById(R.id.button_camera)
        botaoAnexo = findViewById(R.id.button_attachment)
        imagePreview = findViewById(R.id.image_preview)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupPermissionLaunchers()
        setupImageLaunchers()

        botaoRetornar.setOnClickListener { finish() }

        botaoCamera.setOnClickListener { handleCameraPermission() }
        botaoAnexo.setOnClickListener { handleStoragePermission() }

        botaoRegistrarRisco.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
                return@setOnClickListener
            }
            if (anexoPhotoUri != null) {
                uploadImageToCloudinary(anexoPhotoUri!!,
                    onSuccess = { url -> initiateLocationAndSaveRisk(url) },
                    onError = {
                        Toast.makeText(this, "Erro ao enviar imagem.", Toast.LENGTH_SHORT).show()
                        botaoRegistrarRisco.isEnabled = true
                    }
                )
            } else {
                initiateLocationAndSaveRisk(null)
            }
        }
    }

    private fun setupPermissionLaunchers() {
        requestCameraPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) openCamera()
                else Toast.makeText(this, "Permissão da câmera negada.", Toast.LENGTH_SHORT).show()
            }

        requestStoragePermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                val granted = permissions.values.any { it }
                if (granted) openGallery()
                else Toast.makeText(this, "Permissão de armazenamento negada.", Toast.LENGTH_SHORT).show()
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
            } else Toast.makeText(this, "Falha ao capturar imagem.", Toast.LENGTH_SHORT).show()
        }

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                anexoPhotoUri = it
                imagePreview.setImageURI(it)
                imagePreview.visibility = View.VISIBLE
            }
        }
    }

    private fun handleCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> openCamera()
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) -> {
                Toast.makeText(this, "Permissão da câmera é necessária.", Toast.LENGTH_LONG).show()
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        val photoFile = try { createImageFile() } catch (ex: Exception) {
            Log.e(TAG, "Erro ao criar arquivo de imagem", ex)
            Toast.makeText(this, "Erro ao preparar câmera.", Toast.LENGTH_SHORT).show()
            null
        }
        photoFile?.let {
            val photoURI = FileProvider.getUriForFile(this, "$packageName.provider", it)
            currentPhotoUri = photoURI
            takePictureLauncher.launch(photoURI)
        }
    }

    @Throws(Exception::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        if (storageDir == null || (!storageDir.exists() && !storageDir.mkdirs())) {
            throw Exception("Falha ao criar diretório de imagens.")
        }
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun handleStoragePermission() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        val granted = permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
        if (granted) openGallery()
        else requestStoragePermissionLauncher.launch(permissions)
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun initiateLocationAndSaveRisk(imageUrl: String?) {
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
                        location.longitude,
                        imageUrl
                    )
                } else {
                    Toast.makeText(this@RegistrarRiscoActivity, "Não foi possível obter a localização.", Toast.LENGTH_SHORT).show()
                    botaoRegistrarRisco.isEnabled = true
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }

    private fun uploadImageToCloudinary(imageUri: Uri, onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
        try {
            val inputStream = contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes() ?: throw Exception("Erro ao ler imagem")

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "image.jpg", bytes.toRequestBody("image/*".toMediaTypeOrNull()))
                .addFormDataPart("upload_preset", "YOUR_UPLOAD_PRESET")
                .build()

            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/YOUR_CLOUD_NAME/image/upload")
                .post(requestBody)
                .build()

            OkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) { onError(e) }
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        onError(IOException("Erro no upload: ${response.message}"))
                        return
                    }
                    val responseBody = response.body?.string()
                    val json = JSONObject(responseBody ?: "")
                    val url = json.getString("secure_url")
                    runOnUiThread { onSuccess(url) }
                }
            })

        } catch (e: Exception) {
            onError(e)
        }
    }

    private fun salvarRiscoComLocalizacao(descricao: String, localReferencia: String, latitude: Double, longitude: Double, imageUrl: String?) {
        if (descricao.isBlank()) {
            Toast.makeText(this, "A descrição do risco não pode estar vazia.", Toast.LENGTH_SHORT).show()
            botaoRegistrarRisco.isEnabled = true
            return
        }

        val risco = hashMapOf(
            "descricao" to descricao,
            "data" to Calendar.getInstance().time.toString(),
            "localReferencia" to localReferencia,
            "emailUsuario" to FirebaseAuth.getInstance().currentUser?.email,
            "latitude" to latitude,
            "longitude" to longitude
        )

        if (!imageUrl.isNullOrBlank()) {
            risco["imagemUrl"] = imageUrl
        }

        db.collection("riscos")
            .add(risco)
            .addOnSuccessListener {
                Toast.makeText(this, "Risco registrado com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao registrar risco.", Toast.LENGTH_SHORT).show()
                botaoRegistrarRisco.isEnabled = true
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initiateLocationAndSaveRisk(null)
        } else {
            Toast.makeText(this, "Permissão de localização negada.", Toast.LENGTH_SHORT).show()
            botaoRegistrarRisco.isEnabled = true
        }
    }
}