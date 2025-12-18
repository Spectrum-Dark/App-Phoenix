package com.spectrum.phoenix.logic.configuracion

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.tasks.await
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class BackupRepository {
    private val db = FirebaseDatabase.getInstance()
    private val rootRef = db.reference
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    // LISTA BLANCA DE NODOS OFICIALES DE PHOENIX ENTERPRISE
    private val ALLOWED_KEYS = listOf("Productos", "Entradas", "Clientes", "Ventas", "Creditos")

    suspend fun exportData(context: Context): Result<String> {
        return try {
            val snapshot = rootRef.get().await()
            val fullData = snapshot.value as? Map<*, *> ?: return Result.failure(Exception("La base de datos está vacía"))
            
            // Exportar solo lo que está en la Lista Blanca
            val filteredData = fullData.filterKeys { it in ALLOWED_KEYS }
            
            if (filteredData.isEmpty()) {
                return Result.failure(Exception("No existen registros de negocio para exportar"))
            }
            
            val jsonString = gson.toJson(filteredData)
            val fileName = "Phoenix_Backup_${SimpleDateFormat("ddMMyyyy_HHmm", Locale.getDefault()).format(Date())}.json"
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { os ->
                        os.write(jsonString.toByteArray())
                    }
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = java.io.File(downloadsDir, fileName)
                file.writeText(jsonString)
            }
            Result.success("Respaldo de datos vitales guardado con éxito")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importData(context: Context, uri: Uri): Result<Unit> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }
            
            if (jsonString.isBlank()) throw Exception("El archivo está vacío")

            val dataMap = gson.fromJson(jsonString, Map::class.java) as? Map<String, Any> 
                ?: throw Exception("Formato JSON no válido")
            
            // VALIDACIÓN: Filtrar solo las llaves permitidas por la app
            val validMap = dataMap.filterKeys { it in ALLOWED_KEYS }
            
            // Si después de filtrar no queda nada de Phoenix, es un archivo intruso
            if (validMap.isEmpty()) {
                return Result.failure(Exception("El archivo no es un respaldo válido de Phoenix Enterprise"))
            }
            
            // Restaurar solo los nodos oficiales sin tocar Usuarios o Logs
            validMap.forEach { (key, value) ->
                rootRef.child(key).setValue(value).await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
