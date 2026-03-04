package com.yago.aegis.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.R
import com.yago.aegis.data.Exercise
import com.yago.aegis.ui.components.AegisAlertDialog
import com.yago.aegis.ui.components.AegisTopBar
import com.yago.aegis.ui.components.ExerciseCard
import com.yago.aegis.ui.theme.AegisBronze
import com.yago.aegis.ui.theme.BackgroundBlackGrey
import com.yago.aegis.ui.theme.AegisCard
import com.yago.aegis.ui.theme.AegisWhite
import com.yago.aegis.viewmodel.RoutinesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesLibraryScreen(
    routinesViewModel: RoutinesViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    var exerciseToDelete by remember { mutableStateOf<Exercise?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val exercises by routinesViewModel.allExercises.collectAsState(initial = emptyList())

    val filteredExercises = exercises.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    // Diálogo de eliminación
    if (exerciseToDelete != null) {
        AegisAlertDialog(
            title = "¿Eliminar ejercicio?",
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
            confirmButtonColor = Color(0xFF800000) // Rojo Burdeos para coherencia de peligro
        )
    }

    Scaffold(
        // ✅ Usamos el color de fondo definido en tu Theme
        containerColor = BackgroundBlackGrey,
        topBar = {
            // ✅ Usamos el componente que definimos en el Canvas
            AegisTopBar(
                title = stringResource(R.string.exercices_title)
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // BOTÓN CREAR NUEVO EJERCICIO
            Button(
                onClick = onNavigateToCreate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp),
                shape = RoundedCornerShape(12.dp),
                // ✅ Usamos AegisCard o un color oscuro coherente
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = null, tint = AegisBronze)
                Spacer(modifier = Modifier.width(12.dp))
                Text("CREAR NUEVO EJERCICIO", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BUSCADOR
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar en la librería...", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AegisCard,
                    unfocusedContainerColor = AegisCard,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = AegisBronze,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = AegisBronze
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // LISTA DE EJERCICIOS
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(filteredExercises) { exercise ->
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
    }
}