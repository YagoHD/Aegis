package com.yago.aegis.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
        Spacer(modifier = Modifier.height(24.dp))

        // --- SECCIÓN 1: OBJETIVOS SEMANALES ---
        SectionHeader(text = "PERSONALIZA TU PANTALLA")
        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "DÍAS DE ENTRENAMIENTO SEMANAL",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(12.dp))

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
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { viewModel.updateTargetDays(day) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                color = if (isSelected) Color.Black else Color.White,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        VerticalDividerSection()

        // --- SECCIÓN 2: VISIBILIDAD DE MÓDULOS ---
        SectionHeader(text = "VISIBILIDAD DE MÓDULOS")
        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                SettingsRow("VOLUMEN SEMANAL", showVolume) { viewModel.toggleVolumeCard(it) }
                SettingsRow("DISCIPLINA SEMANAL", showDiscipline) { viewModel.toggleDisciplineCard(it) }
                SettingsRow("EVOLUCIÓN DE CARGA", showEvolution) { viewModel.toggleEvolutionGraph(it) }
                SettingsRow("ANÁLISIS POR EJERCICIO", showAnalytics) { viewModel.toggleAnalyticsList(it) }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}