package com.spectrum.phoenix.logic.dashboard

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.spectrum.phoenix.logic.model.ActivityLog
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class ActivityLogRepository {
    private val db = FirebaseDatabase.getInstance()
    private val logsRef = db.getReference("Logs")

    private fun getNowFull(): String = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault()).format(Date())
    private fun getTodayDate(): String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

    suspend fun logAction(action: String, details: String): Result<Unit> {
        return try {
            val newRef = logsRef.push()
            val id = newRef.key ?: throw Exception("No ID")
            val log = ActivityLog(id, action, details, System.currentTimeMillis(), getNowFull())
            logsRef.child(id).setValue(log).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getLogs(): Flow<List<ActivityLog>> = callbackFlow {
        val today = getTodayDate()
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allLogs = snapshot.children.mapNotNull { it.getValue(ActivityLog::class.java) }
                
                // 1. Identificar logs antiguos para ELIMINAR de la base de datos
                val oldLogs = allLogs.filter { !it.date.startsWith(today) }
                if (oldLogs.isNotEmpty()) {
                    oldLogs.forEach { log -> logsRef.child(log.id).removeValue() }
                }

                // 2. Mostrar solo los de HOY en la app
                val todayLogs = allLogs.filter { it.date.startsWith(today) }
                    .sortedByDescending { it.timestamp }
                
                trySend(todayLogs)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        logsRef.addValueEventListener(listener)
        awaitClose { logsRef.removeEventListener(listener) }
    }
}
