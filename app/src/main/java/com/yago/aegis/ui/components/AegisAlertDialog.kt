package com.yago.aegis.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.yago.aegis.R
import com.yago.aegis.ui.theme.AegisBronze

// Actualiza tu archivo AegisAlertDialog.kt
@Composable
fun AegisAlertDialog(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "SÍ",
    dismissText: String = "NO",
    confirmButtonColor: Color = AegisBronze,
    // ✅ Cambiamos message: String por un bloque Composable
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title.uppercase(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            // ✅ Aquí se dibujará lo que le pasemos (Texto o TextField)
            content()
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, color = confirmButtonColor, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText, color = Color.White)
            }
        },
        containerColor = Color(0xFF161616),
        titleContentColor = Color.White,
        textContentColor = Color.Gray
    )
}