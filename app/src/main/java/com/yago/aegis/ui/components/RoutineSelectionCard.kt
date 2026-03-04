package com.yago.aegis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.data.Routine
import com.yago.aegis.ui.theme.AegisBronze
import com.yago.aegis.ui.theme.AegisCard
import java.util.Calendar
import java.util.concurrent.TimeUnit

@Composable
fun RoutineSelectionCard(
    routine: Routine,
    displayTags: String,
    onStartClick: () -> Unit
) {
    // Obtenemos las listas de forma segura para evitar el error de nulidad
    val safeDates = routine.lastCompletedDates ?: emptyList()
    val safeExercises = routine.exercises ?: emptyList()

    // Lógica para el texto de último entrenamiento
    val lastPerformedText = remember(safeDates) {
        if (safeDates.isEmpty()) {
            "Never performed"
        } else {
            calculateLastPerformed(safeDates)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AegisCard)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // TAGS DINÁMICOS
                if (displayTags.isNotEmpty()) {
                    Text(
                        text = displayTags,
                        color = AegisBronze,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // TÍTULO DE LA RUTINA
                Text(
                    text = routine.name,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                // TEXTO DINÁMICO (Antes era estático "Yesterday")
                Text(
                    text = lastPerformedText,
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // FILA INFERIOR: NOMBRES DE EJERCICIOS Y BOTÓN START
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        safeExercises.take(3).forEach { exercise ->
                            ExerciseNameCapsule(exercise.name)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onStartClick,
                        colors = ButtonDefaults.buttonColors(containerColor = AegisBronze),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("START", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // ICONO DE LA RUTINA (Mantenemos tu lógica de routine.iconRes original)
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.TopEnd),
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.05f)
            ) {
                Icon(
                    painter = painterResource(id = routine.iconRes),
                    contentDescription = null,
                    tint = AegisBronze,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

private fun calculateLastPerformed(dates: List<Long>): String {
    if (dates.isEmpty()) return "Never performed"

    val now = System.currentTimeMillis()
    val lastDate = dates.last()

    val oneWeekAgo = now - TimeUnit.DAYS.toMillis(7)
    val sessionsThisWeek = dates.count { it >= oneWeekAgo }

    return if (sessionsThisWeek > 1) {
        "$sessionsThisWeek times this week"
    } else {
        val diffInMs = now - lastDate
        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMs)

        when {
            diffInDays < 1 -> "Last performed: Today"
            diffInDays == 1L -> "Last performed: Yesterday"
            else -> "Last performed: $diffInDays days ago"
        }
    }
}

@Composable
fun ExerciseNameCapsule(name: String) {
    Box(
        modifier = Modifier
            .height(24.dp)
            .widthIn(max = 80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            color = Color.LightGray,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}