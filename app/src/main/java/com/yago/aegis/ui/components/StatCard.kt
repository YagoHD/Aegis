package com.yago.aegis.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
    // Usamos Surface para el borde técnico y el fondo unificado
    Surface(
        modifier = modifier.height(130.dp), // Un poco más compacta y agresiva
        color = MaterialTheme.colorScheme.surfaceVariant, // 30%: SurfaceDark
        shape = RoundedCornerShape(8.dp), // Esquinas unificadas (8.dp)
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) // Borde acero sutil
        )
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxSize()
        ) {
            // ETIQUETA: AegisSteel (Gris técnico)
            Text(
                text = title.uppercase(),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )

            Spacer(Modifier.weight(1f))

            // VALOR PRINCIPAL: Contundente con kerning negativo
            Text(
                text = mainValue.uppercase(),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.8).sp
            )

            // SUBVALOR / TENDENCIA
            if (subValue.isNotEmpty()) {
                Text(
                    text = subValue.uppercase(),
                    color = if (isPositive && subValue.contains("+"))
                        Color(0xFF81C784) // Un verde más técnico/pastel que no rompa el estilo
                    else
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    letterSpacing = 0.5.sp
                )
            }

            // BARRA DE CARGA AEGIS
            if (showProgress) {
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp) // Más fina, más técnica
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary, // AegisBronze
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}