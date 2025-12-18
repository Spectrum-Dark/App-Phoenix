package com.spectrum.phoenix.logic.reports

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.spectrum.phoenix.ui.components.ToastController
import com.spectrum.phoenix.ui.components.ToastType
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class ReportGenerator(private val context: Context, private val toast: ToastController) {

    private val pageWidth = 612
    private val pageHeight = 792
    private val margin = 40f
    
    private val paintText = Paint().apply { textSize = 10f; color = Color.BLACK }
    private val paintHeader = Paint().apply { textSize = 14f; isFakeBoldText = true; color = Color.parseColor("#1A73E8") }
    private val paintTableLabel = Paint().apply { textSize = 9f; isFakeBoldText = true; color = Color.DKGRAY }
    private val paintTotal = Paint().apply { textSize = 11f; isFakeBoldText = true; color = Color.BLACK }

    fun generatePDF(title: String, headers: List<String>, data: List<List<String>>, footer: List<String>? = null) {
        val pdfDocument = PdfDocument()
        var pageNumber = 1
        
        fun createNewPage(): Pair<PdfDocument.Page, Canvas> {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            canvas.drawText("PHOENIX ENTERPRISE - SISTEMA DE CONTROL", margin, 40f, paintHeader)
            canvas.drawText("REPORTE: $title", margin, 60f, paintText.apply { isFakeBoldText = true })
            canvas.drawText("Fecha: ${SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault()).format(Date())}", margin, 75f, paintText.apply { isFakeBoldText = false })
            canvas.drawLine(margin, 85f, pageWidth - margin, 85f, Paint().apply { color = Color.LTGRAY })
            
            var headerX = margin
            val colWidth = (pageWidth - (margin * 2)) / headers.size
            headers.forEach { header ->
                canvas.drawText(header, headerX, 105f, paintTableLabel)
                headerX += colWidth
            }
            canvas.drawLine(margin, 110f, pageWidth - margin, 110f, Paint().apply { color = Color.BLACK; strokeWidth = 0.5f })
            canvas.drawText("Página $pageNumber", pageWidth / 2f - 20f, pageHeight - 20f, paintText.apply { textSize = 8f })
            
            return Pair(page, canvas)
        }

        var (currentPage, canvas) = createNewPage()
        var currentY = 130f

        data.forEach { row ->
            if (currentY > pageHeight - 100f) {
                pdfDocument.finishPage(currentPage)
                pageNumber++
                val next = createNewPage()
                currentPage = next.first
                canvas = next.second
                currentY = 130f
            }

            var colX = margin
            val colWidth = (pageWidth - (margin * 2)) / headers.size
            row.forEach { cell ->
                val truncated = if (cell.length > 22) cell.take(19) + "..." else cell
                canvas.drawText(truncated, colX, currentY, paintText.apply { textSize = 9f })
                colX += colWidth
            }
            currentY += 20f
        }

        footer?.let {
            canvas.drawLine(margin, currentY, pageWidth - margin, currentY, Paint().apply { color = Color.BLACK; strokeWidth = 1f })
            currentY += 25f
            var footX = margin
            val colWidth = (pageWidth - (margin * 2)) / headers.size
            it.forEach { cell ->
                canvas.drawText(cell, footX, currentY, paintTotal)
                footX += colWidth
            }
        }

        pdfDocument.finishPage(currentPage)

        val fileName = "${title.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    val outputStream = context.contentResolver.openOutputStream(it)
                    outputStream?.use { os -> pdfDocument.writeTo(os) }
                    toast.show("PDF guardado en Descargas", ToastType.SUCCESS)
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)
                pdfDocument.writeTo(FileOutputStream(file))
                toast.show("PDF guardado en Descargas", ToastType.SUCCESS)
            }
        } catch (e: Exception) {
            toast.show("Error: ${e.message}", ToastType.ERROR)
        } finally {
            pdfDocument.close()
        }
    }
}
