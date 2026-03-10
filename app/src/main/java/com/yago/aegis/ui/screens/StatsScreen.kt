package com.yago.aegis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yago.aegis.ui.components.*
import com.yago.aegis.viewmodel.StatsViewModel

@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToExerciseDetail: (Long) -> Unit = {}
) {
    // 1. Estados de Visibilidad (conectados al DataStore)
    val showVolume by viewModel.showVolumeCard.collectAsState(initial = true)
    val showDiscipline by viewModel.showDisciplineCard.collectAsState(initial = true)
    val showEvolution by viewModel.showEvolutionGraph.collectAsState(initial = true)
    val showAnalytics by viewModel.showAnalyticsList.collectAsState(initial = true)

    // 2. Objetivo de días dinámico
    val targetDays by viewModel.targetDaysPerWeek.collectAsState(initial = 5)

    // 3. Datos de rendimiento
    val weeklyStats by viewModel.weeklyDiscipline.collectAsState(initial = 0 to 5)
    val volumeStats by viewModel.weeklyVolumeStats.collectAsState(initial = 0.0 to 0.0)
    val filteredList by viewModel.filteredExercises.collectAsState(initial = emptyList())
    val monthlyData by viewModel.monthlyVolumeEvolution.collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        // --- BARRA SUPERIOR ---
        item {
            AegisTopBar(
                title = "ESTADISTICAS",
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }

        // --- MÉTRICAS RÁPIDAS (DISCIPLINA Y VOLUMEN) ---
        if (showDiscipline || showVolume) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (showDiscipline) {
                        StatCard(
                            title = "DISCIPLINA SEMANAL",
                            // Usamos targetDays para que el "/5" cambie según los ajustes
                            mainValue = "${weeklyStats.first}/$targetDays",
                            subValue = "SESSIONS",
                            modifier = Modifier.weight(1.0f),
                            showProgress = true,
                            // Calculamos el progreso real basado en el objetivo del usuario
                            progress = if (targetDays > 0) weeklyStats.first.toFloat() / targetDays else 0f
                        )
                    }

                    if (showVolume) {
                        StatCard(
                            title = "VOLUMEN SEMANAL",
                            mainValue = formatVolume(volumeStats.first),
                            subValue = when {
                                volumeStats.second.isNaN() -> "NEW START"
                                volumeStats.second >= 0 -> "+${volumeStats.second.toInt()}%"
                                else -> "${volumeStats.second.toInt()}%"
                            },
                            isPositive = volumeStats.second.isNaN() || volumeStats.second >= 0,
                            modifier = Modifier.weight(1.0f)
                        )
                    }
                }
            }
        }

        // --- SECCIÓN DE EVOLUCIÓN MENSUAL ---
        if (showEvolution) {
            item {
                Box(Modifier.padding(horizontal = 16.dp)) {
                    WeightEvolutionSection(monthlyData = monthlyData)
                }
            }
        }

        // --- ANALÍTICAS (CABECERA Y LISTADO) ---
        if (showAnalytics) {
            item {
                Box(Modifier.padding(horizontal = 16.dp)) {
                    ExerciseAnalyticsHeader(
                        searchQuery = viewModel.searchQuery,
                        onSearchChange = { viewModel.searchQuery = it }
                    )
                }
            }

            items(filteredList) { exercise ->
                Box(Modifier.padding(horizontal = 16.dp)) {
                    ExerciseStatRow(
                        exercise = exercise,
                        onClick = { onNavigateToExerciseDetail(exercise.id) }
                    )
                }
            }
        }

        // ESPACIADO FINAL
        item { Spacer(Modifier.height(80.dp)) }
    }
}