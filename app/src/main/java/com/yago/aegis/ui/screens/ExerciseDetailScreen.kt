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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExerciseDetailScreen(
    exerciseId: Long,
    viewModel: StatsViewModel,
    onBack: () -> Unit
) {
    val exercises by viewModel.allExercises.collectAsState()
    val exercise = remember(exercises, exerciseId) { exercises.find { it.id == exerciseId } }
    val history by viewModel.getExerciseHistory(exerciseId).collectAsState(initial = emptyList())

    // ── exerciseName PRIMERO — el resto depende de él ──
    val exerciseName = remember(history) {
        history.flatMap { it.exercisesProgress }
            .find { it.exercise.id == exerciseId }?.exercise?.name
    }

    val prRecord = remember(history, exerciseName) {
        history.flatMap { session ->
            session.exercisesProgress
                .filter {
                    it.exercise.id == exerciseId ||
                    (exerciseName != null && it.exercise.name == exerciseName)
                }
                .flatMap { it.sets }
        }.maxOfOrNull { it.weight } ?: 0.0
    }

    val lastLiftFormatted = remember(history, exerciseName) {
        val lastDate = history.lastOrNull()?.date ?: 0L
        if (lastDate == 0L) "--"
        else SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(lastDate)).uppercase()
    }

    val percentageGain = remember(history, exerciseName) {
        val allWeights = history.flatMap { session ->
            session.exercisesProgress
                .filter {
                    it.exercise.id == exerciseId ||
                    (exerciseName != null && it.exercise.name == exerciseName)
                }
                .flatMap { it.sets }
                .map { it.weight }
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

    val chartData = remember(history, exerciseName) {
        history
            .mapNotNull { session ->
                val sets = session.exercisesProgress
                    .filter {
                        it.exercise.id == exerciseId ||
                        (exerciseName != null && it.exercise.name == exerciseName)
                    }
                    .flatMap { it.sets }
                    .filter { it.isCompleted && it.weight > 0 }
                val maxW = sets.maxOfOrNull { it.weight }?.toFloat()
                if (maxW != null) Pair(session.date, maxW) else null
            }
            .sortedBy { it.first }
            .takeLast(10)
    }

    val weightHistoryForChart = remember(chartData) { chartData.map { it.second } }

    val chartDateLabels = remember(chartData) {
        val sdf = SimpleDateFormat("d MMM", Locale.getDefault())
        when {
            chartData.isEmpty() -> emptyList()
            chartData.size == 1 -> listOf(sdf.format(Date(chartData[0].first)))
            else -> listOf(
                sdf.format(Date(chartData.first().first)),
                sdf.format(Date(chartData[chartData.size / 2].first)),
                sdf.format(Date(chartData.last().first))
            )
        }
    }

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
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            item {
                ProgressionChartSection(
                    currentMax = "${prRecord.toInt()} kg",
                    percentageGain = percentageGain,
                    dataPoints = weightHistoryForChart,
                    dateLabels = chartDateLabels
                )
            }

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

            item {
                SectionHeader(text = "HISTORIAL DE SETS")
                Spacer(Modifier.height(16.dp))
            }

            itemsIndexed(history.takeLast(5).reversed()) { index, session ->
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
                if (index < 4) Spacer(Modifier.height(8.dp))
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}
