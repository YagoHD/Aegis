package com.yago.aegis.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.ui.components.ExerciseSessionCard
import com.yago.aegis.ui.components.SessionProgressHeader
import com.yago.aegis.ui.theme.AegisBronze
import com.yago.aegis.viewmodel.RoutinesViewModel // ✅ Importante añadir este import
import com.yago.aegis.viewmodel.WorkoutViewModel

@Composable
fun ActiveSessionScreen(
    workoutViewModel: WorkoutViewModel,
    routinesViewModel: RoutinesViewModel, // ✅ AÑADIDO: Ahora la pantalla ya conoce este ViewModel
    onFinishWorkout: () -> Unit
) {
    // Si no hay sesión activa, salimos para evitar errores
    val session = workoutViewModel.activeSession ?: return

    Scaffold(
        containerColor = Color.Black
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // 1. CABECERA DE PROGRESO (Ej: 4 de 6 ejercicios)
            item {
                SessionProgressHeader(session)
            }

            // 2. LISTA DINÁMICA DE EJERCICIOS
            items(session.exercisesProgress) { progress ->
                ExerciseSessionCard(
                    progress = progress,
                    onAddSet = {
                        workoutViewModel.addSet(progress.exercise.id)
                    },
                    onUpdateSet = { setId, w, r, c ->
                        workoutViewModel.updateSet(progress.exercise.id, setId, w, r, c)
                    },
                    onDeleteSet = { setId ->
                        workoutViewModel.removeSet(progress.exercise.id, setId)
                    },
                    onToggleExercise = {
                        workoutViewModel.toggleExerciseCompleted(progress.exercise.id)
                    }
                )
            }

            // 3. BOTÓN DE FINALIZAR
            item {
                Button(
                    onClick = {
                        // Ahora sí podemos pasarle el routinesViewModel porque está en los parámetros
                        workoutViewModel.finishWorkout(routinesViewModel) {
                            onFinishWorkout()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AegisBronze),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "FINISH WORKOUT",
                        color = Color.Black,
                        style = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}