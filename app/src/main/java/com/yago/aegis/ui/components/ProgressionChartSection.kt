package com.yago.aegis.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ProgressionChartSection(
    currentMax: String,
    percentageGain: String,
    dataPoints: List<Float>,
    dateLabels: List<String> = emptyList()
) {
    val recentMonths = remember { getRecentMonths(5) }
    val bronzeColor = MaterialTheme.colorScheme.primary
    val bronzeArgb = bronzeColor.toArgb()
    val surfaceArgb = MaterialTheme.colorScheme.surfaceVariant.toArgb()

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // ── CABECERA ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "PROGRESO TEMPORAL",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = currentMax.split(" ")[0],
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1).sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "KG",
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
                Surface(
                    color = bronzeColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, bronzeColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "↗ $percentageGain",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = bronzeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── GRÁFICA ──
            // Padding superior extra para que las etiquetas no se corten arriba
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val validPoints = dataPoints.filter { it > 0f }
                    if (validPoints.isEmpty()) return@Canvas

                    val maxW = validPoints.maxOrNull() ?: 100f
                    val minW = validPoints.minOrNull() ?: 0f

                    // Reservar espacio arriba para etiquetas y abajo para puntos
                    val topPad = 22.dp.toPx()
                    val bottomPad = 8.dp.toPx()
                    val graphHeight = size.height - topPad - bottomPad
                    val range = if (maxW == minW) maxW.coerceAtLeast(1f) else maxW - minW

                    // Calcular coordenadas de todos los puntos
                    val step = if (dataPoints.size > 1) size.width / (dataPoints.size - 1) else size.width / 2f
                    val coords = dataPoints.mapIndexed { i, w ->
                        if (w > 0f) {
                            val x = if (dataPoints.size == 1) size.width / 2f else i * step
                            val norm = (w - minW) / range
                            val y = topPad + graphHeight - (norm * graphHeight)
                            Offset(x, y)
                        } else null
                    }

                    // ── 1. Degradado de relleno ──
                    val filledCoords = coords.filterNotNull()
                    if (filledCoords.size > 1) {
                        val fillPath = Path().apply {
                            moveTo(filledCoords.first().x, filledCoords.first().y)
                            filledCoords.drop(1).forEach { lineTo(it.x, it.y) }
                            lineTo(filledCoords.last().x, size.height)
                            lineTo(filledCoords.first().x, size.height)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(bronzeColor.copy(alpha = 0.25f), Color.Transparent),
                                startY = topPad,
                                endY = size.height
                            )
                        )
                    }

                    // ── 2. Línea principal ──
                    if (filledCoords.size > 1) {
                        val linePath = Path().apply {
                            moveTo(filledCoords.first().x, filledCoords.first().y)
                            filledCoords.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(
                            path = linePath,
                            color = bronzeColor,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // ── 3. Puntos y etiquetas (siempre encima de la línea) ──
                    coords.forEachIndexed { i, offset ->
                        if (offset == null) return@forEachIndexed
                        val w = dataPoints[i]
                        val label = if (w % 1 == 0f) "${w.toInt()}kg" else "${"%.1f".format(w)}kg"

                        // Anillo bronce con centro oscuro
                        drawCircle(color = bronzeColor, radius = 5.dp.toPx(), center = offset)
                        drawCircle(color = Color(0xFF1A1A1A), radius = 2.5.dp.toPx(), center = offset)

                        // Etiqueta SIEMPRE encima del punto — en un chip estilo Aegis
                        drawIntoCanvas { canvas ->
                            val textPaint = Paint().apply {
                                color = bronzeArgb
                                textSize = 8.5.dp.toPx()
                                textAlign = Paint.Align.CENTER
                                typeface = Typeface.DEFAULT_BOLD
                                isAntiAlias = true
                            }

                            // Medir texto para dibujar el fondo
                            val textW = textPaint.measureText(label)
                            val chipPadH = 3.dp.toPx()
                            val chipPadV = 2.dp.toPx()
                            val chipW = textW + chipPadH * 2
                            val chipH = 10.dp.toPx()
                            val chipTop = offset.y - 5.dp.toPx() - chipH - 2.dp.toPx()
                            val chipLeft = offset.x - chipW / 2

                            // Fondo del chip (mismo color que la surface)
                            val bgPaint = Paint().apply {
                                color = surfaceArgb
                                isAntiAlias = true
                                style = Paint.Style.FILL
                            }
                            val borderPaint = Paint().apply {
                                color = bronzeArgb
                                isAntiAlias = true
                                style = Paint.Style.STROKE
                                strokeWidth = 0.8.dp.toPx()
                                alpha = 160
                            }

                            val rect = android.graphics.RectF(chipLeft, chipTop, chipLeft + chipW, chipTop + chipH)
                            val radius = 3.dp.toPx()
                            canvas.nativeCanvas.drawRoundRect(rect, radius, radius, bgPaint)
                            canvas.nativeCanvas.drawRoundRect(rect, radius, radius, borderPaint)

                            // Texto centrado en el chip
                            canvas.nativeCanvas.drawText(
                                label,
                                offset.x,
                                chipTop + chipH - chipPadV - 0.5.dp.toPx(),
                                textPaint
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── ETIQUETAS EJE X ──
            val labels = if (dateLabels.isNotEmpty()) dateLabels else recentMonths
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.forEach {
                    Text(
                        text = it.uppercase(),
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

fun getRecentMonths(count: Int = 5): List<String> {
    val current = YearMonth.now()
    return (0 until count).map { i ->
        current.minusMonths((count - 1 - i).toLong())
            .month
            .getDisplayName(TextStyle.SHORT, Locale.getDefault())
            .uppercase()
    }
}
