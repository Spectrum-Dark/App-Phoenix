package com.spectrum.phoenix.logic.reports

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spectrum.phoenix.logic.almacen.ProductRepository
import com.spectrum.phoenix.logic.clientes.ClientRepository
import com.spectrum.phoenix.logic.clientes.CreditRepository
import com.spectrum.phoenix.logic.ventas.SaleRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ReportesViewModel : ViewModel() {
    private val productRepo = ProductRepository()
    private val clientRepo = ClientRepository()
    private val creditRepo = CreditRepository()
    private val saleRepo = SaleRepository()

    private fun getToday(): String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

    fun generateReport(context: Context, type: String) {
        val generator = ReportGenerator(context)
        val today = getToday()
        
        viewModelScope.launch {
            when (type) {
                // CATEGORÍA: INVENTARIO
                "ENTRADAS" -> {
                    val entries = productRepo.getEntries().first()
                    // FILTRO HOY: Solo entradas de este día
                    val data = entries.filter { it.date.startsWith(today) }.map { listOf(it.date.takeLast(8), it.productName, it.quantity.toString()) }
                    generator.generatePDF("Entradas de Hoy ($today)", listOf("Hora", "Producto", "Cant."), data)
                }
                "PROXIMOS_VENCER" -> {
                    val products = productRepo.getProducts().first()
                    val soon = products.filter { isExpiringSoon(it.expiryDate) }
                    val data = soon.map { listOf(it.name, it.expiryDate ?: "N/A", it.quantity.toString()) }
                    generator.generatePDF("Próximos a Vencer (Corte $today)", listOf("Producto", "Vence", "Stock"), data)
                }
                "VENCIDOS" -> {
                    val products = productRepo.getProducts().first()
                    val expired = products.filter { isExpired(it.expiryDate) }
                    val data = expired.map { listOf(it.name, it.expiryDate ?: "N/A", it.quantity.toString()) }
                    generator.generatePDF("Productos Vencidos (Corte $today)", listOf("Producto", "Venció", "Stock"), data)
                }
                "TODOS_PRODUCTOS" -> {
                    val products = productRepo.getProducts().first()
                    val data = products.map { listOf(it.name, it.quantity.toString(), "C$ ${String.format("%.2f", it.price)}") }
                    generator.generatePDF("Inventario Total ($today)", listOf("Producto", "Stock", "Precio"), data)
                }

                // CATEGORÍA: CLIENTES
                "CLIENTES_CREDITOS" -> {
                    val credits = creditRepo.getCredits().first()
                    val active = credits.filter { it.totalDebt > 0 }
                    val data = active.map { listOf(it.clientName, "C$ ${String.format("%.2f", it.totalDebt)}", it.lastUpdate.take(10)) }
                    val total = active.sumOf { it.totalDebt }
                    generator.generatePDF("Deudores Activos ($today)", listOf("Nombre", "Deuda", "F. Act."), data, listOf("TOTAL DEUDAS", "C$ ${String.format("%.2f", total)}", ""))
                }
                "TODOS_CLIENTES" -> {
                    val clients = clientRepo.getClients().first()
                    val data = clients.map { listOf(it.name + " " + it.lastName, it.registrationDate) }
                    generator.generatePDF("Lista de Afiliados ($today)", listOf("Nombre", "F. Registro"), data)
                }

                // CATEGORÍA: FINANZAS Y VENTAS (FILTRADAS POR HOY)
                "VENTAS_GENERALES" -> {
                    val sales = saleRepo.getSales().first().filter { it.clientId == null && it.date.startsWith(today) }
                    val data = sales.flatMap { s -> s.items.map { i -> listOf(s.date.takeLast(8), i.productName, i.quantity.toString(), "C$ ${String.format("%.2f", i.subtotal)}") } }
                    val total = sales.sumOf { it.total }
                    generator.generatePDF("Ventas Contado de Hoy ($today)", listOf("Hora", "Producto", "Cant.", "Subt."), data, listOf("TOTAL VENTAS", "", "", "C$ ${String.format("%.2f", total)}"))
                }
                "VENTAS_CREDITOS" -> {
                    val sales = saleRepo.getSales().first().filter { it.clientId != null && it.date.startsWith(today) }
                    val data = sales.flatMap { s -> s.items.map { i -> listOf(s.clientName, i.productName, "C$ ${String.format("%.2f", i.subtotal)}", s.date.takeLast(8)) } }
                    val total = sales.sumOf { it.total }
                    generator.generatePDF("Ventas Crédito de Hoy ($today)", listOf("Cliente", "Producto", "Subt.", "Hora"), data, listOf("TOTAL CRÉDITOS", "", "C$ ${String.format("%.2f", total)}", ""))
                }
                "VENTAS_TOTALES" -> {
                    val sales = saleRepo.getSales().first().filter { it.date.startsWith(today) }
                    val data = sales.flatMap { s -> s.items.map { i -> listOf(i.productName, i.quantity.toString(), "C$ ${String.format("%.2f", i.subtotal)}", s.date.takeLast(8)) } }
                    val total = sales.sumOf { it.total }
                    generator.generatePDF("Resumen de Ventas Hoy ($today)", listOf("Producto", "Cant.", "Subtotal", "Hora"), data, listOf("TOTAL GENERAL", "", "C$ ${String.format("%.2f", total)}", ""))
                }
            }
        }
    }

    private fun isExpiringSoon(dateStr: String?): Boolean {
        if (dateStr == null) return false
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = sdf.parse(dateStr) ?: return false
            val cal = Calendar.getInstance()
            val today = cal.time
            cal.add(Calendar.DAY_OF_YEAR, 7)
            date.after(today) && date.before(cal.time)
        } catch (e: Exception) { false }
    }

    private fun isExpired(dateStr: String?): Boolean {
        if (dateStr == null) return false
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = sdf.parse(dateStr) ?: return false
            date.before(Calendar.getInstance().time)
        } catch (e: Exception) { false }
    }
}
