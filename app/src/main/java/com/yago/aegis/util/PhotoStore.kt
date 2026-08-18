package com.yago.aegis.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File

/**
 * Copia una foto elegida (content://) a un FICHERO propio de la app, para que no se "pierda"
 * cuando Android revoca el permiso del content:// (problema típico del selector de galería).
 * No borra las anteriores: el historial del log visual las referencia y deben persistir.
 * (Robustez LOCAL: no cruza de dispositivo — para eso haría falta Firebase Storage.)
 */
object PhotoStore {

    private const val MAX_PX = 1440
    private const val JPEG_QUALITY = 85

    fun copyToAppFile(context: Context, source: Uri): Uri? = runCatching {
        val bmp = context.contentResolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it) }
            ?: return null
        val scaled = downscale(bmp)
        if (scaled !== bmp) bmp.recycle()
        val file = File(context.filesDir, "photo_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
        scaled.recycle()
        Uri.fromFile(file)
    }.getOrNull()

    private fun downscale(src: Bitmap): Bitmap {
        val max = maxOf(src.width, src.height)
        if (max <= MAX_PX) return src
        val s = MAX_PX.toFloat() / max
        return Bitmap.createScaledBitmap(src, (src.width * s).toInt(), (src.height * s).toInt(), true)
    }
}
