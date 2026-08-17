package com.yago.aegis.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Avatar como thumbnail pequeño en base64, para SINCRONIZARLO vía Firestore sin montar Storage.
 * Se reduce a <=128px y JPEG q70 -> ~5-10 KB, que caben de sobra en el doc de publicProfiles.
 * Mi avatar se muestra desde el content:// local (calidad plena); el base64 es solo para que
 * los amigos me vean.
 */
object AvatarImage {

    private const val MAX_PX = 256
    private const val JPEG_QUALITY = 70

    /** Lee el content:// Uri, lo reduce y lo devuelve como JPEG base64 (null si falla). */
    fun encode(context: Context, uri: Uri): String? = runCatching {
        val src = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            ?: return null
        val scaled = downscale(src)
        if (scaled !== src) src.recycle()
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        scaled.recycle()
        Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }.getOrNull()

    /** Decodifica el base64 a ImageBitmap para pintarlo (null si vacío o inválido). */
    fun decode(base64: String?): ImageBitmap? {
        if (base64.isNullOrBlank()) return null
        return runCatching {
            val bytes = Base64.decode(base64, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }.getOrNull()
    }

    private fun downscale(src: Bitmap): Bitmap {
        val max = maxOf(src.width, src.height)
        if (max <= MAX_PX) return src
        val scale = MAX_PX.toFloat() / max
        return Bitmap.createScaledBitmap(src, (src.width * scale).toInt(), (src.height * scale).toInt(), true)
    }

    private const val AVATAR_MAX_PX = 512

    /**
     * Guarda el bitmap YA recortado como avatar en un fichero PROPIO de la app (filesDir) y
     * devuelve su Uri. Un fichero propio persiste y se lee siempre (a diferencia del content://
     * prestado por el selector, que puede caducar). Borra el avatar anterior.
     */
    fun saveAvatar(context: Context, bitmap: Bitmap): Uri {
        context.filesDir.listFiles { f -> f.name.startsWith("avatar_") }?.forEach { it.delete() }
        val max = maxOf(bitmap.width, bitmap.height)
        val scaled = if (max > AVATAR_MAX_PX) {
            val s = AVATAR_MAX_PX.toFloat() / max
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * s).toInt(), (bitmap.height * s).toInt(), true)
        } else bitmap
        val file = File(context.filesDir, "avatar_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { scaled.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        return Uri.fromFile(file)
    }
}
