package com.yago.aegis.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.yago.aegis.data.Exercise
import com.yago.aegis.ui.theme.AegisBronze
import com.yago.aegis.viewmodel.RoutinesViewModel
import com.yago.aegis.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRoutineScreen(
    routineId: Int,
    routinesViewModel: RoutinesViewModel,
    onNavigateBack: () -> Unit
) {
    val originalRoutine = routinesViewModel.routines.find { it.id == routineId }
    var tempName by remember { mutableStateOf(originalRoutine?.name ?: "") }
    val tempExercises = remember {
        mutableStateListOf<Exercise>().apply {
            addAll(originalRoutine?.exercises ?: emptyList())
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        tempName.uppercase(),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        // ✅ Usamos stringResource para la descripción de accesibilidad
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.content_desc_back), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
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

            // ✅ Texto de etiqueta desde XML
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
                trailingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp)) },
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
                // ✅ Texto desde XML
                Text(stringResource(R.string.label_exercises), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                // ✅ Combinamos el número con el texto del XML
                Text("${tempExercises.size} ${stringResource(R.string.label_added_suffix)}", color = AegisBronze, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(tempExercises) { index, exercise ->
                    ExerciseEditCard(
                        exercise = exercise,
                        onDelete = { tempExercises.removeAt(index) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { /* Próximo paso: Abrir selector */ },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.DarkGray),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AegisBronze)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        // ✅ Texto desde XML
                        Text(stringResource(R.string.btn_add_exercise), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Button(
                onClick = {
                    routinesViewModel.updateRoutineFull(routineId, tempName, tempExercises)
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
                // ✅ Texto desde XML
                Text(stringResource(R.string.btn_save_routine), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun ExerciseEditCard(exercise: Exercise, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161616), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono cuadrado oscuro
        Box(
            modifier = Modifier.size(40.dp).background(Color(0xFF0A0A0A), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = AegisBronze, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(exercise.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("${exercise.type} • ${exercise.muscleGroup}", color = Color.Gray, fontSize = 12.sp)
        }

        // Acciones: Borrar y Reordenar
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.DarkGray, modifier = Modifier.size(20.dp))
        }
        Icon(Icons.Default.DragHandle, contentDescription = "Reorder", tint = Color.DarkGray, modifier = Modifier.size(20.dp))
    }
}