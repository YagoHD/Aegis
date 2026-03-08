package com.yago.aegis.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.ui.components.AegisTopBar
import com.yago.aegis.ui.components.RoutineSelectionCard
import com.yago.aegis.viewmodel.RoutinesViewModel
import com.yago.aegis.viewmodel.WorkoutViewModel

@Composable
fun SelectRoutineScreen(
    routinesViewModel: RoutinesViewModel,
    workoutViewModel: WorkoutViewModel,
    onNavigateToCreateRoutine: () -> Unit,
    onStartWorkout: (Int) -> Unit
) {
    val routines = routinesViewModel.routines

    Scaffold(
        // Cambiamos BackgroundBlackGrey por el background puro del Theme (050505)
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AegisTopBar(
                title = "SELECCIONA TU MISIÓN", // Un toque más agresivo/técnico
                actions = {
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
                            text = "TUS RUTINAS",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${routines.size} RUTINAS A ELEGIR",
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
                                "NUEVA",
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

                    RoutineSelectionCard(
                        routine = safeRoutine,
                        displayTags = routineTags,
                        lastPerformedText = workoutViewModel.calculateLastPerformed(safeRoutine.lastCompletedDates).uppercase(),
                        onStartClick = {
                            workoutViewModel.startWorkout(safeRoutine)
                            onStartWorkout(safeRoutine.id)
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
            "NO ACTIVE ROUTINES DETECTED",
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}