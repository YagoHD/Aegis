package com.yago.aegis.ui.components

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.yago.aegis.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.yago.aegis.data.BodyMeasure
import com.yago.aegis.data.BodySnapshot
import com.yago.aegis.data.PhotoRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BodyHistorySection(
    bodyHistory: List<BodySnapshot>,
    photoHistory: List<PhotoRecord>,
    customMeasures: List<BodyMeasure>   // Medidas actuales para saber qué IDs mostrar
) {
    if (bodyHistory.isEmpty() && photoHistory.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {

        // ── GRÁFICAS DE MÉTRICAS ─────────────────────────────────────────────
        if (bodyHistory.size >= 2) {
            val recent = remember(bodyHistory) { bodyHistory.takeLast(12) }

            val weightLabel = stringResource(R.string.chart_weight_label)
            val fatLabel = stringResource(R.string.chart_fat_label)
            val kgLabel = stringResource(R.string.label_kg)
            val cmLabel = stringResource(R.string.label_cm)

            // Peso corporal
            val massPoints = recent.map { it.mass.toFloatOrNull() ?: 0f }
            if (massPoints.any { it > 0f }) {
                MetricLineChart(
                    label = weightLabel,
                    unit = kgLabel,
                    snapshots = recent,
                    valueSelector = { it.mass.toFloatOrNull() ?: 0f }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Grasa corporal
            val fatPoints = recent.map { it.bodyFat.toFloatOrNull() ?: 0f }
            if (fatPoints.any { it > 0f }) {
                MetricLineChart(
                    label = fatLabel,
                    unit = "%",
                    snapshots = recent,
                    valueSelector = { it.bodyFat.toFloatOrNull() ?: 0f }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Medidas personalizadas con al menos 2 puntos no cero
            customMeasures.forEach { measure ->
                val pts = recent.map { snap ->
                    snap.customMeasures.find { it.id == measure.id }
                        ?.value?.toFloatOrNull() ?: 0f
                }
                if (pts.count { it > 0f } >= 2) {
                    MetricLineChart(
                        label = measure.name.uppercase(),
                        unit = cmLabel,
                        snapshots = recent,
                        valueSelector = { snap ->
                            snap.customMeasures.find { it.id == measure.id }
                                ?.value?.toFloatOrNull() ?: 0f
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // ── HISTORIAL FOTOGRÁFICO ────────────────────────────────────────────
        if (photoHistory.isNotEmpty()) {
            Text(
                text = stringResource(R.string.history_photos_title),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(end = 4.dp)
            ) {
                items(photoHistory.reversed()) { record ->
                    PhotoHistoryThumb(record)
                }
            }
        }
    }
}

@Composable
private fun MetricLineChart(
    label: String,
    unit: String,
    snapshots: List<BodySnapshot>,
    valueSelector: (BodySnapshot) -> Float
) {
    val bronzeColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val points = snapshots.map(valueSelector)
    val validPts = points.filter { it > 0f }
    if (validPts.isEmpty()) return

    val first = validPts.first()
    val last = validPts.last()
    val delta = last - first
    val deltaSign = if (delta >= 0f) "+" else ""
    val deltaTxt = "$deltaSign${"%.1f".format(delta)} $unit"

    val dateFmt = remember { SimpleDateFormat("dd/MM", Locale.getDefault()) }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    color = secondaryColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Surface(
                    color = bronzeColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = deltaTxt,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        color = bronzeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "%.1f".format(last),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    color = secondaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Gráfica
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val validIndices = points.indices.filter { points[it] > 0f }
                    if (validIndices.size < 2) return@Canvas

                    val minV = validPts.min()
                    val maxV = validPts.max()
                    val range = (maxV - minV).coerceAtLeast(0.01f)
                    val topPad = 4.dp.toPx()
                    val bottomPad = 4.dp.toPx()
                    val graphH = size.height - topPad - bottomPad
                    val step = size.width / (points.size - 1).coerceAtLeast(1)

                    fun xOf(i: Int) = i * step
                    fun yOf(v: Float) = topPad + graphH - ((v - minV) / range * graphH)

                    // Degradado de relleno
                    val filled = validIndices.map { Offset(xOf(it), yOf(points[it])) }
                    if (filled.size > 1) {
                        val fillPath = Path().apply {
                            moveTo(filled.first().x, filled.first().y)
                            filled.drop(1).forEach { lineTo(it.x, it.y) }
                            lineTo(filled.last().x, size.height)
                            lineTo(filled.first().x, size.height)
                            close()
                        }
                        drawPath(
                            fillPath,
                            Brush.verticalGradient(
                                listOf(bronzeColor.copy(alpha = 0.25f), Color.Transparent)
                            )
                        )
                        // Línea
                        val linePath = Path().apply {
                            moveTo(filled.first().x, filled.first().y)
                            filled.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(linePath, bronzeColor, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
                        // Puntos
                        filled.forEach { pt ->
                            drawCircle(bronzeColor, radius = 3.dp.toPx(), center = pt)
                            drawCircle(Color(0xFF080808), radius = 1.5.dp.toPx(), center = pt)
                        }
                    }
                }
            }

            // Etiquetas de fecha (primera y última)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dateFmt.format(Date(snapshots.first().date)),
                    color = secondaryColor.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateFmt.format(Date(snapshots.last().date)),
                    color = secondaryColor.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PhotoHistoryThumb(record: PhotoRecord) {
    Box(
        modifier = Modifier
            .size(width = 110.dp, height = 160.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.BottomCenter
    ) {
        AsyncImage(
            model = Uri.parse(record.uri),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Overlay fecha
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                    )
                )
                .padding(bottom = 8.dp, top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = record.dateLabel,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
