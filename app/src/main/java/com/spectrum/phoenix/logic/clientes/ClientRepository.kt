package com.spectrum.phoenix.logic.clientes

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.spectrum.phoenix.logic.dashboard.ActivityLogRepository
import com.spectrum.phoenix.logic.model.Client
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClientRepository {
    private val db = FirebaseDatabase.getInstance()
    private val clientsRef = db.getReference("Clientes")
    private val logRepo = ActivityLogRepository()

    suspend fun addClient(name: String, lastName: String): Result<Unit> {
        return try {
            val newRef = clientsRef.push()
            val id = newRef.key ?: throw Exception("No ID")
            val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val client = Client(id, name, lastName, date)
            clientsRef.child(id).setValue(client).await()
            logRepo.logAction("Cliente Registrado", "Se afilió a: $name $lastName")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateClient(client: Client): Result<Unit> {
        return try {
            clientsRef.child(client.id).setValue(client).await()
            logRepo.logAction("Cliente Editado", "Se actualizó a: ${client.name} ${client.lastName}")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteClient(clientId: String): Result<Unit> {
        return try {
            val client = clientsRef.child(clientId).get().await().getValue(Client::class.java)
            clientsRef.child(clientId).removeValue().await()
            logRepo.logAction("Cliente Eliminado", "Se eliminó a: ${client?.name ?: clientId}")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getClients(): Flow<List<Client>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val clients = snapshot.children.mapNotNull { child ->
                    try { child.getValue(Client::class.java) } catch (e: Exception) { null }
                }.sortedByDescending { it.timestamp }
                trySend(clients)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        clientsRef.addValueEventListener(listener)
        awaitClose { clientsRef.removeEventListener(listener) }
    }
}
