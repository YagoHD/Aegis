package com.yago.aegis.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.ui.theme.AegisBronze
import com.yago.aegis.ui.theme.AegisSteel
import com.yago.aegis.ui.theme.AegisWhite
import com.yago.aegis.ui.theme.MatteBlack

@Composable
fun MetricInput(
    label: String,
    unit: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Etiquetas superiores (Ej: HEIGHT CONFIGURATION y CENTIMETERS)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = AegisBronze, // Usamos el bronce para resaltar el título
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = unit,
                color = AegisSteel,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Fila del valor e icono
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    // Dibujamos la línea inferior sutil de la imagen
                    drawLine(
                        color = AegisSteel.copy(alpha = 0.5f),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BasicTextField(
                value = value,
                onValueChange = { if (it.length <= 5) onValueChange(it) }, // Límite de caracteres
                textStyle = TextStyle(
                    color = if (value == "000" || value == "00.0") AegisSteel else AegisWhite,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                cursorBrush = SolidColor(AegisBronze),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text("000", color = AegisSteel, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    }
                    innerTextField()
                }
            )

            // Icono cuadrado con fondo oscuro
            Surface(
                color = MatteBlack,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AegisSteel,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun AegisTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isSingleLine: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Etiqueta superior en pequeño y negrita
        Text(
            text = label.uppercase(),
            color = AegisSteel,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // El campo de entrada
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp)), // Esquinas suaves como en la imagen
            placeholder = {
                Text(
                    text = placeholder,
                    color = AegisSteel.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            },
            singleLine = isSingleLine,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MatteBlack,
                unfocusedContainerColor = MatteBlack,
                focusedTextColor = AegisWhite,
                unfocusedTextColor = AegisWhite,
                cursorColor = AegisBronze,
                focusedIndicatorColor = Color.Transparent, // Quitamos la línea fea de abajo
                unfocusedIndicatorColor = Color.Transparent
            ),
            textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
        )
    }
}