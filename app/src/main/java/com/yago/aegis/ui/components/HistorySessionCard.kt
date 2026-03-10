import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.data.WorkoutSession
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistorySessionCard(
    session: WorkoutSession,
    exerciseId: Long
) {
    val progress = session.exercisesProgress.find { it.exercise.id == exerciseId } ?: return

    // Formato de fecha técnico (AegisSteel)
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dateString = sdf.format(Date(session.date))
    val totalVolume = progress.sets.sumOf { it.weight * it.reps }

    Column(modifier = Modifier.fillMaxWidth()) {
        // --- CABECERA: FECHA y VOLUMEN (Bronce) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateString.uppercase(),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )

            // Volumen resaltado en Bronce como dato clave
            Text(
                text = "${totalVolume.toInt()} KG TOTAL",
                color = MaterialTheme.colorScheme.primary, // AegisBronze
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- TABLA DE SETS (Cabeceras Técnicas) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val headerStyle = androidx.compose.ui.text.TextStyle(
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Text("SET", modifier = Modifier.weight(1f), style = headerStyle)
            Text("PESO", modifier = Modifier.weight(1f), style = headerStyle, textAlign = TextAlign.Center)
            Text("REPETICIONES", modifier = Modifier.weight(1f), style = headerStyle, textAlign = TextAlign.End)
        }

        // Listado de cada Set con separadores sutiles
        progress.sets.forEachIndexed { index, set ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Número de SET (AegisSteel)
                Text(
                    text = "${index + 1}",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                // Peso (Blanco/Impacto)
                Text(
                    text = "${set.weight}kg",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.2).sp
                )

                // Reps (Blanco/Impacto)
                Text(
                    text = "${set.reps}",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.End,
                    letterSpacing = (-0.2).sp
                )
            }

            // Divisor casi invisible para mantener el look limpio
            if (index < progress.sets.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                    thickness = 0.5.dp
                )
            }
        }
    }
}