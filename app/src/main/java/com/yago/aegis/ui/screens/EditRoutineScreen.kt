package com.yago.aegis.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.yago.aegis.R
import com.yago.aegis.data.globalExerciseIcons
import com.yago.aegis.ui.components.AegisTopBar
import com.yago.aegis.ui.components.ExerciseCard
import com.yago.aegis.viewmodel.RoutinesViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyColumnState

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditRoutineScreen(
    routineId: Int,
    routinesViewModel: RoutinesViewModel,
    onNavigateBack: () -> Unit,
    navController: NavHostController,
) {
    val originalRoutine = remember(routineId) {
        routinesViewModel.routines.find { it.id == routineId }
    }

    // --- LÓGICA DE REORDENAMIENTO ---
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyColumnState(lazyListState) { from, to ->
        routinesViewModel.tempExercises.apply {
            // Restamos 4 porque hay 4 items antes de la lista (Spacer, Nombre, Iconos, Cabecera)
            add(to.index - 4, removeAt(from.index - 4))
        }
    }

    var tempName by remember { mutableStateOf(originalRoutine?.name ?: "") }
    var selectedIconName by remember { mutableStateOf(originalRoutine?.iconName ?: "dumbbell") }

    LaunchedEffect(routineId) {
        if (routinesViewModel.tempExercises.isEmpty()) {
            originalRoutine?.let {
                routinesViewModel.setTempExercises(it.exercises)
            }
        }
    }

    Scaffold(
        topBar = {
            AegisTopBar(
                title = tempName.uppercase(),
                navigationIcon = {
                    IconButton(onClick = {
                        routinesViewModel.clearTempExercises()
                        onNavigateBack()
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            state = lazyListState, // Esencial para el reordenamiento
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ITEM 0
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // ITEM 1: NOMBRE
            item {
                Column {
                    Text(
                        text = stringResource(R.string.label_routine_name).uppercase(),
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
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

            // ITEM 2: ICONO
            item {
                Column {
                    Text(
                        text = stringResource(R.string.select_icon).uppercase(),
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
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

            // ITEM 3: CABECERA EJERCICIOS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = stringResource(R.string.label_exercises).uppercase(),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${routinesViewModel.tempExercises.size} ${stringResource(R.string.label_added_suffix)}".uppercase(),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // LISTA REORDENABLE (A partir del ITEM 4)
            items(
                items = routinesViewModel.tempExercises,
                key = { it.id }
            ) { exercise ->
                ReorderableItem(reorderableState, key = exercise.id) { isDragging ->
                    // 1. Efecto visual de "elevación" al arrastrar
                    val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)

                    Surface(
                        shadowElevation = elevation,
                        shape = RoundedCornerShape(8.dp), // Ajustado a 8.dp para consistencia Obsidiana
                        color = Color.Transparent
                    ) {
                        ExerciseCard(
                            exercise = exercise,
                            onDelete = { routinesViewModel.tempExercises.remove(exercise) },
                            showReorderHandle = true,
                            // ✅ CORRECTO: draggableHandle() se usa sin parámetros
                            dragHandleModifier = Modifier.draggableHandle()
                        )
                    }
                }
            }

            // BOTÓN AÑADIR
            item {
                Surface(
                    onClick = { navController.navigate("add_exercise") },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.btn_add_exercise).uppercase(),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // BOTÓN GUARDAR
            item {
                Button(
                    onClick = {
                        routinesViewModel.updateRoutineFull(
                            id = routineId,
                            newName = tempName,
                            newExercises = routinesViewModel.tempExercises.toList(),
                            newIconName = selectedIconName
                        )
                        routinesViewModel.clearTempExercises()
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.btn_save_routine).uppercase(),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}