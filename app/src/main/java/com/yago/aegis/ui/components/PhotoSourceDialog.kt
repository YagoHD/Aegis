package com.yago.aegis.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.yago.aegis.data.PhotoType
import com.yago.aegis.ui.theme.AegisBronze
import com.yago.aegis.ui.theme.AegisWhite

@Composable
fun PhotoSourceDialog(
    onDismiss: () -> Unit,
    onConfirm: (PhotoType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF161616),
        title = { Text("Actualizar Log Visual", color = AegisWhite) },
        text = { Text("¿Qué foto quieres cambiar?", color = Color.Gray) },
        confirmButton = {
            TextButton(onClick = { onConfirm(PhotoType.ACTUAL) }) {
                Text("ACTUAL", color = AegisBronze)
            }
        },
        dismissButton = {
            TextButton(onClick = { onConfirm(PhotoType.BASE) }) {
                Text("BASE", color = AegisBronze)
            }
        }
    )
}