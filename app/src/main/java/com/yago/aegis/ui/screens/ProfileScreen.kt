package com.yago.aegis.ui.screens

import BiometricCard
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
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
import com.yago.aegis.data.PhotoType
import com.yago.aegis.ui.components.*
import com.yago.aegis.viewmodel.ProfileViewModel

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

        // ── BOTÓN GUARDAR SNAPSHOT + HISTORIAL ──────────────────────────────
        var snapshotSaved by remember { mutableStateOf(false) }

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
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (snapshotSaved) stringResource(R.string.saved_today_label) else stringResource(R.string.save_today_btn),
                    color = if (snapshotSaved) MaterialTheme.colorScheme.secondary
                            else MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }

        if (state.bodyHistory.isNotEmpty() || state.photoHistory.isNotEmpty()) {
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
