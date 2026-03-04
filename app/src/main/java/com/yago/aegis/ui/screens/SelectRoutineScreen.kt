package com.yago.aegis.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    // Obtenemos la lista de rutinas directamente del ViewModel
    val routines = routinesViewModel.routines

    Scaffold(
        containerColor = BackgroundBlackGrey,
        topBar = {
            AegisTopBar(
                title = "SELECT ROUTINE",
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.Gray
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
                .padding(horizontal = 16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Routines",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    TextButton(onClick = onNavigateToCreateRoutine) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = AegisBronze,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New", color = AegisBronze, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (routines.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tienes rutinas creadas aún.",
                            color = Color.Gray
                        )
                    }
                }
            } else {
                items(routines) { routine ->
                    val routineTags = routine.exercises
                        .flatMap { it.tags }
                        .toSet()
                        .joinToString(" ") { it.uppercase() }

                    val safeRoutine = if (routine.iconRes <= 0) {
                        routine.copy(iconRes = R.drawable.ic_launcher_foreground)
                    } else {
                        routine
                    }

                    RoutineSelectionCard(
                        routine = safeRoutine,
                        displayTags = routineTags,
                        onStartClick = {
                            workoutViewModel.startWorkout(safeRoutine)
                            onStartWorkout(safeRoutine.id)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}