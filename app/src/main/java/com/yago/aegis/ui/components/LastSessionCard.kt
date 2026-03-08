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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LastSessionCard(lastSetsText: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            // Color de fondo ligeramente más profundo que la tarjeta principal
            .background(
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                shape = RoundedCornerShape(4.dp) // Esquinas más técnicas
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Un pequeño indicador visual (como un LED de estado)
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "ENTRENAMIENTO PREVIO",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp,
                    letterSpacing = 1.5.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // El texto de las series con una fuente monoespaciada o técnica
        Text(
            text = if (lastSetsText.isEmpty()) "NO HAY DATOS" else lastSetsText,
            color = if (lastSetsText.isEmpty()) MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f) else Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            lineHeight = 20.sp
        )
    }
}