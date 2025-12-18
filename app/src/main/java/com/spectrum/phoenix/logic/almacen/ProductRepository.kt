package com.spectrum.phoenix.logic.almacen

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.spectrum.phoenix.logic.model.Entry
import com.spectrum.phoenix.logic.model.Product
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProductRepository {

    private val db = FirebaseDatabase.getInstance() // Usar instancia por defecto
    private val productsRef = db.getReference("Productos")
    private val entriesRef = db.getReference("Entradas")

    private fun getTodayDate(): String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

    suspend fun addProduct(product: Product): Result<Unit> {
        return try {
            val newRef = productsRef.push()
            val id = newRef.key ?: throw Exception("No se pudo generar el ID")
            val finalProduct = product.copy(id = id)
            
            productsRef.child(id).setValue(finalProduct).await()
            saveOrUpdateEntryLogic(finalProduct, isNew = true, oldQty = 0)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(product: Product, oldQuantity: Int): Result<Unit> {
        return try {
            productsRef.child(product.id).setValue(product).await()
            saveOrUpdateEntryLogic(product, isNew = false, oldQty = oldQuantity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun saveOrUpdateEntryLogic(product: Product, isNew: Boolean, oldQty: Int) {
        try {
            val today = getTodayDate()
            val diff = product.quantity - oldQty
            val currentTime = System.currentTimeMillis()
            val isRecentCorrection = (currentTime - product.timestamp) < 5 * 60 * 1000

            val snapshot = entriesRef.child(product.id).get().await()
            val currentEntry = snapshot.getValue(Entry::class.java)

            val newEntryQty: Int = when {
                isNew || diff < 0 || isRecentCorrection -> product.quantity
                diff > 0 -> {
                    if (currentEntry != null && currentEntry.date.startsWith(today)) {
                        currentEntry.quantity + diff
                    } else {
                        diff
                    }
                }
                else -> return 
            }

            val entry = Entry(
                id = product.id,
                productId = product.id,
                productName = product.name,
                quantity = newEntryQty,
                date = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            )
            entriesRef.child(product.id).setValue(entry).await()
        } catch (e: Exception) {
            // Error silencioso en entradas para no bloquear la venta/producto
        }
    }

    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            productsRef.child(productId).removeValue().await()
            entriesRef.child(productId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getProducts(): Flow<List<Product>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = snapshot.children.mapNotNull { child ->
                    try {
                        child.getValue(Product::class.java)
                    } catch (e: Exception) {
                        null // Ignorar productos con datos corruptos en lugar de cerrar la app
                    }
                }
                trySend(products)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        productsRef.addValueEventListener(listener)
        awaitClose { productsRef.removeEventListener(listener) }
    }
}
