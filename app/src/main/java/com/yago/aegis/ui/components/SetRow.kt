package com.yago.aegis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.data.ExerciseSet

@Composable
fun SetRow(
    index: Int,
    set: ExerciseSet,
    onUpdate: (Double, Int, Boolean) -> Unit,
    onDelete: () -> Unit,
    totalSets: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. INDICADOR TÁCTICO DE SERIE
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(
                    if (set.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(6.dp)
                )
                .border(
                    width = 1.dp,
                    color = if (set.isCompleted) Color.Transparent else MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index.toString().padStart(2, '0'),
                color = if (set.isCompleted) Color.Black else MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )
        }

        // 2. CAMPO PESO (KG)
        SetInputField(
            value = if (set.weight == 0.0) "" else set.weight.toString(),
            label = "KG",
            modifier = Modifier.weight(1f),
            isCompleted = set.isCompleted,
            onValueChange = { stringValue ->
                // Solo actualizamos el valor numérico real si es un número válido (no termina en punto)
                if (stringValue.isNotEmpty() && !stringValue.endsWith(".")) {
                    val weight = stringValue.toDoubleOrNull() ?: 0.0
                    onUpdate(weight, set.reps, set.isCompleted)
                } else if (stringValue.isEmpty()) {
                    onUpdate(0.0, set.reps, set.isCompleted)
                }
            }
        )

        // 3. CAMPO REPETICIONES (REPS)
        SetInputField(
            value = if (set.reps == 0) "" else set.reps.toString(),
            label = "REPS",
            modifier = Modifier.weight(1f),
            isCompleted = set.isCompleted,
            onValueChange = { stringValue ->
                val reps = stringValue.toIntOrNull() ?: 0
                onUpdate(set.weight, reps, set.isCompleted)
            }
        )

        // 4. BOTÓN BORRAR
        if (index > 1) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Set",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun SetInputField(
    value: String,
    label: String,
    isCompleted: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Estado local para manejar el texto mientras el usuario escribe (evita errores con el punto decimal)
    var textValue by remember(value) { mutableStateOf(value) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                fontSize = 9.sp,
                letterSpacing = 1.sp
            )
        )

        BasicTextField(
            value = textValue,
            onValueChange = { newValue ->
                // Reemplazamos coma por punto y filtramos para que solo haya un punto
                val filtered = newValue.replace(",", ".")
                if (filtered.count { it == '.' } <= 1 && filtered.all { it.isDigit() || it == '.' }) {
                    textValue = filtered
                    onValueChange(filtered)
                }
            },
            textStyle = TextStyle(
                color = if (isCompleted) MaterialTheme.colorScheme.primary else Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .background(
                            color = if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (textValue.isEmpty()) {
                        Text(
                            "0",
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}