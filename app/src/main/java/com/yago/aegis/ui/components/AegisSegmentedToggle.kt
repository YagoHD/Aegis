package com.yago.aegis.ui.components
import com.yago.aegis.ui.theme.Spacing
import com.yago.aegis.ui.theme.Radius

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Conmutador genérico de 2+ segmentos con el mismo look que RoutinesExercisesToggle
 * (segmento activo en Bronce, resto transparente). Reutilizable para "EJERCICIOS / CREAR
 * EJERCICIO" y otros pares.
 */
@Composable
fun AegisSegmentedToggle(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(MaterialTheme.colorScheme.surface)
            .padding(Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        options.forEachIndexed { index, text ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable(enabled = !selected) { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text.uppercase(),
                    color = if (selected) Color.Black else MaterialTheme.colorScheme.secondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
