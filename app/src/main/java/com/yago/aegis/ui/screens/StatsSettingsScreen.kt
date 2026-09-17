package com.yago.aegis.ui.screens
import com.yago.aegis.ui.theme.Spacing
import com.yago.aegis.ui.theme.Radius

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yago.aegis.R
import com.yago.aegis.ui.components.AegisTopBar
import com.yago.aegis.ui.components.SectionHeader
import com.yago.aegis.ui.components.SettingsRow
import com.yago.aegis.ui.components.VerticalDividerSection
import com.yago.aegis.viewmodel.StatsViewModel

@Composable
fun StatsSettingsScreen(
    viewModel: StatsViewModel,
) {
    val scrollState = rememberScrollState()

    // Suponiendo que estos estados vienen de tu ViewModel conectados al DataStore
    val showVolume by viewModel.showVolumeCard.collectAsState(initial = true)
    val showDiscipline by viewModel.showDisciplineCard.collectAsState(initial = true)
    val showEvolution by viewModel.showEvolutionGraph.collectAsState(initial = true)
    val showAnalytics by viewModel.showAnalyticsList.collectAsState(initial = true)
    val targetDays by viewModel.targetDaysPerWeek.collectAsState(initial = 5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(Spacing.xl))

        // --- SECCIÓN 1: OBJETIVOS SEMANALES ---
        SectionHeader(text = stringResource(R.string.stats_settings_title))
        Spacer(modifier = Modifier.height(Spacing.lg))

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(Radius.lg)
        ) {
            Column(modifier = Modifier.padding(Spacing.lg)) {
                Text(
                    text = stringResource(R.string.weekly_training_days_title),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(Spacing.md))

                // Un selector simple de días (puedes usar un Slider o botones)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..7).forEach { day ->
                        val isSelected = targetDays == day
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(Radius.md))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { viewModel.updateTargetDays(day) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        VerticalDividerSection()

        // --- SECCIÓN 2: VISIBILIDAD DE MÓDULOS ---
        SectionHeader(text = stringResource(R.string.modules_visibility_title))
        Spacer(modifier = Modifier.height(Spacing.md))

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(Radius.lg)
        ) {
            Column(modifier = Modifier.padding(Spacing.sm)) {
                SettingsRow(stringResource(R.string.volume_module_label), showVolume) { viewModel.toggleVolumeCard(it) }
                SettingsRow(stringResource(R.string.discipline_module_label), showDiscipline) { viewModel.toggleDisciplineCard(it) }
                SettingsRow(stringResource(R.string.evolution_graph_module_label), showEvolution) { viewModel.toggleEvolutionGraph(it) }
                SettingsRow(stringResource(R.string.exercise_analytics_module_label), showAnalytics) { viewModel.toggleAnalyticsList(it) }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}