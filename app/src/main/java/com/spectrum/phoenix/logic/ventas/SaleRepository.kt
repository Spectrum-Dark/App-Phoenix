package com.spectrum.phoenix.logic.ventas

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.spectrum.phoenix.logic.clientes.CreditRepository
import com.spectrum.phoenix.logic.dashboard.ActivityLogRepository
import com.spectrum.phoenix.logic.model.Sale
import com.spectrum.phoenix.logic.model.Product
import com.spectrum.phoenix.logic.model.Credit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SaleRepository {
    private val db = FirebaseDatabase.getInstance()
    private val salesRef = db.getReference("Ventas")
    private val productsRef = db.getReference("Productos")
    private val creditsRef = db.getReference("Creditos")
    private val creditRepository = CreditRepository()
    private val logRepo = ActivityLogRepository()

    suspend fun registerSale(sale: Sale): Result<Unit> {
        return try {
            val newSaleRef = salesRef.push()
            val id = newSaleRef.key ?: throw Exception("No se pudo generar ID")
            val date = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault()).format(Date())
            val finalSale = sale.copy(id = id, date = date)

            salesRef.child(id).setValue(finalSale).await()

            finalSale.clientId?.let { cid ->
                creditRepository.addCharge(cid, finalSale.clientName, finalSale.total, id, finalSale.items)
            }

            finalSale.items.forEach { item ->
                try {
                    val productSnapshot = productsRef.child(item.productId).get().await()
                    val product = productSnapshot.getValue(Product::class.java)
                    if (product != null) {
                        val newQty = product.quantity - item.quantity
                        productsRef.child(item.productId).child("quantity").setValue(newQty).await()
                    }
                } catch (e: Exception) {}
            }
            
            logRepo.logAction("Venta Realizada", "Monto: C$ ${String.format("%.2f", finalSale.total)} - Cliente: ${finalSale.clientName}")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getSales(): Flow<List<Sale>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(Sale::class.java) }
                    .sortedByDescending { it.timestamp }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        salesRef.addValueEventListener(listener)
        awaitClose { salesRef.removeEventListener(listener) }
    }

    suspend fun revertSale(sale: Sale): Result<Unit> {
        return try {
            sale.items.forEach { item ->
                val productSnapshot = productsRef.child(item.productId).get().await()
                val product = productSnapshot.getValue(Product::class.java)
                if (product != null) {
                    val restoredQty = product.quantity + item.quantity
                    productsRef.child(item.productId).child("quantity").setValue(restoredQty).await()
                }
            }

            sale.clientId?.let { cid ->
                val creditSnapshot = creditsRef.child(cid).get().await()
                val credit = creditSnapshot.getValue(Credit::class.java)
                if (credit != null) {
                    val newDebt = (credit.totalDebt - sale.total).coerceAtLeast(0.0)
                    val newHistory = credit.history.filter { it.id != sale.id }
                    creditsRef.child(cid).setValue(credit.copy(totalDebt = newDebt, history = newHistory)).await()
                }
            }

            salesRef.child(sale.id).removeValue().await()
            logRepo.logAction("Venta Revertida", "Se anuló venta de C$ ${String.format("%.2f", sale.total)} de ${sale.clientName}")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
