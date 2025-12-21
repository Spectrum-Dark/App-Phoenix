package com.spectrum.phoenix.logic.almacen

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.spectrum.phoenix.logic.dashboard.ActivityLogRepository
import com.spectrum.phoenix.logic.model.Entry
import com.spectrum.phoenix.logic.model.Product
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProductRepository {

    private val db = FirebaseDatabase.getInstance()
    private val productsRef = db.getReference("Productos")
    private val entriesRef = db.getReference("Entradas")
    private val logRepo = ActivityLogRepository()

    private fun getTodayDate(): String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

    suspend fun addProduct(product: Product): Result<Unit> {
        return try {
            val newRef = productsRef.push()
            val id = newRef.key ?: throw Exception("No se pudo generar el ID")
            val finalProduct = product.copy(id = id)
            
            // setValue sin await() para éxito instantáneo offline
            productsRef.child(id).setValue(finalProduct) 
            
            // Lógica de entrada: Para un producto nuevo no necesitamos consultar el servidor
            val entry = Entry(
                id = id,
                productId = id,
                productName = product.name,
                quantity = product.quantity,
                date = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault()).format(Date())
            )
            entriesRef.child(id).setValue(entry)
            
            logRepo.logAction("Producto Agregado", "Se registró el producto: ${product.name}")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(product: Product, oldQuantity: Int): Result<Unit> {
        return try {
            productsRef.child(product.id).setValue(product)
            
            // En actualización, calculamos la diferencia localmente sin bloquear con await()
            val diff = product.quantity - oldQuantity
            if (diff != 0) {
                // Actualizamos la entrada de forma optimista
                // Nota: Si necesitas historial acumulado preciso, Firebase se encargará de sincronizar 
                // las transacciones cuando vuelva el internet.
                val entry = Entry(
                    id = product.id,
                    productId = product.id,
                    productName = product.name,
                    quantity = product.quantity,
                    date = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault()).format(Date())
                )
                entriesRef.child(product.id).setValue(entry)
            }
            
            logRepo.logAction("Producto Editado", "Se actualizó el producto: ${product.name}")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            productsRef.child(productId).removeValue()
            entriesRef.child(productId).removeValue()
            logRepo.logAction("Producto Eliminado", "ID: $productId")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getProducts(): Flow<List<Product>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = snapshot.children.mapNotNull { child ->
                    try { child.getValue(Product::class.java) } catch (e: Exception) { null }
                }
                trySend(products)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        productsRef.addValueEventListener(listener)
        awaitClose { productsRef.removeEventListener(listener) }
    }

    fun getEntries(): Flow<List<Entry>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val entries = snapshot.children.mapNotNull { it.getValue(Entry::class.java) }
                    .sortedByDescending { it.date }
                trySend(entries)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        entriesRef.addValueEventListener(listener)
        awaitClose { entriesRef.removeEventListener(listener) }
    }
}
