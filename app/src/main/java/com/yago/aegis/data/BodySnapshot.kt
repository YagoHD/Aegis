package com.yago.aegis.data

/**
 * Instantánea de métricas corporales tomada en una fecha concreta.
 * Se guarda manualmente cuando el usuario pulsa "GUARDAR HOY".
 */
data class BodySnapshot(
    val date: Long = System.currentTimeMillis(),
    val mass: String = "0.0",
    val bodyFat: String = "0.0",
    val customMeasures: List<BodyMeasure> = emptyList()
)

/**
 * Foto archivada en el historial visual. Las fotos "ACTUAL" anteriores
 * se mueven aquí cuando el usuario sube una nueva.
 */
data class PhotoRecord(
    val date: Long = System.currentTimeMillis(),
    val uri: String = "",
    val dateLabel: String = ""
)
