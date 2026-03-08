import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign

@Composable
fun BiometricCard(
    label: String,
    value: String,
    unit: String,
    onValueChange: ((String) -> Unit)? = null
) {
    var tempValue by remember(value) { mutableStateOf(value) }

    Column(
        modifier = Modifier
            .fillMaxWidth() // Dejamos que el Row superior controle el tamaño
            .clip(RoundedCornerShape(8.dp)) // Esquinas más cerradas para un look más serio
            .background(MaterialTheme.colorScheme.surface) // 30%: SurfaceDark
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ) // Borde técnico casi invisible
            .padding(12.dp)
    ) {
        // ETIQUETA: AegisSteel (Gris técnico)
        Text(
            text = label.uppercase(),
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (onValueChange != null) {
                BasicTextField(
                    value = tempValue,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            tempValue = newValue
                            onValueChange(newValue)
                        }
                    },
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground, // AegisWhite
                        fontSize = 22.sp, // Un poco más grande para impacto
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Start,
                        letterSpacing = (-0.5).sp // Kerning negativo para look moderno
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary), // 10% Bronce
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f, fill = false)
                )
            } else {
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // UNIDAD: AegisSteel más pequeño
            Text(
                text = unit.uppercase(),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 3.dp)
            )
        }
    }
}