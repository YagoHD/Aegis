package com.yago.aegis.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.R
import com.yago.aegis.ui.components.AegisTopBar
import com.yago.aegis.ui.components.RoutineSelectionCard
import com.yago.aegis.ui.theme.AegisBronze
import com.yago.aegis.ui.theme.BackgroundBlackGrey
import com.yago.aegis.viewmodel.RoutinesViewModel
import com.yago.aegis.viewmodel.WorkoutViewModel

@Composable
fun SelectRoutineScreen(
    routinesViewModel: RoutinesViewModel,
    workoutViewModel: WorkoutViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToCreateRoutine: () -> Unit,
    onStartWorkout: (Int) -> Unit
) {
    val routines = routinesViewModel.routines

    Scaffold(
        containerColor = BackgroundBlackGrey,
        topBar = {
            AegisTopBar(
                title = "SELECT ROUTINE",
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings", tint = Color.Gray)
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // CABECERA CON BOTÓN NEW
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Your Routines", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)

                    TextButton(onClick = onNavigateToCreateRoutine) {
                        Icon(Icons.Default.Add, null, tint = AegisBronze, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New", color = AegisBronze, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ESTADO VACÍO
            if (routines.isEmpty()) {
                item {
                    EmptyRoutinesPlaceholder()
                }
            } else {
                // LISTA DE RUTINAS
                items(routines, key = { it.id }) { routine ->
                    // 1. Preparamos los datos
                    val safeRoutine = workoutViewModel.getSafeRoutine(routine)

                    val routineTags = remember(routine.exercises) {
                        routine.exercises.flatMap { it.tags }.toSet().joinToString(" ") { it.uppercase() }
                    }

                    // 2. Pintamos la tarjeta
                    RoutineSelectionCard(
                        routine = safeRoutine,
                        displayTags = routineTags,
                        lastPerformedText = workoutViewModel.calculateLastPerformed(safeRoutine.lastCompletedDates),
                        onStartClick = {
                            workoutViewModel.startWorkout(safeRoutine)
                            onStartWorkout(safeRoutine.id)
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun EmptyRoutinesPlaceholder() {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
        Text("No tienes rutinas creadas aún.", color = Color.Gray)
    }
}