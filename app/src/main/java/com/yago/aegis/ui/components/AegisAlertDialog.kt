package com.yago.aegis.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Primitivo de diálogo del design system: única fuente del "chrome" (fondo surface + borde
 * técnico de 1dp + esquinas de 28dp). Todos los diálogos de la app deben construirse sobre este
 * en vez de repetir el mismo `containerColor` + `Modifier.border(...)` a mano.
 */
@Composable
fun AegisDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
            shape = RoundedCornerShape(28.dp)
        ),
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        title = title,
        text = text
    )
}

@Composable
fun AegisAlertDialog(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "SÍ",
    dismissText: String = "NO",
    confirmButtonColor: Color = MaterialTheme.colorScheme.primary,
    confirmEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    AegisDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(
                text = title.uppercase(),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
        },

        text = {
            content()
        },

        confirmButton = {
            TextButton(onClick = onConfirm, enabled = confirmEnabled) {
                Text(
                    text = confirmText.uppercase(),
                    color = if (confirmEnabled) confirmButtonColor else confirmButtonColor.copy(alpha = 0.35f),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = dismissText.uppercase(),
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    )
}