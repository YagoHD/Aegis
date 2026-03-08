package com.yago.aegis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.ui.components.AegisStepProgress
import com.yago.aegis.ui.components.AegisTopBar
import com.yago.aegis.ui.components.MetricInput
import com.yago.aegis.ui.theme.AegisBronze
import com.yago.aegis.ui.theme.AegisSteel
import com.yago.aegis.ui.theme.AegisWhite
import com.yago.aegis.ui.theme.BackgroundBlackGrey

@Composable
fun MetricsScreen(
    onComplete: (Double, String) -> Unit,
    onBack: () -> Unit
) {
    // Estados para los inputs
    var height by remember { mutableStateOf("") }
    var mass by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlackGrey)
            .padding(24.dp)
    ) {
        // 1. Barra superior de navegación (Paso 03)
        AegisTopBar(
            title = "PARÁMETROS",
            subtitle = "PASO 03",
            navigationIcon = { // ✅ Ahora el usuario puede volver a IdentityScreen
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = AegisWhite)
                }
            }
        )
        AegisStepProgress(currentStep = 3)

        Spacer(modifier = Modifier.height(40.dp))

        // 2. Cabecera de la pantalla
        Text(
            text = "MÉTRICAS CORPORALES",
            color = AegisWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Introduce tus datos para calcular tu IMC",
            color = AegisSteel,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(64.dp))

        // 3. Inputs métricos usando tu componente MetricInput
        MetricInput(
            label = "ALTURA",
            unit = "CENTIMETROS",
            value = height,
            onValueChange = { height = it },
            icon = Icons.Default.Straighten // Icono de regla
        )

        Spacer(modifier = Modifier.height(48.dp))

        MetricInput(
            label = "PESO",
            unit = "KILOGRAMOS",
            value = mass,
            onValueChange = { mass = it },
            icon = Icons.Default.MonitorWeight // Icono de báscula
        )

        Spacer(modifier = Modifier.weight(1f))

        // 4. Botón de finalización
        // Solo se habilita si hay datos (puedes añadir validación extra si quieres)
        val isEnabled = height.isNotEmpty() && mass.isNotEmpty()

        Button(
            onClick = {
                val h = height.toDoubleOrNull() ?: 0.0
                if (h > 0.0 && mass.isNotEmpty()) onComplete(h, mass)
            },
            enabled = isEnabled,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AegisBronze,
                contentColor = Color.Black,
                disabledContainerColor = AegisSteel
            ),
            shape = RoundedCornerShape(8.dp) // Unificado
        ) {
            Text("FINALIZAR CONFIGURACIÓN", fontWeight = FontWeight.ExtraBold)
        }
    }
}