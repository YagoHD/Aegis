package com.yago.aegis.ui.components

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 4.dp) // Un pequeño margen interno para no pegar al borde
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            // CONTENEDOR DEL ICONO: Usamos surfaceVariant (el gris oscuro técnico)
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(8.dp), // Menos redondeado para look más agresivo
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = null // Mantenerlo limpio
            ) {
                Icon(
                    imageVector = getExerciseIcon(exercise.iconName),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary, // AegisBronze
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // NOMBRE: Black Italic (Sello de la casa)
                Text(
                    text = exercise.name.uppercase(),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                Spacer(Modifier.height(2.dp))

                // STATS: Mezclamos Gris Acero con un toque de Bronce en el PR
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = exercise.muscleGroup.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.secondary, // AegisSteel
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )

                    Text(
                        text = "  •  ",
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    )

                    Text(
                        text = "PR: ${exercise.oneRepMax} KG",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f), // Toque de bronce
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }

            // INDICADOR DE ACCIÓN
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                modifier = Modifier.size(18.dp)
            )
        }

        // SEPARADOR AEGIS: Muy sutil, usando el color de los bordes del sistema
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
            thickness = 1.dp
        )
    }
}