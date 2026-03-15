package com.yago.aegis.ui.screens

import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.ui.components.AegisAlertDialog
import com.yago.aegis.ui.components.AegisTopBar
import com.yago.aegis.ui.components.ExerciseSessionCard
import com.yago.aegis.ui.components.SessionProgressHeader
import com.yago.aegis.viewmodel.ProfileViewModel
import com.yago.aegis.viewmodel.RoutinesViewModel
import com.yago.aegis.viewmodel.WorkoutViewModel
import kotlin.math.roundToInt

@Composable
fun ActiveSessionScreen(
    workoutViewModel: WorkoutViewModel,
    routinesViewModel: RoutinesViewModel,
    onFinishWorkout: () -> Unit,
    profileViewModel: ProfileViewModel,
    onNavigateToSettings: () -> Unit = {}
) {
    val session by workoutViewModel.activeSession.collectAsState()
    val uncompletedWithData by workoutViewModel.uncompletedWithData.collectAsState()
    val timerSeconds by workoutViewModel.timerSeconds.collectAsState()
    val timerRunning by workoutViewModel.timerRunning.collectAsState()
    val timerFinished by workoutViewModel.timerFinished.collectAsState()
    val restTimerSeconds by workoutViewModel.restTimerSeconds.collectAsState()
    val showRestTimer by workoutViewModel.showRestTimer.collectAsState()
    val timerVibrate by workoutViewModel.timerVibrate.collectAsState()
    val timerSound by workoutViewModel.timerSound.collectAsState()
    val savedPosX by workoutViewModel.timerPosX.collectAsState()
    val savedPosY by workoutViewModel.timerPosY.collectAsState()

    var showCancelDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val density = LocalDensity.current

    val currentSession = session ?: return

    // Tamaño del contenedor (para calcular límites del drag)
    var containerWidthPx by remember { mutableStateOf(0) }
    var containerHeightPx by remember { mutableStateOf(0) }
    val fabSizePx = with(density) { 80.dp.toPx() }

    // Posición del FAB en píxeles — se inicializa con la posición guardada o la por defecto
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var positionInitialized by remember { mutableStateOf(false) }

    // Inicializar posición cuando tengamos el tamaño del contenedor y la posición guardada
    LaunchedEffect(savedPosX, savedPosY, containerWidthPx, containerHeightPx) {
        if (containerWidthPx > 0 && containerHeightPx > 0 && !positionInitialized) {
            if (savedPosX == -1f || savedPosY == -1f) {
                // Posición por defecto: esquina inferior derecha
                offsetX = containerWidthPx - fabSizePx - with(density) { 20.dp.toPx() }
                offsetY = containerHeightPx - fabSizePx - with(density) { 24.dp.toPx() }
            } else {
                // Posición guardada — clampear por si cambió el tamaño de pantalla
                offsetX = savedPosX.coerceIn(0f, (containerWidthPx - fabSizePx).coerceAtLeast(0f))
                offsetY = savedPosY.coerceIn(0f, (containerHeightPx - fabSizePx).coerceAtLeast(0f))
            }
            positionInitialized = true
        }
    }

    // Vibrar y/o sonar al terminar el timer
    LaunchedEffect(timerFinished) {
        if (timerFinished) {
            if (timerVibrate) vibrate(context)
            if (timerSound) playSound(context)
            workoutViewModel.onTimerFinishedHandled()
        }
    }

    val totalSeconds = restTimerSeconds.toFloat().coerceAtLeast(1f)
    val timerProgress = if (timerRunning || timerSeconds > 0) timerSeconds / totalSeconds else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = timerProgress,
        animationSpec = tween(durationMillis = 800),
        label = "timerArc"
    )

    if (showCancelDialog) {
        AegisAlertDialog(
            title = "ABANDONAR SESIÓN",
            confirmText = "CANCELAR ENTRENAMIENTO",
            dismissText = "CONTINUAR",
            onDismiss = { showCancelDialog = false },
            onConfirm = {
                showCancelDialog = false
                workoutViewModel.cancelWorkout { onFinishWorkout() }
            }
        ) {
            Text(
                text = "Se perderá todo el progreso de esta sesión. Esta acción no se puede deshacer.",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 14.sp
            )
        }
    }

    if (uncompletedWithData.isNotEmpty()) {
        AegisAlertDialog(
            title = "EJERCICIOS SIN MARCAR",
            confirmText = "MARCAR Y FINALIZAR",
            dismissText = "REVISAR",
            onDismiss = { workoutViewModel.dismissUncompletedDialog() },
            onConfirm = {
                workoutViewModel.forceFinishWorkout(routinesViewModel) {
                    profileViewModel.incrementDisciplineDay()
                    onFinishWorkout()
                }
            }
        ) {
            Column {
                Text(
                    text = "Has introducido datos en los siguientes ejercicios pero no los has marcado como completados:",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                uncompletedWithData.forEach { progress ->
                    Text(
                        text = "• ${progress.exercise.name.uppercase()}",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }

    // Box raíz para medir el tamaño disponible y posicionar el FAB con offset absoluto
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                containerWidthPx = coords.size.width
                containerHeightPx = coords.size.height
            }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                AegisTopBar(
                    title = currentSession.routineName.uppercase(),
                    subtitle = "SESIÓN EN CURSO",
                    navigationIcon = {
                        IconButton(onClick = { showCancelDialog = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Ajustes entrenamiento",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    SessionProgressHeader(currentSession)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    itemsIndexed(
                        items = currentSession.exercisesProgress,
                        key = { _, progress -> progress.exercise.id }
                    ) { index, progress ->
                        Column {
                            ExerciseSessionCard(
                                progress = progress,
                                onAddSet = { workoutViewModel.addSet(progress.exercise.id) },
                                onUpdateSet = { setId, w, r, c ->
                                    workoutViewModel.updateSet(progress.exercise.id, setId, w, r, c)
                                },
                                onDeleteSet = { setId ->
                                    workoutViewModel.removeSet(progress.exercise.id, setId)
                                },
                                onToggleExercise = {
                                    workoutViewModel.toggleExerciseCompleted(progress.exercise.id)
                                }
                            )
                            if (index < currentSession.exercisesProgress.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(top = 16.dp, start = 8.dp, end = 8.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                workoutViewModel.requestFinishWorkout()
                                if (workoutViewModel.uncompletedWithData.value.isEmpty()) {
                                    workoutViewModel.finishWorkout(routinesViewModel) {
                                        profileViewModel.incrementDisciplineDay()
                                        onFinishWorkout()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 100.dp)
                                .height(60.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Text(
                                text = "FINALIZAR ENTRENAMIENTO",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // ─── TEMPORIZADOR FLOTANTE CON DRAG ───
        if (showRestTimer && positionInitialized) {
            val maxX = (containerWidthPx - fabSizePx).coerceAtLeast(0f)
            val maxY = (containerHeightPx - fabSizePx).coerceAtLeast(0f)

            // Estado local durante el arrastre (sin guardar en cada frame)
            var isDragging by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .size(80.dp)
                    // Pulsación larga inicia el drag; tap simple inicia/para el timer
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { isDragging = true },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offsetX = (offsetX + dragAmount.x).coerceIn(0f, maxX)
                                offsetY = (offsetY + dragAmount.y).coerceIn(0f, maxY)
                            },
                            onDragEnd = {
                                isDragging = false
                                // Guardar posición al soltar
                                workoutViewModel.saveTimerPosition(offsetX, offsetY)
                            },
                            onDragCancel = {
                                isDragging = false
                                workoutViewModel.saveTimerPosition(offsetX, offsetY)
                            }
                        )
                    }
            ) {
                RestTimerFab(
                    seconds = timerSeconds,
                    totalSeconds = restTimerSeconds,
                    isRunning = timerRunning,
                    progress = animatedProgress,
                    isDragging = isDragging,
                    onTap = {
                        if (!isDragging) {
                            if (timerRunning) workoutViewModel.stopTimer()
                            else workoutViewModel.startTimer()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun RestTimerFab(
    seconds: Int,
    totalSeconds: Int,
    isRunning: Boolean,
    progress: Float,
    isDragging: Boolean,
    onTap: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val dangerColor = Color(0xFFE57373)

    val isLow = seconds <= 10 && isRunning
    val arcColor = if (isLow) dangerColor else primaryColor

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(80.dp)
    ) {
        // Fondo — se ilumina cuando está siendo arrastrado
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    if (isDragging) surfaceColor.copy(alpha = 0.95f)
                    else surfaceColor
                )
        )

        // Arco de progreso
        androidx.compose.foundation.Canvas(modifier = Modifier.size(80.dp)) {
            drawArc(
                color = secondaryColor.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
            if (progress > 0f) {
                drawArc(
                    color = arcColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }
            // Indicador de "arrastrable" cuando está en reposo
            if (isDragging) {
                drawCircle(
                    color = arcColor.copy(alpha = 0.15f),
                    radius = size.minDimension / 2f - 2.dp.toPx()
                )
            }
        }

        // Contenido interior
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .then(
                    if (!isDragging) Modifier.clickable { onTap() }
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isRunning || seconds > 0) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatTimerDisplay(seconds),
                        color = if (isLow) dangerColor else MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "REST",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Iniciar descanso",
                        tint = if (isDragging) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = if (isDragging) "MOVER" else "REST",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

private fun formatTimerDisplay(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return if (m > 0) "$m:${s.toString().padStart(2, '0')}" else "${s}s"
}

private fun vibrate(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator.vibrate(
                VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(500)
            }
        }
    } catch (e: Exception) { }
}

private fun playSound(context: Context) {
    try {
        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = RingtoneManager.getRingtone(context, notification)
        ringtone.play()
    } catch (e: Exception) { }
}
