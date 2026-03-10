package com.yago.aegis.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExerciseAnalyticsHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // ETIQUETA SUPERIOR: Estilo técnico (AegisSteel)
        Text(
            text = "ANÁLISIS DE RENDIMIENTO",
            color = MaterialTheme.colorScheme.secondary, // Cambiado a Steel para dejar el Bronze al buscador
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp
        )

        Spacer(Modifier.height(12.dp))

        // --- BUSCADOR ESTILO AEGIS (Unificado con BiometricCard) ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    "FILTRAR EJERCICIOS...",
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(8.dp), // Esquinas unificadas (8.dp)
            singleLine = true,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.2).sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                // FONDO: SurfaceVariant (Gris oscuro técnico)
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,

                // BORDES: Unificados con el 0.15f de la ExerciseCard
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),

                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
    }
}