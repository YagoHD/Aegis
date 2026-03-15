package com.yago.aegis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.yago.aegis.data.DefaultExercises
import com.yago.aegis.data.Exercise
import com.yago.aegis.data.globalExerciseIcons
import com.yago.aegis.ui.components.AegisAlertDialog
import com.yago.aegis.ui.components.AegisTagManager
import com.yago.aegis.ui.components.AegisTopBar
import com.yago.aegis.viewmodel.RoutinesViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditExerciseScreen(
    routinesViewModel: RoutinesViewModel,
    exerciseToEdit: Exercise? = null,
    onNavigateBack: () -> Unit
) {
    // 1. ESTADOS (Se mantienen igual, funcionan bien)
    var exerciseName by remember { mutableStateOf(exerciseToEdit?.name ?: "") }
    val selectedTags = remember { mutableStateListOf<String>().apply {
        exerciseToEdit?.tags?.let { tags -> addAll(tags.filter { it != DefaultExercises.BASE_TAG }) }
    } }
    var selectedIconName by remember { mutableStateOf(exerciseToEdit?.iconName ?: "dumbbell") }

    val savedGlobalTags by routinesViewModel.globalTags.collectAsState()
    var showTagDialog by remember { mutableStateOf(false) }
    var newTagText by remember { mutableStateOf("") }

    // 2. DIÁLOGO DE TAGS (Integrado en el sistema de diseño)
    if (showTagDialog) {
        AegisAlertDialog(
            title = "NUEVO TAG",
            confirmText = "AÑADIR",
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
            OutlinedTextField(
                value = newTagText,
                onValueChange = { newTagText = it },
                placeholder = { Text("EJ: PECHO", color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onBackground),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }

    Scaffold(
        // ✅ Usamos el fondo del sistema (050505)
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AegisTopBar(
                title = if (exerciseToEdit == null) "NUEVO EJERCICIO" else "EDITAR EJERCICIO",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        },
        bottomBar = {
            // ✅ Botón "CREATE" en Bronce brillante
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
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary, // AegisBronze
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = if (exerciseToEdit == null) "CREAR" else "EDITAR",
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 1. NOMBRE DEL EJERCICIO
            item {
                SectionLabel(stringResource(R.string.label_exercise_name).uppercase())
                EditInput(
                    value = exerciseName,
                    onValueChange = { exerciseName = it },
                    placeholder = "EJ: PRESS BANCA INCLINADO"
                )
            }

            // 2. TAGS & CATEGORÍAS
            item {
                // AegisTagManager debería usar MaterialTheme.colorScheme.primary para los seleccionados
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

            // 3. SELECCIÓN DE ICONO
            item {
                SectionLabel(stringResource(R.string.select_icon).uppercase())
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    maxItemsInEachRow = 4,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    globalExerciseIcons.forEach { (name, icon) ->
                        // ✅ Reutilizamos el selector modular
                        AegisIconSelector(
                            icon = icon,
                            isSelected = selectedIconName == name,
                            onClick = { selectedIconName = name }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

// --- COMPONENTES DE DISEÑO EXCLUSIVOS ---
@Composable
fun EditInput(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        placeholder = {
            Text(
                text = placeholder.uppercase(),
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
        },
        shape = RoundedCornerShape(8.dp), // Consistencia con el resto de la app
        textStyle = androidx.compose.ui.text.TextStyle(
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        ),
        colors = OutlinedTextFieldDefaults.colors(
            // Usamos surfaceVariant (0E0E0E) para que destaque sutilmente sobre el fondo
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            // Bordes refinados
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun EditIconBox(icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(64.dp) // Un pelín más compacto para que quepan mejor en filas de 4
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
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
            // El icono se apaga cuando no está seleccionado para dar foco al activo
            tint = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
            modifier = Modifier.size(26.dp)
        )
    }
}