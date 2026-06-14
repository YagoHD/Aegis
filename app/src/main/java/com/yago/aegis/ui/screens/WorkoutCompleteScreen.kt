package com.yago.aegis.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.yago.aegis.R
import com.yago.aegis.data.ExerciseSummary
import com.yago.aegis.data.WorkoutSummary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt


@Composable
fun WorkoutCompleteScreen(
    summary: WorkoutSummary,
    previousVolume: Double,
    onFinish: (notes: String) -> Unit,
    onNavigateToHistory: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val motivationalQuotes = listOf(
        stringResource(R.string.quote_1),
        stringResource(R.string.quote_2),
        stringResource(R.string.quote_3),
        stringResource(R.string.quote_4),
        stringResource(R.string.quote_5),
        stringResource(R.string.quote_6),
        stringResource(R.string.quote_7),
        stringResource(R.string.quote_8),
        stringResource(R.string.quote_9),
        stringResource(R.string.quote_10)
    )
    val quote = remember { motivationalQuotes.random() }
    var sessionNotes by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // ─── PR detection ───
    val hasNewPR = remember(summary) { summary.exercises.any { it.isNewPR } }
    val prExercises = remember(summary) { summary.exercises.filter { it.isNewPR } }
    var prBannerVisible by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "pr_pulse")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "border_alpha"
    )
    LaunchedEffect(hasNewPR) {
        if (hasNewPR) {
            delay(700)
            prBannerVisible = true
        }
    }

    // ─── Share card capture ───
    val graphicsLayer = rememberGraphicsLayer()

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
                text = stringResource(R.string.workout_complete_title),
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

            // ─── PR CELEBRATION BANNER ───
            AnimatedVisibility(
                visible = prBannerVisible,
                enter = slideInVertically(tween(400)) { -it } + fadeIn(tween(400))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(20.dp))
                    PRCelebrationBanner(prExercises = prExercises, borderAlpha = borderAlpha)
                }
            }

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
                            text = stringResource(R.string.total_volume_label),
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
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
                            text = stringResource(R.string.label_kg_lower),
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

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
                            text = stringResource(R.string.volume_comparison_label, formatVolume(previousVolume)),
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
                            text = stringResource(R.string.duration_label),
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
                            text = stringResource(R.string.exercises_label),
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
                    text = stringResource(R.string.completed_exercises_section),
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

            // ─── SHARE CARD ───
            Text(
                text = stringResource(R.string.share_section_label),
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // La card se captura con graphicsLayer al dibujarse
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(graphicsLayer)
                    }
            ) {
                WorkoutShareCard(summary = summary, previousVolume = previousVolume)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                        shareWorkoutBitmap(context, bitmap)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.Black
                )
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.btn_share_workout),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ─── NOTAS DE SESIÓN ───
            OutlinedTextField(
                value = sessionNotes,
                onValueChange = { sessionNotes = it },
                placeholder = {
                    Text(
                        stringResource(R.string.session_notes_placeholder),
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 80.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                ),
                textStyle = TextStyle(fontSize = 13.sp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(20.dp))

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
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.view_full_history),
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── BOTÓN FINISH SESSION ───
            Button(
                onClick = { onFinish(sessionNotes) },
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
                    text = stringResource(R.string.btn_finish_session),
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

// ─────────────────────────────────────────────
// PR CELEBRATION BANNER
// ─────────────────────────────────────────────

@Composable
private fun PRCelebrationBanner(prExercises: List<ExerciseSummary>, borderAlpha: Float) {
    val gold = MaterialTheme.colorScheme.primary
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, gold.copy(alpha = borderAlpha), RoundedCornerShape(12.dp)),
        color = gold.copy(alpha = 0.07f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = gold,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = stringResource(R.string.new_pr_title),
                    color = gold,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                val multiplePrLabel = stringResource(R.string.multiple_pr_label)
                val subtitle = when {
                    prExercises.isEmpty() -> ""
                    prExercises.size == 1 -> prExercises.first().name.uppercase()
                    else -> "${prExercises.size} $multiplePrLabel"
                }
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        color = gold.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// WORKOUT SHARE CARD (se captura como imagen)
// ─────────────────────────────────────────────

private val ShareCardBg = Color(0xFF080808)
private val ShareCardSurface = Color(0xFF161616)
private val ShareBronze = Color(0xFFB39371)
private val ShareAccent = Color(0xFFC4A882)  // bronce claro para énfasis

@Composable
private fun WorkoutShareCard(summary: WorkoutSummary, previousVolume: Double = 0.0) {
    val dateStr = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    }
    val durationStr = remember(summary.durationMs) {
        val totalMin = summary.durationMs / 60000
        if (totalMin >= 60) "${totalMin / 60}h ${totalMin % 60}m" else "${totalMin}m"
    }
    val totalSets = remember(summary) { summary.exercises.sumOf { it.sets } }
    val volumeDiff = remember(summary, previousVolume) {
        if (previousVolume > 0 && summary.totalVolume > 0)
            ((summary.totalVolume - previousVolume) / previousVolume * 100).toInt()
        else null
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ShareCardBg, RoundedCornerShape(16.dp))
            .border(1.dp, ShareBronze.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(22.dp)
    ) {
        // Header: AEGIS logo + fecha
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AEGIS",
                color = ShareAccent,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                letterSpacing = 4.sp
            )
            Text(
                text = dateStr.uppercase(),
                color = ShareBronze.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = ShareBronze.copy(alpha = 0.18f))
        Spacer(modifier = Modifier.height(16.dp))

        // Nombre de la rutina + badge % mejora
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = summary.routineName.uppercase(),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                lineHeight = 28.sp,
                modifier = Modifier.weight(1f)
            )
            if (volumeDiff != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = if (volumeDiff >= 0) ShareBronze.copy(alpha = 0.15f)
                            else Color(0xFF8B3A3A).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(
                        0.5.dp,
                        if (volumeDiff >= 0) ShareBronze.copy(alpha = 0.5f)
                        else Color(0xFF9B4A4A).copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = if (volumeDiff >= 0) "↑ +$volumeDiff%" else "↓ $volumeDiff%",
                        color = if (volumeDiff >= 0) ShareAccent else Color(0xFFBF7070),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = stringResource(R.string.share_card_subtitle),
            color = ShareBronze.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Stats chips (ahora 4: volumen, duración, ejercicios, sets)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShareStatChip(
                value = formatVolumeShare(summary.totalVolume),
                unit = "kg",
                label = "VOLUMEN",
                modifier = Modifier.weight(1f)
            )
            ShareStatChip(
                value = durationStr,
                unit = "",
                label = "TIEMPO",
                modifier = Modifier.weight(1f)
            )
            ShareStatChip(
                value = "${summary.exerciseCount}",
                unit = "",
                label = "EJERC.",
                modifier = Modifier.weight(1f)
            )
            ShareStatChip(
                value = "$totalSets",
                unit = "",
                label = "SETS",
                modifier = Modifier.weight(1f)
            )
        }

        if (summary.exercises.isNotEmpty()) {
            Spacer(modifier = Modifier.height(18.dp))

            // Lista de ejercicios (máx 5)
            summary.exercises.take(5).forEach { exercise ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(ShareBronze, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = exercise.name.uppercase(),
                        color = Color.White.copy(alpha = 0.88f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.2.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (exercise.isNewPR) {
                        Surface(
                            color = ShareBronze.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(0.5.dp, ShareAccent.copy(alpha = 0.7f))
                        ) {
                            Text(
                                text = "PR",
                                color = ShareAccent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    } else {
                        val detail = when {
                            exercise.isBodyweight -> "${exercise.sets} sets · BW"
                            exercise.maxWeight > 0 -> "${exercise.sets} × ${formatWeightShare(exercise.maxWeight)}kg"
                            else -> "${exercise.sets} sets"
                        }
                        Text(
                            text = detail,
                            color = ShareBronze.copy(alpha = 0.55f),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            if (summary.exercises.size > 5) {
                Text(
                    text = "+${summary.exercises.size - 5} más",
                    color = ShareBronze.copy(alpha = 0.45f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 15.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        HorizontalDivider(color = ShareBronze.copy(alpha = 0.13f))
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.trained_with_label),
                color = ShareBronze.copy(alpha = 0.45f),
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            )
            if (summary.exercises.any { it.isNewPR }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = ShareAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.new_pr_badge),
                        color = ShareAccent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ShareStatChip(value: String, unit: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(ShareCardSurface, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    color = ShareBronze.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                )
            }
        }
        Text(
            text = label,
            color = ShareBronze.copy(alpha = 0.55f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}

// ─────────────────────────────────────────────
// EXERCISE SUMMARY ROW (sin cambios)
// ─────────────────────────────────────────────

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

            if (exercise.isNewPR) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
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

// ─────────────────────────────────────────────
// SHARE HELPER
// ─────────────────────────────────────────────

private fun shareWorkoutBitmap(context: Context, bitmap: Bitmap) {
    try {
        val file = File(context.cacheDir, "aegis_share.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_intent_text))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir entrenamiento"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// ─────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────

@Composable
private fun buildExerciseSubtitle(exercise: ExerciseSummary): String {
    val setsText = "${exercise.sets} sets"
    return if (exercise.isBodyweight) {
        "$setsText · ${stringResource(R.string.bodyweight_label)}"
    } else if (exercise.maxWeight > 0) {
        val avgFormatted = if (exercise.avgWeight % 1 == 0.0) "${exercise.avgWeight.toInt()}" else "%.1f".format(exercise.avgWeight)
        val maxFormatted = if (exercise.maxWeight % 1 == 0.0) "${exercise.maxWeight.toInt()}" else "%.1f".format(exercise.maxWeight)
        if (exercise.avgWeight == exercise.maxWeight) "$setsText · ${maxFormatted}kg avg"
        else "$setsText · ${avgFormatted}kg avg · ${maxFormatted}kg max"
    } else {
        setsText
    }
}

private fun formatVolume(volume: Double): String = when {
    volume >= 1000 -> "%,.0f".format(volume)
    else -> volume.toInt().toString()
}

private fun formatVolumeShare(volume: Double): String = when {
    volume >= 1000 -> "%,.0f".format(volume)
    else -> volume.toInt().toString()
}

private fun formatWeightShare(weight: Double): String =
    if (weight % 1 == 0.0) weight.toInt().toString() else "%.1f".format(weight)

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
