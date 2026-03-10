package com.yago.aegis.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatCard(
    title: String,
    mainValue: String,
    subValue: String,
    modifier: Modifier = Modifier,
    isPositive: Boolean = true,
    showProgress: Boolean = false,
    progress: Float = 0f
) {
    Card(
        modifier = modifier.height(140.dp),
        // Usamos surfaceVariant (0E0E0E) para que contraste con el fondo (050505)
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp) // Redondeado más técnico, menos circular
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // TÍTULO: Estilo etiqueta técnica
            Text(
                text = title.uppercase(),
                color = MaterialTheme.colorScheme.secondary, // AegisSteel
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
            )

            Spacer(Modifier.weight(1f))

            // VALOR PRINCIPAL: Grande y contundente
            Text(
                text = mainValue,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp // Un poco más apretado para look "racing"
                )
            )

            // SUBVALOR: Dinámico (Verde si es positivo, Acero si es neutro)
            Text(
                text = subValue,
                color = if (isPositive && subValue.contains("+"))
                    Color(0xFF4CAF50)
                else
                    MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                )
            )

            if (showProgress) {
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary, // AegisBronze
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}