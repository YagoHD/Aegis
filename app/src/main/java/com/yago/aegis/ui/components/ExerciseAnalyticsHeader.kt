package com.yago.aegis.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
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
    onSearchChange: (String) -> Unit,
    availableTags: List<String> = emptyList(),
    selectedTag: String = "ALL",
    onTagSelected: (String) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "ANÁLISIS DE RENDIMIENTO",
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    "FILTRAR EJERCICIOS...",
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                )
            },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Close, null,
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                    }
                }
            },
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.2).sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        // Chips de filtro por tag
        if (availableTags.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            TagFilterRow(
                tags = availableTags,
                selectedTag = selectedTag,
                onTagSelected = onTagSelected
            )
        }
    }
}
