package com.yago.aegis.data

/**
 * Catálogo de ejercicios base. Se importan manualmente desde la librería
 * cuando el usuario pulsa "Cargar ejercicios base".
 */
object DefaultExercises {

    fun getAll(): List<Exercise> = pecho + espalda + hombros + biceps + triceps + piernas

    // ─── PECHO ───────────────────────────────────────────────────────────────
    private val pecho = listOf(
        ex("Press Banca", "PECHO", "PUSH", "dumbbell"),
        ex("Press Banca Inclinado", "PECHO", "PUSH", "dumbbell"),
        ex("Press Banca Declinado", "PECHO", "PUSH", "dumbbell"),
        ex("Press Mancuernas", "PECHO", "PUSH", "dumbbell"),
        ex("Press Mancuernas Inclinado", "PECHO", "PUSH", "dumbbell"),
        ex("Aperturas Mancuernas", "PECHO", "PUSH", "dumbbell"),
        ex("Aperturas Polea", "PECHO", "PUSH", "layers"),
        ex("Fondos en Paralelas", "PECHO", "PUSH", "body"),
        ex("Flexiones", "PECHO", "PUSH", "body"),
        ex("Press Pecho Máquina", "PECHO", "PUSH", "dumbbell"),
        ex("Cruces Polea Alta", "PECHO", "PUSH", "layers"),
        ex("Pull Over", "PECHO", "PUSH", "dumbbell")
    )

    // ─── ESPALDA ─────────────────────────────────────────────────────────────
    private val espalda = listOf(
        ex("Peso Muerto", "ESPALDA", "PULL", "dumbbell"),
        ex("Dominadas", "ESPALDA", "PULL", "body"),
        ex("Jalón al Pecho", "ESPALDA", "PULL", "layers"),
        ex("Jalón tras Nuca", "ESPALDA", "PULL", "layers"),
        ex("Remo con Barra", "ESPALDA", "PULL", "dumbbell"),
        ex("Remo con Mancuerna", "ESPALDA", "PULL", "dumbbell"),
        ex("Remo en Polea Baja", "ESPALDA", "PULL", "layers"),
        ex("Remo Máquina", "ESPALDA", "PULL", "dumbbell"),
        ex("Pullover Polea", "ESPALDA", "PULL", "layers"),
        ex("Buenos Días", "ESPALDA", "PULL", "dumbbell"),
        ex("Hiperextensiones", "ESPALDA", "PULL", "body"),
        ex("Encogimientos con Barra", "ESPALDA", "PULL", "dumbbell")
    )

    // ─── HOMBROS ─────────────────────────────────────────────────────────────
    private val hombros = listOf(
        ex("Press Militar Barra", "HOMBROS", "PUSH", "dumbbell"),
        ex("Press Militar Mancuernas", "HOMBROS", "PUSH", "dumbbell"),
        ex("Press Arnold", "HOMBROS", "PUSH", "dumbbell"),
        ex("Elevaciones Laterales", "HOMBROS", "PUSH", "dumbbell"),
        ex("Elevaciones Frontales", "HOMBROS", "PUSH", "dumbbell"),
        ex("Pájaro / Elevaciones Posteriores", "HOMBROS", "PULL", "dumbbell"),
        ex("Face Pull", "HOMBROS", "PULL", "layers"),
        ex("Press Máquina Hombro", "HOMBROS", "PUSH", "dumbbell"),
        ex("Elevaciones Laterales Polea", "HOMBROS", "PUSH", "layers"),
        ex("Remo al Mentón", "HOMBROS", "PULL", "dumbbell")
    )

    // ─── BÍCEPS ──────────────────────────────────────────────────────────────
    private val biceps = listOf(
        ex("Curl Barra", "BÍCEPS", "PULL", "dumbbell"),
        ex("Curl Mancuernas", "BÍCEPS", "PULL", "dumbbell"),
        ex("Curl Martillo", "BÍCEPS", "PULL", "dumbbell"),
        ex("Curl Concentrado", "BÍCEPS", "PULL", "dumbbell"),
        ex("Curl Predicador", "BÍCEPS", "PULL", "dumbbell"),
        ex("Curl Polea Baja", "BÍCEPS", "PULL", "layers"),
        ex("Curl Barra Z", "BÍCEPS", "PULL", "dumbbell"),
        ex("Curl Inclinado Mancuernas", "BÍCEPS", "PULL", "dumbbell"),
        ex("Curl Cable Unilateral", "BÍCEPS", "PULL", "layers")
    )

    // ─── TRÍCEPS ─────────────────────────────────────────────────────────────
    private val triceps = listOf(
        ex("Press Francés", "TRÍCEPS", "PUSH", "dumbbell"),
        ex("Fondos en Banco", "TRÍCEPS", "PUSH", "body"),
        ex("Extensión Polea Alta", "TRÍCEPS", "PUSH", "layers"),
        ex("Extensión Polea con Cuerda", "TRÍCEPS", "PUSH", "layers"),
        ex("Extensión Overhead Polea", "TRÍCEPS", "PUSH", "layers"),
        ex("Extensión Mancuerna Overhead", "TRÍCEPS", "PUSH", "dumbbell"),
        ex("Press Cerrado", "TRÍCEPS", "PUSH", "dumbbell"),
        ex("Patada de Tríceps", "TRÍCEPS", "PUSH", "dumbbell"),
        ex("Tríceps Máquina", "TRÍCEPS", "PUSH", "dumbbell")
    )

    // ─── PIERNAS ─────────────────────────────────────────────────────────────
    private val piernas = listOf(
        ex("Sentadilla", "PIERNAS", "LEGS", "dumbbell"),
        ex("Sentadilla Frontal", "PIERNAS", "LEGS", "dumbbell"),
        ex("Sentadilla Hack", "PIERNAS", "LEGS", "dumbbell"),
        ex("Prensa de Piernas", "PIERNAS", "LEGS", "dumbbell"),
        ex("Zancadas", "PIERNAS", "LEGS", "run"),
        ex("Zancadas con Mancuernas", "PIERNAS", "LEGS", "dumbbell"),
        ex("Extensión de Cuádriceps", "PIERNAS", "LEGS", "dumbbell"),
        ex("Curl Femoral Tumbado", "PIERNAS", "LEGS", "dumbbell"),
        ex("Curl Femoral Sentado", "PIERNAS", "LEGS", "dumbbell"),
        ex("Peso Muerto Rumano", "PIERNAS", "LEGS", "dumbbell"),
        ex("Hip Thrust", "PIERNAS", "LEGS", "dumbbell"),
        ex("Elevación de Talones de Pie", "PIERNAS", "LEGS", "body"),
        ex("Elevación de Talones Sentado", "PIERNAS", "LEGS", "body"),
        ex("Abductor Máquina", "PIERNAS", "LEGS", "dumbbell"),
        ex("Aductor Máquina", "PIERNAS", "LEGS", "dumbbell"),
        ex("Step Up", "PIERNAS", "LEGS", "run")
    )

    private fun ex(name: String, muscleGroup: String, tag: String, icon: String) = Exercise(
        id = name.hashCode().toLong().let { if (it < 0) it * -1 + 1_000_000 else it },
        name = name.uppercase(),
        type = tag,
        muscleGroup = muscleGroup,
        tags = listOf(tag, muscleGroup),
        iconName = icon,
        notes = "",
        lastPerformance = "",
        oneRepMax = 0.0,
        bestSet = "--",
        history = emptyList()
    )
}
