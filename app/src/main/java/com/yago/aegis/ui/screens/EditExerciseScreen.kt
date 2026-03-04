package com.yago.aegis.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.yago.aegis.ui.theme.AegisBronze
import com.yago.aegis.ui.theme.AegisWhite
import com.yago.aegis.viewmodel.RoutinesViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditExerciseScreen(
    routinesViewModel: RoutinesViewModel,
    exerciseToEdit: Exercise? = null, // Si es null -> Modo Crear
    onNavigateBack: () -> Unit
) {
    // --- ESTADOS DE LA INTERFAZ ---
    var exerciseName by remember { mutableStateOf(exerciseToEdit?.name ?: "") }
    val selectedTags = remember { mutableStateListOf<String>().apply {
        exerciseToEdit?.tags?.let { addAll(it) }
    } }
    var selectedIconName by remember { mutableStateOf(exerciseToEdit?.iconName ?: "dumbbell") }

    // Estados para gestión de Tags
    val savedGlobalTags by routinesViewModel.globalTags.collectAsState()
    var showTagDialog by remember { mutableStateOf(false) }
    var newTagText by remember { mutableStateOf("") }

    // Iconos disponibles (puedes añadir más)
    val exerciseIcons = listOf(
        "dumbbell" to Icons.Default.FitnessCenter,
        "body" to Icons.Default.AccessibilityNew,
        "kick" to Icons.Default.SportsMartialArts,
        "run" to Icons.Default.DirectionsRun,
        "walk" to Icons.Default.DirectionsWalk,
        "chart" to Icons.Default.ShowChart,
        "timer" to Icons.Default.Timer,
        "yoga" to Icons.Default.SelfImprovement
    )

    // --- DIÁLOGO PARA CREAR TAGS ---
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
                // ✅ Solo definimos el cuerpo del diálogo
                OutlinedTextField(
                    value = newTagText,
                    onValueChange = { newTagText = it.uppercase() },
                    placeholder = { Text("Ej: PECHO", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AegisBronze,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = AegisBronze
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFF0F0E0E), // Fondo ultra oscuro
        topBar = {
            AegisTopBar(
                title = if (exerciseToEdit == null) "NEW EXERCISE" else "EDIT EXERCISE",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            // BOTÓN GUARDAR (Fijado abajo como en la imagen)
            Button(
                onClick = {
                    if (exerciseName.isNotBlank()) {
                        val updatedExercise = Exercise(
                            id = exerciseToEdit?.id ?: System.currentTimeMillis(),
                            name = exerciseName,
                            tags = selectedTags.toList(),
                            iconName = selectedIconName,
                            muscleGroup = selectedTags.firstOrNull() ?: "",
                            type = "",
                        )
                        routinesViewModel.saveOrUpdateExercise(updatedExercise)
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1C1A)),
                border = BorderStroke(1.dp, Color(0xFF33302E))
            ) {
                Text(
                    "CREATE",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 1. NOMBRE DEL EJERCICIO
            item {
                SectionLabel(stringResource(R.string.label_exercise_name))
                EditInput(
                    value = exerciseName,
                    onValueChange = { exerciseName = it },
                    placeholder = "Incline Bench Press"
                )
            }

            // 2. TAGS & CATEGORÍAS
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

            // 3. REFERENCIA VISUAL (ICONOS)
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
        }
    }
}

// --- COMPONENTES DE DISEÑO EXCLUSIVOS ---
@Composable
fun EditInput(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        placeholder = { Text(placeholder, color = Color.DarkGray) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF161513),
            unfocusedContainerColor = Color(0xFF161513),
            focusedBorderColor = Color(0xFF423E3A),
            unfocusedBorderColor = Color(0xFF2C2926),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )
}



@Composable
fun EditIconBox(icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(70.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF161513))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) AegisBronze else Color(0xFF2C2926),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) AegisBronze else Color.DarkGray,
            modifier = Modifier.size(30.dp)
        )
    }
}