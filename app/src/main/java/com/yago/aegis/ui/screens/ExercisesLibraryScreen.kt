package com.yago.aegis.ui.screens

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

    val filteredExercises by routinesViewModel.filteredLibraryExercises.collectAsState()
    val allExercises by routinesViewModel.allExercises.collectAsState()
    val availableTags by routinesViewModel.availableLibraryTags.collectAsState()
    val hasDefaultExercises by routinesViewModel.hasDefaultExercises.collectAsState()
    val isEmpty = allExercises.isEmpty()

    // ─── DIÁLOGO DINÁMICO cargar / eliminar ───
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
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // ─── BOTÓN CREAR ───
            Surface(
                onClick = onNavigateToCreate,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "CREAR NUEVO EJERCICIO",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ─── BOTÓN DINÁMICO: CARGAR o ELIMINAR ejercicios base ───
            val btnBorderColor = when {
                hasDefaultExercises -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                isEmpty             -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                else                -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
            }
            val btnIconColor = when {
                hasDefaultExercises -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                isEmpty             -> MaterialTheme.colorScheme.primary
                else                -> MaterialTheme.colorScheme.secondary
            }
            val btnTextColor = btnIconColor
            val btnIcon = if (hasDefaultExercises) Icons.Default.DeleteSweep else Icons.Default.Download
            val btnLabel = if (hasDefaultExercises) "ELIMINAR EJERCICIOS BASE" else "CARGAR EJERCICIOS BASE"

            Surface(
                onClick = { showLoadDefaultsDialog = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, btnBorderColor)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = btnIcon,
                        contentDescription = null,
                        tint = btnIconColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = btnLabel,
                        color = btnTextColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ─── BUSCADOR + TAGS (solo si hay ejercicios) ───
            if (!isEmpty) {
                OutlinedTextField(
                    value = routinesViewModel.librarySearchQuery,
                    onValueChange = { routinesViewModel.librarySearchQuery = it },
                    placeholder = {
                        Text(
                            "BUSCAR EN LA LIBRERÍA...",
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (routinesViewModel.librarySearchQuery.isNotEmpty()) {
                            IconButton(onClick = { routinesViewModel.librarySearchQuery = "" }) {
                                Icon(
                                    Icons.Default.Close, null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
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
                    textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
                )

                // ─── TAGS ───
                if (availableTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    TagFilterRow(
                        tags = availableTags,
                        selectedTag = routinesViewModel.selectedLibraryTag,
                        onTagSelected = { routinesViewModel.selectedLibraryTag = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // ─── LISTA o ESTADO VACÍO ───
            if (isEmpty) {
                EmptyLibraryState()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    // Contador al buscar o filtrar
                    val isFiltering = routinesViewModel.librarySearchQuery.isNotEmpty()
                            || routinesViewModel.selectedLibraryTag != "ALL"
                    if (isFiltering) {
                        item {
                            Text(
                                text = "${filteredExercises.size} EJERCICIOS ENCONTRADOS",
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
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

                    // Sin resultados
                    if (filteredExercises.isEmpty() && isFiltering) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.SearchOff, null,
                                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
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
