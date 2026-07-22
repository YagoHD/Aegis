package com.yago.aegis.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.R
import com.yago.aegis.ui.components.AegisAlertDialog
import com.yago.aegis.ui.components.AegisTopBar
import com.yago.aegis.ui.components.RoutineSelectionCard
import com.yago.aegis.viewmodel.RoutinesViewModel
import com.yago.aegis.viewmodel.WorkoutViewModel

@Composable
fun SelectRoutineScreen(
    routinesViewModel: RoutinesViewModel,
    workoutViewModel: WorkoutViewModel,
    onNavigateToCreateRoutine: () -> Unit,
    onStartWorkout: (Int) -> Unit,
    onNavigateToPlateCalculator: () -> Unit = {},
    onResumeSession: () -> Unit = {},
    onStartCustomWorkout: (String) -> Unit = {}
) {
    val routines = routinesViewModel.routines
    val activeSession by workoutViewModel.activeSession.collectAsState()
    val isPaused by workoutViewModel.isPaused.collectAsState()
    val pausedRoutineName = if (isPaused) activeSession?.routineName else null

    // Mientras haya una sesión activa se bloquea empezar cualquier OTRA rutina.
    val activeName = activeSession?.routineName
    val hasActiveSession = activeSession != null
    var showLockedDialog by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }
    var customName by remember { mutableStateOf("") }

    if (showCustomDialog) {
        AegisAlertDialog(
            title = stringResource(R.string.custom_workout_dialog_title),
            confirmText = stringResource(R.string.custom_workout_start_btn),
            dismissText = stringResource(R.string.btn_cancel),
            onDismiss = { showCustomDialog = false },
            onConfirm = {
                if (customName.isNotBlank()) {
                    showCustomDialog = false
                    onStartCustomWorkout(customName)
                }
            }
        ) {
            OutlinedTextField(
                value = customName,
                onValueChange = { customName = it },
                placeholder = {
                    Text(
                        stringResource(R.string.custom_workout_name_placeholder),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }

    if (showLockedDialog && activeName != null) {
        AegisAlertDialog(
            title = stringResource(R.string.active_session_lock_title),
            confirmText = stringResource(R.string.cancel_active_session_btn),
            dismissText = stringResource(R.string.btn_close),
            onDismiss = { showLockedDialog = false },
            onConfirm = {
                showLockedDialog = false
                workoutViewModel.cancelWorkout { }
            },
            confirmButtonColor = MaterialTheme.colorScheme.error
        ) {
            Text(
                text = stringResource(R.string.active_session_lock_message, activeName),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }

    Scaffold(
        // Cambiamos BackgroundBlackGrey por el background puro del Theme (050505)
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AegisTopBar(
                title = stringResource(R.string.select_routine_title),
                actions = {
                    IconButton(onClick = onNavigateToPlateCalculator) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = stringResource(R.string.plate_calculator_desc),
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp) // Mantenemos el padding de lujo
        ) {
            // --- BANNER DE SESIÓN EN CURSO ---
            if (hasActiveSession && activeName != null) {
                item {
                    ActiveSessionBanner(
                        name = activeName,
                        onResume = onResumeSession,
                        onCancel = { workoutViewModel.cancelWorkout { } }
                    )
                }
            }

            // --- BOTÓN ENTRENAMIENTO LIBRE (oculto si hay sesión activa) ---
            if (!hasActiveSession) {
                item {
                    Surface(
                        onClick = {
                            customName = ""
                            showCustomDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                            .height(52.dp),
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.custom_workout_btn),
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                letterSpacing = 1.5.sp
                            )
                        }
                    }
                }
            }

            // --- CABECERA TÉCNICA ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.your_routines_label),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${routines.size} ${stringResource(R.string.routines_count_label)}",
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    }

                    // Botón "NEW" con estilo minimalista
                    Surface(
                        onClick = onNavigateToCreateRoutine,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                stringResource(R.string.new_routine_btn),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // --- ESTADO VACÍO REFINADO ---
            if (routines.isEmpty()) {
                item {
                    EmptyRoutinesPlaceholder()
                }
            } else {
                // --- LISTA DE RUTINAS ---
                items(routines, key = { it.id }) { routine ->
                    val safeRoutine = workoutViewModel.getSafeRoutine(routine)

                    // Generamos los tags con un estilo más técnico
                    val routineTags = remember(routine.exercises) {
                        routine.exercises.flatMap { it.tags }
                            .distinct()
                            .take(3) // No saturar la tarjeta
                            .joinToString("  ") { "#${it.uppercase()}" }
                    }

                    val isThisActive = activeName == safeRoutine.name
                    val isThisPaused = pausedRoutineName == safeRoutine.name
                    val isLocked = hasActiveSession && !isThisActive
                    val pausedBadge = stringResource(R.string.paused_session_badge)
                    RoutineSelectionCard(
                        routine = safeRoutine,
                        displayTags = routineTags,
                        isLocked = isLocked,
                        isActiveSession = isThisActive,
                        lastPerformedText = if (isThisPaused) pausedBadge
                                           else workoutViewModel.calculateLastPerformed(safeRoutine.lastCompletedDates).uppercase(),
                        onStartClick = {
                            when {
                                isLocked -> showLockedDialog = true
                                isThisActive -> onResumeSession()
                                else -> {
                                    workoutViewModel.startWorkout(safeRoutine)
                                    onStartWorkout(safeRoutine.id)
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun ActiveSessionBanner(
    name: String,
    onResume: () -> Unit,
    onCancel: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.active_session_lock_title),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name.uppercase(),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onResume,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.btn_continue),
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                }
                TextButton(onClick = onCancel) {
                    Text(
                        text = stringResource(R.string.cancel_active_session_btn),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyRoutinesPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.no_routines_message),
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}