package com.yago.aegis.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.data.DefaultExercises
import com.yago.aegis.data.Exercise
import com.yago.aegis.data.getExerciseIcon

@Composable
fun ExerciseCard(
    exercise: Exercise,
    onDelete: () -> Unit,
    onEdit: (() -> Unit)? = null,
    showReorderHandle: Boolean = false,
    modifier: Modifier = Modifier,
    isAddMode: Boolean = false,
    // Añadimos este parámetro para pasar el modificador de arrastre desde la Screen
    dragHandleModifier: Modifier = Modifier
) {
    Surface(
        // Aplicamos el modifier base (que trae el padding o lo que venga de la lista)
        modifier = modifier
            .fillMaxWidth()
            .then(if (onEdit != null) Modifier.clickable { onEdit() } else Modifier),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. ICONO DINÁMICO
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getExerciseIcon(exercise.iconName),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 2. TEXTO E INFORMACIÓN
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name.uppercase(),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = exercise.tags
                        .filter { it != DefaultExercises.BASE_TAG }
                        .takeIf { it.isNotEmpty() }
                        ?.joinToString(" • ")
                        ?.uppercase()
                        ?: "${exercise.type} • ${exercise.muscleGroup}".uppercase(),
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // 3. ACCIONES Y MANEJADOR
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isAddMode) {
                    IconButton(onClick = onEdit ?: {}) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Add",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                // --- MANEJADOR DE REORDEN ---
                if (showReorderHandle) {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = "Reorder",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = dragHandleModifier // ✅ Aquí aplicamos el arrastre
                            .padding(start = 8.dp)
                            .size(24.dp)
                    )
                }
            }
        }
    }
}