package com.yago.aegis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
fun LibraryExerciseCard(
    exercise: Exercise,
    onAdd: () -> Unit,
    onDelete: () -> Unit
) {
    val RojoBurdeos = Color(0xFF800000) // Definimos el color aquí o en tu Theme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161616), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. CONTENEDOR DEL ICONO DINÁMICO
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.Black, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getExerciseIcon(exercise.iconName),
                contentDescription = null,
                tint = AegisBronze,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 2. TEXTOS (NOMBRE Y TAGS)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exercise.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            val subtitle = if (exercise.tags.isNotEmpty()) {
                exercise.tags.joinToString(" • ")
            } else {
                exercise.muscleGroup
            }
            Text(
                text = subtitle,
                color = Color.Gray,
                fontSize = 11.sp
            )
        }

        // 3. BOTÓN ELIMINAR (Papelera Rojo Burdeos)
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete from Library",
                tint = RojoBurdeos,
                modifier = Modifier.size(22.dp)
            )
        }

        // 4. BOTÓN AÑADIR (AddBox Bronce)
        IconButton(onClick = onAdd) {
            Icon(
                imageVector = Icons.Default.AddBox,
                contentDescription = "Add to Routine",
                tint = AegisBronze,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}