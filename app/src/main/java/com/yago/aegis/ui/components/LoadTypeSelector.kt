package com.yago.aegis.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.R
import com.yago.aegis.data.LoadType

/**
 * Selector del tipo de carga de un ejercicio: Normal / Peso corporal / Asistido.
 * Determina cómo se registra el peso de cada serie y cómo se calcula la carga efectiva.
 */
@Composable
fun LoadTypeSelector(
    selected: LoadType,
    onSelect: (LoadType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.load_type_label),
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LoadChip(
                label = stringResource(R.string.load_type_normal),
                isSelected = selected == LoadType.NORMAL,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(LoadType.NORMAL) }
            )
            LoadChip(
                label = stringResource(R.string.load_type_bodyweight),
                isSelected = selected == LoadType.BODYWEIGHT,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(LoadType.BODYWEIGHT) }
            )
            LoadChip(
                label = stringResource(R.string.load_type_assisted),
                isSelected = selected == LoadType.ASSISTED,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(LoadType.ASSISTED) }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when (selected) {
                LoadType.NORMAL -> stringResource(R.string.load_type_normal_desc)
                LoadType.BODYWEIGHT -> stringResource(R.string.load_type_bodyweight_desc)
                LoadType.ASSISTED -> stringResource(R.string.load_type_assisted_desc)
            },
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
    }
}

@Composable
private fun LoadChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
        )
    ) {
        Text(
            text = label,
            textAlign = TextAlign.Center,
            color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.secondary,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
        )
    }
}
