package com.yago.aegis.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.data.Routine
import com.yago.aegis.data.getExerciseIcon

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
            .padding(vertical = 10.dp),
        shape = RoundedCornerShape(12.dp), // Esquinas más agresivas
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
        )
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 1. Tags (Estilo Metadato)
                if (displayTags.isNotEmpty()) {
                    Text(
                        text = displayTags.uppercase(),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            fontSize = 9.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // 2. Título (Negrita Itálica para dinamismo)
                Text(
                    text = routine.name.uppercase(),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    letterSpacing = 0.5.sp
                )

                // 3. Última sesión — bronce si está pausada
                val isPausedText = lastPerformedText.startsWith("⏸")
                Text(
                    text = lastPerformedText.uppercase(),
                    color = if (isPausedText) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.secondary,
                    fontSize = 10.sp,
                    fontWeight = if (isPausedText) FontWeight.Black else FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 4. Fila inferior: Ejercicios y Acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom // Alineado a la base para look técnico
                ) {
                    // Cápsulas de ejercicios
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        routine.exercises.take(3).forEach { exercise ->
                            ExerciseNameCapsule(exercise.name)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Botón Start (El foco principal)
                    Button(
                        onClick = onStartClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(6.dp), // Botón más cuadrado = más serio
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = "START",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // 5. Icono de rutina (Esquina superior derecha)
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .align(Alignment.TopEnd),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = getExerciseIcon(routine.iconName ?: "dumbbell"),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

@Composable
fun ExerciseNameCapsule(name: String) {
    Surface(
        modifier = Modifier.widthIn(max = 85.dp),
        shape = RoundedCornerShape(4.dp), // Cápsulas más rectangulares
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Text(
            text = name.uppercase(),
            color = Color.LightGray,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}