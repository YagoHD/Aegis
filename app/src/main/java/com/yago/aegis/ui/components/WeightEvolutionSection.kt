package com.yago.aegis.ui.components

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
    // Calculamos el máximo para escalar las barras, evitando división por cero
    val maxVolume = monthlyData.maxOfOrNull { it.second }?.takeIf { it > 0.0 } ?: 1.0

    Column(modifier = Modifier.fillMaxWidth()) {
        // CABECERA DE SECCIÓN
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "EVOLUCIÓN DE CARGA",
                color = MaterialTheme.colorScheme.primary, // AegisBronze
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    fontSize = 11.sp
                )
            )
            Text(
                text = "ÚLTIMOS 3 MESES",
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
        }

        Spacer(Modifier.height(12.dp))

        // CONTENEDOR DEL GRÁFICO
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant // 0E0E0E
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                monthlyData.forEachIndexed { index, (month, volume) ->
                    val isLastMonth = index == monthlyData.size - 1
                    // Escalado proporcional de la barra
                    val barHeightFraction = (volume / maxVolume).toFloat().coerceIn(0.1f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Valor numérico (Solo lo destacamos en el mes actual o con tono suave)
                        Text(
                            text = formatVolume(volume),
                            color = if (isLastMonth) Color.White else MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isLastMonth) FontWeight.Black else FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )

                        Spacer(Modifier.height(6.dp))

                        // LA BARRA AEGIS
                        Box(
                            modifier = Modifier
                                .width(34.dp)
                                .fillMaxHeight(barHeightFraction * 0.7f) // Reservamos espacio para texto
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    if (isLastMonth) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                                )
                        )

                        Spacer(Modifier.height(10.dp))

                        // NOMBRE DEL MES
                        Text(
                            text = month.uppercase(),
                            color = if (isLastMonth) Color.White else MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                fontSize = 10.sp
                            )
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