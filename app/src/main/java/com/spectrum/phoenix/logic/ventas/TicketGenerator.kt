package com.spectrum.phoenix.logic.ventas

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.spectrum.phoenix.logic.model.Sale
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class TicketGenerator(private val context: Context) {

    fun generateAndShareTicket(sale: Sale) {
        val pdfDocument = PdfDocument()
        
        // Tamaño de ticket térmico estándar (aprox 58mm o 80mm de ancho)
        // Usaremos 300 puntos de ancho (~105mm) para buena compatibilidad
        val pageWidth = 300
        val pageHeight = 450 + (sale.items.size * 30) // Altura dinámica
        
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        
        val paint = Paint()
        val margin = 20f
        var currentY = 40f

        // Encabezado
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 16f
        paint.isFakeBoldText = true
        canvas.drawText("PHOENIX ENTERPRISE", pageWidth / 2f, currentY, paint)
        
        currentY += 20f
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("SISTEMA DE GESTIÓN CORPORATIVA", pageWidth / 2f, currentY, paint)
        
        currentY += 25f
        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, paint)
        
        // Datos de la Venta
        currentY += 20f
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 9f
        canvas.drawText("FECHA: ${sale.date}", margin, currentY, paint)
        currentY += 15f
        canvas.drawText("CLIENTE: ${sale.clientName}", margin, currentY, paint)
        currentY += 15f
        canvas.drawText("TICKET ID: ${sale.id.takeLast(8).uppercase()}", margin, currentY, paint)
        
        currentY += 15f
        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, paint)
        
        // Tabla de Productos
        currentY += 20f
        paint.isFakeBoldText = true
        canvas.drawText("DESCRIPCIÓN", margin, currentY, paint)
        canvas.drawText("CANT", pageWidth - 100f, currentY, paint)
        canvas.drawText("TOTAL", pageWidth - 50f, currentY, paint)
        
        paint.isFakeBoldText = false
        currentY += 10f
        
        sale.items.forEach { item ->
            currentY += 20f
            val name = if (item.productName.length > 18) item.productName.take(15) + ".." else item.productName
            canvas.drawText(name, margin, currentY, paint)
            canvas.drawText(item.quantity.toString(), pageWidth - 90f, currentY, paint)
            canvas.drawText(String.format("%.0f", item.subtotal), pageWidth - 50f, currentY, paint)
        }
        
        currentY += 25f
        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, paint)
        
        // Total
        currentY += 30f
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("TOTAL A PAGAR:", margin, currentY, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("C$ ${String.format("%.2f", sale.total)}", pageWidth - margin, currentY, paint)
        
        // Pie de Ticket
        currentY += 40f
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("¡GRACIAS POR SU COMPRA!", pageWidth / 2f, currentY, paint)
        currentY += 15f
        canvas.drawText("Spectrum Software Solutions", pageWidth / 2f, currentY, paint)

        pdfDocument.finishPage(page)

        // Guardar y Compartir
        val file = File(context.cacheDir, "Ticket_Phoenix_${System.currentTimeMillis()}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            shareFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }
    }

    private fun shareFile(file: File) {
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir Ticket de Venta"))
    }
}
