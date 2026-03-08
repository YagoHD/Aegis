package com.yago.aegis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.ui.components.AegisStepProgress
import com.yago.aegis.ui.components.AegisTopBar
import com.yago.aegis.ui.components.MetricInput


@Composable
fun MetricsScreen(
    onComplete: (Double, String) -> Unit,
    onBack: () -> Unit
) {
    var height by remember { mutableStateOf("") }
    var mass by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // 60%: BackgroundBlack (Fondo profundo)
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        // --- 1. NAVEGACIÓN Y PROGRESO ---
        AegisTopBar(
            title = "PARÁMETROS",
            subtitle = "PASO 03",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        )

        AegisStepProgress(currentStep = 3)

        Spacer(modifier = Modifier.height(32.dp))

        // --- 2. CABECERA TÉCNICA ---
        Text(
            text = "MÉTRICAS CORPORALES",
            color = MaterialTheme.colorScheme.onBackground, // AegisWhite
            fontSize = 24.sp, // Ajustado para elegancia
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )

        Text(
            text = "Introduce tus datos de precisión para calibrar tu perfil de rendimiento.",
            color = MaterialTheme.colorScheme.secondary, // AegisSteel
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // --- 3. INPUTS DE PRECISIÓN ---
        // Usamos SurfaceDark (30%) dentro de MetricInput para las tarjetas
        MetricInput(
            label = "ALTURA CONFIGURACIÓN",
            unit = "CM",
            value = height,
            onValueChange = { height = it },
            icon = Icons.Default.Straighten
        )

        Spacer(modifier = Modifier.height(32.dp))

        MetricInput(
            label = "MASA ACTUAL",
            unit = "KG",
            value = mass,
            onValueChange = { mass = it },
            icon = Icons.Default.MonitorWeight
        )

        // El Spacer con weight empuja el botón al final sin necesidad de scroll
        Spacer(modifier = Modifier.weight(1f))

        // --- 4. BOTÓN DE FINALIZACIÓN (10% Bronce) ---
        val isEnabled = height.isNotEmpty() && mass.isNotEmpty()

        Button(
            onClick = {
                val h = height.toDoubleOrNull() ?: 0.0
                if (h > 0.0 && mass.isNotEmpty()) onComplete(h, mass)
            },
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary, // AegisBronze
                contentColor = Color.Black,
                disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(8.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text(
                text = "FINALIZAR CONFIGURACIÓN",
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}