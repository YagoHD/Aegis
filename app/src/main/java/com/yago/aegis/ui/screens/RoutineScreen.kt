package com.yago.aegis.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.yago.aegis.viewmodel.ProfileViewModel
import com.yago.aegis.R
import com.yago.aegis.ui.components.RoutineCard
import com.yago.aegis.ui.theme.AegisBronze
import com.yago.aegis.viewmodel.RoutinesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineScreen(
    routinesViewModel: RoutinesViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToEditRoutine: (Int) -> Unit
) {
    // ESTADOS PARA EL DIÁLOGO
    var showDialog by remember { mutableStateOf(false) }
    var textState by remember { mutableStateOf("") }
    var routineToEdit by remember { mutableStateOf<Routine?>(null) }

    // --- DIÁLOGO DE CREAR / EDITAR ---
    if (showDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = Color(0xFF161616),
            title = {
                Text(
                    text = if (routineToEdit == null) "NUEVA RUTINA" else "EDITAR RUTINA",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    value = textState,
                    onValueChange = { textState = it },
                    label = { Text("Nombre de la rutina", color = Color.Gray) },
                    singleLine = true,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AegisBronze,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    if (textState.isNotBlank()) {
                        if (routineToEdit == null) {
                            routinesViewModel.addRoutine(textState)
                        } else {
                            routinesViewModel.updateRoutine(routineToEdit!!.id, textState)
                        }
                        showDialog = false
                    }
                }) {
                    Text("GUARDAR", color = AegisBronze, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDialog = false }) {
                    Text("CANCELAR", color = Color.Gray)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.height(48.dp),
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Text(
                        stringResource(R.string.routine_title).uppercase(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
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
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ✅ Leemos la lista del ViewModel
                items(routinesViewModel.routines) { routine ->
                    RoutineCard(
                        routine = routine,
                        onEdit = {
                            // ✅ En lugar de abrir el diálogo, navegamos a la pantalla completa
                            onNavigateToEditRoutine(routine.id)
                        },
                        onDelete = { routinesViewModel.removeRoutine(routine) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BOTÓN CREAR
            Button(
                onClick = {
                    routineToEdit = null
                    textState = ""
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
}