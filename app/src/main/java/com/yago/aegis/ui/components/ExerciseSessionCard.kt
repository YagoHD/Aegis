package com.yago.aegis.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.data.ExerciseProgress

@Composable
fun ExerciseSessionCard(
    progress: ExerciseProgress,
    onAddSet: () -> Unit,
    onUpdateSet: (String, Double, Int, Boolean) -> Unit,
    onDeleteSet: (String) -> Unit,
    onToggleExercise: () -> Unit,
    onSwitchVariant: ((Int) -> Unit)? = null   // newVariantIndex
) {
    val isExerciseDone = progress.sets.isNotEmpty() && progress.sets.all { it.isCompleted }
    val hasVariants = progress.slotVariants.size > 1 && onSwitchVariant != null
    val currentVariantIndex = if (hasVariants)
        progress.slotVariants.indexOfFirst { it.id == progress.exercise.id }.coerceAtLeast(0)
    else 0
    val totalVariants = progress.slotVariants.size

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // --- CABECERA TÉCNICA ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Nombre del ejercicio con flechas de variante si aplica
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasVariants) {
                    val canPrev = currentVariantIndex > 0
                    IconButton(
                        onClick = { if (canPrev) onSwitchVariant!!(currentVariantIndex - 1) },
                        modifier = Modifier.size(28.dp),
                        enabled = canPrev
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Variante anterior",
                            tint = if (canPrev) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                }
                Text(
                    text = progress.exercise.name.uppercase(),
                    style = TextStyle(
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp,
                        color = if (isExerciseDone) MaterialTheme.colorScheme.primary else Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
                if (hasVariants) {
                    Spacer(modifier = Modifier.width(2.dp))
                    val canNext = currentVariantIndex < totalVariants - 1
                    IconButton(
                        onClick = { if (canNext) onSwitchVariant!!(currentVariantIndex + 1) },
                        modifier = Modifier.size(28.dp),
                        enabled = canNext
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Variante siguiente",
                            tint = if (canNext) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            IconButton(onClick = onToggleExercise) {
                Icon(
                    imageVector = if (isExerciseDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isExerciseDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // Indicador de variante (sólo cuando hay múltiples)
        if (hasVariants) {
            Text(
                text = "${currentVariantIndex + 1} / $totalVariants  —  ${progress.slotVariants.getOrNull(
                    if (currentVariantIndex < totalVariants - 1) currentVariantIndex + 1 else currentVariantIndex - 1
                )?.name?.uppercase() ?: ""}",
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
        }

        // --- TARJETA DE REFERENCIA (Look Acero) ---
        LastSessionCard(lastSetsText = progress.exercise.lastPerformance)

        Spacer(modifier = Modifier.height(12.dp))

        // --- LISTA DE SERIES (SETS) ---
        progress.sets.forEachIndexed { index, set ->
            SetRow(
                index = index + 1,
                set = set,
                totalSets = progress.sets.size,
                onUpdate = { w, r, c -> onUpdateSet(set.id, w, r, c) },
                onDelete = { onDeleteSet(set.id) }
            )
        }

        // --- BOTÓN AÑADIR SERIE (Estilo Outlined Sutil) ---
        TextButton(
            onClick = onAddSet,
            modifier = Modifier
                .align(Alignment.Start) // Alineación a la izquierda para flujo de lectura técnico
                .padding(top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "AÑADIR SERIE",
                style = TextStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}