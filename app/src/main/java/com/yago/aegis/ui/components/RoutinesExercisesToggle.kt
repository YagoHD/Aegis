package com.yago.aegis.ui.components

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.R

/**
 * Conmutador RUTINAS | EJERCICIOS. La librería de ejercicios vive "dentro" de Rutinas
 * (ya no es pestaña de la bottom bar) y se alterna con este toggle.
 */
@Composable
fun RoutinesExercisesToggle(
    isRoutines: Boolean,
    onSelectRoutines: () -> Unit,
    onSelectExercises: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ToggleSegment(
            text = stringResource(R.string.nav_routine),
            selected = isRoutines,
            modifier = Modifier.weight(1f),
            onClick = onSelectRoutines
        )
        ToggleSegment(
            text = stringResource(R.string.nav_exercices),
            selected = !isRoutines,
            modifier = Modifier.weight(1f),
            onClick = onSelectExercises
        )
    }
}

@Composable
private fun ToggleSegment(
    text: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(enabled = !selected) { onClick() }
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
