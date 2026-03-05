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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.yago.aegis.ui.theme.AegisBronze

@Composable
fun SetRow(
    index: Int,
    set: ExerciseSet,
    onUpdate: (Double, Int, Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(64.dp), // Un poco más de altura para que los campos respiren
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. INDICADOR DE NÚMERO DE SERIE
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    if (set.isCompleted) AegisBronze else Color(0xFF1A1A1A),
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index.toString(),
                color = if (set.isCompleted) Color.Black else Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        // 2. CAMPO PESO (KG)
        SetInputField(
            value = if (set.weight == 0.0) "" else set.weight.toString(),
            label = "KG",
            modifier = Modifier.weight(1f),
            onValueChange = { newValue ->
                // Filtramos para aceptar comas o puntos y convertir a Double
                val cleanedInput = newValue.replace(",", ".")
                val weight = cleanedInput.toDoubleOrNull() ?: 0.0
                onUpdate(weight, set.reps, set.isCompleted)
            }
        )

        // 3. CAMPO REPETICIONES (REPS)
        SetInputField(
            value = if (set.reps == 0) "" else set.reps.toString(),
            label = "REPS",
            modifier = Modifier.weight(1f),
            onValueChange = { newValue ->
                val reps = newValue.toIntOrNull() ?: 0
                onUpdate(set.weight, reps, set.isCompleted)
            }
        )

        // 4. BOTÓN BORRAR (Papelera roja discreta)
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Set",
                tint = Color.Red.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SetInputField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), // Cambiado a Decimal para el peso
            cursorBrush = SolidColor(AegisBronze),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .background(Color(0xFF111111), RoundedCornerShape(8.dp))
                .border(1.dp, Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(vertical = 8.dp)
        )
    }
}