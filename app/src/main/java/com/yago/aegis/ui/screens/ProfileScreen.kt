package com.yago.aegis.ui.screens

import BiometricCard
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.R
import com.yago.aegis.data.LevelState
import com.yago.aegis.data.PhotoType
import com.yago.aegis.data.XpEntry
import com.yago.aegis.data.SyncState
import com.yago.aegis.ui.components.*
import com.yago.aegis.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToTrain: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            AegisTopBar(
                title = stringResource(R.string.profile_title),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.content_desc_settings),
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            ProfileContent(viewModel, onNavigateToTrain)
        }
    }
}

@Composable
fun ProfileContent(viewModel: ProfileViewModel, onNavigateToTrain: () -> Unit = {}) {
    // Un solo collectAsState para todo el estado (UiState sellado)
    val state by viewModel.uiState.collectAsState()
    val user = state.user
    val syncState by viewModel.syncState.collectAsState()
    val imc = viewModel.calcularBMI()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    var photoTypeTarget by remember { mutableStateOf(PhotoType.BASE) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            viewModel.updatePhoto(uri = it.toString(), type = photoTypeTarget)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .imePadding()
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        ProfileHeader(
            name = user.name,
            disciplineDay = user.disciplineDay,
            profilePhotoUri = user.profilePhotoUri,
            currentStreak = user.currentStreak
        )

        Spacer(modifier = Modifier.height(20.dp))
        LevelCard(level = state.level, breakdown = state.levelBreakdown)

        SyncIndicator(syncState = syncState, onRetry = { viewModel.retrySync() })

        if (user.disciplineDay == 0) {
            Spacer(modifier = Modifier.height(24.dp))
            NewUserBanner(onNavigateToTrain)
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = stringResource(R.string.label_biometrics).uppercase(),
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                BiometricCard(stringResource(R.string.label_mass), user.currentMass, "KG") {
                    viewModel.updateMass(it)
                }
            }
            if (state.showBodyFat) {
                Box(modifier = Modifier.weight(1f)) {
                    BiometricCard(stringResource(R.string.label_body_fat), user.bodyFat, "%") {
                        viewModel.updateBodyFat(it)
                    }
                }
            }
            if (state.showBMI) {
                Box(modifier = Modifier.weight(1f)) {
                    BiometricCard(stringResource(R.string.label_bmi), "%.1f".format(imc), "")
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        if (state.showGirths) {
            Text(
                text = stringResource(R.string.label_key_girths).uppercase(),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp)
            ) {
                state.customMeasures.forEach { measure ->
                    GirthRow(measure.name, measure.value) { newValue ->
                        viewModel.updateMeasureValue(measure.id, newValue)
                    }
                }
            }
        }

        // ── BOTÓN GUARDAR MEDIDAS + CHECK ✓ 3s ──────────────────────────────
        var snapshotSaved by remember { mutableStateOf(false) }
        // Cada vez que se guarda, el check aparece 3 s y luego vuelve a "Guardar".
        LaunchedEffect(snapshotSaved) {
            if (snapshotSaved) {
                delay(3000)
                snapshotSaved = false
            }
        }
        val savedGreen = Color(0xFF7FB069)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = {
                    viewModel.saveBodySnapshot()
                    snapshotSaved = true
                }
            ) {
                Icon(
                    imageVector = if (snapshotSaved) Icons.Default.CheckCircle else Icons.Default.Save,
                    contentDescription = null,
                    tint = if (snapshotSaved) savedGreen else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (snapshotSaved) stringResource(R.string.saved_today_label) else stringResource(R.string.btn_save),
                    color = if (snapshotSaved) savedGreen else MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }

        if (state.showEvolution && (state.bodyHistory.isNotEmpty() || state.photoHistory.isNotEmpty())) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.evolution_section_title),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            BodyHistorySection(
                bodyHistory = state.bodyHistory,
                photoHistory = state.photoHistory,
                customMeasures = state.customMeasures
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        if (state.showVisualLog) {
            VisualLogSection(
                baseUri = user.basePhotoUri?.let { Uri.parse(it) },
                baseDate = user.basePhotoDate,
                actualUri = user.actualPhotoUri?.let { Uri.parse(it) },
                actualDate = user.actualPhotoDate,
                onAddClick = { showDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(25.dp))
    }

    if (showDialog) {
        PhotoSourceDialog(
            onDismiss = { showDialog = false },
            onConfirm = { type ->
                photoTypeTarget = type
                showDialog = false
                launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        )
    }
}

@Composable
private fun NewUserBanner(onNavigateToTrain: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.welcome_aegis_title),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.welcome_first_workout_message),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
        Surface(
            onClick = onNavigateToTrain,
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.wrapContentSize()
        ) {
            Text(
                text = stringResource(R.string.btn_go),
                color = Color.Black,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}

/** Nivel y barra de XP del usuario (premia constancia, independiente del Panteón). */
@Composable
private fun LevelCard(level: LevelState, breakdown: List<XpEntry>) {
    var expanded by remember { mutableStateOf(false) }
    val canExpand = breakdown.isNotEmpty()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .then(if (canExpand) Modifier.clickable { expanded = !expanded } else Modifier)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.level_label, level.level),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.xp_format, level.xpIntoLevel, level.xpForLevel),
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                if (canExpand) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(level.progress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.level_breakdown_title),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            breakdown.take(20).forEach { entry -> XpRow(entry) }
        }
    }
}

@Composable
private fun XpRow(entry: XpEntry) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (entry.isStreak)
                    stringResource(R.string.level_breakdown_streak, entry.label.toIntOrNull() ?: 0)
                else entry.label,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            if (!entry.isStreak) {
                Text(
                    text = formatShortDate(entry.date),
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 10.sp
                )
            }
        }
        Text(
            text = stringResource(R.string.level_xp_gain, entry.points),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black
        )
    }
}

private fun formatShortDate(millis: Long): String =
    SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(millis))

/** Indicador discreto de sincronización (US-01): solo visible al sincronizar o en error. */
@Composable
private fun SyncIndicator(syncState: SyncState, onRetry: () -> Unit) {
    when (syncState) {
        is SyncState.Syncing -> {
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.sync_syncing),
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 11.sp
                )
            }
        }
        is SyncState.Error -> {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.sync_error),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = onRetry,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = stringResource(R.string.sync_retry),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
        is SyncState.Cached -> {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CloudQueue,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.sync_cached),
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = onRetry,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = stringResource(R.string.sync_retry),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
        else -> { /* Idle / Success: discreto, no se muestra nada */ }
    }
}
