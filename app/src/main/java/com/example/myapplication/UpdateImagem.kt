package com.example.myapplication

import android.graphics.Bitmap
import android.util.Base64
import okhttp3.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

fun uploadImageToCloudinary(
    bitmap: Bitmap,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    val cloudName = "dc8vglf2d"          // Cloud Name do Cloudinary
    val uploadPreset = "SafeZone"    // Upload Preset configurado no Cloudinary

    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
    val imageBytes = byteArrayOutputStream.toByteArray()
    val base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT)

    val requestBody = FormBody.Builder()
        .add("file", "data:image/jpeg;base64,$base64Image")
        .add("upload_preset", uploadPreset)
        .build()

    val request = Request.Builder()
        .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
        .post(requestBody)
        .build()

    val client = OkHttpClient()
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onError("Erro ao enviar imagem: ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                val json = JSONObject(response.body?.string() ?: "")
                val imageUrl = json.getString("secure_url")
                onSuccess(imageUrl)
            } else {
                onError("Erro: ${response.message}")
            }
        }
    })
}