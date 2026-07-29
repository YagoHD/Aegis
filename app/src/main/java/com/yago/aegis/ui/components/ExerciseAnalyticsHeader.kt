package com.yago.aegis.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.R
import com.yago.aegis.data.Routine

@Composable
fun ExerciseAnalyticsHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    availableTags: List<String> = emptyList(),
    selectedTag: String = "ALL",
    onTagSelected: (String) -> Unit = {},
    onlyWithData: Boolean = false,
    onToggleOnlyWithData: () -> Unit = {},
    routines: List<Routine> = emptyList(),
    selectedRoutineId: Int? = null,
    onRoutineSelected: (Int?) -> Unit = {}
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

        // Filtros rápidos: "CON DATOS" (toggle) + selector de rutina
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderChip(
                label = stringResource(R.string.filter_with_data),
                isSelected = onlyWithData,
                onClick = onToggleOnlyWithData
            )

            if (routines.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .size(width = 1.dp, height = 18.dp)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f))
                )
                Text(
                    text = stringResource(R.string.filter_routine_label),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                HeaderChip(
                    label = stringResource(R.string.filter_all_routines),
                    isSelected = selectedRoutineId == null,
                    onClick = { onRoutineSelected(null) }
                )
                routines.forEach { r ->
                    HeaderChip(
                        label = r.name.uppercase(),
                        isSelected = selectedRoutineId == r.id,
                        onClick = { onRoutineSelected(r.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else Color.Transparent,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
        )
    ) {
        Text(
            text = label,
            color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.secondary,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
