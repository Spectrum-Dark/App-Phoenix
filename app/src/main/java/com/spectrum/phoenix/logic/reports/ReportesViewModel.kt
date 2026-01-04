package com.spectrum.phoenix.logic.reports

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spectrum.phoenix.logic.almacen.ProductRepository
import com.spectrum.phoenix.logic.clientes.ClientRepository
import com.spectrum.phoenix.logic.clientes.CreditRepository
import com.spectrum.phoenix.logic.model.SaleItem
import com.spectrum.phoenix.logic.ventas.SaleRepository
import com.spectrum.phoenix.ui.components.ToastController
import com.spectrum.phoenix.ui.components.ToastType
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

    fun generateReport(context: Context, type: String, toast: ToastController) {
        val generator = ReportGenerator(context, toast)
        val today = getToday()
        
        viewModelScope.launch {
            when (type) {
                "ENTRADAS" -> {
                    val entries = productRepo.getEntries().first()
                    val data = entries.filter { it.date.startsWith(today) }.map { listOf(it.date.takeLast(11), it.productName, it.quantity.toString()) }
                    if (data.isNotEmpty()) generator.generatePDF("Entradas de Hoy ($today)", listOf("Hora", "Producto", "Cant."), data)
                    else toast.show("No hay entradas registradas hoy", ToastType.INFO)
                }
                "PROXIMOS_VENCER" -> {
                    val products = productRepo.getProducts().first()
                    val soon = products.filter { isExpiringSoon(it.expiryDate) }
                    val data = soon.map { listOf(it.name, it.expiryDate ?: "N/A", it.quantity.toString()) }
                    if (data.isNotEmpty()) generator.generatePDF("Próximos a Vencer", listOf("Producto", "Vence", "Stock"), data)
                    else toast.show("No hay productos por vencer", ToastType.SUCCESS)
                }
                "VENCIDOS" -> {
                    val products = productRepo.getProducts().first()
                    val expired = products.filter { isExpired(it.expiryDate) }
                    val data = expired.map { listOf(it.name, it.expiryDate ?: "N/A", it.quantity.toString()) }
                    if (data.isNotEmpty()) generator.generatePDF("Productos Vencidos", listOf("Producto", "Venció", "Stock"), data)
                    else toast.show("No hay productos vencidos", ToastType.SUCCESS)
                }
                "TODOS_PRODUCTOS" -> {
                    val products = productRepo.getProducts().first()
                    val data = products.map { listOf(it.name, it.quantity.toString(), "C$ ${String.format("%.2f", it.price)}") }
                    if (data.isNotEmpty()) generator.generatePDF("Inventario Total", listOf("Producto", "Stock", "Precio"), data)
                    else toast.show("Inventario vacío", ToastType.INFO)
                }
                "CATALOGO_BARRAS" -> {
                    val products = productRepo.getProducts().first()
                    if (products.isNotEmpty()) {
                        generator.generateBarcodeCatalog(products)
                    } else {
                        toast.show("No hay productos para generar etiquetas", ToastType.INFO)
                    }
                }
                "CLIENTES_CREDITOS" -> {
                    val credits = creditRepo.getCredits().first()
                    val active = credits.filter { it.totalDebt > 0 }
                    val data = active.map { listOf(it.clientName, "C$ ${String.format("%.2f", it.totalDebt)}", it.lastUpdate.take(10)) }
                    if (data.isNotEmpty()) {
                        val total = active.sumOf { it.totalDebt }
                        generator.generatePDF("Afiliados con Crédito", listOf("Nombre", "Deuda", "F. Act."), data, listOf("TOTAL DEUDAS", "C$ ${String.format("%.2f", total)}", ""))
                    } else toast.show("No hay clientes con deuda", ToastType.SUCCESS)
                }
                "TODOS_CLIENTES" -> {
                    val clients = clientRepo.getClients().first()
                    val data = clients.map { listOf(it.name + " " + it.lastName, it.registrationDate) }
                    if (data.isNotEmpty()) generator.generatePDF("Todos los Clientes", listOf("Nombre", "F. Registro"), data)
                    else toast.show("No hay clientes registrados", ToastType.INFO)
                }
                "VENTAS_GENERALES" -> {
                    val sales = saleRepo.getSales().first().filter { it.clientId == null && it.date.startsWith(today) }
                    val allItems = sales.flatMap { it.items }
                    val groupedData = groupItemsByProduct(allItems)
                    
                    if (groupedData.isNotEmpty()) {
                        val total = sales.sumOf { it.total }
                        generator.generatePDF("Ventas Contado Hoy", listOf("Producto", "Cantidad", "Subtotal", "Prom. Precio"), groupedData, listOf("TOTAL VENTAS", "", "C$ ${String.format("%.2f", total)}", ""))
                    } else toast.show("No hay ventas de contado hoy", ToastType.INFO)
                }
                "VENTAS_CREDITOS" -> {
                    val sales = saleRepo.getSales().first().filter { it.clientId != null && it.date.startsWith(today) }
                    
                    if (sales.isNotEmpty()) {
                        val reportData = mutableListOf<List<String>>()
                        val groupedByClient = sales.groupBy { it.clientName }
                        
                        groupedByClient.forEach { (clientName, clientSales) ->
                            val clientItems = clientSales.flatMap { it.items }
                            val productSummary = clientItems.groupBy { it.productName }
                            
                            var firstRow = true
                            productSummary.forEach { (prodName, items) ->
                                val qty = items.sumOf { it.quantity }
                                val sub = items.sumOf { it.subtotal }
                                reportData.add(listOf(
                                    if (firstRow) clientName else "",
                                    prodName,
                                    qty.toString(),
                                    "C$ ${String.format("%.2f", sub)}"
                                ))
                                firstRow = false
                            }
                            // Fila de subtotal por cliente
                            val clientTotal = clientSales.sumOf { it.total }
                            reportData.add(listOf("SUBTOTAL ${clientName.uppercase()}", "", "", "C$ ${String.format("%.2f", clientTotal)}"))
                            reportData.add(listOf("", "", "", "")) // Espacio en blanco para separar clientes
                        }

                        val grandTotal = sales.sumOf { it.total }
                        generator.generatePDF(
                            "Créditos Detallados ($today)", 
                            listOf("Cliente", "Producto", "Cant.", "Subtotal"), 
                            reportData, 
                            listOf("TOTAL CRÉDITOS", "", "", "C$ ${String.format("%.2f", grandTotal)}")
                        )
                    } else toast.show("No hay ventas al crédito hoy", ToastType.INFO)
                }
                "VENTAS_TOTALES" -> {
                    val sales = saleRepo.getSales().first().filter { it.date.startsWith(today) }
                    val allItems = sales.flatMap { it.items }
                    val groupedData = groupItemsByProduct(allItems)

                    if (groupedData.isNotEmpty()) {
                        val total = sales.sumOf { it.total }
                        generator.generatePDF("Ventas Totales Hoy", listOf("Producto", "Cantidad Tot.", "Venta Tot.", "Precio Ref."), groupedData, listOf("TOTAL HOY", "", "C$ ${String.format("%.2f", total)}", ""))
                    } else toast.show("No se han realizado ventas hoy", ToastType.INFO)
                }
            }
        }
    }

    private fun groupItemsByProduct(items: List<SaleItem>): List<List<String>> {
        return items.groupBy { it.productName }
            .map { (name, productItems) ->
                val totalQty = productItems.sumOf { it.quantity }
                val totalSubtotal = productItems.sumOf { it.subtotal }
                val avgPrice = totalSubtotal / totalQty
                listOf(
                    name,
                    totalQty.toString(),
                    "C$ ${String.format("%.2f", totalSubtotal)}",
                    "C$ ${String.format("%.2f", avgPrice)}"
                )
            }.sortedByDescending { it[2] } 
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
