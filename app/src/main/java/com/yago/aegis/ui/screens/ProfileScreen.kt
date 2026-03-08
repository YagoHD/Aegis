package com.yago.aegis.ui.screens

import BiometricCard
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.R
import com.yago.aegis.data.PhotoType
import com.yago.aegis.ui.components.*
import com.yago.aegis.ui.theme.AegisWhite
import com.yago.aegis.ui.theme.BackgroundBlackGrey
import com.yago.aegis.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainProfileScreen(viewModel: ProfileViewModel, onNavigateToSettings: () -> Unit) {
    Scaffold(
        topBar = {
            AegisTopBar(
                title = stringResource(R.string.profile_title),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ajustes",
                            tint = AegisWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        },
        containerColor = BackgroundBlackGrey
    ) { paddingValues ->
        // Importante: paddingValues aquí contendrá el alto de la TopBar (48.dp)
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
    val context = LocalContext.current // Contexto dentro de la función correcta

    var showDialog by remember { mutableStateOf(false) }
    var photoTypeTarget by remember { mutableStateOf(PhotoType.BASE) }

    // ✅ ESTE ES EL LAUNCHER CORRECTO CON PERMISOS PERSISTENTES
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            try {
                // 2. Intentamos persistir el permiso (esencial para que sobreviva al reinicio)
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Si falla, al menos tenemos la Uri, pero el Photo Picker suele permitirlo
                e.printStackTrace()
            }

            // 3. Actualizamos el ViewModel
            viewModel.updatePhoto(uri = it.toString(), type = photoTypeTarget)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            // ✅ Añadimos esto para que el contenido "flote" sobre el teclado
            .imePadding()
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
                baseUri = user.basePhotoUri?.let { Uri.parse(it) },
                baseDate = user.basePhotoDate,
                actualUri = user.actualPhotoUri?.let { Uri.parse(it) },
                actualDate = user.actualPhotoDate,
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
                // ✅ CORRECCIÓN: Usamos el Request específico para PickVisualMedia
                launcher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )
    }
}