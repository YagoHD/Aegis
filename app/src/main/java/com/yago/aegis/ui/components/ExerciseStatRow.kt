package com.yago.aegis.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.data.Exercise
import com.yago.aegis.data.getExerciseIcon

@Composable
fun ExerciseStatRow(
    exercise: Exercise,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp) // Espaciado entre tarjetas igual que en tus listas
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant, // 30%: SurfaceDark/Variant
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) // El mismo acero sutil
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. ICONO DINÁMICO (Con el mismo recuadro que tu ExerciseCard)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getExerciseIcon(exercise.iconName),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary, // AegisBronze
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 2. TEXTO E INFORMACIÓN (Mismo estilo de jerarquía)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name.uppercase(),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp, // Alineado con tu ExerciseCard
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${exercise.type} • ${exercise.muscleGroup}".uppercase(),
                    color = MaterialTheme.colorScheme.secondary, // AegisSteel
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // 3. SECCIÓN DE DATOS (En lugar de iconos, mostramos el PR)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "PR RECORD",
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${exercise.oneRepMax.toInt()} KG",
                    color = MaterialTheme.colorScheme.primary, // Resaltado en Bronce
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // INDICADOR DE ENTRADA
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}