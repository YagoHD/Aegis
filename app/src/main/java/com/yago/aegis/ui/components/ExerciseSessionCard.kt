package com.yago.aegis.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.yago.aegis.ui.theme.AegisBronze

@Composable
fun ExerciseSessionCard(
    progress: ExerciseProgress,
    onAddSet: () -> Unit,
    onUpdateSet: (String, Double, Int, Boolean) -> Unit,
    onDeleteSet: (String) -> Unit,
    onToggleExercise: () -> Unit // <-- Nueva acción
) {
    val isExerciseDone = progress.sets.isNotEmpty() && progress.sets.all { it.isCompleted }
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
        // TÍTULO: 01. BENCH PRESS
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = progress.exercise.name.uppercase(),
                style = TextStyle(
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = if (isExerciseDone) AegisBronze else Color.White
                ),
                modifier = Modifier.weight(1f)
            )

            // ✅ AQUÍ ESTÁ EL CHECK A LA DERECHA DEL NOMBRE
            IconButton(onClick = onToggleExercise) {
                Icon(
                    imageVector = if (isExerciseDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isExerciseDone) AegisBronze else Color.DarkGray,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        LastSessionCard(lastSetsText = progress.exercise.lastPerformance ?: "No hay datos previos")

        Spacer(modifier = Modifier.height(8.dp))
        // FILAS DE SERIES (Como en tu imagen)
        progress.sets.forEachIndexed { index, set ->
            SetRow(
                index = index + 1,
                set = set,
                onUpdate = { w, r, c -> onUpdateSet(set.id, w, r, c) },
                onDelete = { onDeleteSet(set.id) }
            )
        }

        // BOTÓN + ADD SET
        TextButton(onClick = onAddSet, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Icon(Icons.Default.Add, contentDescription = null, tint = AegisBronze)
            Text("ADD SET", color = AegisBronze)
        }
    }
}