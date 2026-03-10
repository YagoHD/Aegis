package com.yago.aegis.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WeightEvolutionSection(monthlyData: List<Pair<String, Double>>) {
    val maxVolume = monthlyData.maxOfOrNull { it.second }?.takeIf { it > 0.0 } ?: 1.0
    val orangeAegis = MaterialTheme.colorScheme.primary

    Column(modifier = Modifier.fillMaxWidth()) {
        // --- CABECERA ESTILO BIOMETRIC (ETIQUETA) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "EVOLUCIÓN DE CARGA",
                color = MaterialTheme.colorScheme.secondary, // AegisSteel
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )

            // Badge de tiempo sutil
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
            ) {
                Text(
                    text = "ÚLTIMOS 3 MESES",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // --- CONTENEDOR TÉCNICO AEGIS ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            color = MaterialTheme.colorScheme.surfaceVariant, // Fondo SurfaceDark
            shape = RoundedCornerShape(8.dp), // Esquinas unificadas (8.dp)
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) // Borde acero
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                monthlyData.forEachIndexed { index, (month, volume) ->
                    val isLastMonth = index == monthlyData.size - 1
                    val barHeightFraction = (volume / maxVolume).toFloat().coerceIn(0.05f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // VALOR: Estilo numérico pesado
                        Text(
                            text = formatVolume(volume),
                            color = if (isLastMonth) Color.White else MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontWeight = if (isLastMonth) FontWeight.ExtraBold else FontWeight.Bold,
                            letterSpacing = (-0.2).sp
                        )

                        Spacer(Modifier.height(8.dp))

                        // LA BARRA TÉCNICA
                        Box(
                            modifier = Modifier
                                .width(38.dp)
                                .fillMaxHeight(barHeightFraction * 0.75f)
                                .clip(RoundedCornerShape(4.dp)) // Esquinas más rectas
                                .background(
                                    if (isLastMonth) orangeAegis
                                    else orangeAegis.copy(alpha = 0.15f) // El mes pasado es un "fantasma" del actual
                                )
                        )

                        Spacer(Modifier.height(12.dp))

                        // NOMBRE DEL MES (AegisSteel)
                        Text(
                            text = month.uppercase(),
                            color = if (isLastMonth) Color.White else MaterialTheme.colorScheme.secondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}
fun formatVolume(volume: Double): String {
    return when {
        volume >= 1000000 -> "%.1fM KG".format(volume / 1000000)
        volume >= 1000 -> "%.1fK KG".format(volume / 1000)
        else -> "${volume.toInt()} KG"
    }
}