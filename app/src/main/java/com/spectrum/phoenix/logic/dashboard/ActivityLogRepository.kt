package com.spectrum.phoenix.logic.dashboard

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.spectrum.phoenix.PhoenixApp
import com.spectrum.phoenix.logic.model.ActivityLog
import com.spectrum.phoenix.logic.session.SessionManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.text.SimpleDateFormat
import java.util.*

class ActivityLogRepository {
    private val db = FirebaseDatabase.getInstance()
    private val logsRef = db.getReference("Logs")
    private val managuaTimeZone = TimeZone.getTimeZone("America/Managua")
    
    // Obtenemos el contexto global a través de PhoenixApp
    private val sessionManager = SessionManager(PhoenixApp.context)

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.timeZone = managuaTimeZone
        return sdf.format(Date())
    }

    private fun getNowTime(): String {
        val sdf = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        sdf.timeZone = managuaTimeZone
        return sdf.format(Date())
    }

    suspend fun logAction(action: String, details: String): Result<Unit> {
        return try {
            val newRef = logsRef.push()
            val id = newRef.key ?: throw Exception("No ID")
            
            // AHORA EL NOMBRE SIEMPRE SERÁ EL DEL USUARIO LOGUEADO
            val currentUserName = sessionManager.getUserName() ?: "Personal"
            
            val log = ActivityLog(
                id = id, 
                action = action, 
                details = details, 
                timestamp = System.currentTimeMillis(), 
                date = getTodayDate(),
                time = getNowTime(),
                userName = currentUserName
            )
            
            logsRef.child(id).setValue(log)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getLogs(): Flow<List<ActivityLog>> = callbackFlow {
        val today = getTodayDate()
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allTodayLogs = snapshot.children.mapNotNull { it.getValue(ActivityLog::class.java) }
                    .filter { it.date == today }
                    .sortedByDescending { it.timestamp }
                
                val recentLogs = allTodayLogs.take(10)
                trySend(recentLogs)

                val logsToDelete = snapshot.children.mapNotNull { it.getValue(ActivityLog::class.java) }
                    .filter { it.date != today || it !in recentLogs }
                
                if (logsToDelete.isNotEmpty()) {
                    logsToDelete.forEach { log -> logsRef.child(log.id).removeValue() }
                }
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        logsRef.addValueEventListener(listener)
        awaitClose { logsRef.removeEventListener(listener) }
    }
}
