package com.spectrum.phoenix.logic.config

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spectrum.phoenix.logic.model.UpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class UpdateViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UpdateRepository()
    private val context = application.applicationContext

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress

    val currentVersion: String = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
    } catch (e: Exception) {
        "1.0"
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            _updateState.value = UpdateState.Checking
            val result = repository.checkUpdate()
            result.onSuccess { info ->
                // Comparamos versiones. Asegúrate que en build.gradle sea igual que en el JSON
                if (info.latestVersion != currentVersion) {
                    _updateState.value = UpdateState.Available(info)
                } else {
                    _updateState.value = UpdateState.UpToDate
                }
            }.onFailure {
                _updateState.value = UpdateState.Error("Error al buscar: ${it.message}")
            }
        }
    }

    fun downloadAndInstall(apkUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _updateState.value = UpdateState.Downloading
            try {
                val client = OkHttpClient.Builder()
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .build()
                
                val request = Request.Builder().url(apkUrl).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    throw Exception("Servidor respondió con código ${response.code}. Verifica que el link del APK sea correcto en update.json.")
                }

                val body = response.body ?: throw Exception("El archivo está vacío")
                val totalBytes = body.contentLength()
                
                // Usamos el directorio de archivos externos para evitar problemas de permisos de escritura
                val file = File(context.getExternalFilesDir(null), "update.apk")
                if (file.exists()) file.delete()
                
                body.byteStream().use { input ->
                    FileOutputStream(file).use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead: Int
                        var downloadedBytes = 0L
                        
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            if (totalBytes > 0) {
                                _downloadProgress.value = downloadedBytes.toFloat() / totalBytes.toFloat()
                            }
                        }
                    }
                }
                
                _updateState.value = UpdateState.ReadyToInstall(file)
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error("Fallo: ${e.message}")
            }
        }
    }

    fun installApk(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context, 
                "${context.packageName}.fileprovider", 
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            _updateState.value = UpdateState.Error("No se pudo abrir el instalador: ${e.message}")
        }
    }
}

sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    object UpToDate : UpdateState()
    data class Available(val info: UpdateInfo) : UpdateState()
    object Downloading : UpdateState()
    data class ReadyToInstall(val file: File) : UpdateState()
    data class Error(val message: String) : UpdateState()
}
