package com.spectrum.phoenix.logic.clientes

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.spectrum.phoenix.logic.model.Credit
import com.spectrum.phoenix.logic.model.CreditMovement
import com.spectrum.phoenix.logic.model.SaleItem
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreditRepository {
    private val db = FirebaseDatabase.getInstance()
    private val creditsRef = db.getReference("Creditos")

    private fun getToday(): String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    // CAMBIO A 12 HORAS AM/PM
    private fun getNowFull(): String = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault()).format(Date())

    suspend fun addCharge(clientId: String, clientName: String, amount: Double, saleId: String, items: List<SaleItem>): Result<Unit> {
        return try {
            val snapshot = creditsRef.child(clientId).get().await()
            var credit = snapshot.getValue(Credit::class.java) ?: Credit(id = clientId, clientId = clientId, clientName = clientName)
            
            val today = getToday()
            val history = credit.history.toMutableList()
            
            val existingIndex = history.indexOfFirst { it.type == "CARGO" && it.date == today }

            if (existingIndex != -1) {
                val currentCargo = history[existingIndex]
                val updatedItems = currentCargo.items.toMutableList().apply { addAll(items) }
                
                history[existingIndex] = currentCargo.copy(
                    amount = currentCargo.amount + amount,
                    fullDate = getNowFull(),
                    items = updatedItems,
                    description = "Compras acumuladas hoy"
                )
            } else {
                val newMovement = CreditMovement(
                    id = saleId,
                    date = today,
                    fullDate = getNowFull(),
                    amount = amount,
                    type = "CARGO",
                    description = "Compra de productos",
                    items = items
                )
                history.add(newMovement)
            }

            credit = credit.copy(
                totalDebt = credit.totalDebt + amount,
                lastUpdate = getNowFull(),
                history = history
            )
            
            creditsRef.child(clientId).setValue(credit).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addAbono(clientId: String, amount: Double, description: String): Result<Unit> {
        return try {
            val snapshot = creditsRef.child(clientId).get().await()
            val credit = snapshot.getValue(Credit::class.java) ?: throw Exception("No se encontró registro")
            
            val movement = CreditMovement(
                id = creditsRef.push().key ?: "",
                date = getToday(),
                fullDate = getNowFull(),
                amount = amount,
                type = "ABONO",
                description = description
            )
            
            val newHistory = credit.history.toMutableList().apply { add(movement) }
            val newDebt = (credit.totalDebt - amount).coerceAtLeast(0.0)
            
            creditsRef.child(clientId).setValue(credit.copy(
                totalDebt = newDebt,
                lastUpdate = getNowFull(),
                history = newHistory
            )).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCredits(): Flow<List<Credit>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(Credit::class.java) }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        creditsRef.addValueEventListener(listener)
        awaitClose { creditsRef.removeEventListener(listener) }
    }
}
