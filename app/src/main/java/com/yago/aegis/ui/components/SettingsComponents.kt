package com.yago.aegis.ui.components

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.R
import com.yago.aegis.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsMenu(viewModel: ProfileViewModel) {
    // Usamos el uiState sellado del ViewModel refactorizado
    val state by viewModel.uiState.collectAsState()
    val user = state.user

    var newMeasureName by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var tempName by remember(user.name) { mutableStateOf(user.name) }
    var tempHeight by remember(user.height) { mutableStateOf(user.height.toString()) }

    val context = LocalContext.current
    val avatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) { e.printStackTrace() }
            viewModel.updateAvatar(it.toString())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(text = "DATOS DE USUARIO")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .clickable { avatarLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            AegisTextField(
                label = "NOMBRE",
                value = tempName,
                onValueChange = { newValue ->
                    tempName = newValue
                    viewModel.updateName(newValue)
                },
                placeholder = "Introduce tu nombre...",
                modifier = Modifier.weight(1f)
            )
        }

        AegisTextField(
            label = stringResource(R.string.label_height),
            value = tempHeight,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) {
                    tempHeight = newValue
                    newValue.toIntOrNull()?.let { viewModel.updateHeight(it.toDouble()) }
                }
            },
            placeholder = "185",
            modifier = Modifier.fillMaxWidth()
        )

        VerticalDividerSection()

        SectionHeader(text = stringResource(R.string.settings_title_interface))
        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                SettingsRow(stringResource(R.string.settings_label_body_fat), state.showBodyFat) { viewModel.toggleBodyFat(it) }
                SettingsRow(stringResource(R.string.settings_label_bmi), state.showBMI) { viewModel.toggleBMI(it) }
                SettingsRow(stringResource(R.string.settings_label_visual_log), state.showVisualLog) { viewModel.toggleVisualLog(it) }
                SettingsRow(stringResource(R.string.settings_label_girths), state.showGirths) { viewModel.toggleGirths(it) }
            }
        }

        VerticalDividerSection()

        SectionHeader(text = stringResource(R.string.settings_title_manage_measures))

        state.customMeasures.forEach { measure ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = measure.name.uppercase(),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { viewModel.removeMeasure(measure.id) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                AegisTextField(
                    label = "NUEVA MÉTRICA",
                    value = newMeasureName,
                    onValueChange = { newMeasureName = it },
                    placeholder = "Ej: Antebrazo",
                    modifier = Modifier.onFocusChanged {
                        if (it.isFocused) {
                            coroutineScope.launch {
                                delay(300)
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        }
                    }
                )
            }

            IconButton(
                onClick = {
                    if (newMeasureName.isNotBlank()) {
                        viewModel.addMeasure(newMeasureName)
                        newMeasureName = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp,
            fontSize = 11.sp
        )
    )
}

@Composable
fun VerticalDividerSection() {
    Spacer(modifier = Modifier.height(24.dp))
    HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
fun SettingsRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.uppercase(),
            color = if (checked) Color.White else MaterialTheme.colorScheme.secondary,
            style = TextStyle(
                fontWeight = if (checked) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp
            ),
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            )
        )
    }
}
