package com.spectrum.phoenix.logic.config

import com.google.gson.Gson
import com.spectrum.phoenix.logic.model.UpdateInfo
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateRepository {
    private val client = OkHttpClient()
    private val gson = Gson()
    
    // URL raw de tu archivo update.json en GitHub
    private val updateUrl = "https://raw.githubusercontent.com/Spectrum-Dark/App-Phoenix/master/update.json"

    suspend fun checkUpdate(): Result<UpdateInfo> = withContext(Dispatchers.IO) {
        return@withContext try {
            val request = Request.Builder().url(updateUrl).build()
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val json = response.body?.string()
                val updateInfo = gson.fromJson(json, UpdateInfo::class.java)
                Result.success(updateInfo)
            } else {
                Result.failure(Exception("Error al conectar con el servidor de actualizaciones"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
