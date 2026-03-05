package com.yago.aegis.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.data.WorkoutSession
import com.yago.aegis.ui.theme.AegisBronze

@Composable
fun SessionProgressHeader(session: WorkoutSession) {
    // Calculamos cuántos ejercicios se han completado (todos sus sets están en true)
    val completedExercises = session.exercisesProgress.count { progress ->
        progress.sets.isNotEmpty() && progress.sets.all { it.isCompleted }
    }
    val totalExercises = session.exercisesProgress.size

    // Calculamos el porcentaje para la barra (de 0.0f a 1.0f)
    val progressPercent = if (totalExercises > 0) completedExercises.toFloat() / totalExercises else 0f
    val animatedProgress by animateFloatAsState(targetValue = progressPercent)

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "SESSION PROGRESS",
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "$completedExercises of $totalExercises EXERCISES",
                color = AegisBronze,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Barra de progreso personalizada al estilo de tu imagen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF1A1A1A)) // Fondo oscuro de la barra
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(AegisBronze) // Parte rellena (Bronce)
            )
        }
    }
}