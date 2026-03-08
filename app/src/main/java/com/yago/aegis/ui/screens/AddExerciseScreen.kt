package com.yago.aegis.ui.screens

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
    val backgroundBlackgrey = colorResource(id = R.color.backgroundBlackgrey)
    val savedGlobalTags by routinesViewModel.globalTags.collectAsState()
    val libraryExercises by routinesViewModel.allExercises.collectAsState(initial = emptyList<Exercise>())
    var exerciseName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedIconName by remember { mutableStateOf("dumbbell") }
    val selectedTags = remember { mutableStateListOf<String>() }
    // --- ESTADOS PARA EL DIÁLOGO DE TAGS ---
    var showTagDialog by remember { mutableStateOf(false) }
    var newTagText by remember { mutableStateOf("") }
    var exerciseToDelete by remember { mutableStateOf<Exercise?>(null) }
    // --- ESTADO PARA LA BÚSQUEDA ---
    var searchQuery by remember { mutableStateOf("") }
    // Lógica de filtrado en tiempo real
    val filteredExercises = libraryExercises.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    val exerciseIcons = listOf(
        "dumbbell" to Icons.Default.FitnessCenter,
        "body" to Icons.Default.AccessibilityNew,
        "legs" to Icons.Default.DirectionsRun,
        "heart" to Icons.Default.Favorite,
        "timer" to Icons.Default.Timer,
        "bolt" to Icons.Default.Bolt,
        "layers" to Icons.Default.Layers
    )

    // --- DIÁLOGO RÁPIDO DE TAGS ---
    if (showTagDialog) {
        AegisAlertDialog(
            title = "CREAR TAG GLOBAL",
            confirmText = "GUARDAR",
            dismissText = "CANCELAR",
            onDismiss = { showTagDialog = false },
            onConfirm = {
                if (newTagText.isNotBlank()) {
                    routinesViewModel.addGlobalTag(newTagText)
                    newTagText = ""
                    showTagDialog = false
                }
            },
            content = {
                OutlinedTextField(
                    value = newTagText,
                    onValueChange = { newTagText = it.uppercase() },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AegisBronze,
                        unfocusedBorderColor = Color.DarkGray,
                        cursorColor = AegisBronze
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        )
    }

    Scaffold(
        topBar = {
            // ✅ Usamos el componente unificado del Canvas
            AegisTopBar(
                title = stringResource(R.string.title_new_exercise),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = AegisBronze // Mantenemos tu toque de color bronce para la flecha
                        )
                    }
                }
            )
        },
        containerColor = backgroundBlackgrey
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(10.dp)) }

            // SECCIÓN: NOMBRE
            item {
                SectionLabel(stringResource(R.string.label_exercise_name))
                AegisInput(
                    value = exerciseName,
                    onValueChange = { exerciseName = it },
                    placeholder = stringResource(R.string.hint_exercise_name)
                )
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

            // SECCIÓN: ICONOS
            item {
                SectionLabel(stringResource(R.string.select_icon))
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    maxItemsInEachRow = 4,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    globalExerciseIcons.forEach { (name, icon) ->
                        EditIconBox(
                            icon = icon,
                            isSelected = selectedIconName == name,
                            onClick = { selectedIconName = name } // ✅ Esto cambiará el estado
                        )
                    }
                }
            }
            // BOTÓN CREAR
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
                    colors = ButtonDefaults.buttonColors(containerColor = AegisBronze),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CREATE & ADD TO ROUTINE", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                }
            }

            // SEPARADOR LIBRERÍA
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 10.dp)) {
                    Divider(modifier = Modifier.weight(1f), color = Color.DarkGray)
                    Text(
                        " OR SELECT FROM LIBRARY ",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Divider(modifier = Modifier.weight(1f), color = Color.DarkGray)
                }
            }

            // BUSCADOR LIBRERÍA (Funcional)
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search exercises...", color = Color.Gray, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF161616),
                        unfocusedContainerColor = Color(0xFF161616),
                        focusedBorderColor = AegisBronze,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // LISTA DE EJERCICIOS FILTRADA
            items(filteredExercises) { exercise ->
                val isAlreadyInRoutine = routinesViewModel.tempExercises.any { it.id == exercise.id }

                ExerciseCard(
                    exercise = exercise,
                    isAddMode = true, // ✅ ACTIVAMOS EL MODO AÑADIR
                    onEdit = {
                        if (!isAlreadyInRoutine) {
                            routinesViewModel.addExerciseToTemp(exercise)
                        }
                    },
                    onDelete = { /* Ya no se usa aquí, podemos dejarlo vacío */ },
                    modifier = Modifier.alpha(if (isAlreadyInRoutine) 0.5f else 1f)
                )
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}
// --- COMPONENTES DE APOYO ---

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        color = Color(0xFFC5A358), // Un tono más dorado/bronce
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
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
        placeholder = { Text(placeholder, color = Color.DarkGray, fontSize = 14.sp) },
        singleLine = singleLine,
        minLines = minLines,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF161616),
            unfocusedContainerColor = Color(0xFF161616),
            focusedBorderColor = AegisBronze,
            unfocusedBorderColor = Color.DarkGray,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun TagChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) AegisBronze else Color(0xFF161616)) // Cambio de color táctico
            .border(1.dp, if (isSelected) AegisBronze else Color.DarkGray, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black else Color.LightGray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun IconBox(icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) AegisBronze.copy(alpha = 0.1f) else Color(0xFF161616))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) AegisBronze else Color.DarkGray,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) AegisBronze else Color.Gray,
            modifier = Modifier.size(28.dp)
        )
    }
}