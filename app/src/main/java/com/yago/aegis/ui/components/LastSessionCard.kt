package com.yago.aegis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.R

/**
 * "Ghost": muestra la marca de la sesión anterior como OBJETIVO A BATIR + sugerencia
 * de progresión. Enmarca el entreno previo como reto directo para el usuario.
 */
@Composable
fun LastSessionCard(lastSetsText: String, suggestion: String? = null) {
    val hasData = lastSetsText.isNotEmpty()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                shape = RoundedCornerShape(4.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.previous_workout_label),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp,
                    letterSpacing = 1.5.sp
                ),
                modifier = Modifier.weight(1f)
            )
            // Sello "A BATIR": convierte la referencia en un objetivo
            if (hasData) {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = stringResource(R.string.ghost_target),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (!hasData) stringResource(R.string.label_no_data) else lastSetsText,
            color = if (!hasData)
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            else Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            lineHeight = 20.sp
        )

        // Sugerencia de progresión (objetivo concreto de hoy)
        if (suggestion != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "→",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = suggestion,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                )
            }
        }
    }
}
