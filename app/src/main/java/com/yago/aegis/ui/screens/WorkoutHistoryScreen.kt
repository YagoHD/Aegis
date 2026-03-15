package com.yago.aegis.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.data.WorkoutSession
import com.yago.aegis.ui.components.AegisTopBar
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WorkoutHistoryScreen(
    sessions: List<WorkoutSession>,
    onBack: () -> Unit
) {
    // Ordenamos de más reciente a más antiguo
    val sorted = remember(sessions) { sessions.sortedByDescending { it.date } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AegisTopBar(
                title = "HISTORIAL",
                subtitle = "${sorted.size} SESIONES",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (sorted.isEmpty()) {
            EmptyHistoryState()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Agrupar por mes para separadores
                val grouped = sorted.groupBy { session ->
                    SimpleDateFormat("MMMM yyyy", Locale("es")).format(Date(session.date))
                        .replaceFirstChar { it.uppercase() }
                }

                grouped.forEach { (month, monthSessions) ->
                    item(key = month) {
                        Text(
                            text = month.uppercase(),
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    itemsIndexed(
                        items = monthSessions,
                        key = { _, session -> session.id }
                    ) { _, session ->
                        HistorySessionRow(session = session)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorySessionRow(session: WorkoutSession) {
    var expanded by remember { mutableStateOf(false) }

    val dateStr = remember(session.date) {
        SimpleDateFormat("EEE dd MMM · HH:mm", Locale("es"))
            .format(Date(session.date))
            .replaceFirstChar { it.uppercase() }
    }

    val completedExercises = remember(session) {
        session.exercisesProgress.filter { prog ->
            prog.sets.any { it.isCompleted }
        }
    }

    val exerciseCount = completedExercises.size

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // ─── FILA COMPACTA ───
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicador lateral bronce
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(36.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(2.dp)
                        )
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.routineName.uppercase(),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$dateStr · $exerciseCount ejercicios",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // ─── DETALLE EXPANDIBLE ───
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(200)),
                exit = shrinkVertically(animationSpec = tween(200))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 31.dp, end = 16.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    if (completedExercises.isEmpty()) {
                        Text(
                            "Sin ejercicios completados",
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic
                        )
                    } else {
                        completedExercises.forEach { progress ->
                            val completedSets = progress.sets.filter { it.isCompleted }
                            val setsText = completedSets.joinToString("   ") { set ->
                                val w = if (set.weight == 0.0) "BW"
                                        else if (set.weight % 1 == 0.0) "${set.weight.toInt()}kg"
                                        else "${"%.1f".format(set.weight)}kg"
                                "$w × ${set.reps}"
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = progress.exercise.name.uppercase(),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.3.sp,
                                    modifier = Modifier.width(120.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = setsText,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "SIN HISTORIAL",
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Completa tu primer entrenamiento\npara ver tu historial aquí.",
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
            fontSize = 13.sp,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}
