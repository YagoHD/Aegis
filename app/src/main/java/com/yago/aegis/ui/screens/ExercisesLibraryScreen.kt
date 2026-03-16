package com.yago.aegis.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
    var baseSectionExpanded by remember { mutableStateOf(true) }

    val userExercises by routinesViewModel.filteredUserExercises.collectAsState()
    val baseExercises by routinesViewModel.filteredBaseExercises.collectAsState()
    val availableTags by routinesViewModel.availableLibraryTags.collectAsState()
    val hasDefaultExercises by routinesViewModel.hasDefaultExercises.collectAsState()

    val isFiltering = routinesViewModel.librarySearchQuery.isNotEmpty()
            || routinesViewModel.selectedLibraryTag != "ALL"

    // ─── DIÁLOGO cargar / eliminar base ───
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
                    "Se eliminarán todos los ejercicios de la sección BASE. Tus ejercicios personalizados no se verán afectados en ningún caso."
                else
                    "Se añadirán más de 60 ejercicios organizados por grupo muscular. Aparecerán en una sección separada y no afectarán a tus ejercicios.",
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
                text = "¿Estás seguro de que quieres eliminar '${exerciseToDelete?.name}'? Esta acción es irreversible.",
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

            // ─── FILA DE BOTONES ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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
                        Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("CREAR", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.sp)
                    }
                }

                val baseColor = if (hasDefaultExercises) MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                else MaterialTheme.colorScheme.secondary
                Surface(
                    onClick = { showLoadDefaultsDialog = true },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp,
                        if (hasDefaultExercises) MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            if (hasDefaultExercises) Icons.Default.DeleteSweep else Icons.Default.Download,
                            null, tint = baseColor, modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (hasDefaultExercises) "ELIMINAR BASE" else "CARGAR BASE",
                            color = baseColor, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ─── BUSCADOR + BOTÓN FILTRO ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = routinesViewModel.librarySearchQuery,
                    onValueChange = { routinesViewModel.librarySearchQuery = it },
                    placeholder = {
                        Text("BUSCAR...", color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                            fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    },
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (routinesViewModel.librarySearchQuery.isNotEmpty()) {
                            IconButton(onClick = { routinesViewModel.librarySearchQuery = "" }) {
                                Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
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

                val filterActive = routinesViewModel.selectedLibraryTag != "ALL"
                Surface(
                    onClick = { showFilters = !showFilters },
                    shape = RoundedCornerShape(8.dp),
                    color = if (filterActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp,
                        if (filterActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            if (filterActive) Icons.Default.FilterAlt else Icons.Default.FilterList,
                            null,
                            tint = if (filterActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
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
                        if (routinesViewModel.selectedLibraryTag != "ALL") {
                            TextButton(
                                onClick = { routinesViewModel.selectedLibraryTag = "ALL" },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("LIMPIAR", color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ─── LISTA CON SECCIONES ───
            val totalCount = userExercises.size + baseExercises.size

            if (totalCount == 0 && !isFiltering) {
                EmptyLibraryState()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {

                    // ── SECCIÓN: MIS EJERCICIOS ──
                    if (userExercises.isNotEmpty() || !isFiltering) {
                        item {
                            SectionHeader(
                                title = "MIS EJERCICIOS",
                                count = userExercises.size,
                                isExpandable = false
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    if (userExercises.isEmpty() && !isFiltering) {
                        item {
                            Text(
                                text = "Aún no tienes ejercicios propios. Pulsa CREAR para añadir el primero.",
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    } else {
                        items(userExercises, key = { it.id }) { exercise ->
                            ExerciseCard(
                                exercise = exercise,
                                onEdit = { onNavigateToEdit(exercise.name) },
                                onDelete = { exerciseToDelete = exercise },
                                showReorderHandle = false,
                                isAddMode = false
                            )
                        }
                    }

                    // ── SEPARADOR ──
                    if (hasDefaultExercises) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // ── SECCIÓN: EJERCICIOS BASE (plegable) ──
                        item {
                            SectionHeader(
                                title = "EJERCICIOS BASE",
                                count = baseExercises.size,
                                isExpandable = true,
                                expanded = baseSectionExpanded,
                                onToggle = { baseSectionExpanded = !baseSectionExpanded }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (baseSectionExpanded) {
                            items(baseExercises, key = { it.id }) { exercise ->
                                ExerciseCard(
                                    exercise = exercise,
                                    onEdit = { onNavigateToEdit(exercise.name) },
                                    onDelete = { exerciseToDelete = exercise },
                                    showReorderHandle = false,
                                    isAddMode = false
                                )
                            }
                        }
                    }

                    // Sin resultados al buscar
                    if (totalCount == 0 && isFiltering) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.SearchOff, null,
                                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                    modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("SIN RESULTADOS",
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                    fontSize = 11.sp, fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    isExpandable: Boolean,
    expanded: Boolean = true,
    onToggle: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isExpandable) Modifier.clickable { onToggle() } else Modifier)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$count",
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        if (isExpandable) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(16.dp)
            )
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
        Text("LIBRERÍA VACÍA", color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
            fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Carga los ejercicios base o crea\nlos tuyos desde cero.",
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
            fontSize = 13.sp, fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center, lineHeight = 20.sp)
    }
}
