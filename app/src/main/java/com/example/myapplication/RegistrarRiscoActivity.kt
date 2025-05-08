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
import com.google.firebase.Firebase // KTX import
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth // KTX import
import com.google.firebase.firestore.firestore // KTX import
import com.google.firebase.firestore.ktx.firestore // KTX import
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegistrarRiscoActivity : AppCompatActivity() {

    private val db = Firebase.firestore // Firestore instance

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

    private var currentPhotoUri: Uri? = null // Para URI da foto tirada pela câmera
    private var anexoPhotoUri: Uri? = null // Para guardar URI da galeria ou câmera para preview

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

        //inicializa o cliente localização
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupPermissionLaunchers()
        setupImageLaunchers()

        botaoRetornar.setOnClickListener {
            finish()
        }

        botaoCamera.setOnClickListener {
            handleCameraPermission()
        }

        botaoAnexo.setOnClickListener {
            handleStoragePermission()
        }

        botaoRegistrarRisco.setOnClickListener {
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
            initiateLocationAndSaveRisk()
        }
    }


    private fun setupPermissionLaunchers() {
        requestCameraPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Permissão da câmera negada.", Toast.LENGTH_SHORT).show()
                }
            }

        requestStoragePermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                val readImagesGranted = permissions[Manifest.permission.READ_MEDIA_IMAGES] ?: false
                val readExternalStorageGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false

                if (readImagesGranted || readExternalStorageGranted) {
                    openGallery()
                } else {
                    Toast.makeText(this, "Permissão de armazenamento negada.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun setupImageLaunchers() {
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                currentPhotoUri?.let { uri ->
                    anexoPhotoUri = uri // Salva o URI para preview
                    imagePreview.setImageURI(uri)
                    imagePreview.visibility = View.VISIBLE
                    Log.d(TAG, "Foto tirada e URI definida para preview: $uri")
                }
            } else {
                Toast.makeText(this, "Falha ao capturar imagem.", Toast.LENGTH_SHORT).show()
            }
        }

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                anexoPhotoUri = it // Salva o URI para preview
                imagePreview.setImageURI(it)
                imagePreview.visibility = View.VISIBLE
                Log.d(TAG, "Imagem da galeria selecionada e URI definida para preview: $it")
            }
        }
    }

    private fun handleCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) -> {
                Toast.makeText(this, "Permissão da câmera é necessária para tirar fotos.", Toast.LENGTH_LONG).show()
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: Exception) {
            Log.e(TAG, "Erro ao criar arquivo de imagem", ex)
            Toast.makeText(this, "Erro ao preparar câmera.", Toast.LENGTH_SHORT).show()
            null
        }
        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                it
            )
            currentPhotoUri = photoURI
            takePictureLauncher.launch(photoURI)
        }
    }

    @Throws(Exception::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        if (storageDir == null || (!storageDir.exists() && !storageDir.mkdirs())) {
            Log.e(TAG, "Falha ao criar diretório de imagens.")
            throw Exception("Falha ao criar diretório de imagens.")
        }
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }


    private fun handleStoragePermission() {
        val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val allPermissionsGranted = permissionsToRequest.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allPermissionsGranted) {
            openGallery()
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissionsToRequest.first())) {
                Toast.makeText(this, "Permissão de armazenamento é necessária para anexar fotos.", Toast.LENGTH_LONG).show()
            }
            requestStoragePermissionLauncher.launch(permissionsToRequest)
        }
    }


    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun initiateLocationAndSaveRisk() {
        botaoRegistrarRisco.isEnabled = false

        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 3000
        ).setMaxUpdates(1).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location: Location? = locationResult.lastLocation
                if (location != null) {
                    Log.d(TAG, "Localização obtida. URI da imagem para preview (se houver): $anexoPhotoUri")
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
                    botaoRegistrarRisco.isEnabled = true
                }
            }
            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                if (!locationAvailability.isLocationAvailable) {
                    Toast.makeText(
                        this@RegistrarRiscoActivity,
                        "Localização não disponível. Verifique as configurações do GPS.",
                        Toast.LENGTH_LONG
                    ).show()
                    botaoRegistrarRisco.isEnabled = true
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                mainLooper
            )
        } else {
            Toast.makeText(this, "Permissão de localização não concedida.", Toast.LENGTH_SHORT).show()
            botaoRegistrarRisco.isEnabled = true
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

        if (descricao.isBlank()) {
            Toast.makeText(this, "A descrição do risco não pode estar vazia.", Toast.LENGTH_SHORT).show()
            botaoRegistrarRisco.isEnabled = true
            return
        }

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
                Toast.makeText(this, "Risco registrado com sucesso!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Risco salvo no Firestore. ID: ${it.id}")
                finish() // Fecha a activity
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erro ao registrar risco: ", e)
                Toast.makeText(this, "Erro ao registrar risco.", Toast.LENGTH_SHORT).show()
                botaoRegistrarRisco.isEnabled = true
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initiateLocationAndSaveRisk()
            } else {
                Toast.makeText(this, "Permissão de localização negada.", Toast.LENGTH_SHORT).show()
                botaoRegistrarRisco.isEnabled = true
            }
        }
    }
}