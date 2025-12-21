package com.spectrum.phoenix.ui.main.productos.almacen

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object BarcodeUtils {
    // Ahora es una función suspendida para no bloquear la UI
    suspend fun generateBarcode(content: String, width: Int = 800, height: Int = 250): Bitmap? = withContext(Dispatchers.Default) {
        return@withContext try {
            val hints = mapOf(EncodeHintType.MARGIN to 2)
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                content,
                BarcodeFormat.CODE_128,
                width,
                height,
                hints
            )
            
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
                }
            }
            
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
