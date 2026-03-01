import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField // Importante
import androidx.compose.foundation.text.KeyboardOptions // Para el teclado numérico
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.ui.theme.AegisBronze
import com.yago.aegis.ui.theme.AegisCard
import com.yago.aegis.ui.theme.AegisWhite

@Composable
fun BiometricCard(
    label: String,
    value: String,
    unit: String,
    onValueChange: ((String) -> Unit)? = null // Nueva función para avisar del cambio
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AegisCard)
            .padding(12.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label.uppercase(),
            color = AegisBronze,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(verticalAlignment = Alignment.Bottom) {
            // Sustituimos el Text por BasicTextField
            BasicTextField(
                value = value,
                onValueChange = { onValueChange?.invoke(it) }, // Solo se ejecuta si no es null
                enabled = onValueChange != null, // SI ES NULL, NO ES CLICABLE
                readOnly = onValueChange == null, // Evita que salga el cursor
                textStyle = TextStyle(
                    color = AegisWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                ),
                cursorBrush = SolidColor(AegisBronze), // El palito del cursor será bronce
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), // Teclado con números y coma
                modifier = Modifier.width(IntrinsicSize.Min) // Para que el cursor esté pegado al número
            )

            if (unit.isNotEmpty()) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 2.dp) // Alineación fina con el número
                )
            }
        }
    }
}