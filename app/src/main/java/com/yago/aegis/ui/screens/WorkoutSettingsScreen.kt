package com.yago.aegis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.ui.components.AegisTopBar
import com.yago.aegis.ui.components.SectionHeader
import com.yago.aegis.ui.components.SettingsRow
import com.yago.aegis.ui.components.VerticalDividerSection
import com.yago.aegis.viewmodel.WorkoutViewModel
import kotlinx.coroutines.launch

@Composable
fun WorkoutSettingsScreen(
    workoutViewModel: WorkoutViewModel,
    onNavigateBack: () -> Unit,
    onSave: suspend (seconds: Int, vibrate: Boolean, sound: Boolean, showTimer: Boolean) -> Unit
) {
    val currentSeconds by workoutViewModel.restTimerSeconds.collectAsState()
    val currentVibrate by workoutViewModel.timerVibrate.collectAsState()
    val currentSound by workoutViewModel.timerSound.collectAsState()
    val currentShow by workoutViewModel.showRestTimer.collectAsState()

    var timerSeconds by remember(currentSeconds) { mutableStateOf(currentSeconds) }
    var vibrate by remember(currentVibrate) { mutableStateOf(currentVibrate) }
    var sound by remember(currentSound) { mutableStateOf(currentSound) }
    var showTimer by remember(currentShow) { mutableStateOf(currentShow) }

    val scrollState = rememberScrollState()

    // Opciones de tiempo predefinidas (en segundos)
    val timeOptions = listOf(30, 45, 60, 90, 120, 150, 180, 240, 300)

    androidx.compose.runtime.rememberCoroutineScope().let { scope ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
        ) {
            AegisTopBar(
                title = "AJUSTES DE ENTRENAMIENTO",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                Spacer(modifier = Modifier.height(24.dp))

                // ─── SECCIÓN TEMPORIZADOR ───
                SectionHeader(text = "TEMPORIZADOR DE DESCANSO")
                Spacer(modifier = Modifier.height(16.dp))

                // Mostrar/ocultar temporizador
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        SettingsRow("MOSTRAR TEMPORIZADOR", showTimer) {
                            showTimer = it
                            scope.launch { onSave(timerSeconds, vibrate, sound, it) }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Selector de tiempo
                Text(
                    text = "TIEMPO DE DESCANSO",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Grid de opciones de tiempo
                val rows = timeOptions.chunked(3)
                rows.forEach { rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowOptions.forEach { seconds ->
                            val isSelected = timerSeconds == seconds
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .then(
                                        if (isSelected) Modifier.then(
                                            Modifier.background(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                RoundedCornerShape(8.dp)
                                            )
                                        ) else Modifier
                                    )
                                    .clickable {
                                        timerSeconds = seconds
                                        scope.launch { onSave(seconds, vibrate, sound, showTimer) }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = formatTimerOption(seconds),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onBackground,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    if (seconds >= 60) {
                                        Text(
                                            text = "${seconds}s",
                                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                            else MaterialTheme.colorScheme.secondary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        // Relleno si la fila no está completa
                        repeat(3 - rowOptions.size) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                VerticalDividerSection()

                // ─── SECCIÓN ALERTAS ───
                SectionHeader(text = "ALERTA AL FINALIZAR")
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        SettingsRow("VIBRACIÓN", vibrate) {
                            vibrate = it
                            scope.launch { onSave(timerSeconds, it, sound, showTimer) }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                        SettingsRow("SONIDO", sound) {
                            sound = it
                            scope.launch { onSave(timerSeconds, vibrate, it, showTimer) }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Resumen visual
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "CONFIGURACIÓN ACTUAL",
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                formatTimerOption(timerSeconds),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Column {
                            if (vibrate) Text("· Vibración activada", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            if (sound) Text("· Sonido activado", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            if (!vibrate && !sound) Text("· Solo visual", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

private fun formatTimerOption(seconds: Int): String {
    return when {
        seconds < 60 -> "${seconds}s"
        seconds % 60 == 0 -> "${seconds / 60}min"
        else -> "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"
    }
}
