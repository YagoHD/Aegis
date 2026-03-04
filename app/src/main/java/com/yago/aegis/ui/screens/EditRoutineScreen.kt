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
import com.yago.aegis.ui.theme.AegisBronze
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

    // Estados locales para la edición
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
                title = tempName,
                navigationIcon = {
                    IconButton(onClick = {
                        routinesViewModel.clearTempExercises()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                }
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        // Usamos LazyColumn para que toda la pantalla tenga scroll si hay muchos iconos o ejercicios
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // SECCIÓN: NOMBRE
            item {
                Column {
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
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF161616),
                            unfocusedContainerColor = Color(0xFF161616),
                            focusedBorderColor = AegisBronze
                        )
                    )
                }
            }

            // SECCIÓN: SELECCIÓN DE ICONO (Nueva)
            item {
                Column {
                    Text(
                        text = stringResource(R.string.select_icon),
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        maxItemsInEachRow = 5,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        globalExerciseIcons.forEach { (name, icon) ->
                            // Reutilizamos tu componente EditIconBox
                            EditIconBox(
                                icon = icon,
                                isSelected = selectedIconName == name,
                                onClick = { selectedIconName = name }
                            )
                        }
                    }
                }
            }

            // SECCIÓN: CABECERA EJERCICIOS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.label_exercises), color = Color.White, fontWeight = FontWeight.Bold)
                    Text(
                        "${routinesViewModel.tempExercises.size} ${stringResource(R.string.label_added_suffix)}",
                        color = AegisBronze,
                        fontSize = 12.sp
                    )
                }
            }

            // LISTA DE EJERCICIOS REORDENABLES
            items(
                items = routinesViewModel.tempExercises,
                key = { it.id } // Usar el ID es más seguro que el hashCode
            ) { exercise ->
                // Aquí iría tu lógica de ReorderableItem que ya tienes...
                // (Omitido por brevedad, pero mantenlo igual que en tu código)
                ExerciseCard(
                    exercise = exercise,
                    onDelete = { routinesViewModel.tempExercises.remove(exercise) }
                )
            }

            // BOTÓN AÑADIR EJERCICIO
            item {
                OutlinedButton(
                    onClick = { navController.navigate("add_exercise") },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AegisBronze)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.btn_add_exercise), fontWeight = FontWeight.Bold)
                }
            }

            // BOTÓN GUARDAR
            item {
                Button(
                    onClick = {
                        // IMPORTANTE: Actualizamos también el iconName
                        routinesViewModel.updateRoutineFull(
                            id = routineId,
                            newName = tempName,
                            newExercises = routinesViewModel.tempExercises.toList(),
                            newIconName = selectedIconName // Debes añadir este parámetro a tu función
                        )
                        routinesViewModel.clearTempExercises()
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AegisBronze),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.btn_save_routine), color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}