package com.yago.aegis.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.data.ExerciseSummary
import com.yago.aegis.data.WorkoutSummary
import kotlin.math.roundToInt

private val motivationalQuotes = listOf(
    "\"La disciplina es el puente entre metas y logros.\"",
    "\"El dolor de hoy es la fuerza de mañana.\"",
    "\"No pares cuando estés cansado. Para cuando hayas terminado.\"",
    "\"Cada rep te acerca a quien quieres ser.\"",
    "\"El único mal entrenamiento es el que no hiciste.\"",
    "\"Forja tu disciplina. Construye tu legado.\"",
    "\"El cuerpo logra lo que la mente cree.\"",
    "\"Consistencia sobre intensidad. Siempre.\"",
    "\"Los campeones se hacen cuando nadie mira.\"",
    "\"Hoy superaste al de ayer. Eso es suficiente.\""
)

@Composable
fun WorkoutCompleteScreen(
    summary: WorkoutSummary,
    previousVolume: Double,
    onFinish: () -> Unit,
    onNavigateToHistory: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val quote = remember { motivationalQuotes.random() }

    // Animaciones de contador
    val animatedVolume = remember { Animatable(0f) }
    val animatedDuration = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animatedVolume.animateTo(
            targetValue = summary.totalVolume.toFloat(),
            animationSpec = tween(durationMillis = 1200)
        )
    }
    LaunchedEffect(Unit) {
        animatedDuration.animateTo(
            targetValue = (summary.durationMs / 1000f),
            animationSpec = tween(durationMillis = 1000)
        )
    }

    // Diferencia de volumen vs sesión anterior
    val volumeDiff = if (previousVolume > 0) {
        ((summary.totalVolume - previousVolume) / previousVolume * 100).roundToInt()
    } else null

    // Progreso de la barra comparativa (0f..1f)
    val barProgress = if (previousVolume > 0 && summary.totalVolume > 0) {
        (summary.totalVolume / (summary.totalVolume.coerceAtLeast(previousVolume))).toFloat().coerceIn(0f, 1f)
    } else 1f

    val animatedBar = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animatedBar.animateTo(barProgress, animationSpec = tween(1400))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // ─── CABECERA ───
            Text(
                text = "ENTRENAMIENTO\nCOMPLETADO",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp,
                lineHeight = 46.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = summary.routineName.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ─── TARJETA VOLUMEN TOTAL ───
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "VOLUMEN TOTAL",
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        // Badge de porcentaje
                        if (volumeDiff != null) {
                            Surface(
                                color = if (volumeDiff >= 0)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else Color(0xFFB3261E).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = if (volumeDiff >= 0) "↑ +$volumeDiff%" else "↓ $volumeDiff%",
                                    color = if (volumeDiff >= 0) MaterialTheme.colorScheme.primary
                                    else Color(0xFFCF6679),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = formatVolume(animatedVolume.value.toDouble()),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1).sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "kg",
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Barra comparativa
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                RoundedCornerShape(2.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedBar.value)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                        )
                    }

                    if (previousVolume > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Vs. ${formatVolume(previousVolume)} kg de la última sesión",
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ─── DURACIÓN Y EJERCICIOS ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Duración
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "DURACION",
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatDuration(animatedDuration.value.toLong()),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Ejercicios
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "EJERCICIOS",
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${summary.exerciseCount}",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ─── EJERCICIOS COMPLETADOS ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EJERCICIOS COMPLETADOS",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            summary.exercises.forEach { exercise ->
                ExerciseSummaryRow(exercise)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ─── FRASE MOTIVACIONAL ───
            Text(
                text = quote,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ─── ENLACE AL HISTORIAL ───
            TextButton(
                onClick = onNavigateToHistory,
                modifier = Modifier.fillMaxWidth()
            ) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = androidx.compose.ui.Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "VER HISTORIAL COMPLETO",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── BOTÓN FINISH SESSION ───
            Button(
                onClick = onFinish,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onBackground
                )
            ) {
                Text(
                    text = "FINALIZAR SESION",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "»",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ExerciseSummaryRow(exercise: ExerciseSummary) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono check
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name.uppercase(),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = buildExerciseSubtitle(exercise),
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Badge NEW PR
            if (exercise.isNewPR) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "NEW PR!",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

private fun buildExerciseSubtitle(exercise: ExerciseSummary): String {
    val setsText = "${exercise.sets} sets"
    return if (exercise.isBodyweight) {
        "$setsText · Bodyweight"
    } else if (exercise.maxWeight > 0) {
        val avgFormatted = if (exercise.avgWeight % 1 == 0.0) "${exercise.avgWeight.toInt()}" else "%.1f".format(exercise.avgWeight)
        val maxFormatted = if (exercise.maxWeight % 1 == 0.0) "${exercise.maxWeight.toInt()}" else "%.1f".format(exercise.maxWeight)
        if (exercise.avgWeight == exercise.maxWeight) "$setsText · ${maxFormatted}kg avg"
        else "$setsText · ${avgFormatted}kg avg · ${maxFormatted}kg max"
    } else {
        setsText
    }
}

private fun formatVolume(volume: Double): String {
    return when {
        volume >= 1000 -> "%,.0f".format(volume)
        else -> volume.toInt().toString()
    }
}

private fun formatDuration(seconds: Long): String {
    val totalSeconds = seconds.coerceAtLeast(0)
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return when {
        h > 0 -> "${h}h ${m}m"
        m > 0 -> "${m}m ${s}s"
        else -> "${s}s"
    }
}
