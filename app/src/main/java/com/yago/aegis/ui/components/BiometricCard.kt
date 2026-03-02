import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.ui.theme.AegisBronze

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
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF161616))
            .padding(12.dp)
            .width(115.dp) // ✅ Ancho fijo un poco mayor para evitar que se corte el número
    ) {
        Text(
            text = label.uppercase(),
            color = Color.Gray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (onValueChange != null) {
                BasicTextField(
                    value = tempValue,
                    onValueChange = { newValue ->
                        // Filtro para números
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            tempValue = newValue
                            onValueChange(newValue)
                        }
                    },
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start // ✅ Asegura que empiece desde la izquierda
                    ),
                    cursorBrush = SolidColor(AegisBronze),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f, fill = false) // ✅ La clave: crece pero no empuja de más
                )
            } else {
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Espacio mínimo para que el KG no se pegue literal al número
            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = unit,
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}