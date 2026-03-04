package com.yago.aegis.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.data.Exercise
import com.yago.aegis.data.getExerciseIcon
import com.yago.aegis.ui.theme.AegisBronze

@Composable
fun ExerciseCard(
    exercise: Exercise,
    onDelete: () -> Unit,
    onEdit: (() -> Unit)? = null,
    showReorderHandle: Boolean = false,
    modifier: Modifier = Modifier,
    isAddMode: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF161616), RoundedCornerShape(12.dp))
            // Si onEdit no es null, hacemos que la tarjeta sea clickable
            .then(if (onEdit != null) Modifier.clickable { onEdit() } else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. ICONO DINÁMICO
        // Dentro de ExerciseCard.kt
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF252525), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                // ✅ CLAVE: Aquí debe llamar a la función que creamos en Exercise.kt
                imageVector = getExerciseIcon(exercise.iconName),
                contentDescription = null,
                tint = AegisBronze,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 2. TEXTO
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exercise.name.uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = if (exercise.tags.isNotEmpty()) exercise.tags.joinToString(" • ")
                else "${exercise.type} • ${exercise.muscleGroup}",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        // 3. ACCIONES
        if (isAddMode) {
            // ICONO DE AÑADIR (Solo en pantalla Add)
            IconButton(onClick = onEdit ?: {}) { // Usamos la acción de añadir
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "Add",
                    tint = AegisBronze, // Color bronce para que resalte
                    modifier = Modifier.size(28.dp)
                )
            }
        } else {
            // ICONO DE BORRAR (En Librería y Rutina)
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.DarkGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Solo mostramos el "arrastrador" si estamos en la pantalla de rutinas
        if (showReorderHandle) {
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Reorder",
                tint = Color.DarkGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}