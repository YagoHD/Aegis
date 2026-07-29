package com.yago.aegis.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.R
import com.yago.aegis.data.PhotoType

@Composable
fun PhotoSourceDialog(
    onDismiss: () -> Unit,
    onConfirm: (PhotoType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        // 30%: SurfaceDark para el contenedor
        containerColor = MaterialTheme.colorScheme.surface,
        // Borde fino de 1.dp para mantener el lenguaje técnico
        modifier = Modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
            shape = RoundedCornerShape(28.dp) // Radio estándar de M3 AlertDialog
        ),
        title = {
            Text(
                text = stringResource(R.string.visual_log_title),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
        },
        text = {
            Text(
                text = stringResource(R.string.photo_dialog_message),
                color = MaterialTheme.colorScheme.secondary, // AegisSteel
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            // El botón principal (Actual) destaca más
            TextButton(onClick = { onConfirm(PhotoType.ACTUAL) }) {
                Text(
                    text = stringResource(R.string.photo_state_actual),
                    color = MaterialTheme.colorScheme.primary, // AegisBronze
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { onConfirm(PhotoType.BASE) }) {
                Text(
                    text = stringResource(R.string.photo_state_base),
                    color = MaterialTheme.colorScheme.secondary, // Más sutil que el actual
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    )
}