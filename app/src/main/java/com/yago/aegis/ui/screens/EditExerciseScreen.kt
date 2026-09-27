package com.yago.aegis.ui.screens
import com.yago.aegis.ui.theme.Spacing
import com.yago.aegis.ui.theme.Radius

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
import com.yago.aegis.ui.globalExerciseIcons
import com.yago.aegis.data.resolveLoadType
import com.yago.aegis.ui.components.AegisAlertDialog
import com.yago.aegis.ui.components.AegisTagManager
import com.yago.aegis.ui.components.AegisTopBar
import com.yago.aegis.ui.components.LoadTypeSelector
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
    var notes by remember { mutableStateOf(exerciseToEdit?.notes ?: "") }
    var loadType by remember { mutableStateOf(exerciseToEdit?.resolveLoadType() ?: com.yago.aegis.data.LoadType.NORMAL) }

    val savedGlobalTags by routinesViewModel.globalTags.collectAsState()

    Scaffold(
        // ✅ Usamos el fondo del sistema (050505)
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AegisTopBar(
                title = if (exerciseToEdit == null) stringResource(R.string.title_new_exercise) else stringResource(R.string.title_edit_exercise),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.content_desc_back),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            item { Spacer(modifier = Modifier.height(Spacing.sm)) }

            // 1. NOMBRE DEL EJERCICIO
            item {
                SectionLabel(stringResource(R.string.label_exercise_name))
                EditInput(
                    value = exerciseName,
                    onValueChange = { exerciseName = it },
                    placeholder = stringResource(R.string.exercise_name_placeholder)
                )
            }

            // 1b. TIPO DE CARGA: Normal / Peso corporal / Asistido
            item {
                LoadTypeSelector(
                    selected = loadType,
                    onSelect = { loadType = it },
                    modifier = Modifier.padding(vertical = Spacing.xs)
                )
            }

            // 1c. NOTAS DE FORMA
            item {
                SectionLabel(stringResource(R.string.form_notes_section_title))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.sm),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.form_notes_placeholder),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
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
                    allowEdit = false
                )
            }

            // 3. SELECCIÓN DE ICONO
            item {
                SectionLabel(stringResource(R.string.select_icon))
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.md),
                    maxItemsInEachRow = 4,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
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

            // BOTÓN CREAR / GUARDAR dentro del scroll (evita el hueco negro sobre el teclado
            // que dejaba el bottomBar fijo al abrirse el teclado).
            item {
                Button(
                    onClick = {
                        if (exerciseName.isNotBlank()) {
                            // Anti-trampas: si era un ejercicio BASE, conserva su BASE_TAG al editar.
                            val wasBase = exerciseToEdit?.tags?.contains(DefaultExercises.BASE_TAG) == true
                            val finalTags = if (wasBase) selectedTags.toList() + DefaultExercises.BASE_TAG
                                            else selectedTags.toList()
                            val updatedExercise = Exercise(
                                id = exerciseToEdit?.id ?: System.currentTimeMillis(),
                                name = exerciseName,
                                tags = finalTags,
                                iconName = selectedIconName,
                                muscleGroup = if (wasBase) (exerciseToEdit?.muscleGroup ?: "") else (selectedTags.firstOrNull() ?: ""),
                                type = "",
                                notes = notes.trim(),
                                isBodyweight = loadType == com.yago.aegis.data.LoadType.BODYWEIGHT,
                                loadType = loadType.name,
                                lastPerformance = exerciseToEdit?.lastPerformance ?: "",
                                oneRepMax = exerciseToEdit?.oneRepMax ?: 0.0,
                                bestSet = exerciseToEdit?.bestSet,
                                history = exerciseToEdit?.history ?: emptyList(),
                                muscleContributions = exerciseToEdit?.muscleContributions ?: emptyList()
                            )
                            routinesViewModel.saveOrUpdateExercise(updatedExercise)
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.md)
                        .height(56.dp),
                    shape = RoundedCornerShape(Radius.md),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary, // AegisBronze
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = if (exerciseToEdit == null) stringResource(R.string.btn_create) else stringResource(R.string.btn_edit),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(Spacing.xl)) }
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
            .padding(top = Spacing.sm),
        placeholder = {
            Text(
                text = placeholder.uppercase(),
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
        },
        shape = RoundedCornerShape(Radius.md), // Consistencia con el resto de la app
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
            .clip(RoundedCornerShape(Radius.md))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
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
            // El icono se apaga cuando no está seleccionado para dar foco al activo
            tint = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
            modifier = Modifier.size(26.dp)
        )
    }
}