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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.R
import com.yago.aegis.data.Exercise
import com.yago.aegis.data.globalExerciseIcons
import com.yago.aegis.ui.components.AegisAlertDialog
import com.yago.aegis.ui.components.AegisTagManager
import com.yago.aegis.ui.components.AegisTopBar
import com.yago.aegis.ui.components.ExerciseCard
import com.yago.aegis.ui.theme.AegisBronze
import com.yago.aegis.viewmodel.RoutinesViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseScreen(
    routinesViewModel: RoutinesViewModel,
    onNavigateBack: () -> Unit,
    onExerciseCreated: (Exercise) -> Unit
) {
    val savedGlobalTags by routinesViewModel.globalTags.collectAsState()
    val libraryExercises by routinesViewModel.allExercises.collectAsState(initial = emptyList())

    var exerciseName by remember { mutableStateOf("") }
    var selectedIconName by remember { mutableStateOf("dumbbell") }
    val selectedTags = remember { mutableStateListOf<String>() }

    var showTagDialog by remember { mutableStateOf(false) }
    var newTagText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    // Filtro de búsqueda
    val filteredExercises = libraryExercises.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    // --- DIÁLOGO DE TAGS (ESTILO UNIFICADO) ---
    if (showTagDialog) {
        AegisAlertDialog(
            title = "NUEVO TAG GLOBAL",
            confirmText = "GUARDAR",
            dismissText = "CANCELAR",
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
                placeholder = "EJ: PECHO"
            )
        }
    }

    Scaffold(
        topBar = {
            AegisTopBar(
                title = stringResource(R.string.title_new_exercise).uppercase(),
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
                    SectionLabel(stringResource(R.string.label_exercise_name).uppercase())
                    EditInput(
                        value = exerciseName,
                        onValueChange = { exerciseName = it },
                        placeholder = stringResource(R.string.hint_exercise_name).uppercase()
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
                    SectionLabel(stringResource(R.string.select_icon).uppercase())
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
                            val primaryTag = selectedTags.firstOrNull() ?: "GENERAL"
                            val newExercise = Exercise(
                                name = exerciseName,
                                type = primaryTag,
                                muscleGroup = primaryTag,
                                tags = selectedTags.toList(),
                                iconName = selectedIconName,
                                notes = ""
                            )
                            routinesViewModel.saveOrUpdateExercise(newExercise)
                            routinesViewModel.addExerciseToTemp(newExercise)
                            exerciseName = ""
                            selectedTags.clear()
                            selectedIconName = "dumbbell"
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
                        "CREATE & ADD TO ROUTINE",
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
                        text = "  OR SELECT FROM LIBRARY  ",
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
                        Text("Search exercises...",
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

            // LISTA DE LIBRERÍA
            items(filteredExercises) { exercise ->
                val isAlreadyInRoutine = routinesViewModel.tempExercises.any { it.id == exercise.id }

                ExerciseCard(
                    exercise = exercise,
                    isAddMode = true,
                    onEdit = {
                        if (!isAlreadyInRoutine) {
                            routinesViewModel.addExerciseToTemp(exercise)
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