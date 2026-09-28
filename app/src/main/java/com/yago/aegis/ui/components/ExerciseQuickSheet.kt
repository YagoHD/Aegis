package com.yago.aegis.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.R
import com.yago.aegis.ui.theme.Spacing
import com.yago.aegis.viewmodel.StatsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Ficha rápida de un ejercicio COMO PANEL sobre el entreno (no cambia de pantalla).
 * Se abre tocando el título del ejercicio durante la sesión y se cierra tocando fuera, arrastrando
 * o con la X. Muestra el histórico de pesos (gráfica de progresión) + PR + última sesión.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseQuickSheet(
    exerciseId: Long,
    viewModel: StatsViewModel,
    onDismiss: () -> Unit
) {
    val exercises by viewModel.allExercises.collectAsState()
    val exercise = remember(exercises, exerciseId) { exercises.find { it.id == exerciseId } }
    val history by viewModel.getExerciseHistory(exerciseId).collectAsState(initial = emptyList())

    // Nombre "de respaldo" desde el historial (por si el ejercicio ya no está en la biblioteca).
    val historyName = remember(history, exerciseId) {
        history.flatMap { it.exercisesProgress }.find { it.exercise.id == exerciseId }?.exercise?.name
    }
    val matchName = exercise?.name ?: historyName

    fun setsOf(session: com.yago.aegis.data.WorkoutSession) =
        session.exercisesProgress
            .filter { it.exercise.id == exerciseId || (matchName != null && it.exercise.name == matchName) }
            .flatMap { it.sets }
            .filter { it.isCompleted && it.weight > 0 }

    val prRecord = remember(history) { history.flatMap { setsOf(it) }.maxOfOrNull { it.weight } ?: 0.0 }

    val chartData = remember(history) {
        history.mapNotNull { s -> setsOf(s).maxOfOrNull { it.weight }?.toFloat()?.let { s.date to it } }
            .sortedBy { it.first }
            .takeLast(10)
    }
    val weights = remember(chartData) { chartData.map { it.second } }
    val labels = remember(chartData) {
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
    val gain = remember(chartData) {
        val m = chartData.map { it.second }
        if (m.size >= 2 && m.first() > 0f) {
            val g = ((m.last() - m.first()) / m.first() * 100).toInt()
            if (g >= 0) "+$g%" else "$g%"
        } else "0%"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = (matchName ?: "").uppercase(),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.btn_cancel),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(Modifier.height(Spacing.md))

            if (chartData.isEmpty()) {
                Text(
                    text = stringResource(R.string.empty_history_subtitle),
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xxl)
                )
            } else {
                ProgressionChartSection(
                    currentMax = "${prRecord.toInt()} kg",
                    percentageGain = gain,
                    dataPoints = weights,
                    dateLabels = labels
                )
                Spacer(Modifier.height(Spacing.lg))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    StatCard(
                        title = stringResource(R.string.pr_record_title),
                        mainValue = "${prRecord.toInt()} ${stringResource(R.string.label_kg_lower)}",
                        modifier = Modifier.weight(1f),
                        subValue = stringResource(R.string.max_historical_label)
                    )
                    StatCard(
                        title = stringResource(R.string.last_workout_card_title),
                        mainValue = SimpleDateFormat("MMM dd", Locale.getDefault())
                            .format(Date(history.lastOrNull()?.date ?: 0L)).uppercase(),
                        modifier = Modifier.weight(1f),
                        subValue = stringResource(R.string.last_session_label)
                    )
                }
            }
        }
    }
}
