package com.yago.aegis.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
    dataPoints: List<Float>
) {
    val recentMonths = remember { getRecentMonths(5) }
    val orangeAegis = MaterialTheme.colorScheme.primary // Usamos el Bronce del tema

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant, // 30%: SurfaceDark
        shape = RoundedCornerShape(8.dp), // Esquinas unificadas (8.dp)
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) // Borde acero sutil
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // --- CABECERA ESTILO AEGIS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "PROGRESO TEMPORAL",
                        color = MaterialTheme.colorScheme.secondary, // AegisSteel
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

                // Badge de porcentaje con estilo técnico
                Surface(
                    color = orangeAegis.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, orangeAegis.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "↗ $percentageGain",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = orangeAegis,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- DIBUJO DE LA GRÁFICA ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val validPoints = dataPoints.filter { it > 0f }

                    if (validPoints.isNotEmpty()) {
                        val maxWeight = validPoints.maxOrNull() ?: 100f
                        val minWeight = validPoints.minOrNull() ?: 0f

                        val verticalPadding = 30f
                        val graphHeight = size.height - (verticalPadding * 2)
                        val range = if (maxWeight - minWeight == 0f) maxWeight else maxWeight - minWeight

                        val path = Path()
                        val distance = size.width / (dataPoints.size - 1)
                        var firstPointIndex: Int? = null

                        dataPoints.forEachIndexed { index, weight ->
                            if (weight > 0f) {
                                val x = index * distance
                                val normalizedY = (weight - minWeight) / range
                                val y = size.height - verticalPadding - (normalizedY * graphHeight)

                                if (firstPointIndex == null) {
                                    path.moveTo(x, y)
                                    firstPointIndex = index
                                } else {
                                    path.lineTo(x, y)
                                }

                                // Punto técnico en cada nodo
                                drawCircle(
                                    color = orangeAegis,
                                    radius = 3.dp.toPx(),
                                    center = androidx.compose.ui.geometry.Offset(x, y)
                                )
                            }
                        }

                        if (validPoints.size > 1) {
                            // Línea principal de bronce
                            drawPath(
                                path = path,
                                color = orangeAegis,
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Relleno degradado que nace de la línea
                            val fillPath = Path().apply {
                                addPath(path)
                                lineTo((dataPoints.indices.last { dataPoints[it] > 0f }) * distance, size.height)
                                lineTo((firstPointIndex ?: 0) * distance, size.height)
                                close()
                            }

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(orangeAegis.copy(alpha = 0.2f), Color.Transparent)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Meses abajo: Etiquetas AegisSteel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                recentMonths.forEach {
                    Text(
                        text = it,
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