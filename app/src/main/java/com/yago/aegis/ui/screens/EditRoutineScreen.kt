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
import com.yago.aegis.ui.components.AegisTopBar
import com.yago.aegis.ui.components.ExerciseCard
import com.yago.aegis.ui.theme.AegisBronze
import com.yago.aegis.viewmodel.RoutinesViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyColumnState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRoutineScreen(
    routineId: Int,
    routinesViewModel: RoutinesViewModel,
    onNavigateBack: () -> Unit,
    navController: NavHostController,
) {
    // 1. CARGA DE DATOS: Al entrar, pasamos los ejercicios de la rutina al ViewModel
    LaunchedEffect(routineId) {
        if (routinesViewModel.tempExercises.isEmpty()) {
            val routine = routinesViewModel.routines.find { it.id == routineId }
            routine?.let {
                routinesViewModel.setTempExercises(it.exercises)
            }
        }
    }

    // Estado local solo para el nombre (el ViewModel maneja la lista de ejercicios)
    val originalRoutine = routinesViewModel.routines.find { it.id == routineId }
    var tempName by remember { mutableStateOf(originalRoutine?.name ?: "") }

    Scaffold(
        topBar = {
            AegisTopBar(
                title = tempName,
                navigationIcon = {
                    IconButton(onClick = {
                        routinesViewModel.clearTempExercises()
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.content_desc_back),
                            tint = Color.White
                        )
                    }
                }
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.label_routine_name),
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = tempName,
                onValueChange = { tempName = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontWeight = FontWeight.Bold),
                trailingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AegisBronze,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedContainerColor = Color(0xFF161616),
                    unfocusedContainerColor = Color(0xFF161616)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.label_exercises),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                // ✅ Leemos el tamaño directamente del ViewModel
                Text(
                    "${routinesViewModel.tempExercises.size} ${stringResource(R.string.label_added_suffix)}",
                    color = AegisBronze,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val lazyListState = rememberLazyListState()
            val reorderableLazyColumnState = rememberReorderableLazyColumnState(
                lazyListState = lazyListState
            ) { from, to ->
                routinesViewModel.moveExercise(from.index, to.index)
            }
            LazyColumn(
                state = lazyListState, // Conectamos el estado
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = routinesViewModel.tempExercises,
                    key = { it.name + it.hashCode() } // ✅ OBLIGATORIO: Cada item necesita una llave única
                ) { exercise ->
                    // 3. Envolvemos cada tarjeta en un 'ReorderableItem'
                    ReorderableItem(
                        reorderableLazyColumnState,
                        key = exercise.name + exercise.hashCode()
                    ) { isDragging ->
                        // Efecto de elevación mientras arrastramos
                        val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)

                        Surface(
                            shadowElevation = elevation,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            // Dentro del ReorderableItem en rutinas
                            ExerciseCard(
                                exercise = exercise,
                                onDelete = { routinesViewModel.tempExercises.remove(exercise) },
                                showReorderHandle = true,
                                modifier = Modifier.draggableHandle()
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { navController.navigate("add_exercise") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.DarkGray),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AegisBronze)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.btn_add_exercise),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp)) // Espacio extra al final
                }
            }

            // BOTÓN GUARDAR
            Button(
                onClick = {
                    // ✅ Aquí es donde la magia ocurre: pasamos los temporales a la rutina real
                    routinesViewModel.updateRoutineFull(
                        id = routineId,
                        newName = tempName,
                        newExercises = routinesViewModel.tempExercises.toList()
                    )
                    // Limpiamos después de guardar para dejar todo ordenado
                    routinesViewModel.clearTempExercises()
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(vertical = 12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AegisBronze),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.btn_save_routine), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}