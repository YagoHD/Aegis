package com.yago.aegis.data

/**
 * Catálogo de ejercicios base.
 *
 * BASE_TAG = "__base__" es el único marcador fiable para identificar
 * ejercicios base. Es invisible para el usuario pero la app lo usa
 * internamente para cargar y eliminar sin tocar ejercicios del usuario.
 */
object DefaultExercises {

    const val BASE_TAG = "__base__"
    // Sufijo invisible (Zero Width Space) añadido a todos los nombres base
    // Hace que "PRESS BANCA" (usuario) != "PRESS BANCA\u200B" (base)
    private const val ZWS = "\u200B"

    fun getAll(): List<Exercise> = pecho + espalda + hombros + biceps + triceps + piernas

    // Devuelve solo los nombres en mayúsculas para comparación rápida
    fun getAllNames(): Set<String> = getAll().map { it.name }.toSet()

    // ─── PECHO ───────────────────────────────────────────────────────────────
    private val pecho = listOf(
        ex("Press Banca", "PECHO", "PUSH"),
        ex("Press Banca Inclinado", "PECHO", "PUSH"),
        ex("Press Banca Declinado", "PECHO", "PUSH"),
        ex("Press Mancuernas", "PECHO", "PUSH"),
        ex("Press Mancuernas Inclinado", "PECHO", "PUSH"),
        ex("Aperturas Mancuernas", "PECHO", "PUSH"),
        ex("Aperturas Polea", "PECHO", "PUSH"),
        ex("Fondos en Paralelas", "PECHO", "PUSH", "body"),
        ex("Flexiones", "PECHO", "PUSH", "body"),
        ex("Press Pecho Máquina", "PECHO", "PUSH"),
        ex("Cruces Polea Alta", "PECHO", "PUSH"),
        ex("Pull Over", "PECHO", "PUSH")
    )

    // ─── ESPALDA ─────────────────────────────────────────────────────────────
    private val espalda = listOf(
        ex("Peso Muerto", "ESPALDA", "PULL"),
        ex("Dominadas", "ESPALDA", "PULL", "body"),
        ex("Jalón al Pecho", "ESPALDA", "PULL", "layers"),
        ex("Jalón tras Nuca", "ESPALDA", "PULL", "layers"),
        ex("Remo con Barra", "ESPALDA", "PULL"),
        ex("Remo con Mancuerna", "ESPALDA", "PULL"),
        ex("Remo en Polea Baja", "ESPALDA", "PULL", "layers"),
        ex("Remo Máquina", "ESPALDA", "PULL"),
        ex("Pullover Polea", "ESPALDA", "PULL", "layers"),
        ex("Buenos Días", "ESPALDA", "PULL"),
        ex("Hiperextensiones", "ESPALDA", "PULL", "body"),
        ex("Encogimientos con Barra", "ESPALDA", "PULL")
    )

    // ─── HOMBROS ─────────────────────────────────────────────────────────────
    private val hombros = listOf(
        ex("Press Militar Barra", "HOMBROS", "PUSH"),
        ex("Press Militar Mancuernas", "HOMBROS", "PUSH"),
        ex("Press Arnold", "HOMBROS", "PUSH"),
        ex("Elevaciones Laterales", "HOMBROS", "PUSH"),
        ex("Elevaciones Frontales", "HOMBROS", "PUSH"),
        ex("Pájaro / Elevaciones Posteriores", "HOMBROS", "PULL"),
        ex("Face Pull", "HOMBROS", "PULL", "layers"),
        ex("Press Máquina Hombro", "HOMBROS", "PUSH"),
        ex("Elevaciones Laterales Polea", "HOMBROS", "PUSH", "layers"),
        ex("Remo al Mentón", "HOMBROS", "PULL")
    )

    // ─── BÍCEPS ──────────────────────────────────────────────────────────────
    private val biceps = listOf(
        ex("Curl Barra", "BÍCEPS", "PULL"),
        ex("Curl Mancuernas", "BÍCEPS", "PULL"),
        ex("Curl Martillo", "BÍCEPS", "PULL"),
        ex("Curl Concentrado", "BÍCEPS", "PULL"),
        ex("Curl Predicador", "BÍCEPS", "PULL"),
        ex("Curl Polea Baja", "BÍCEPS", "PULL", "layers"),
        ex("Curl Barra Z", "BÍCEPS", "PULL"),
        ex("Curl Inclinado Mancuernas", "BÍCEPS", "PULL"),
        ex("Curl Cable Unilateral", "BÍCEPS", "PULL", "layers")
    )

    // ─── TRÍCEPS ─────────────────────────────────────────────────────────────
    private val triceps = listOf(
        ex("Press Francés", "TRÍCEPS", "PUSH"),
        ex("Fondos en Banco", "TRÍCEPS", "PUSH", "body"),
        ex("Extensión Polea Alta", "TRÍCEPS", "PUSH", "layers"),
        ex("Extensión Polea con Cuerda", "TRÍCEPS", "PUSH", "layers"),
        ex("Extensión Overhead Polea", "TRÍCEPS", "PUSH", "layers"),
        ex("Extensión Mancuerna Overhead", "TRÍCEPS", "PUSH"),
        ex("Press Cerrado", "TRÍCEPS", "PUSH"),
        ex("Patada de Tríceps", "TRÍCEPS", "PUSH"),
        ex("Tríceps Máquina", "TRÍCEPS", "PUSH")
    )

    // ─── PIERNAS ─────────────────────────────────────────────────────────────
    private val piernas = listOf(
        ex("Sentadilla", "PIERNAS", "LEGS"),
        ex("Sentadilla Frontal", "PIERNAS", "LEGS"),
        ex("Sentadilla Hack", "PIERNAS", "LEGS"),
        ex("Prensa de Piernas", "PIERNAS", "LEGS"),
        ex("Zancadas", "PIERNAS", "LEGS", "run"),
        ex("Zancadas con Mancuernas", "PIERNAS", "LEGS"),
        ex("Extensión de Cuádriceps", "PIERNAS", "LEGS"),
        ex("Curl Femoral Tumbado", "PIERNAS", "LEGS"),
        ex("Curl Femoral Sentado", "PIERNAS", "LEGS"),
        ex("Peso Muerto Rumano", "PIERNAS", "LEGS"),
        ex("Hip Thrust", "PIERNAS", "LEGS"),
        ex("Elevación de Talones de Pie", "PIERNAS", "LEGS", "body"),
        ex("Elevación de Talones Sentado", "PIERNAS", "LEGS", "body"),
        ex("Abductor Máquina", "PIERNAS", "LEGS"),
        ex("Aductor Máquina", "PIERNAS", "LEGS"),
        ex("Step Up", "PIERNAS", "LEGS", "run")
    )

    private fun ex(
        name: String,
        muscleGroup: String,
        tag: String,
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
        history = emptyList()
    )
}
