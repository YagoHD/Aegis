package com.yago.aegis

import BiometricCard
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.ui.components.GirthRow
import com.yago.aegis.ui.components.ProfileHeader
import com.yago.aegis.ui.components.VisualLogSection
import com.yago.aegis.ui.theme.AegisBronze
import com.yago.aegis.viewmodel.ProfileViewModel // Importamos tu lógica
import com.yago.aegis.ui.theme.AegisTheme
import com.yago.aegis.ui.theme.AegisWhite
import android.net.Uri
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults
import com.yago.aegis.ui.components.SettingsMenu
import com.yago.aegis.data.PhotoType
import com.yago.aegis.ui.components.PhotoSourceDialog

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: ProfileViewModel by viewModels()

        setContent {
            AegisTheme {
                // 1. ESTADOS PARA EL MENÚ DESLIZANTE
                val sheetState = androidx.compose.material3.rememberModalBottomSheetState()
                var showSettingsSheet by remember { mutableStateOf(false) }

                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text(stringResource(R.string.profile_title), color = AegisWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                            navigationIcon = {
                                IconButton(onClick = { /* Volver */ }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = AegisWhite)
                                }
                            },
                            actions = {
                                IconButton(onClick = { showSettingsSheet = true }) { // AHORA SÍ ABRE AJUSTES
                                    Icon(Icons.Default.Settings, contentDescription = null, tint = AegisWhite)
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
                        )
                    },
                    containerColor = Color(0xFF0A0A0A)
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        ProfileScreen(viewModel)
                    }

                    // 2. EL MENÚ DE AJUSTES QUE APARECE DESDE ABAJO
                    if (showSettingsSheet) {
                        androidx.compose.material3.ModalBottomSheet(
                            onDismissRequest = { showSettingsSheet = false },
                            sheetState = sheetState,
                            containerColor = Color(0xFF161616)
                        ) {
                            SettingsMenu(viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val user = viewModel.user
    val imc = viewModel.calcularBMI()
    val scrollState = rememberScrollState()
    var showDialog by remember { mutableStateOf(false) }
    var photoTypeTarget by remember { mutableStateOf(PhotoType.BASE) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.updatePhoto(
                uri = it.toString(),   // 🔥 convertimos aquí
                type = photoTypeTarget
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        ProfileHeader(name = user.name, disciplineDay = user.disciplineDay)

        Spacer(modifier = Modifier.height(32.dp))

        // --- 2. BIOMETRÍA (CON FILTROS) ---
        Text(text = stringResource(R.string.label_biometrics), color = AegisWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // MASA: Siempre visible
            Box(modifier = Modifier.weight(1f)) {
                BiometricCard(stringResource(R.string.label_mass), user.currentMass, "KG") {
                    viewModel.updateMass(it)
                }
            }

            // GRASA: Solo si showBodyFat es true
            if (viewModel.showBodyFat) {
                Box(modifier = Modifier.weight(1f)) {
                    BiometricCard(stringResource(R.string.label_body_fat), user.bodyFat, "%") {
                        viewModel.updateBodyFat(it)
                    }
                }
            }

            // IMC: Solo si showBMI es true
            if (viewModel.showBMI) {
                Box(modifier = Modifier.weight(1f)) {
                    BiometricCard(stringResource(R.string.label_bmi), "%.1f".format(imc), "")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 3. MEDIDAS DINÁMICAS ---
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

        // --- 4. LOG VISUAL ---
        if (viewModel.showVisualLog) {

            val baseUri = user.basePhotoUri?.let { Uri.parse(it) }
            val actualUri = user.actualPhotoUri?.let { Uri.parse(it) }

            VisualLogSection(
                baseUri,
                actualUri,
                onAddClick = { showDialog = true }
            )
        }
    }
    // ... dentro de ProfileScreen ...

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
