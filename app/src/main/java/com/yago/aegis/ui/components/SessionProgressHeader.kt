package com.yago.aegis.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.data.WorkoutSession

@Composable
fun SessionProgressHeader(session: WorkoutSession) {
    // Lógica de cálculo (Mantenemos tu lógica impecable)
    val completedExercises = session.exercisesProgress.count { progress ->
        progress.sets.isNotEmpty() && progress.sets.all { it.isCompleted }
    }
    val totalExercises = session.exercisesProgress.size

    val progressPercent = if (totalExercises > 0) completedExercises.toFloat() / totalExercises else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progressPercent,
        label = "progressAnimation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // --- TEXTOS SUPERIORES ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "PROGRESS STATUS",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    fontSize = 10.sp
                )
            )

            // Indicador numérico (Ej: 4 / 6)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = completedExercises.toString(),
                    color = MaterialTheme.colorScheme.primary, // Bronce
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
                Text(
                    text = " / $totalExercises",
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- BARRA DE PROGRESO TÉCNICA ---
        // Usamos una superficie con un borde muy fino para que parezca un carril
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp) // Un pelín más gruesa para que se aprecie el color
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant) // Fondo 161616
        ) {
            // Parte rellena (Bronce con degradado o sólido)
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary)
            )
        }

        // --- SUBTEXTO INFORMATIVO ---
        if (progressPercent > 0f) {
            Text(
                text = "${(progressPercent * 100).toInt()}% COMPLETED",
                modifier = Modifier.padding(top = 6.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}