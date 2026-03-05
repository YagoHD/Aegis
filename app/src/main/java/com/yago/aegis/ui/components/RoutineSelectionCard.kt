package com.yago.aegis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.yago.aegis.data.getExerciseIcon
import com.yago.aegis.ui.theme.AegisBronze
import com.yago.aegis.ui.theme.AegisCard


@Composable
fun RoutineSelectionCard(
    routine: Routine,
    displayTags: String,
    onStartClick: () -> Unit,
    lastPerformedText: String,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AegisCard)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 1. Tags (solo si existen)
                if (displayTags.isNotEmpty()) {
                    Text(
                        text = displayTags,
                        color = AegisBronze,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // 2. Título
                Text(
                    text = routine.name,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                // 3. Texto dinámico del ViewModel
                Text(
                    text = lastPerformedText,
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Fila inferior (Ejercicios y Botón)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cápsulas de ejercicios (Manejo de nulos directo)
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        routine.exercises.take(3).forEach { exercise ->
                            ExerciseNameCapsule(exercise.name)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Botón Start
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

            // 5. Icono de rutina
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.TopEnd),
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.05f)
            ) {
                Icon(
                    imageVector = getExerciseIcon(routine.iconName ?: "dumbbell"),
                    contentDescription = null,
                    tint = AegisBronze,
                    modifier = Modifier.padding(8.dp)
                )
            }
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