package com.yago.aegis.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.yago.aegis.R
import com.yago.aegis.ui.theme.AegisBronze
import com.yago.aegis.viewmodel.ProfileViewModel

@Composable
fun SettingsMenu(viewModel: ProfileViewModel) {
    var newMeasureName by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title_interface),
            color = AegisBronze,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        SettingsRow(stringResource(R.string.settings_label_body_fat), viewModel.showBodyFat) { viewModel.toggleBodyFat(it) }
        SettingsRow(stringResource(R.string.settings_label_bmi), viewModel.showBMI) { viewModel.toggleBMI(it) }
        SettingsRow(stringResource(R.string.settings_label_visual_log), viewModel.showVisualLog) { viewModel.toggleVisualLog(it) }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.settings_title_manage_measures),
            color = AegisBronze,
            fontWeight = FontWeight.Bold
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
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.settings_desc_delete),
                        tint = Color.Red.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.settings_title_add_metric),
            color = AegisBronze,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newMeasureName,
                onValueChange = { newMeasureName = it },
                label = { Text(stringResource(R.string.settings_hint_new_measure), color = Color.Gray) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )
            IconButton(onClick = {
                if (newMeasureName.isNotBlank()) {
                    viewModel.addMeasure(newMeasureName)
                    newMeasureName = ""
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.settings_desc_add),
                    tint = AegisBronze
                )
            }
        }
        Spacer(modifier = Modifier.height(250.dp))
    }
}

@Composable
fun SettingsRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
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