package com.yago.aegis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.yago.aegis.R
import com.yago.aegis.data.ExerciseSet
import com.yago.aegis.data.LoadType
import com.yago.aegis.data.effectiveWeight

@Composable
fun SetRow(
    index: Int,
    set: ExerciseSet,
    onUpdate: (weight: Double, reps: Int, completed: Boolean, modifier: Double) -> Unit,
    onDelete: () -> Unit,
    totalSets: Int,
    loadType: LoadType = LoadType.NORMAL,
    bodyweight: Double = 0.0
) {
    // Valor que se muestra en el primer campo: peso directo (NORMAL) o lastre/asistencia (BW/ASSISTED)
    val fieldValue = if (loadType == LoadType.NORMAL) set.weight else set.loadModifier
    val fieldLabel = when (loadType) {
        LoadType.NORMAL -> stringResource(R.string.label_kg)
        LoadType.BODYWEIGHT -> stringResource(R.string.label_lastre)
        LoadType.ASSISTED -> stringResource(R.string.label_asistencia)
    }
    // Subtexto con el peso efectivo (solo en corporal/asistido, para que el usuario vea la carga real)
    val effectiveHint = if (loadType != LoadType.NORMAL)
        "= ${fmtKg(effectiveWeight(loadType, bodyweight, set.loadModifier))} kg" else null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. INDICADOR TÁCTICO DE SERIE
        val haptic = LocalHapticFeedback.current
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
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (!set.isCompleted) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onUpdate(set.weight, set.reps, !set.isCompleted, set.loadModifier)
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index.toString().padStart(2, '0'),
                color = if (set.isCompleted) Color.Black else MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )
        }

        // 2. CAMPO PESO / LASTRE / ASISTENCIA
        SetInputField(
            value = if (fieldValue == 0.0) "" else fmtKg(fieldValue),
            label = fieldLabel,
            subLabel = effectiveHint,
            modifier = Modifier.weight(1f),
            isCompleted = set.isCompleted,
            onValueChange = { stringValue ->
                if (stringValue.isNotEmpty() && !stringValue.endsWith(".")) {
                    val entered = stringValue.toDoubleOrNull() ?: 0.0
                    val (eff, mod) = resolveWeights(loadType, bodyweight, entered)
                    onUpdate(eff, set.reps, set.isCompleted, mod)
                } else if (stringValue.isEmpty()) {
                    val (eff, mod) = resolveWeights(loadType, bodyweight, 0.0)
                    onUpdate(eff, set.reps, set.isCompleted, mod)
                }
            }
        )

        // 3. CAMPO REPETICIONES (REPS)
        SetInputField(
            value = if (set.reps == 0) "" else set.reps.toString(),
            label = stringResource(R.string.label_reps),
            modifier = Modifier.weight(1f),
            isCompleted = set.isCompleted,
            onValueChange = { stringValue ->
                val reps = stringValue.toIntOrNull() ?: 0
                onUpdate(set.weight, reps, set.isCompleted, set.loadModifier)
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
                    contentDescription = stringResource(R.string.set_row_delete_desc),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.size(32.dp))
        }
    }
}

/** Devuelve (pesoEfectivo, modificador) para el tipo de carga a partir de lo tecleado. */
private fun resolveWeights(loadType: LoadType, bodyweight: Double, entered: Double): Pair<Double, Double> =
    if (loadType == LoadType.NORMAL) entered to 0.0
    else effectiveWeight(loadType, bodyweight, entered) to entered

private fun fmtKg(v: Double): String = if (v % 1.0 == 0.0) v.toInt().toString() else "%.1f".format(v)

@Composable
fun SetInputField(
    value: String,
    label: String,
    isCompleted: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    subLabel: String? = null
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

        // Subtexto: peso efectivo (corporal/asistido)
        if (subLabel != null) {
            Text(
                text = subLabel,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
