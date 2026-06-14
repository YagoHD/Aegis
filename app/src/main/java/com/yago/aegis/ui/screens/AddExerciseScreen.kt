package com.yago.aegis.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.R
import com.yago.aegis.data.DefaultExercises
import com.yago.aegis.data.Exercise
import com.yago.aegis.data.globalExerciseIcons
import com.yago.aegis.ui.components.AegisAlertDialog
import com.yago.aegis.ui.components.TagFilterRow
import com.yago.aegis.ui.components.AegisTagManager
import com.yago.aegis.ui.components.AegisTopBar
import com.yago.aegis.ui.components.ExerciseCard
import com.yago.aegis.viewmodel.RoutinesViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseScreen(
    routinesViewModel: RoutinesViewModel,
    slotIndex: Int = -1,    // -1 = nuevo slot | >= 0 = añadir variante al slot existente
    onNavigateBack: () -> Unit,
    onExerciseCreated: (Exercise) -> Unit
) {
    val isVariantMode = slotIndex >= 0
    val savedGlobalTags by routinesViewModel.globalTags.collectAsState()
    val libraryExercises by routinesViewModel.allExercises.collectAsState(initial = emptyList())

    var exerciseName by remember { mutableStateOf("") }
    var selectedIconName by remember { mutableStateOf("dumbbell") }
    val selectedTags = remember { mutableStateListOf<String>() }
    var notes by remember { mutableStateOf("") }
    var isBodyweight by remember { mutableStateOf(false) }

    var showTagDialog by remember { mutableStateOf(false) }
    var newTagText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("ALL") }

    // Tags disponibles derivados de los ejercicios de la librería
    val availableTags = remember(libraryExercises) {
        libraryExercises.flatMap { it.tags }
            .map { it.uppercase() }
            .filter { it != DefaultExercises.BASE_TAG.uppercase() }
            .distinct().sorted()
    }

    // Filtro de búsqueda + tag
    val filteredExercises = libraryExercises.filter { exercise ->
        val matchesQuery = searchQuery.isBlank() || exercise.name.contains(searchQuery, ignoreCase = true)
        val matchesTag = selectedTag == "ALL" || exercise.tags.any { it.uppercase() == selectedTag.uppercase() }
            || exercise.muscleGroup.uppercase() == selectedTag.uppercase()
        matchesQuery && matchesTag
    }

    // --- DIÁLOGO DE TAGS (ESTILO UNIFICADO) ---
    if (showTagDialog) {
        AegisAlertDialog(
            title = stringResource(R.string.new_global_tag_title),
            confirmText = stringResource(R.string.btn_save),
            dismissText = stringResource(R.string.btn_cancel),
            onDismiss = { showTagDialog = false },
            onConfirm = {
                if (newTagText.isNotBlank()) {
                    routinesViewModel.addGlobalTag(newTagText.uppercase())
                    newTagText = ""
                    showTagDialog = false
                }
            }
        ) {
            EditInput(
                value = newTagText,
                onValueChange = { newTagText = it },
                placeholder = stringResource(R.string.tag_placeholder)
            )
        }
    }

    Scaffold(
        topBar = {
            AegisTopBar(
                title = if (isVariantMode) stringResource(R.string.title_add_variant) else stringResource(R.string.title_new_exercise),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary // Flecha en Bronce
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background // 050505
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 1. SECCIÓN: CREACIÓN DE EJERCICIO
            item {
                Column {
                    SectionLabel(stringResource(R.string.label_exercise_name))
                    EditInput(
                        value = exerciseName,
                        onValueChange = { exerciseName = it },
                        placeholder = stringResource(R.string.exercise_name_placeholder)
                    )
                }
            }

            // 1b. TIPO: BODYWEIGHT
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.bodyweight_section_title),
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = stringResource(R.string.bodyweight_description),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = isBodyweight,
                        onCheckedChange = { isBodyweight = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }

            // 1c. NOTAS DE FORMA
            item {
                Column {
                    SectionLabel(stringResource(R.string.form_notes_section_title))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.form_notes_placeholder),
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                                fontSize = 11.sp
                            )
                        },
                        minLines = 2,
                        maxLines = 4,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 13.sp
                        ),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            item {
                AegisTagManager(
                    allTags = savedGlobalTags,
                    selectedTags = selectedTags.toSet(),
                    onTagClick = { tag ->
                        if (selectedTags.contains(tag)) selectedTags.remove(tag)
                        else selectedTags.add(tag)
                    },
                    onAddClick = { showTagDialog = true },
                    onRemoveSelectedClick = {
                        routinesViewModel.removeGlobalTags(selectedTags.toList())
                        selectedTags.clear()
                    }
                )
            }

            // 2. SECCIÓN: ICONOS (Usando el IconSelector que ya tenemos)
            item {
                Column {
                    SectionLabel(stringResource(R.string.select_icon))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        maxItemsInEachRow = 5,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        globalExerciseIcons.forEach { (name, icon) ->
                            AegisIconSelector(
                                icon = icon,
                                isSelected = selectedIconName == name,
                                onClick = { selectedIconName = name }
                            )
                        }
                    }
                }
            }

            // BOTÓN CREAR (Bronce principal)
            item {
                Button(
                    onClick = {
                        if (exerciseName.isNotBlank()) {
                            // Si no hay tags, dejamos la lista vacía y los grupos como "PENDIENTE" o el nombre del ejercicio
                            val hasTags = selectedTags.isNotEmpty()

                            val newExercise = Exercise(
                                name = exerciseName.trim().uppercase(),
                                type = if (hasTags) selectedTags.first() else "",
                                muscleGroup = if (hasTags) selectedTags.first() else " ",
                                tags = selectedTags.toList(),
                                iconName = selectedIconName,
                                notes = notes.trim(),
                                isBodyweight = isBodyweight
                            )

                            routinesViewModel.saveOrUpdateExercise(newExercise)
                            routinesViewModel.addExerciseToTemp(newExercise, slotIndex)

                            // Reset de campos
                            exerciseName = ""
                            selectedTags.clear()
                            selectedIconName = "dumbbell"
                            notes = ""
                            isBodyweight = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if (isVariantMode) stringResource(R.string.btn_create_and_add_variant) else stringResource(R.string.btn_create_and_add_routine),
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }

            // 3. SECCIÓN: LIBRERÍA (Separador visual táctico)
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                    )
                    Text(
                        text = "  ${stringResource(R.string.label_or_select_library)}  ",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                    )
                }
            }

            // BUSCADOR LIBRERÍA (Look Obsidiana)
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(stringResource(R.string.search_exercises_placeholder),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                            fontSize = 13.sp)
                    },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // FILTRO POR TAG
            if (availableTags.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    TagFilterRow(
                        tags = availableTags,
                        selectedTag = selectedTag,
                        onTagSelected = { selectedTag = it }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // LISTA DE LIBRERÍA
            items(filteredExercises) { exercise ->
                // Grisado si el ejercicio ya está en CUALQUIER slot (modo nuevo o variante).
                // Un ejercicio solo puede aparecer una vez en toda la rutina para evitar
                // IDs duplicados que causan crash en la sesión activa.
                val isAlreadyInRoutine = routinesViewModel.tempSlots.any { slot ->
                    slot.variants.any { it.id == exercise.id }
                }

                ExerciseCard(
                    exercise = exercise,
                    isAddMode = true,
                    onEdit = {
                        if (!isAlreadyInRoutine) {
                            routinesViewModel.addExerciseToTemp(exercise, slotIndex)
                        }
                    },
                    onDelete = {},
                    modifier = Modifier.alpha(if (isAlreadyInRoutine) 0.4f else 1f)
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}
// --- COMPONENTES DE APOYO ---

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = MaterialTheme.colorScheme.secondary, // Bronce suave o gris técnico
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
    )
}

@Composable
fun AegisInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholder.uppercase(),
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                fontSize = 12.sp,
                letterSpacing = 0.5.sp
            )
        },
        singleLine = singleLine,
        minLines = minLines,
        textStyle = androidx.compose.ui.text.TextStyle(
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun TagChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
        )
    ) {
        Text(
            text = text.uppercase(),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.secondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun IconBox(icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
            modifier = Modifier.size(26.dp)
        )
    }
}