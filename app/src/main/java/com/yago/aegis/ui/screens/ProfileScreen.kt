package com.yago.aegis.ui.screens

import BiometricCard
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.R
import com.yago.aegis.data.PhotoType
import com.yago.aegis.ui.components.*
import com.yago.aegis.ui.theme.AegisWhite
import com.yago.aegis.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainProfileScreen(viewModel: ProfileViewModel, onNavigateToSettings: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(stringResource(R.string.profile_title), color = AegisWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Ajustes", tint = AegisWhite)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color(0xFF0A0A0A)
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            ProfileContent(viewModel)
        }
    }
}

@Composable
fun ProfileContent(viewModel: ProfileViewModel) {
    val user = viewModel.user
    val imc = viewModel.calcularBMI()
    val scrollState = rememberScrollState()
    var showDialog by remember { mutableStateOf(false) }
    var photoTypeTarget by remember { mutableStateOf(PhotoType.BASE) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updatePhoto(uri = it.toString(), type = photoTypeTarget) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        ProfileHeader(
            name = user.name,
            disciplineDay = user.disciplineDay,
            profilePhotoUri = user.profilePhotoUri
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- BIOMETRÍA ---
        Text(text = stringResource(R.string.label_biometrics), color = AegisWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                BiometricCard(stringResource(R.string.label_mass), user.currentMass, "KG") {
                    viewModel.updateMass(it)
                }
            }

            if (viewModel.showBodyFat) {
                Box(modifier = Modifier.weight(1f)) {
                    BiometricCard(stringResource(R.string.label_body_fat), user.bodyFat, "%") {
                        viewModel.updateBodyFat(it)
                    }
                }
            }

            if (viewModel.showBMI) {
                Box(modifier = Modifier.weight(1f)) {
                    BiometricCard(stringResource(R.string.label_bmi), "%.1f".format(imc), "")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- MEDIDAS DINÁMICAS ---
        if (viewModel.showGirths) {
            Text(text = stringResource(R.string.label_key_girths), color = AegisWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF161616))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                viewModel.customMeasures.forEach { measure ->
                    GirthRow(measure.name, measure.value) { newValue ->
                        viewModel.updateMeasureValue(measure.id, newValue)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- LOG VISUAL ---
        if (viewModel.showVisualLog) {
            VisualLogSection(
                user.basePhotoUri?.let { Uri.parse(it) },
                user.actualPhotoUri?.let { Uri.parse(it) },
                onAddClick = { showDialog = true }
            )
        }
    }

    if (showDialog) {
        PhotoSourceDialog(
            onDismiss = { showDialog = false },
            onConfirm = { type ->
                photoTypeTarget = type
                showDialog = false
                launcher.launch("image/*")
            }
        )
    }
}