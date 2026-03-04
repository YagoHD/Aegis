package com.yago.aegis.ui.components

import com.yago.aegis.ui.screens.SectionLabel
import com.yago.aegis.ui.screens.TagChip
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yago.aegis.ui.theme.AegisBronze

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
    val RojoBurdeos = Color(0xFF800000)
    val haySeleccionados = selectedTags.isNotEmpty()

    Column(modifier = modifier.fillMaxWidth()) {
        // Cabecera con etiquetas y botones de acción
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reutilizamos tu SectionLabel si la tienes definida, si no, un Text estándar
            SectionLabel("TAGS & CATEGORIES")

            Spacer(modifier = Modifier.weight(1f))

            // Botón Eliminar (Rojo si hay selección)
            IconButton(
                onClick = onRemoveSelectedClick,
                modifier = Modifier.size(24.dp),
                enabled = haySeleccionados
            ) {
                Icon(
                    imageVector = Icons.Default.RemoveCircleOutline,
                    contentDescription = "Eliminar seleccionados",
                    tint = if (haySeleccionados) RojoBurdeos else Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Botón Añadir (Bronce)
            IconButton(
                onClick = onAddClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircleOutline,
                    contentDescription = "Añadir nuevo",
                    tint = AegisBronze
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Contenedor fluido de etiquetas
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            allTags.forEach { tag ->
                val isSelected = selectedTags.contains(tag)
                TagChip(
                    text = tag,
                    isSelected = isSelected,
                    onClick = { onTagClick(tag) }
                )
            }
        }
    }
}