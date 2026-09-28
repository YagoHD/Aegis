package com.yago.aegis.ui.components
import com.yago.aegis.ui.theme.Spacing
import com.yago.aegis.ui.theme.Radius

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

/**
 * Coach-mark de pantalla completa: fondo desvanecido + tarjeta con uno o varios pasos.
 * Aparece la PRIMERA vez que se entra a una pantalla. Al terminar (o saltar) llama a onFinish,
 * que persiste el "visto".
 */
@Composable
fun TutorialOverlay(
    titleRes: Int,
    stepRes: List<Int>,
    onFinish: () -> Unit
) {
    if (stepRes.isEmpty()) return
    var step by remember { mutableIntStateOf(0) }
    val isLast = step >= stepRes.lastIndex

    AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Scrim: desvanece la pantalla. Tocar el fondo salta todo el tutorial.
                .background(Color.Black.copy(alpha = 0.82f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onFinish() },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xl)
                    // Consumir el click para que no cierre al tocar la tarjeta.
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {},
                shape = RoundedCornerShape(Radius.xl),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(Spacing.xl)) {
                    Text(
                        text = stringResource(R.string.tut_badge),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        text = stringResource(titleRes).uppercase(),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(Spacing.md))
                    Text(
                        text = stringResource(stepRes[step]),
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(Spacing.xl))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Puntos de progreso (solo si hay más de un paso)
                        if (stepRes.size > 1) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                stepRes.indices.forEach { i ->
                                    Box(
                                        modifier = Modifier
                                            .size(if (i == step) 8.dp else 6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (i == step) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                                            )
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.weight(1f))

                        if (!isLast) {
                            Text(
                                text = stringResource(R.string.tut_skip),
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(Radius.sm))
                                    .clickable { onFinish() }
                                    .padding(horizontal = Spacing.md, vertical = Spacing.sm)
                            )
                            Spacer(Modifier.size(Spacing.sm))
                        }

                        // Botón principal Siguiente / Entendido
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(Radius.md))
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable {
                                    if (isLast) onFinish() else step++
                                }
                                .padding(horizontal = Spacing.xl, vertical = 12.dp)
                        ) {
                            Text(
                                text = stringResource(if (isLast) R.string.tut_got_it else R.string.tut_next),
                                color = Color.Black,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Tutorial de una pantalla: clave persistida + título + pasos (ids de string). */
data class ScreenTutorial(val key: String, val titleRes: Int, val stepRes: List<Int>)

/** Devuelve el tutorial asociado a una ruta de navegación, o null si no tiene. */
fun tutorialForRoute(route: String?): ScreenTutorial? {
    val base = route?.substringBefore("/") ?: return null
    return when (base) {
        "profile" -> ScreenTutorial("profile", R.string.tut_profile_title, listOf(R.string.tut_profile_1, R.string.tut_profile_2))
        "routine" -> ScreenTutorial("routine", R.string.tut_routine_title, listOf(R.string.tut_routine_1, R.string.tut_routine_2))
        "train" -> ScreenTutorial("train", R.string.tut_train_title, listOf(R.string.tut_train_1))
        "panteon" -> ScreenTutorial("panteon", R.string.tut_panteon_title, listOf(R.string.tut_panteon_1))
        "stats" -> ScreenTutorial("stats", R.string.tut_stats_title, listOf(R.string.tut_stats_1))
        "active_session", "custom_session" ->
            ScreenTutorial("session", R.string.tut_session_title, listOf(R.string.tut_session_1, R.string.tut_session_2))
        else -> null
    }
}
