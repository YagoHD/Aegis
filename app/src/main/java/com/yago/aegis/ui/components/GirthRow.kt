package com.yago.aegis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*

@Composable
fun GirthRow(label: String, value: String, onValueChange: (String) -> Unit) {
    // Estado local para fluidez total
    var tempValue by remember(value) { mutableStateOf(value) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp), // Un poco más de aire vertical
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- ETIQUETA: AegisWhite con peso medio ---
        Text(
            text = label.uppercase(), // Consistencia en mayúsculas
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.weight(1f)
        )

        // --- CONTENEDOR DE ENTRADA (Módulo Técnico) ---
        Box(
            modifier = Modifier
                .width(90.dp) // Un poco más de ancho para comodidad
                .clip(RoundedCornerShape(4.dp))
                // 30%: SurfaceDark pero un poco más profundo para el input
                .background(MaterialTheme.colorScheme.background)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(vertical = 8.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = tempValue,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        tempValue = newValue
                        onValueChange(newValue)
                    }
                },
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.primary, // 10%: El número brilla en Bronce
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}