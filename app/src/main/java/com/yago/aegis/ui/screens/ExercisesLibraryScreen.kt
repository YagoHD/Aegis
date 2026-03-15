package com.yago.aegis.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.R
import com.yago.aegis.data.Exercise
import com.yago.aegis.ui.components.AegisAlertDialog
import com.yago.aegis.ui.components.AegisTopBar
import com.yago.aegis.ui.components.ExerciseCard
import com.yago.aegis.ui.components.TagFilterRow
import com.yago.aegis.viewmodel.RoutinesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesLibraryScreen(
    routinesViewModel: RoutinesViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    var exerciseToDelete by remember { mutableStateOf<Exercise?>(null) }
    var showLoadDefaultsDialog by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }

    val filteredExercises by routinesViewModel.filteredLibraryExercises.collectAsState()
    val allExercises by routinesViewModel.allExercises.collectAsState()
    val availableTags by routinesViewModel.availableLibraryTags.collectAsState()
    val hasDefaultExercises by routinesViewModel.hasDefaultExercises.collectAsState()
    val isEmpty = allExercises.isEmpty()

    val isFiltering = routinesViewModel.librarySearchQuery.isNotEmpty()
            || routinesViewModel.selectedLibraryTag != "ALL"

    // ─── DIÁLOGO cargar / eliminar ───
    if (showLoadDefaultsDialog) {
        AegisAlertDialog(
            title = if (hasDefaultExercises) "ELIMINAR EJERCICIOS BASE" else "CARGAR EJERCICIOS BASE",
            confirmText = if (hasDefaultExercises) "ELIMINAR" else "CARGAR",
            dismissText = "CANCELAR",
            confirmButtonColor = if (hasDefaultExercises) MaterialTheme.colorScheme.error
                                 else MaterialTheme.colorScheme.primary,
            onDismiss = { showLoadDefaultsDialog = false },
            onConfirm = {
                if (hasDefaultExercises) routinesViewModel.deleteDefaultExercises()
                else routinesViewModel.loadDefaultExercises()
                showLoadDefaultsDialog = false
            }
        ) {
            Text(
                text = if (hasDefaultExercises)
                    "Se eliminarán todos los ejercicios base de la librería. Tus ejercicios personalizados no se verán afectados.\n\nSi algún ejercicio base está en una rutina activa, también se eliminará de ella."
                else
                    "Se añadirán más de 60 ejercicios organizados por grupo muscular (Pecho, Espalda, Hombros, Bíceps, Tríceps y Piernas).\n\nNo se eliminará ningún ejercicio que ya tengas creado.",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }

    // ─── DIÁLOGO eliminar ejercicio individual ───
    if (exerciseToDelete != null) {
        AegisAlertDialog(
            title = "ELIMINAR EJERCICIO",
            onConfirm = {
                exerciseToDelete?.let { routinesViewModel.deleteExerciseFromLibrary(it) }
                exerciseToDelete = null
            },
            onDismiss = { exerciseToDelete = null },
            confirmText = "ELIMINAR",
            confirmButtonColor = Color(0xFFB3261E)
        ) {
            Text(
                text = "¿Estás seguro de que quieres eliminar '${exerciseToDelete?.name}'? Esta acción es irreversible y afectará a tus rutinas.",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AegisTopBar(title = stringResource(R.string.exercices_title)) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ─── FILA DE BOTONES (mismo tamaño, misma línea) ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Botón crear
                Surface(
                    onClick = onNavigateToCreate,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Default.Add, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "CREAR",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Botón cargar/eliminar base
                val baseColor = if (hasDefaultExercises)
                    MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                else if (isEmpty) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.secondary

                Surface(
                    onClick = { showLoadDefaultsDialog = true },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Transparent,
                    border = BorderStroke(
                        1.dp,
                        if (hasDefaultExercises) MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                        else if (isEmpty) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            if (hasDefaultExercises) Icons.Default.DeleteSweep else Icons.Default.Download,
                            null,
                            tint = baseColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (hasDefaultExercises) "ELIMINAR BASE" else "CARGAR BASE",
                            color = baseColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ─── BUSCADOR + BOTÓN FILTRO ───
            if (!isEmpty) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = routinesViewModel.librarySearchQuery,
                        onValueChange = { routinesViewModel.librarySearchQuery = it },
                        placeholder = {
                            Text(
                                "BUSCAR...",
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier.weight(1f),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search, null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (routinesViewModel.librarySearchQuery.isNotEmpty()) {
                                IconButton(onClick = { routinesViewModel.librarySearchQuery = "" }) {
                                    Icon(Icons.Default.Close, null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                        ),
                        textStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    )

                    // Botón filtro — se resalta si hay un tag activo
                    val filterActive = routinesViewModel.selectedLibraryTag != "ALL"
                    Surface(
                        onClick = { showFilters = !showFilters },
                        shape = RoundedCornerShape(8.dp),
                        color = if (filterActive)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (filterActive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                if (filterActive) Icons.Default.FilterAlt else Icons.Default.FilterList,
                                null,
                                tint = if (filterActive) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // ─── TAGS DESPLEGABLES ───
                AnimatedVisibility(
                    visible = showFilters && availableTags.isNotEmpty(),
                    enter = expandVertically(animationSpec = tween(200)),
                    exit = shrinkVertically(animationSpec = tween(200))
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TagFilterRow(
                                tags = availableTags,
                                selectedTag = routinesViewModel.selectedLibraryTag,
                                onTagSelected = { routinesViewModel.selectedLibraryTag = it },
                                modifier = Modifier.weight(1f)
                            )
                            // Botón limpiar filtro
                            if (routinesViewModel.selectedLibraryTag != "ALL") {
                                TextButton(
                                    onClick = { routinesViewModel.selectedLibraryTag = "ALL" },
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text(
                                        "LIMPIAR",
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // ─── LISTA o ESTADO VACÍO ───
            if (isEmpty) {
                EmptyLibraryState()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    if (isFiltering) {
                        item {
                            Text(
                                text = "${filteredExercises.size} EJERCICIOS",
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    items(filteredExercises) { exercise ->
                        ExerciseCard(
                            exercise = exercise,
                            onEdit = { onNavigateToEdit(exercise.name) },
                            onDelete = { exerciseToDelete = exercise },
                            showReorderHandle = false,
                            isAddMode = false
                        )
                    }

                    if (filteredExercises.isEmpty() && isFiltering) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.SearchOff, null,
                                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    "SIN RESULTADOS",
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyLibraryState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "LIBRERÍA VACÍA",
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Carga los ejercicios base o crea\nlos tuyos desde cero.",
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
            fontSize = 13.sp,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}
