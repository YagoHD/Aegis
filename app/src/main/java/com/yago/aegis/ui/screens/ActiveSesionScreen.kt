package com.yago.aegis.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.ui.components.AegisAlertDialog
import com.yago.aegis.ui.components.AegisTopBar
import com.yago.aegis.ui.components.ExerciseSessionCard
import com.yago.aegis.ui.components.SessionProgressHeader
import com.yago.aegis.viewmodel.ProfileViewModel
import com.yago.aegis.viewmodel.RoutinesViewModel
import com.yago.aegis.viewmodel.WorkoutViewModel

@Composable
fun ActiveSessionScreen(
    workoutViewModel: WorkoutViewModel,
    routinesViewModel: RoutinesViewModel,
    onFinishWorkout: () -> Unit,
    profileViewModel: ProfileViewModel,
) {
    val session = workoutViewModel.activeSession ?: return
    var showCancelDialog by remember { mutableStateOf(false) }

    // --- NUEVO ESTADO PARA EL DIÁLOGO DE CONFIRMACIÓN ---
    var exercisesToAutoCheck by remember { mutableStateOf<List<com.yago.aegis.data.ExerciseProgress>>(emptyList()) }

    // --- DIÁLOGO DE ABANDONO (EXISTENTE) ---
    if (showCancelDialog) {
        AegisAlertDialog(
            title = "ABANDONAR SESIÓN",
            confirmText = "CANCELAR ENTRENAMIENTO",
            dismissText = "CONTINUAR",
            onDismiss = { showCancelDialog = false },
            onConfirm = {
                showCancelDialog = false
                onFinishWorkout()
            }
        ) {
            Text(
                text = "Se perderá todo el progreso de esta sesión. Esta acción no se puede deshacer.",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 14.sp
            )
        }
    }

    // --- NUEVO DIÁLOGO: CONFIRMAR EJERCICIOS OLVIDADOS ---
    if (exercisesToAutoCheck.isNotEmpty()) {
        AegisAlertDialog(
            title = "EJERCICIOS SIN MARCAR",
            confirmText = "MARCAR Y FINALIZAR",
            dismissText = "REVISAR",
            onDismiss = { exercisesToAutoCheck = emptyList() },
            onConfirm = {
                // Marcamos todos los pendientes y finalizamos
                exercisesToAutoCheck.forEach {
                    workoutViewModel.toggleExerciseCompleted(it.exercise.id)
                }
                exercisesToAutoCheck = emptyList()

                // Ejecutamos el finalizado real
                workoutViewModel.finishWorkout(routinesViewModel) {
                    profileViewModel.incrementDisciplineDay()
                    onFinishWorkout()
                }
            }
        ) {
            Column {
                Text(
                    text = "Has introducido datos en los siguientes ejercicios pero no los has marcado como completados:",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                exercisesToAutoCheck.forEach { progress ->
                    Text(
                        text = "• ${progress.exercise.name.uppercase()}",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AegisTopBar(
                title = session.routineName.uppercase(),
                subtitle = "SESIÓN EN CURSO",
                navigationIcon = {
                    IconButton(onClick = { showCancelDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                SessionProgressHeader(session)
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                itemsIndexed(
                    items = session.exercisesProgress,
                    key = { _, progress -> progress.exercise.id }
                ) { index, progress ->
                    Column {
                        ExerciseSessionCard(
                            progress = progress,
                            onAddSet = { workoutViewModel.addSet(progress.exercise.id) },
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

                        if (index < session.exercisesProgress.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(top = 16.dp, start = 8.dp, end = 8.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            // --- LÓGICA DE DETECCIÓN ANTES DE FINALIZAR ---
                            val uncompletedWithData = session.exercisesProgress.filter { progress ->
                                val hasData = progress.sets.any { it.weight > 0 || it.reps > 0 }
                                val isNotChecked = !progress.sets.all { it.isCompleted }
                                hasData && isNotChecked
                            }

                            if (uncompletedWithData.isNotEmpty()) {
                                exercisesToAutoCheck = uncompletedWithData
                            } else {
                                workoutViewModel.finishWorkout(routinesViewModel) {
                                    profileViewModel.incrementDisciplineDay()
                                    onFinishWorkout()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 40.dp)
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = "FINALIZAR ENTRENAMIENTO",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }
            }
        }
    }
}