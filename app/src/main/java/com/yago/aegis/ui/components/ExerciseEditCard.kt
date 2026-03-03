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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.FitnessCenter
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
import com.yago.aegis.ui.theme.AegisBronze

@Composable
fun ExerciseEditCard(
    exercise: Exercise,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier // Recibimos el modificador externo
) {
    Row(
        // ✅ APLICADO: Ahora el Row usa el modifier que permite el arrastre
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF161616), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono cuadrado oscuro con el icono del ejercicio
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF0A0A0A), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = AegisBronze,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Información del ejercicio
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exercise.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "${exercise.type} • ${exercise.muscleGroup}",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        // Acciones: Borrar
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.DarkGray,
                modifier = Modifier.size(20.dp)
            )
        }

        // Icono visual de "Reordenar" (las rayitas)
        Icon(
            imageVector = Icons.Default.DragHandle,
            contentDescription = "Reorder",
            tint = Color.DarkGray,
            modifier = Modifier.size(20.dp)
        )
    }
}