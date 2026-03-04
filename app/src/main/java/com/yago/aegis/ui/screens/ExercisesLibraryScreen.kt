package com.yago.aegis.ui.screens

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.yago.aegis.viewmodel.RoutinesViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.data.Exercise
import com.yago.aegis.ui.components.ExerciseLibraryItem
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import com.yago.aegis.ui.components.AegisAlertDialog
import com.yago.aegis.ui.components.ExerciseCard
import com.yago.aegis.ui.theme.AegisBronze

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesLibraryScreen(
    routinesViewModel: RoutinesViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    var exerciseToDelete by remember { mutableStateOf<Exercise?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val exercises by routinesViewModel.allExercises.collectAsState(
        initial = emptyList<Exercise>()
    )

    val filteredExercises = exercises.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }
    // Dentro de ExercisesLibraryScreen.kt
    if (exerciseToDelete != null) {
        AegisAlertDialog(
            title = "¿Eliminar ejercicio?",
            // ✅ Cambiamos 'message' por 'content'
            // ✅ Y envolvemos el texto en un Composable Text()
            content = {
                Text(
                    text = "¿Estás seguro de que quieres eliminar '${exerciseToDelete?.name}'? Esto lo borrará de la librería y de todas tus rutinas.",
                    color = Color.Gray
                )
            },
            onConfirm = {
                exerciseToDelete?.let { routinesViewModel.deleteExerciseFromLibrary(it) }
                exerciseToDelete = null
            },
            onDismiss = { exerciseToDelete = null },
            confirmText = "ELIMINAR",
            confirmButtonColor = AegisBronze
        )
    }
    Scaffold(
        containerColor = Color.Black,
        topBar = {
            // Título centrado con icono de lupa a la derecha
            CenterAlignedTopAppBar(
                title = { Text("EJERCICIOS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // BOTÓN CREAR NUEVO EJERCICIO (Estilo Banner)
            // ✅ Así debe quedar tu botón en ExercisesLibraryScreen
            Button(
                onClick = onNavigateToCreate, // 1. Llamamos a la función que recibimos por parámetro
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF252525))
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("CREAR NUEVO EJERCICIO", color = Color.Gray, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BUSCADOR (TextField)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar en la librería...", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF161616),
                    unfocusedContainerColor = Color(0xFF161616),
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // LISTA DE EJERCICIOS
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filteredExercises) { exercise ->
                    // ✅ Cambiamos 'ExerciseLibraryItem' por 'ExerciseCard'
                    ExerciseCard(
                        exercise = exercise,
                        onEdit = {
                            // Navegamos a la edición
                            onNavigateToEdit(exercise.name)
                        },
                        onDelete = {
                            exerciseToDelete = exercise
                        },
                        showReorderHandle = false,
                        isAddMode = false
                    )
                }
            }
        }
    }
}