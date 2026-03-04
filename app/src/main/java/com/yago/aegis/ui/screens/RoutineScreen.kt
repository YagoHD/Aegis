package com.yago.aegis.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.data.Routine
import com.yago.aegis.R
import com.yago.aegis.data.globalExerciseIcons
import com.yago.aegis.ui.components.AegisAlertDialog
import com.yago.aegis.ui.components.AegisTopBar
import com.yago.aegis.ui.components.RoutineCard
import com.yago.aegis.ui.theme.AegisBronze
import com.yago.aegis.ui.theme.BackgroundBlackGrey
import com.yago.aegis.viewmodel.RoutinesViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RoutineScreen(
    routinesViewModel: RoutinesViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToEditRoutine: (Int) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var textState by remember { mutableStateOf("") }
    // Estado para el icono seleccionado en el diálogo
    var selectedIconName by remember { mutableStateOf("dumbbell") }

    var routineToEdit by remember { mutableStateOf<Routine?>(null) }
    var routineToDelete by remember { mutableStateOf<Routine?>(null) }

    // --- DIÁLOGO DE CREAR ---
    if (showDialog) {
        AegisAlertDialog(
            title = "NUEVA RUTINA",
            confirmText = "GUARDAR",
            dismissText = "CANCELAR",
            onDismiss = { showDialog = false },
            onConfirm = {
                if (textState.isNotBlank()) {
                    // ✅ Pasamos el nombre Y el icono seleccionado
                    routinesViewModel.addRoutine(textState, selectedIconName)
                    showDialog = false
                }
            },
            content = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = textState,
                        onValueChange = { textState = it },
                        label = { Text("Nombre de la rutina", color = Color.Gray) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AegisBronze,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Selecciona un icono", color = Color.Gray, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(8.dp))

                    // ✅ Selector de iconos dentro del diálogo
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        globalExerciseIcons.forEach { (name, icon) ->
                            EditIconBox(
                                icon = icon,
                                isSelected = selectedIconName == name,
                                onClick = { selectedIconName = name }
                            )
                        }
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = { AegisTopBar(title = stringResource(R.string.routine_title)) },
        containerColor = BackgroundBlackGrey
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp) // Un poco más de espacio
            ) {
                items(routinesViewModel.routines) { routine ->
                    // Asegúrate de que RoutineCard use getExerciseIcon(routine.iconName)
                    RoutineCard(
                        routine = routine,
                        onEdit = { onNavigateToEditRoutine(routine.id) },
                        onDelete = { routineToDelete = routine }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    textState = ""
                    selectedIconName = "dumbbell" // Reset al icono por defecto
                    showDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(1.dp, AegisBronze, RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = AegisBronze)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.btn_create_routine).uppercase(),
                    color = AegisBronze,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    if (routineToDelete != null) {
        AegisAlertDialog(
            title = stringResource(R.string.dialog_delete_title),
            content = {
                Text(
                    text = stringResource(R.string.dialog_delete_confirm, routineToDelete?.name ?: ""),
                    color = Color.Gray
                )
            },
            onConfirm = {
                routineToDelete?.let { routinesViewModel.removeRoutine(it) }
                routineToDelete = null
            },
            onDismiss = { routineToDelete = null },
            confirmButtonColor = AegisBronze
        )
    }
}