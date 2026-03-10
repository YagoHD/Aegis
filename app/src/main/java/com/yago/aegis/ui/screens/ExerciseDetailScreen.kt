package com.yago.aegis.ui.screens

import HistorySessionCard
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yago.aegis.ui.components.AegisTopBar
import com.yago.aegis.ui.components.ProgressionChartSection
import com.yago.aegis.ui.components.SectionHeader
import com.yago.aegis.ui.components.StatCard
import com.yago.aegis.viewmodel.StatsViewModel
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import com.yago.aegis.ui.components.getRecentMonths
import java.text.SimpleDateFormat
import java.util.*

//PANTALLA STATS DE EJERCICIO
@Composable
fun ExerciseDetailScreen(
    exerciseId: Long,
    viewModel: StatsViewModel,
    onBack: () -> Unit
) {
    val exercises by viewModel.allExercises.collectAsState()
    val exercise = remember(exercises, exerciseId) { exercises.find { it.id == exerciseId } }
    val history by viewModel.getExerciseHistory(exerciseId).collectAsState(initial = emptyList())

    // --- LÓGICA DE CÁLCULO (Se mantiene igual) ---
    val prRecord = remember(history) {
        history.flatMap { session ->
            session.exercisesProgress
                .filter { it.exercise.id == exerciseId }
                .flatMap { it.sets }
        }.maxOfOrNull { it.weight } ?: 0.0
    }

    val lastLiftFormatted = remember(history) {
        val lastDate = history.lastOrNull()?.date ?: 0L
        if (lastDate == 0L) "--"
        else SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(lastDate)).uppercase()
    }

    val percentageGain = remember(history) {
        val allWeights = history.flatMap { session ->
            session.exercisesProgress.filter { it.exercise.id == exerciseId }
                .flatMap { it.sets }.map { it.weight }
        }
        if (allWeights.size >= 2) {
            val first = allWeights.first()
            val last = allWeights.last()
            if (first == 0.0) "0%"
            else {
                val gain = ((last - first) / first) * 100
                if (gain >= 0) "+${gain.toInt()}%" else "${gain.toInt()}%"
            }
        } else "0%"
    }

    val weightHistoryForChart = remember(history) {
        val monthsLabels = getRecentMonths(5)
        val sdfMonth = SimpleDateFormat("MMM", Locale.getDefault())
        monthsLabels.map { label ->
            history.filter { session ->
                sdfMonth.format(Date(session.date)).uppercase() == label
            }.flatMap { session ->
                session.exercisesProgress.filter { it.exercise.id == exerciseId }.flatMap { it.sets }
            }.maxOfOrNull { it.weight }?.toFloat() ?: 0f
        }
    }

    // --- INTERFAZ TRANSFORMADA ---
    Scaffold(
        topBar = {
            AegisTopBar(
                title = exercise?.name?.uppercase() ?: "DETALLE",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Atrás",
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            // Espaciado modular entre bloques grandes
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            // 1. GRÁFICA DE PROGRESIÓN
            item {
                ProgressionChartSection(
                    currentMax = "${prRecord.toInt()} kg",
                    percentageGain = percentageGain,
                    dataPoints = weightHistoryForChart
                )
            }

            // 2. TARJETAS DE MÉTRICAS (PR y ÚLTIMO LEVANTAMIENTO)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "PR RECORD",
                        mainValue = "${prRecord.toInt()} kg",
                        modifier = Modifier.weight(1f),
                        subValue = "MAX HISTÓRICO",
                    )
                    StatCard(
                        title = "ULTIMO ENTRENO",
                        mainValue = lastLiftFormatted,
                        modifier = Modifier.weight(1f),
                        subValue = "ÚLTIMA SESIÓN",
                    )
                }
            }

            // 3. HISTORIAL RECIENTE (Compacto)
            item {
                SectionHeader(text = "HISTORIAL DE SETS")
                Spacer(Modifier.height(16.dp))
            }

            // Aplicamos la misma lógica de historial compacto que en la lista de ejercicios
            itemsIndexed(history.takeLast(5).reversed()) { index, session ->
                // Envolvemos cada sesión en una superficie con el estilo de la casa
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        HistorySessionCard(session, exerciseId)
                    }
                }

                // Espaciado corto entre tarjetas del historial
                if (index < 4) { // Solo si hay más elementos
                    Spacer(Modifier.height(8.dp))
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}