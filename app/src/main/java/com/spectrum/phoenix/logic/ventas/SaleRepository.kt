package com.spectrum.phoenix.logic.ventas

import com.google.firebase.database.FirebaseDatabase
import com.spectrum.phoenix.logic.model.Sale
import com.spectrum.phoenix.logic.model.Product
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SaleRepository {
    private val db = FirebaseDatabase.getInstance()
    private val salesRef = db.getReference("Ventas")
    private val productsRef = db.getReference("Productos")

    suspend fun registerSale(sale: Sale): Result<Unit> {
        return try {
            val newSaleRef = salesRef.push()
            val id = newSaleRef.key ?: throw Exception("No se pudo generar ID de venta")
            val date = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            val finalSale = sale.copy(id = id, date = date)

            salesRef.child(id).setValue(finalSale).await()

            finalSale.items.forEach { item ->
                try {
                    val productSnapshot = productsRef.child(item.productId).get().await()
                    val product = productSnapshot.getValue(Product::class.java)
                    if (product != null) {
                        val newQty = product.quantity - item.quantity
                        productsRef.child(item.productId).child("quantity").setValue(newQty).await()
                    }
                } catch (e: Exception) {
                    // Si falla la actualización de stock de un item, continuamos con el resto
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
