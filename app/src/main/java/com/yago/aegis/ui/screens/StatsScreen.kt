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
    val showVolume by viewModel.showVolumeCard.collectAsState(initial = true)
    val showDiscipline by viewModel.showDisciplineCard.collectAsState(initial = true)
    val showEvolution by viewModel.showEvolutionGraph.collectAsState(initial = true)
    val showAnalytics by viewModel.showAnalyticsList.collectAsState(initial = true)
    val targetDays by viewModel.targetDaysPerWeek.collectAsState(initial = 5)
    val weeklyStats by viewModel.weeklyDiscipline.collectAsState(initial = 0 to 5)
    val volumeStats by viewModel.weeklyVolumeStats.collectAsState(initial = 0.0 to 0.0)
    val filteredList by viewModel.filteredExercises.collectAsState(initial = emptyList())
    val availableTags by viewModel.availableStatsTags.collectAsState(initial = emptyList())
    val monthlyData by viewModel.monthlyVolumeEvolution.collectAsState(initial = emptyList())

    // Usamos Scaffold para que la TopBar esté fija y el contenido haga scroll debajo
    Scaffold(
        topBar = {
            AegisTopBar(
                title = "ESTADÍSTICAS",
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp), // Padding global para todos los items
            // Aumentamos el espacio entre secciones para el look "modular"
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {

            item { Spacer(Modifier.height(8.dp)) } // Margen superior tras la TopBar

            // --- BLOQUE DE MÉTRICAS (DISCIPLINA Y VOLUMEN) ---
            if (showDiscipline || showVolume) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (showDiscipline) {
                            StatCard(
                                title = "DISCIPLINA SEMANAL",
                                mainValue = "${weeklyStats.first}/$targetDays",
                                subValue = "SESIONES COMPLETADAS",
                                modifier = Modifier.weight(1f),
                                showProgress = true,
                                progress = if (targetDays > 0) weeklyStats.first.toFloat() / targetDays else 0f
                            )
                        }

                        if (showVolume) {
                            StatCard(
                                title = "VOLUMEN SEMANAL",
                                mainValue = formatVolume(volumeStats.first),
                                subValue = when {
                                    volumeStats.second.isNaN() -> "SIN DATOS"
                                    volumeStats.second >= 0 -> "+${volumeStats.second.toInt()}% VS PREV"
                                    else -> "${volumeStats.second.toInt()}% VS PREV"
                                },
                                isPositive = volumeStats.second.isNaN() || volumeStats.second >= 0,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // --- BLOQUE DE EVOLUCIÓN (GRÁFICA DE BARRAS) ---
            if (showEvolution) {
                item {
                    WeightEvolutionSection(monthlyData = monthlyData)
                }
            }

            // --- SECCIÓN DE ANALÍTICAS ---
            if (showAnalytics) {
                item {
                    ExerciseAnalyticsHeader(
                        searchQuery = viewModel.searchQuery,
                        onSearchChange = { viewModel.searchQuery = it },
                        availableTags = availableTags,
                        selectedTag = viewModel.selectedTag,
                        onTagSelected = { viewModel.selectedTag = it }
                    )
                }

                // Usamos un espaciado menor entre las filas de ejercicios (8.dp)
                items(filteredList) { exercise ->
                    ExerciseStatRow(
                        exercise = exercise,
                        onClick = { onNavigateToExerciseDetail(exercise.id) }
                    )
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}