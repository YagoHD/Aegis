package com.yago.aegis.ui.components

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.R
import com.yago.aegis.ui.theme.AegisBronze
import com.yago.aegis.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsMenu(viewModel: ProfileViewModel) {
    var newMeasureName by remember { mutableStateOf("") }
    val user = viewModel.user
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Estado local para la altura (evita que el cursor salte al guardar en DataStore)
    var tempHeight by remember(user.height) { mutableStateOf(user.height.toString()) }

    val context = LocalContext.current // Necesario para el permiso
    val avatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                // ✅ ESTO ES LO QUE HACE QUE LA FOTO NO SE BORRE AL CERRAR
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Guardamos en el ViewModel
            viewModel.updateAvatar(it.toString())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // --- SECCIÓN 1: DATOS PERSONALES ---
        Text(
            text = "DATOS DE USUARIO",
            color = AegisBronze,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledIconButton(
                onClick = { avatarLauncher.launch("image/*") },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF252525)),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
            }

            OutlinedTextField(
                value = user.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("Nombre", color = Color.Gray) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AegisBronze,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- CAMPO DE ALTURA ---
        OutlinedTextField(
            value = tempHeight,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) {
                    tempHeight = newValue
                    newValue.toIntOrNull()?.let { viewModel.updateHeight(it.toDouble()) }
                }
            },
            label = { Text(stringResource(R.string.label_height), color = Color.Gray) },
            placeholder = { Text("185", color = Color.DarkGray) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Straighten, contentDescription = null, tint = AegisBronze) },
            suffix = { Text(stringResource(R.string.unit_meters), color = AegisBronze, fontWeight = FontWeight.Bold) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AegisBronze,
                unfocusedBorderColor = Color.DarkGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(24.dp))

        // --- SECCIÓN 2: INTERFAZ ---
        Text(
            text = stringResource(R.string.settings_title_interface),
            color = AegisBronze,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        SettingsRow(stringResource(R.string.settings_label_body_fat), viewModel.showBodyFat) { viewModel.toggleBodyFat(it) }
        SettingsRow(stringResource(R.string.settings_label_bmi), viewModel.showBMI) { viewModel.toggleBMI(it) }
        SettingsRow(stringResource(R.string.settings_label_visual_log), viewModel.showVisualLog) { viewModel.toggleVisualLog(it) }
        SettingsRow(stringResource(R.string.settings_label_girths), viewModel.showGirths) { viewModel.toggleGirths(it) }

        Spacer(modifier = Modifier.height(24.dp))

        // --- SECCIÓN 3: GESTIÓN DE MEDIDAS ---
        Text(
            text = stringResource(R.string.settings_title_manage_measures),
            color = AegisBronze,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        viewModel.customMeasures.forEach { measure ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(measure.name, color = Color.White)
                IconButton(onClick = { viewModel.removeMeasure(measure.id) }) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.7f))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- SECCIÓN 4: AÑADIR NUEVA MEDIDA ---
        Text(
            text = stringResource(R.string.settings_title_add_metric),
            color = AegisBronze,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newMeasureName,
                onValueChange = { newMeasureName = it },
                label = { Text(stringResource(R.string.settings_hint_new_measure), color = Color.Gray) },
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            coroutineScope.launch {
                                delay(300)
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        }
                    },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AegisBronze,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            IconButton(onClick = {
                if (newMeasureName.isNotBlank()) {
                    viewModel.addMeasure(newMeasureName)
                    newMeasureName = ""
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = null, tint = AegisBronze)
            }
        }

        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.ime))
        Spacer(modifier = Modifier.height(50.dp))
    }
}

@Composable
fun SettingsRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AegisBronze,
                checkedTrackColor = AegisBronze.copy(alpha = 0.5f)
            )
        )
    }
}