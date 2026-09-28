package com.yago.aegis.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.svg.SvgDecoder
import com.yago.aegis.ui.theme.Radius
import com.yago.aegis.ui.theme.Spacing
import kotlinx.coroutines.delay

/**
 * Ilustraciones animadas de ejercicios (PRUEBA — 20 ejercicios).
 *
 * Fuente: workout-guide de Bryl Lim, basado en el arte de **Everkinetic**, bajo **CC BY-SA 4.0**.
 * Son 3 fotogramas SVG por ejercicio (inicio/medio/fin) que se alternan para simular el movimiento
 * (mismo enfoque que otras apps; NO usamos su código, solo la fuente abierta del arte).
 * Los SVG viven en `assets/exercise_anim/<slug>/frame-{1,2,3}.svg`.
 *
 * Mapa nombre (ejercicio base, en español) → slug en inglés del dataset. Si un ejercicio no está
 * mapeado, [exerciseSlugFor] devuelve null y no se muestra animación.
 */
private val NAME_TO_SLUG: Map<String, String> = mapOf(
    "PRESS BANCA" to "bench-press",
    "PRESS BANCA INCLINADO" to "incline-bench-press",
    "SENTADILLA" to "squat",
    "SENTADILLA FRONTAL" to "front-squat",
    "PESO MUERTO" to "deadlift",
    "PESO MUERTO RUMANO" to "romanian-deadlift",
    "REMO CON BARRA" to "barbell-row",
    "DOMINADAS" to "pull-up",
    "DOMINADAS SUPINAS" to "chin-up",
    "FONDOS EN PARALELAS" to "dip",
    "ELEVACIONES LATERALES" to "lateral-raise",
    "CURL MARTILLO" to "hammer-curl",
    "CURL BARRA" to "bicep-curl",
    "CURL MANCUERNAS" to "bicep-curl",
    "PRESS MILITAR BARRA" to "overhead-press",
    "EXTENSIÓN POLEA ALTA" to "tricep-pushdown",
    "PRENSA DE PIERNAS" to "leg-press",
    "EXTENSIÓN DE CUÁDRICEPS" to "leg-extension",
    "CURL FEMORAL TUMBADO" to "lying-leg-curl",
    "HIP THRUST" to "hip-thrust",
    "FLEXIONES" to "push-up"
)

/** Slug de ilustración para un nombre de ejercicio, o null si no hay. Normaliza el ZWS de los base. */
fun exerciseSlugFor(name: String?): String? {
    if (name == null) return null
    return NAME_TO_SLUG[name.replace("​", "").trim().uppercase()]
}

/**
 * Tarjeta con la ilustración animada del ejercicio. No dibuja nada si el ejercicio no está mapeado.
 */
@Composable
fun ExerciseAnimationCard(exerciseName: String?, modifier: Modifier = Modifier) {
    val slug = exerciseSlugFor(exerciseName) ?: return
    val context = LocalContext.current
    // ImageLoader con decoder SVG (Coil no lo trae por defecto).
    val loader = remember {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .build()
    }

    // Alterna 1→2→3→2 en bucle para dar sensación de repetición.
    var frame by remember(slug) { mutableIntStateOf(1) }
    LaunchedEffect(slug) {
        val seq = listOf(1, 2, 3, 2)
        var i = 0
        while (true) {
            frame = seq[i % seq.size]
            i++
            delay(650)
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(Radius.lg)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Crossfade(targetState = frame, animationSpec = tween(300), label = "exercise_frame") { f ->
                // Leemos los bytes del SVG desde assets y se los pasamos a Coil: así no dependemos
                // del esquema de URI de assets; SvgDecoder reconoce el contenido SVG por sí solo.
                val bytes = remember(slug, f) {
                    runCatching {
                        context.assets.open("exercise_anim/$slug/frame-$f.svg").use { it.readBytes() }
                    }.getOrNull()
                }
                AsyncImage(
                    model = bytes,
                    imageLoader = loader,
                    contentDescription = exerciseName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
            // Atribución mínima (CC BY-SA). Pendiente: pantalla de créditos formal.
            Text(
                text = "Ilustración: Everkinetic · CC BY-SA",
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
