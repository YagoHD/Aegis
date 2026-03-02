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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineScreen(viewModel: ProfileViewModel, onNavigateToSettings: () -> Unit) {
    // Lista de ejemplo (Luego vendrá del ViewModel)
    val routines = remember {
        mutableStateListOf(
            Routine(1, "LEGS", 6, 0),
            Routine(2, "PULL", 8, 0),
            Routine(3, "PUSH", 7, 0)
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.height(48.dp),
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Text(
                        stringResource(R.string.routine_title),
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
            // Lista de Rutinas
            LazyColumn(
                modifier = Modifier.weight(1f), // Toma el espacio disponible
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(routines) { routine ->
                    RoutineCard(
                        routine = routine,
                        onEdit = { /* Lógica editar */ },
                        onDelete = { routines.remove(routine) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Crear Nueva Rutina
            Button(
                onClick = {
                    // Ejemplo: Añadir una nueva al pulsar
                    routines.add(Routine(routines.size + 1, "NEW", 0, 0))
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
                Text(stringResource(R.string.btn_create_routine), color = AegisBronze, fontWeight = FontWeight.Bold)
            }
        }
    }
}