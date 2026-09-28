package com.yago.aegis.ui.screens
import com.yago.aegis.ui.theme.Spacing
import com.yago.aegis.ui.theme.Radius

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
import com.yago.aegis.data.AppTags
import com.yago.aegis.data.DefaultExercises
import com.yago.aegis.data.Exercise
import com.yago.aegis.ui.globalExerciseIcons
import com.yago.aegis.ui.components.AegisAlertDialog
import com.yago.aegis.ui.components.AegisSegmentedToggle
import com.yago.aegis.ui.components.TagFilterRow
import com.yago.aegis.ui.components.AegisTagManager
import com.yago.aegis.ui.components.AegisTopBar
import com.yago.aegis.ui.components.ExerciseCard
import com.yago.aegis.ui.components.LoadTypeSelector
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
    var loadType by remember { mutableStateOf(com.yago.aegis.data.LoadType.NORMAL) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("ALL") }
    // Pestañas: EJERCICIOS (biblioteca) por defecto | CREAR EJERCICIO
    var createTab by remember { mutableStateOf(false) }

    // Tags canónicos de la app (fijos)
    val availableTags = AppTags.ALL

    // Filtro de búsqueda + tag
    val filteredExercises = libraryExercises.filter { exercise ->
        val matchesQuery = searchQuery.isBlank() || exercise.name.contains(searchQuery, ignoreCase = true)
        val matchesTag = selectedTag == "ALL" || exercise.tags.any { it.uppercase() == selectedTag.uppercase() }
            || exercise.muscleGroup.uppercase() == selectedTag.uppercase()
        matchesQuery && matchesTag
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Spacing.xl)
        ) {
            Spacer(modifier = Modifier.height(Spacing.sm))
            AegisSegmentedToggle(
                options = listOf(
                    stringResource(R.string.nav_exercices),
                    stringResource(R.string.tab_create_exercise)
                ),
                selectedIndex = if (createTab) 1 else 0,
                onSelect = { createTab = it == 1 }
            )
            Spacer(modifier = Modifier.height(Spacing.lg))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Spacing.xl)
            ) {
              if (createTab) {
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

            // 1b. TIPO DE CARGA: Normal / Peso corporal / Asistido
            item {
                LoadTypeSelector(
                    selected = loadType,
                    onSelect = { loadType = it }
                )
            }

            // 1c. NOTAS DE FORMA
            item {
                Column {
                    SectionLabel(stringResource(R.string.form_notes_section_title))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
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
                        shape = RoundedCornerShape(Radius.md),
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
                    allowEdit = false
                )
            }

            // 2. SECCIÓN: ICONOS (Usando el IconSelector que ya tenemos)
            item {
                Column {
                    SectionLabel(stringResource(R.string.select_icon))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(top = Spacing.md),
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
                                isBodyweight = loadType == com.yago.aegis.data.LoadType.BODYWEIGHT,
                                loadType = loadType.name
                            )

                            routinesViewModel.saveOrUpdateExercise(newExercise)
                            routinesViewModel.addExerciseToTemp(newExercise, slotIndex)

                            // Reset de campos
                            exerciseName = ""
                            selectedTags.clear()
                            selectedIconName = "dumbbell"
                            notes = ""
                            loadType = com.yago.aegis.data.LoadType.NORMAL
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(Radius.md)
                ) {
                    Text(
                        if (isVariantMode) stringResource(R.string.btn_create_and_add_variant) else stringResource(R.string.btn_create_and_add_routine),
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }

              } else {
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
                    shape = RoundedCornerShape(Radius.md)
                )
            }

            // FILTRO POR TAG
            if (availableTags.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    TagFilterRow(
                        tags = availableTags,
                        selectedTag = selectedTag,
                        onTagSelected = { selectedTag = it }
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
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
        modifier = Modifier.padding(bottom = Spacing.sm, start = 2.dp)
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
        shape = RoundedCornerShape(Radius.md)
    )
}

@Composable
fun TagChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(Radius.sm),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
        )
    ) {
        Text(
            text = text.uppercase(),
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = 6.dp),
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary,
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
            .clip(RoundedCornerShape(Radius.md))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(Radius.md)
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