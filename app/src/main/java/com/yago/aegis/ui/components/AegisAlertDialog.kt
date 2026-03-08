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

@Composable
fun AegisAlertDialog(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "SÍ",
    dismissText: String = "NO",
    confirmButtonColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,

        modifier = Modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
            shape = RoundedCornerShape(28.dp)
        ),

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
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText.uppercase(),
                    color = confirmButtonColor,
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