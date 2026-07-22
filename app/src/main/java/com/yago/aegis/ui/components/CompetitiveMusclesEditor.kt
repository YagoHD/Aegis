package com.yago.aegis.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.R
import com.yago.aegis.data.MuscleContribution
import com.yago.aegis.data.MuscleGroup
import com.yago.aegis.data.MuscleSubgroup

/**
 * Editor de las contribuciones musculares competitivas (★) de un ejercicio.
 * El usuario elige de un vocabulario CERRADO (el mismo para todos) y asigna el %.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitiveMusclesEditor(
    value: List<MuscleContribution>,
    onChange: (List<MuscleContribution>) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    val total = value.sumOf { it.percent }
    val totalColor = if (total in 90..110) MaterialTheme.colorScheme.primary
                     else MaterialTheme.colorScheme.secondary

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.competitive_muscles_title),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
            Text(
                text = stringResource(R.string.total_percent_label, total),
                color = totalColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        Text(
            text = stringResource(R.string.competitive_muscles_hint),
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        value.forEachIndexed { index, contrib ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(start = 12.dp, top = 6.dp, bottom = 6.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = (contrib.subgroup?.display ?: contrib.muscle).uppercase(),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                StepperButton(Icons.Default.Remove) {
                    val p = (contrib.percent - 5).coerceIn(0, 100)
                    onChange(value.toMutableList().also { it[index] = contrib.copy(percent = p) })
                }
                Text(
                    text = "${contrib.percent}%",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.widthIn(min = 40.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                StepperButton(Icons.Default.Add) {
                    val p = (contrib.percent + 5).coerceIn(0, 100)
                    onChange(value.toMutableList().also { it[index] = contrib.copy(percent = p) })
                }
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(20.dp)
                        .clickable { onChange(value.filterIndexed { i, _ -> i != index }) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            onClick = { showPicker = true },
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.add_muscle_btn),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }

    if (showPicker) {
        val used = value.mapNotNull { it.subgroup }.toSet()
        ModalBottomSheet(
            onDismissRequest = { showPicker = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp)
            ) {
                MuscleGroup.entries.forEach { group ->
                    val subs = MuscleSubgroup.byGroup[group].orEmpty().filter { it !in used }
                    if (subs.isNotEmpty()) {
                        item {
                            Text(
                                text = group.display.uppercase(),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp,
                                modifier = Modifier.padding(top = 14.dp, bottom = 6.dp)
                            )
                        }
                        items(subs) { sub ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val remaining = (100 - total).coerceAtLeast(0)
                                        val default = if (remaining in 1..100) remaining else 50
                                        onChange(value + MuscleContribution(sub.name, default))
                                        showPicker = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "★ ${sub.display}",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepperButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
    }
}
