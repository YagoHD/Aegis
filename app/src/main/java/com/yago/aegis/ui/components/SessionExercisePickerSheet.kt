package com.yago.aegis.ui.components
import com.yago.aegis.ui.theme.Spacing
import com.yago.aegis.ui.theme.Radius

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.R
import com.yago.aegis.data.Exercise
import com.yago.aegis.ui.getExerciseIcon

/**
 * Hoja inferior para añadir ejercicios de la librería a la sesión activa.
 * Pensada para el ENTRENAMIENTO LIBRE: buscas y añades ejercicios uno a uno.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionExercisePickerSheet(
    exercises: List<Exercise>,
    alreadyAddedIds: Set<Long>,
    onPick: (Exercise) -> Unit,
    onCreateExercise: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    var createTab by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    val filtered = remember(exercises, query) {
        exercises
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
            .sortedBy { it.name.uppercase() }
    }
    val trimmedName = newName.trim()
    val canCreate = trimmedName.isNotEmpty() &&
        exercises.none { it.name.trim().equals(trimmedName, ignoreCase = true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = Spacing.xl)
        ) {
            Text(
                text = stringResource(R.string.btn_add_exercise),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            AegisSegmentedToggle(
                options = listOf(
                    stringResource(R.string.nav_exercices),
                    stringResource(R.string.tab_create_exercise)
                ),
                selectedIndex = if (createTab) 1 else 0,
                onSelect = { createTab = it == 1 }
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            if (createTab) {
                // --- PESTAÑA CREAR EJERCICIO ---
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    placeholder = { Text(stringResource(R.string.exercise_name_placeholder), fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.secondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.md),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.md))
                        .background(
                            if (canCreate) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                        .then(if (canCreate) Modifier.clickable { onCreateExercise(trimmedName) } else Modifier)
                        .padding(horizontal = Spacing.md, vertical = Spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Add, null,
                        tint = if (canCreate) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.md))
                    Text(
                        text = if (trimmedName.isEmpty())
                            stringResource(R.string.title_new_exercise)
                        else
                            stringResource(R.string.create_exercise_named, trimmedName.uppercase()),
                        color = if (canCreate) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                return@Column
            }

            // --- PESTAÑA EJERCICIOS (biblioteca) ---
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(stringResource(R.string.search_placeholder), fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.secondary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.md),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                items(filtered, key = { it.id }) { ex ->
                    val added = ex.id in alreadyAddedIds
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.md))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .then(if (added) Modifier else Modifier.clickable { onPick(ex) })
                            .padding(horizontal = Spacing.md, vertical = Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getExerciseIcon(ex.iconName),
                            contentDescription = null,
                            tint = if (added) MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                                   else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.md))
                        Text(
                            text = ex.name.uppercase(),
                            color = if (added) MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.onBackground,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (added) {
                            Text(
                                text = stringResource(R.string.already_added_label),
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        } else {
                            Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}
