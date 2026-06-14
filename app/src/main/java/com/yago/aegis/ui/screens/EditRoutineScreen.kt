package com.yago.aegis.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.yago.aegis.data.DefaultExercises
import com.yago.aegis.data.ExerciseSlot
import com.yago.aegis.data.effectiveSlots
import com.yago.aegis.data.getExerciseIcon
import com.yago.aegis.data.globalExerciseIcons
import com.yago.aegis.ui.components.AegisTopBar
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
    isNewRoutine: Boolean = false,  // true si venimos de crear, false si editamos una existente
) {
    val originalRoutine = remember(routineId) {
        routinesViewModel.routines.find { it.id == routineId }
    }

    // --- LÓGICA DE REORDENAMIENTO ---
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyColumnState(lazyListState) { from, to ->
        routinesViewModel.tempSlots.apply {
            // Restamos 4 porque hay 4 items antes de la lista (Spacer, Nombre, Iconos, Cabecera)
            add(to.index - 4, removeAt(from.index - 4))
        }
    }

    var tempName by remember { mutableStateOf(originalRoutine?.name ?: "") }
    var selectedIconName by remember { mutableStateOf(originalRoutine?.iconName ?: "dumbbell") }

    // isNewRoutine viene del caller — true sólo cuando venimos del dialog de creación.
    // Evita falsos positivos con rutinas antiguas que tengan 0 ejercicios guardados.

    LaunchedEffect(routineId) {
        if (routinesViewModel.tempSlots.isEmpty()) {
            originalRoutine?.let {
                routinesViewModel.setTempSlots(it.effectiveSlots())
            }
        }
    }

    fun onBackPressed() {
        if (isNewRoutine && routinesViewModel.tempSlots.isEmpty()) {
            // Rutina recién creada, salimos sin añadir ejercicios → eliminarla para no dejar fantasmas
            originalRoutine?.let { routinesViewModel.removeRoutine(it) }
        }
        routinesViewModel.clearTempExercises()
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            AegisTopBar(
                title = if (tempName.isBlank()) stringResource(R.string.new_routine_fallback_title) else tempName.uppercase(),
                navigationIcon = {
                    IconButton(onClick = { onBackPressed() }) {
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
                        text = stringResource(R.string.select_icon),
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
                        text = "${routinesViewModel.tempSlots.size} ${stringResource(R.string.label_added_suffix)}".uppercase(),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // LISTA REORDENABLE (A partir del ITEM 4) — cada ítem es un slot (con 1 o más variantes)
            itemsIndexed(
                items = routinesViewModel.tempSlots,
                key = { _, slot -> slot.id }
            ) { slotIndex, slot ->
                ReorderableItem(reorderableState, key = slot.id) { isDragging ->
                    val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)
                    Surface(
                        shadowElevation = elevation,
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Transparent
                    ) {
                        RoutineSlotCard(
                            slot = slot,
                            onDeleteSlot = { routinesViewModel.removeSlot(slotIndex) },
                            onDeleteVariant = { variantIdx -> routinesViewModel.removeVariantFromSlot(slotIndex, variantIdx) },
                            onAddVariant = { navController.navigate("add_exercise?slotIndex=$slotIndex") },
                            showReorderHandle = true,
                            dragHandleModifier = Modifier.draggableHandle()
                        )
                    }
                }
            }

            // BOTÓN AÑADIR
            item {
                Surface(
                    onClick = { navController.navigate("add_exercise?slotIndex=-1") },
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

            // BOTÓN GUARDAR / CREAR
            item {
                Button(
                    onClick = {
                        routinesViewModel.updateRoutineFull(
                            id = routineId,
                            newName = tempName,
                            newSlots = routinesViewModel.tempSlots.toList(),
                            newIconName = selectedIconName
                        )
                        routinesViewModel.clearTempExercises()
                        onNavigateBack()
                    },
                    enabled = tempName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.Black,
                        disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                        disabledContentColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        if (isNewRoutine) stringResource(R.string.btn_create_routine_label)
                        else stringResource(R.string.btn_save_routine),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ────────────────────────────────────────────────────────────
// SLOT CARD — muestra un slot con su ejercicio principal y variantes
// ────────────────────────────────────────────────────────────
@Composable
private fun RoutineSlotCard(
    slot: ExerciseSlot,
    onDeleteSlot: () -> Unit,
    onDeleteVariant: (Int) -> Unit,   // índice dentro de slot.variants (1+ = variante)
    onAddVariant: () -> Unit,
    showReorderHandle: Boolean = false,
    dragHandleModifier: Modifier = Modifier
) {
    val primaryExercise = slot.variants.firstOrNull() ?: return
    val hasVariants = slot.variants.size > 1

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.dp,
            if (hasVariants) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
        )
    ) {
        Column {
            // Fila principal
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono del ejercicio principal
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(4.dp))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getExerciseIcon(primaryExercise.iconName),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = primaryExercise.name.uppercase(),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                    if (hasVariants) {
                        Text(
                            text = stringResource(R.string.variant_count_label, slot.variants.size - 1, if (slot.variants.size > 2) "s" else ""),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    } else {
                        val tags = primaryExercise.tags.filter { it != DefaultExercises.BASE_TAG }
                        Text(
                            text = if (tags.isNotEmpty()) tags.joinToString(" • ").uppercase()
                                   else "${primaryExercise.type} • ${primaryExercise.muscleGroup}".uppercase(),
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Añadir variante
                    IconButton(onClick = onAddVariant, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = stringResource(R.string.add_variant_desc),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    // Eliminar slot
                    IconButton(onClick = onDeleteSlot, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.content_desc_delete),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    if (showReorderHandle) {
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = stringResource(R.string.content_desc_reorder),
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = dragHandleModifier
                                .padding(start = 4.dp)
                                .size(22.dp)
                        )
                    }
                }
            }

            // Variantes adicionales (2.ª en adelante)
            if (hasVariants) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                )
                slot.variants.drop(1).forEachIndexed { idx, variant ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 56.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = variant.name.uppercase(),
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onDeleteVariant(idx + 1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.delete_variant_desc),
                                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}