package com.yago.aegis.ui.screens

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.yago.aegis.MainActivity
import com.yago.aegis.R
import com.yago.aegis.ui.components.AegisAlertDialog
import com.yago.aegis.ui.components.AegisTopBar
import com.yago.aegis.ui.components.ExerciseSessionCard
import com.yago.aegis.ui.components.SessionExercisePickerSheet
import com.yago.aegis.ui.components.SessionProgressHeader
import com.yago.aegis.viewmodel.ProfileViewModel
import com.yago.aegis.viewmodel.RoutinesViewModel
import com.yago.aegis.viewmodel.WorkoutViewModel
import kotlin.math.roundToInt

private const val WORKOUT_NOTIFICATION_ID = 1001
private const val WORKOUT_CHANNEL_ID = "aegis_workout_session"

@Composable
fun ActiveSessionScreen(
    workoutViewModel: WorkoutViewModel,
    routinesViewModel: RoutinesViewModel,
    onFinishWorkout: () -> Unit,
    onBack: () -> Unit = {},
    profileViewModel: ProfileViewModel,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToPlateCalculator: () -> Unit = {}
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
    var showExercisePicker by remember { mutableStateOf(false) }
    val libraryExercises by routinesViewModel.allExercises.collectAsState()
    val context = LocalContext.current
    val density = LocalDensity.current
    val view = LocalView.current

    val currentSession = session ?: return

    // El gesto de "atrás" abre el mismo diálogo que la flecha: así nunca se sale de
    // la sesión sin pausarla/cancelarla, y no se pierden datos por accidente.
    BackHandler(enabled = true) { showCancelDialog = true }

    // ── PANTALLA SIEMPRE ACTIVA ──────────────────────────────────────────────
    DisposableEffect(Unit) {
        val window = (view.context as? android.app.Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // ── NOTIFICACIÓN DE SESIÓN ACTIVA ────────────────────────────────────────
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) showWorkoutNotification(context, currentSession.routineName)
    }

    DisposableEffect(currentSession.routineName) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            showWorkoutNotification(context, currentSession.routineName)
        } else {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        onDispose {
            cancelWorkoutNotification(context)
        }
    }

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
        // Diálogo personalizado con 3 opciones: Continuar / Pausar / Cancelar
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    stringResource(R.string.exit_session_dialog_title),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                    letterSpacing = 1.sp
                )
            },
            text = {
                Text(
                    stringResource(R.string.exit_session_question),
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Opción 1: Continuar entrenando
                    Button(
                        onClick = { showCancelDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            stringResource(R.string.btn_continue_training),
                            color = androidx.compose.ui.graphics.Color.Black,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                    // Opción 2: Pausar (salir sin perder datos)
                    OutlinedButton(
                        onClick = {
                            showCancelDialog = false
                            workoutViewModel.pauseWorkout()
                            onBack()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                        )
                    ) {
                        Text(
                            stringResource(R.string.btn_pause_session),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                    // Opción 3: Abandonar (perder datos)
                    TextButton(
                        onClick = {
                            showCancelDialog = false
                            workoutViewModel.cancelWorkout { onFinishWorkout() }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            stringResource(R.string.btn_abandon_session),
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            dismissButton = {}
        )
    }

    if (uncompletedWithData.isNotEmpty()) {
        AegisAlertDialog(
            title = stringResource(R.string.uncompleted_exercises_title),
            confirmText = stringResource(R.string.mark_complete_btn),
            dismissText = stringResource(R.string.review_btn),
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
                    text = stringResource(R.string.uncompleted_exercises_message),
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

    // Hoja para añadir ejercicios de la librería a la sesión (entrenamiento libre)
    if (showExercisePicker) {
        SessionExercisePickerSheet(
            exercises = libraryExercises,
            alreadyAddedIds = currentSession.exercisesProgress.map { it.exercise.id }.toSet(),
            onPick = { ex ->
                workoutViewModel.addExerciseToSession(ex)
                showExercisePicker = false
            },
            onCreateExercise = { name ->
                // Crea el ejercicio en la librería (queda guardado) y lo añade a la sesión
                val newEx = com.yago.aegis.data.Exercise(name = name.uppercase(), type = "CUSTOM", muscleGroup = "")
                routinesViewModel.saveOrUpdateExercise(newEx)
                workoutViewModel.addExerciseToSession(newEx)
                showExercisePicker = false
            },
            onDismiss = { showExercisePicker = false }
        )
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
        // Banner de sesión pausada
    val isPaused by workoutViewModel.isPaused.collectAsState()

    Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                AegisTopBar(
                    title = currentSession.routineName.uppercase(),
                    subtitle = stringResource(R.string.active_session_subtitle),
                    navigationIcon = {
                        IconButton(onClick = { showCancelDialog = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.content_desc_back),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToPlateCalculator) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = stringResource(R.string.calc_desc),
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.settings_workout_desc),
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
                        .padding(horizontal = 20.dp)
                        .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 120.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    itemsIndexed(
                        items = currentSession.exercisesProgress,
                        // Key compuesta con índice para garantizar unicidad aunque
                        // el usuario tenga el mismo ejercicio en dos slots por error
                        key = { index, progress -> "${index}_${progress.exercise.id}" }
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
                                },
                                onSwitchVariant = if (progress.slotVariants.size > 1) { variantIndex ->
                                    workoutViewModel.switchVariant(index, variantIndex)
                                } else null
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

                    // Estado vacío (entrenamiento libre sin ejercicios aún)
                    if (currentSession.exercisesProgress.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.session_empty_hint),
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp)
                            )
                        }
                    }

                    // Botón añadir ejercicio: imprescindible en el libre, útil en cualquier sesión
                    item {
                        Surface(
                            onClick = { showExercisePicker = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .height(52.dp),
                            color = Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.btn_add_exercise),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    item {
                        // Solo activo si hay al menos una serie con datos
                        val hasAnyData = currentSession.exercisesProgress.any { prog ->
                            prog.sets.any { it.weight > 0 || it.reps > 0 }
                        }

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
                            enabled = hasAnyData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 100.dp)
                                .height(60.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasAnyData) MaterialTheme.colorScheme.primary
                                                 else MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                contentColor = if (hasAnyData) Color.Black
                                               else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                disabledContentColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = if (hasAnyData) 4.dp else 0.dp)
                        ) {
                            Text(
                                text = if (hasAnyData) stringResource(R.string.btn_finish_workout) else stringResource(R.string.btn_enter_data_to_finish),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = if (hasAnyData) 15.sp else 11.sp,
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
                        text = stringResource(R.string.rest_timer_label),
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
                        contentDescription = stringResource(R.string.start_rest_desc),
                        tint = if (isDragging) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = if (isDragging) stringResource(R.string.move_timer_label) else stringResource(R.string.rest_timer_label),
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

private fun showWorkoutNotification(context: Context, routineName: String) {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Crear canal si no existe (idempotente)
    if (notificationManager.getNotificationChannel(WORKOUT_CHANNEL_ID) == null) {
        val channel = NotificationChannel(
            WORKOUT_CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW   // Sin sonido — solo indicador persistente
        ).apply {
            description = context.getString(R.string.notification_channel_desc)
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    // PendingIntent: al pulsar la notificación, vuelve a la app
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, WORKOUT_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(context.getString(R.string.notification_title, routineName.uppercase()))
        .setContentText(context.getString(R.string.notification_text))
        .setOngoing(true)               // No se puede descartar deslizando
        .setSilent(true)                // Sin sonido ni vibración al mostrar
        .setContentIntent(pendingIntent)
        .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        .build()

    notificationManager.notify(WORKOUT_NOTIFICATION_ID, notification)
}

private fun cancelWorkoutNotification(context: Context) {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(WORKOUT_NOTIFICATION_ID)
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
