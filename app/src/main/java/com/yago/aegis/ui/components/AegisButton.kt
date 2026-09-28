package com.yago.aegis.ui.components
import com.yago.aegis.ui.theme.Radius

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Botón primario del design system Aegis: relleno bronce, texto negro Black con letter-spacing.
 * Úsalo en lugar de `Button(...)` con colores/forma a mano para mantener consistencia.
 */
@Composable
fun AegisPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    fontSize: Int = 13,
    letterSpacing: Int = 1,
    trailingIcon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(Radius.md),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = text.uppercase(),
                fontWeight = FontWeight.Black,
                fontSize = fontSize.sp,
                letterSpacing = letterSpacing.sp
            )
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(trailingIcon, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
    }
}

/**
 * Botón secundario del design system Aegis: contorno, texto bronce.
 */
@Composable
fun AegisSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    fontSize: Int = 13
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(Radius.md),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            contentColor.copy(alpha = if (enabled) 0.6f else 0.2f)
        )
    ) {
        Text(
            text = text.uppercase(),
            color = if (enabled) contentColor else contentColor.copy(alpha = 0.4f),
            fontWeight = FontWeight.Black,
            fontSize = fontSize.sp,
            letterSpacing = 1.sp
        )
    }
}
