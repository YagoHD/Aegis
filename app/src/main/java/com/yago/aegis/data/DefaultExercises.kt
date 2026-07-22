package com.yago.aegis.data

import com.yago.aegis.data.MuscleSubgroup.*

/**
 * Catálogo de ejercicios base.
 *
 * BASE_TAG = "__base__" es el único marcador fiable para identificar
 * ejercicios base. Es invisible para el usuario pero la app lo usa
 * internamente para cargar y eliminar sin tocar ejercicios del usuario.
 *
 * Cada ejercicio lleva sus contribuciones musculares (★) para el Panteón:
 * qué subgrupos trabaja y en qué % (para el eje de volumen). Los % de cada
 * ejercicio suman ~100. Son valores estándar aproximados, ajustables.
 */
object DefaultExercises {

    const val BASE_TAG = "__base__"
    // Sufijo invisible (Zero Width Space) añadido a todos los nombres base
    // Hace que "PRESS BANCA" (usuario) != "PRESS BANCA​" (base)
    private const val ZWS = "​"

    fun getAll(): List<Exercise> = pecho + espalda + hombros + biceps + triceps + piernas

    // Devuelve solo los nombres en mayúsculas para comparación rápida
    fun getAllNames(): Set<String> = getAll().map { it.name }.toSet()

    // ─── PECHO ───────────────────────────────────────────────────────────────
    private val pecho = listOf(
        ex("Press Banca", "PECHO", "PUSH", listOf(c(PECHO_MEDIO, 65), c(TRICEPS, 20), c(DELTOIDE_ANTERIOR, 15))),
        ex("Press Banca Inclinado", "PECHO", "PUSH", listOf(c(PECHO_SUPERIOR, 60), c(DELTOIDE_ANTERIOR, 20), c(TRICEPS, 20))),
        ex("Press Banca Declinado", "PECHO", "PUSH", listOf(c(PECHO_INFERIOR, 65), c(TRICEPS, 20), c(DELTOIDE_ANTERIOR, 15))),
        ex("Press Mancuernas", "PECHO", "PUSH", listOf(c(PECHO_MEDIO, 65), c(TRICEPS, 18), c(DELTOIDE_ANTERIOR, 17))),
        ex("Press Mancuernas Inclinado", "PECHO", "PUSH", listOf(c(PECHO_SUPERIOR, 60), c(DELTOIDE_ANTERIOR, 22), c(TRICEPS, 18))),
        ex("Aperturas Mancuernas", "PECHO", "PUSH", listOf(c(PECHO_MEDIO, 85), c(DELTOIDE_ANTERIOR, 15))),
        ex("Aperturas Polea", "PECHO", "PUSH", listOf(c(PECHO_MEDIO, 85), c(DELTOIDE_ANTERIOR, 15))),
        ex("Fondos en Paralelas", "PECHO", "PUSH", listOf(c(PECHO_INFERIOR, 55), c(TRICEPS, 30), c(DELTOIDE_ANTERIOR, 15)), "body"),
        ex("Flexiones", "PECHO", "PUSH", listOf(c(PECHO_MEDIO, 60), c(TRICEPS, 25), c(DELTOIDE_ANTERIOR, 15)), "body"),
        ex("Press Pecho Máquina", "PECHO", "PUSH", listOf(c(PECHO_MEDIO, 75), c(TRICEPS, 15), c(DELTOIDE_ANTERIOR, 10))),
        ex("Cruces Polea Alta", "PECHO", "PUSH", listOf(c(PECHO_INFERIOR, 70), c(PECHO_MEDIO, 20), c(DELTOIDE_ANTERIOR, 10))),
        ex("Pull Over", "PECHO", "PUSH", listOf(c(DORSAL, 40), c(PECHO_INFERIOR, 40), c(TRICEPS, 20)))
    )

    // ─── ESPALDA ─────────────────────────────────────────────────────────────
    private val espalda = listOf(
        ex("Peso Muerto", "ESPALDA", "PULL", listOf(c(LUMBAR, 25), c(GLUTEO, 25), c(ISQUIOTIBIALES, 25), c(DORSAL, 15), c(TRAPECIO, 10))),
        ex("Dominadas", "ESPALDA", "PULL", listOf(c(DORSAL, 65), c(BICEPS, 20), c(ROMBOIDES, 10), c(TRAPECIO, 5)), "body"),
        ex("Jalón al Pecho", "ESPALDA", "PULL", listOf(c(DORSAL, 65), c(BICEPS, 20), c(ROMBOIDES, 15)), "layers"),
        ex("Jalón tras Nuca", "ESPALDA", "PULL", listOf(c(DORSAL, 60), c(BICEPS, 20), c(ROMBOIDES, 20)), "layers"),
        ex("Remo con Barra", "ESPALDA", "PULL", listOf(c(DORSAL, 45), c(ROMBOIDES, 25), c(TRAPECIO, 15), c(BICEPS, 15))),
        ex("Remo con Mancuerna", "ESPALDA", "PULL", listOf(c(DORSAL, 50), c(ROMBOIDES, 25), c(BICEPS, 15), c(TRAPECIO, 10))),
        ex("Remo en Polea Baja", "ESPALDA", "PULL", listOf(c(DORSAL, 50), c(ROMBOIDES, 25), c(BICEPS, 15), c(TRAPECIO, 10)), "layers"),
        ex("Remo Máquina", "ESPALDA", "PULL", listOf(c(DORSAL, 50), c(ROMBOIDES, 30), c(BICEPS, 20))),
        ex("Pullover Polea", "ESPALDA", "PULL", listOf(c(DORSAL, 80), c(TRICEPS, 20)), "layers"),
        ex("Buenos Días", "ESPALDA", "PULL", listOf(c(ISQUIOTIBIALES, 40), c(LUMBAR, 35), c(GLUTEO, 25))),
        ex("Hiperextensiones", "ESPALDA", "PULL", listOf(c(LUMBAR, 55), c(GLUTEO, 25), c(ISQUIOTIBIALES, 20)), "body"),
        ex("Encogimientos con Barra", "ESPALDA", "PULL", listOf(c(TRAPECIO, 100)))
    )

    // ─── HOMBROS ─────────────────────────────────────────────────────────────
    private val hombros = listOf(
        ex("Press Militar Barra", "HOMBROS", "PUSH", listOf(c(DELTOIDE_ANTERIOR, 55), c(DELTOIDE_LATERAL, 20), c(TRICEPS, 25))),
        ex("Press Militar Mancuernas", "HOMBROS", "PUSH", listOf(c(DELTOIDE_ANTERIOR, 50), c(DELTOIDE_LATERAL, 25), c(TRICEPS, 25))),
        ex("Press Arnold", "HOMBROS", "PUSH", listOf(c(DELTOIDE_ANTERIOR, 50), c(DELTOIDE_LATERAL, 30), c(TRICEPS, 20))),
        ex("Elevaciones Laterales", "HOMBROS", "PUSH", listOf(c(DELTOIDE_LATERAL, 90), c(DELTOIDE_ANTERIOR, 10))),
        ex("Elevaciones Frontales", "HOMBROS", "PUSH", listOf(c(DELTOIDE_ANTERIOR, 90), c(DELTOIDE_LATERAL, 10))),
        ex("Pájaro / Elevaciones Posteriores", "HOMBROS", "PULL", listOf(c(DELTOIDE_POSTERIOR, 85), c(ROMBOIDES, 15))),
        ex("Face Pull", "HOMBROS", "PULL", listOf(c(DELTOIDE_POSTERIOR, 55), c(ROMBOIDES, 25), c(TRAPECIO, 20)), "layers"),
        ex("Press Máquina Hombro", "HOMBROS", "PUSH", listOf(c(DELTOIDE_ANTERIOR, 60), c(DELTOIDE_LATERAL, 20), c(TRICEPS, 20))),
        ex("Elevaciones Laterales Polea", "HOMBROS", "PUSH", listOf(c(DELTOIDE_LATERAL, 90), c(DELTOIDE_ANTERIOR, 10)), "layers"),
        ex("Remo al Mentón", "HOMBROS", "PULL", listOf(c(DELTOIDE_LATERAL, 50), c(TRAPECIO, 30), c(BICEPS, 20)))
    )

    // ─── BÍCEPS ──────────────────────────────────────────────────────────────
    private val biceps = listOf(
        ex("Curl Barra", "BÍCEPS", "PULL", listOf(c(BICEPS, 80), c(BRAQUIAL, 15), c(ANTEBRAZO, 5))),
        ex("Curl Mancuernas", "BÍCEPS", "PULL", listOf(c(BICEPS, 80), c(BRAQUIAL, 15), c(ANTEBRAZO, 5))),
        ex("Curl Martillo", "BÍCEPS", "PULL", listOf(c(BRAQUIAL, 45), c(BICEPS, 35), c(ANTEBRAZO, 20))),
        ex("Curl Concentrado", "BÍCEPS", "PULL", listOf(c(BICEPS, 90), c(BRAQUIAL, 10))),
        ex("Curl Predicador", "BÍCEPS", "PULL", listOf(c(BICEPS, 85), c(BRAQUIAL, 15))),
        ex("Curl Polea Baja", "BÍCEPS", "PULL", listOf(c(BICEPS, 85), c(BRAQUIAL, 10), c(ANTEBRAZO, 5)), "layers"),
        ex("Curl Barra Z", "BÍCEPS", "PULL", listOf(c(BICEPS, 80), c(BRAQUIAL, 15), c(ANTEBRAZO, 5))),
        ex("Curl Inclinado Mancuernas", "BÍCEPS", "PULL", listOf(c(BICEPS, 90), c(BRAQUIAL, 10))),
        ex("Curl Cable Unilateral", "BÍCEPS", "PULL", listOf(c(BICEPS, 85), c(BRAQUIAL, 15)), "layers")
    )

    // ─── TRÍCEPS ─────────────────────────────────────────────────────────────
    private val triceps = listOf(
        ex("Press Francés", "TRÍCEPS", "PUSH", listOf(c(TRICEPS, 100))),
        ex("Fondos en Banco", "TRÍCEPS", "PUSH", listOf(c(TRICEPS, 75), c(DELTOIDE_ANTERIOR, 15), c(PECHO_INFERIOR, 10)), "body"),
        ex("Extensión Polea Alta", "TRÍCEPS", "PUSH", listOf(c(TRICEPS, 100)), "layers"),
        ex("Extensión Polea con Cuerda", "TRÍCEPS", "PUSH", listOf(c(TRICEPS, 100)), "layers"),
        ex("Extensión Overhead Polea", "TRÍCEPS", "PUSH", listOf(c(TRICEPS, 100)), "layers"),
        ex("Extensión Mancuerna Overhead", "TRÍCEPS", "PUSH", listOf(c(TRICEPS, 100))),
        ex("Press Cerrado", "TRÍCEPS", "PUSH", listOf(c(TRICEPS, 60), c(PECHO_MEDIO, 25), c(DELTOIDE_ANTERIOR, 15))),
        ex("Patada de Tríceps", "TRÍCEPS", "PUSH", listOf(c(TRICEPS, 100))),
        ex("Tríceps Máquina", "TRÍCEPS", "PUSH", listOf(c(TRICEPS, 100)))
    )

    // ─── PIERNAS ─────────────────────────────────────────────────────────────
    private val piernas = listOf(
        ex("Sentadilla", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 50), c(GLUTEO, 30), c(ISQUIOTIBIALES, 15), c(LUMBAR, 5))),
        ex("Sentadilla Frontal", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 65), c(GLUTEO, 25), c(ISQUIOTIBIALES, 10))),
        ex("Sentadilla Hack", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 75), c(GLUTEO, 25))),
        ex("Prensa de Piernas", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 65), c(GLUTEO, 25), c(ISQUIOTIBIALES, 10))),
        ex("Zancadas", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 45), c(GLUTEO, 40), c(ISQUIOTIBIALES, 15)), "run"),
        ex("Zancadas con Mancuernas", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 45), c(GLUTEO, 40), c(ISQUIOTIBIALES, 15))),
        ex("Extensión de Cuádriceps", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 100))),
        ex("Curl Femoral Tumbado", "PIERNAS", "LEGS", listOf(c(ISQUIOTIBIALES, 100))),
        ex("Curl Femoral Sentado", "PIERNAS", "LEGS", listOf(c(ISQUIOTIBIALES, 100))),
        ex("Peso Muerto Rumano", "PIERNAS", "LEGS", listOf(c(ISQUIOTIBIALES, 45), c(GLUTEO, 35), c(LUMBAR, 20))),
        ex("Hip Thrust", "PIERNAS", "LEGS", listOf(c(GLUTEO, 75), c(ISQUIOTIBIALES, 25))),
        ex("Elevación de Talones de Pie", "PIERNAS", "LEGS", listOf(c(GEMELOS, 100)), "body"),
        ex("Elevación de Talones Sentado", "PIERNAS", "LEGS", listOf(c(GEMELOS, 100)), "body"),
        ex("Abductor Máquina", "PIERNAS", "LEGS", listOf(c(GLUTEO, 100))),
        ex("Aductor Máquina", "PIERNAS", "LEGS", listOf(c(ADUCTORES, 100))),
        ex("Step Up", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 45), c(GLUTEO, 45), c(ISQUIOTIBIALES, 10)), "run")
    )

    private fun c(sub: MuscleSubgroup, percent: Int) = MuscleContribution(sub.name, percent)

    private fun ex(
        name: String,
        muscleGroup: String,
        tag: String,
        contributions: List<MuscleContribution>,
        icon: String = "dumbbell"
    ) = Exercise(
        id = ("__base__$name").hashCode().toLong().let { if (it < 0) -it else it },
        name = name.uppercase() + ZWS,
        type = tag,
        muscleGroup = muscleGroup,
        tags = listOf(tag, muscleGroup, BASE_TAG),
        iconName = icon,
        notes = "",
        lastPerformance = "",
        oneRepMax = 0.0,
        bestSet = "--",
        history = emptyList(),
        muscleContributions = contributions
    )
}
