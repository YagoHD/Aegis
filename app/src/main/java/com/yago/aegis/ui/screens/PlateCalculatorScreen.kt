package com.yago.aegis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import com.yago.aegis.ui.components.AegisTopBar
import com.yago.aegis.viewmodel.PlateCalculatorViewModel

private val ALL_PLATES = listOf(0.5, 1.25, 2.5, 5.0, 10.0, 15.0, 20.0, 25.0)
private val BAR_OPTIONS = listOf(0f, 10f, 15f, 20f)

@Composable
fun PlateCalculatorScreen(
    viewModel: PlateCalculatorViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val availablePlates by viewModel.availablePlates.collectAsState()
    val barWeight by viewModel.barWeight.collectAsState()

    Scaffold(
        topBar = {
            AegisTopBar(
                title = "CALCULADORA DE PLATOS",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // --- PESO OBJETIVO ---
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PlateLabel("PESO OBJETIVO")
                OutlinedTextField(
                    value = uiState.targetWeight,
                    onValueChange = { viewModel.setTargetWeight(it) },
                    placeholder = {
                        Text("Ej: 100", color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f), fontSize = 24.sp)
                    },
                    suffix = { Text("kg", color = MaterialTheme.colorScheme.secondary, fontSize = 20.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                    )
                )
            }

            // --- PESO DE LA BARRA ---
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PlateLabel("PESO DE LA BARRA")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BAR_OPTIONS.forEach { option ->
                        val isSelected = barWeight == option
                        val label = if (option == 0f) "Sin barra" else "${option.toInt()} kg"
                        Surface(
                            onClick = { viewModel.setBarWeight(option) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                            )
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }
            }

            // --- PLATOS DISPONIBLES ---
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PlateLabel("PLATOS DISPONIBLES")
                PlateGrid(
                    allPlates = ALL_PLATES,
                    selectedPlates = availablePlates,
                    onToggle = { viewModel.togglePlate(it) }
                )
            }

            // --- RESULTADO ---
            if (uiState.targetWeight.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PlateLabel("PLATOS POR LADO")
                    ResultCard(
                        platesPerSide = uiState.platesPerSide,
                        remainder = uiState.remainder,
                        barWeight = barWeight,
                        targetWeight = uiState.targetWeight.toDoubleOrNull() ?: 0.0
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun PlateGrid(
    allPlates: List<Double>,
    selectedPlates: List<Double>,
    onToggle: (Double) -> Unit
) {
    val rows = allPlates.chunked(4)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { plate ->
                    val isSelected = selectedPlates.contains(plate)
                    val label = if (plate % 1 == 0.0) "${plate.toInt()} kg" else "$plate kg"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onToggle(plate) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                        )
                    }
                }
                // Rellenar fila incompleta
                repeat(4 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ResultCard(
    platesPerSide: List<Double>,
    remainder: Double,
    barWeight: Float,
    targetWeight: Double
) {
    val totalActual = barWeight + platesPerSide.sum() * 2

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                if (remainder > 0.001) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (targetWeight < barWeight && targetWeight > 0) {
            Text(
                "El peso objetivo es menor que la barra (${barWeight.toInt()} kg).",
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
            return@Column
        }

        if (platesPerSide.isEmpty() && targetWeight >= barWeight) {
            Text(
                "Solo la barra: ${barWeight.toInt()} kg",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )
            return@Column
        }

        // Platos agrupados
        val grouped = platesPerSide.groupBy { it }.entries.sortedByDescending { it.key }
        grouped.forEach { (plate, list) ->
            val label = if (plate % 1 == 0.0) "${plate.toInt()} kg" else "$plate kg"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    label,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "× ${list.size}",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("TOTAL REAL", color = MaterialTheme.colorScheme.secondary, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Text(
                "${formatWeight(totalActual)} kg",
                color = if (remainder > 0.001) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
        }

        if (remainder > 0.001) {
            Text(
                "No se puede llegar exactamente a $targetWeight kg con los platos disponibles. Faltan ${formatWeight(remainder * 2)} kg (${formatWeight(remainder)} por lado).",
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun PlateLabel(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.secondary,
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 2.sp
    )
}

private fun formatWeight(w: Double): String =
    if (w % 1 == 0.0) w.toInt().toString() else "%.2f".format(w).trimEnd('0').trimEnd('.')
