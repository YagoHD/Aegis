package com.yago.aegis.ui.components

import androidx.compose.foundation.background
import com.yago.aegis.ui.screens.TagChip
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yago.aegis.R
import com.yago.aegis.data.DefaultExercises

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AegisTagManager(
    allTags: List<String>,
    selectedTags: Set<String>,
    onTagClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onRemoveSelectedClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasSelection = selectedTags.isNotEmpty()

    Column(modifier = modifier.fillMaxWidth()) {
        // --- CABECERA TÉCNICA ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Usamos el estilo de cabecera que definimos para Settings
            SectionHeader(text = stringResource(R.string.tags_title))

            Spacer(modifier = Modifier.weight(1f))

            // Botón Eliminar Seleccionados
            // Solo brilla en color "error" si hay algo que borrar
            IconButton(
                onClick = onRemoveSelectedClick,
                modifier = Modifier.size(28.dp),
                enabled = hasSelection
            ) {
                Icon(
                    imageVector = Icons.Default.RemoveCircleOutline,
                    contentDescription = "Eliminar seleccionados",
                    tint = if (hasSelection)
                        MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Botón Añadir (Bronce Aegis)
            IconButton(
                onClick = onAddClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircleOutline,
                    contentDescription = "Añadir nuevo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- CONTENEDOR DE ETIQUETAS (FLOW) ---
        // Aplicamos un ligero fondo oscuro para agrupar las etiquetas visualmente
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(8.dp)
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                allTags.filter { it != DefaultExercises.BASE_TAG }.forEach { tag ->
                    val isSelected = selectedTags.contains(tag)
                    TagChip(
                        text = tag.uppercase(), // Consistencia táctica
                        isSelected = isSelected,
                        onClick = { onTagClick(tag) }
                    )
                }
            }
        }
    }
}