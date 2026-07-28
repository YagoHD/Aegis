package com.yago.aegis.data

import com.yago.aegis.data.MuscleSubgroup.*

/**
 * Catálogo de ejercicios base (canónico e inmutable).
 *
 * Solo estos ejercicios cuentan en el Panteón (competitivo): sus etiquetas ★ y
 * porcentajes son iguales para todos y no editables por el usuario. Los ejercicios
 * creados por el usuario NO puntúan (anti-trampas).
 *
 * BASE_TAG = "__base__" identifica los base. Los nombres llevan un Zero Width Space
 * invisible para no colisionar con nombres de usuario.
 */
object DefaultExercises {

    const val BASE_TAG = "__base__"
    private const val ZWS = "​"

    fun getAll(): List<Exercise> =
        pecho + espalda + hombros + biceps + triceps + antebrazo + piernas + core

    fun getAllNames(): Set<String> = getAll().map { it.name }.toSet()

    // ─── PECHO ───────────────────────────────────────────────────────────────
    private val pecho = listOf(
        ex("Press Banca", "PECHO", "PUSH", listOf(c(PECHO_MEDIO, 65), c(TRICEPS, 20), c(DELTOIDE_ANTERIOR, 15))),
        ex("Press Banca Mancuernas", "PECHO", "PUSH", listOf(c(PECHO_MEDIO, 65), c(TRICEPS, 18), c(DELTOIDE_ANTERIOR, 17))),
        ex("Press Banca Multipower", "PECHO", "PUSH", listOf(c(PECHO_MEDIO, 68), c(TRICEPS, 20), c(DELTOIDE_ANTERIOR, 12))),
        ex("Press Pecho Máquina", "PECHO", "PUSH", listOf(c(PECHO_MEDIO, 75), c(TRICEPS, 15), c(DELTOIDE_ANTERIOR, 10))),
        ex("Press Banca Inclinado", "PECHO", "PUSH", listOf(c(PECHO_SUPERIOR, 60), c(DELTOIDE_ANTERIOR, 20), c(TRICEPS, 20))),
        ex("Press Inclinado Mancuernas", "PECHO", "PUSH", listOf(c(PECHO_SUPERIOR, 60), c(DELTOIDE_ANTERIOR, 22), c(TRICEPS, 18))),
        ex("Press Inclinado Máquina", "PECHO", "PUSH", listOf(c(PECHO_SUPERIOR, 68), c(DELTOIDE_ANTERIOR, 20), c(TRICEPS, 12))),
        ex("Press Inclinado Multipower", "PECHO", "PUSH", listOf(c(PECHO_SUPERIOR, 62), c(DELTOIDE_ANTERIOR, 22), c(TRICEPS, 16))),
        ex("Press Banca Declinado", "PECHO", "PUSH", listOf(c(PECHO_INFERIOR, 65), c(TRICEPS, 20), c(DELTOIDE_ANTERIOR, 15))),
        ex("Press Declinado Mancuernas", "PECHO", "PUSH", listOf(c(PECHO_INFERIOR, 65), c(TRICEPS, 20), c(DELTOIDE_ANTERIOR, 15))),
        ex("Aperturas Mancuernas", "PECHO", "PUSH", listOf(c(PECHO_MEDIO, 85), c(DELTOIDE_ANTERIOR, 15))),
        ex("Aperturas Inclinado Mancuernas", "PECHO", "PUSH", listOf(c(PECHO_SUPERIOR, 80), c(DELTOIDE_ANTERIOR, 20))),
        ex("Aperturas Máquina", "PECHO", "PUSH", listOf(c(PECHO_MEDIO, 90), c(DELTOIDE_ANTERIOR, 10))),
        ex("Cruces Polea Alta", "PECHO", "PUSH", listOf(c(PECHO_INFERIOR, 70), c(PECHO_MEDIO, 20), c(DELTOIDE_ANTERIOR, 10)), "layers"),
        ex("Cruces Polea Baja", "PECHO", "PUSH", listOf(c(PECHO_SUPERIOR, 70), c(DELTOIDE_ANTERIOR, 30)), "layers"),
        ex("Fondos en Paralelas", "PECHO", "PUSH", listOf(c(PECHO_INFERIOR, 55), c(TRICEPS, 30), c(DELTOIDE_ANTERIOR, 15)), "body"),
        ex("Flexiones", "PECHO", "PUSH", listOf(c(PECHO_MEDIO, 60), c(TRICEPS, 25), c(DELTOIDE_ANTERIOR, 15)), "body"),
        ex("Flexiones Declinadas", "PECHO", "PUSH", listOf(c(PECHO_SUPERIOR, 55), c(TRICEPS, 25), c(DELTOIDE_ANTERIOR, 20)), "body"),
        ex("Pull Over Mancuerna", "PECHO", "PUSH", listOf(c(DORSAL, 40), c(PECHO_INFERIOR, 40), c(TRICEPS, 20))),
        ex("Press Svend", "PECHO", "PUSH", listOf(c(PECHO_MEDIO, 90), c(DELTOIDE_ANTERIOR, 10))),
        ex("Fondos Asistidos Máquina", "PECHO", "PUSH", listOf(c(PECHO_INFERIOR, 55), c(TRICEPS, 30), c(DELTOIDE_ANTERIOR, 15))),
        ex("Press Landmine", "PECHO", "PUSH", listOf(c(PECHO_SUPERIOR, 55), c(DELTOIDE_ANTERIOR, 30), c(TRICEPS, 15))),
        ex("Cruces Polea Media", "PECHO", "PUSH", listOf(c(PECHO_MEDIO, 85), c(DELTOIDE_ANTERIOR, 15)), "layers"),
        ex("Aperturas Declinado Mancuernas", "PECHO", "PUSH", listOf(c(PECHO_INFERIOR, 85), c(DELTOIDE_ANTERIOR, 15))),
        ex("Press en Suelo", "PECHO", "PUSH", listOf(c(PECHO_MEDIO, 60), c(TRICEPS, 30), c(DELTOIDE_ANTERIOR, 10))),
        ex("Press Pecho en Polea", "PECHO", "PUSH", listOf(c(PECHO_MEDIO, 80), c(DELTOIDE_ANTERIOR, 10), c(TRICEPS, 10)), "layers")
    )

    // ─── ESPALDA ─────────────────────────────────────────────────────────────
    private val espalda = listOf(
        ex("Peso Muerto", "ESPALDA", "PULL", listOf(c(LUMBAR, 25), c(GLUTEO, 25), c(ISQUIOTIBIALES, 25), c(DORSAL, 15), c(TRAPECIO, 10))),
        ex("Peso Muerto Sumo", "ESPALDA", "PULL", listOf(c(GLUTEO, 30), c(CUADRICEPS, 20), c(LUMBAR, 20), c(ISQUIOTIBIALES, 20), c(TRAPECIO, 10))),
        ex("Rack Pull", "ESPALDA", "PULL", listOf(c(LUMBAR, 35), c(TRAPECIO, 25), c(DORSAL, 25), c(GLUTEO, 15))),
        ex("Dominadas", "ESPALDA", "PULL", listOf(c(DORSAL, 65), c(BICEPS, 20), c(ROMBOIDES, 10), c(TRAPECIO, 5)), "body"),
        ex("Dominadas Supinas", "ESPALDA", "PULL", listOf(c(DORSAL, 55), c(BICEPS, 30), c(ROMBOIDES, 15)), "body"),
        ex("Dominadas Neutras", "ESPALDA", "PULL", listOf(c(DORSAL, 60), c(BICEPS, 25), c(ROMBOIDES, 15)), "body"),
        ex("Dominadas Asistidas Máquina", "ESPALDA", "PULL", listOf(c(DORSAL, 65), c(BICEPS, 20), c(ROMBOIDES, 15)), "layers"),
        ex("Jalón al Pecho", "ESPALDA", "PULL", listOf(c(DORSAL, 65), c(BICEPS, 20), c(ROMBOIDES, 15)), "layers"),
        ex("Jalón Supino", "ESPALDA", "PULL", listOf(c(DORSAL, 55), c(BICEPS, 30), c(ROMBOIDES, 15)), "layers"),
        ex("Jalón Neutro", "ESPALDA", "PULL", listOf(c(DORSAL, 60), c(BICEPS, 25), c(ROMBOIDES, 15)), "layers"),
        ex("Jalón tras Nuca", "ESPALDA", "PULL", listOf(c(DORSAL, 60), c(BICEPS, 20), c(ROMBOIDES, 20)), "layers"),
        ex("Jalón Unilateral Polea", "ESPALDA", "PULL", listOf(c(DORSAL, 65), c(BICEPS, 20), c(ROMBOIDES, 15)), "layers"),
        ex("Remo con Barra", "ESPALDA", "PULL", listOf(c(DORSAL, 45), c(ROMBOIDES, 25), c(TRAPECIO, 15), c(BICEPS, 15))),
        ex("Remo Pendlay", "ESPALDA", "PULL", listOf(c(DORSAL, 45), c(ROMBOIDES, 25), c(TRAPECIO, 15), c(BICEPS, 15))),
        ex("Remo con Mancuerna", "ESPALDA", "PULL", listOf(c(DORSAL, 50), c(ROMBOIDES, 25), c(BICEPS, 15), c(TRAPECIO, 10))),
        ex("Remo en Polea Baja", "ESPALDA", "PULL", listOf(c(DORSAL, 50), c(ROMBOIDES, 25), c(BICEPS, 15), c(TRAPECIO, 10)), "layers"),
        ex("Remo Máquina", "ESPALDA", "PULL", listOf(c(DORSAL, 50), c(ROMBOIDES, 30), c(BICEPS, 20))),
        ex("Remo Máquina Hammer", "ESPALDA", "PULL", listOf(c(DORSAL, 55), c(ROMBOIDES, 25), c(BICEPS, 20))),
        ex("Remo T", "ESPALDA", "PULL", listOf(c(DORSAL, 50), c(ROMBOIDES, 25), c(TRAPECIO, 10), c(BICEPS, 15))),
        ex("Remo Invertido", "ESPALDA", "PULL", listOf(c(DORSAL, 50), c(ROMBOIDES, 30), c(BICEPS, 20)), "body"),
        ex("Pullover Polea", "ESPALDA", "PULL", listOf(c(DORSAL, 80), c(TRICEPS, 20)), "layers"),
        ex("Encogimientos con Barra", "ESPALDA", "PULL", listOf(c(TRAPECIO, 100))),
        ex("Encogimientos con Mancuernas", "ESPALDA", "PULL", listOf(c(TRAPECIO, 100))),
        ex("Encogimientos Máquina", "ESPALDA", "PULL", listOf(c(TRAPECIO, 100))),
        ex("Hiperextensiones", "ESPALDA", "PULL", listOf(c(LUMBAR, 55), c(GLUTEO, 25), c(ISQUIOTIBIALES, 20)), "body"),
        ex("Buenos Días", "ESPALDA", "PULL", listOf(c(ISQUIOTIBIALES, 40), c(LUMBAR, 35), c(GLUTEO, 25))),
        ex("Peso Muerto Trap Bar", "ESPALDA", "PULL", listOf(c(CUADRICEPS, 25), c(GLUTEO, 25), c(LUMBAR, 20), c(ISQUIOTIBIALES, 20), c(TRAPECIO, 10))),
        ex("Remo con Barra Supino", "ESPALDA", "PULL", listOf(c(DORSAL, 50), c(ROMBOIDES, 20), c(BICEPS, 20), c(TRAPECIO, 10))),
        ex("Remo Meadows", "ESPALDA", "PULL", listOf(c(DORSAL, 55), c(ROMBOIDES, 20), c(BICEPS, 15), c(TRAPECIO, 10))),
        ex("Encogimientos Multipower", "ESPALDA", "PULL", listOf(c(TRAPECIO, 100))),
        ex("Remo Gironda", "ESPALDA", "PULL", listOf(c(DORSAL, 50), c(ROMBOIDES, 25), c(BICEPS, 15), c(TRAPECIO, 10)), "layers"),
        ex("Remo Landmine", "ESPALDA", "PULL", listOf(c(DORSAL, 55), c(ROMBOIDES, 25), c(BICEPS, 20))),
        ex("Jalón con Cuerda", "ESPALDA", "PULL", listOf(c(DORSAL, 65), c(BICEPS, 20), c(ROMBOIDES, 15)), "layers"),
        ex("Peso Muerto Déficit", "ESPALDA", "PULL", listOf(c(LUMBAR, 25), c(GLUTEO, 25), c(ISQUIOTIBIALES, 25), c(DORSAL, 15), c(TRAPECIO, 10))),
        ex("Encogimientos en Polea", "ESPALDA", "PULL", listOf(c(TRAPECIO, 100)), "layers")
    )

    // ─── HOMBROS ─────────────────────────────────────────────────────────────
    private val hombros = listOf(
        ex("Press Militar Barra", "HOMBROS", "PUSH", listOf(c(DELTOIDE_ANTERIOR, 55), c(DELTOIDE_LATERAL, 20), c(TRICEPS, 25))),
        ex("Press Militar Mancuernas", "HOMBROS", "PUSH", listOf(c(DELTOIDE_ANTERIOR, 50), c(DELTOIDE_LATERAL, 25), c(TRICEPS, 25))),
        ex("Press Militar Máquina", "HOMBROS", "PUSH", listOf(c(DELTOIDE_ANTERIOR, 60), c(DELTOIDE_LATERAL, 20), c(TRICEPS, 20))),
        ex("Press Arnold", "HOMBROS", "PUSH", listOf(c(DELTOIDE_ANTERIOR, 50), c(DELTOIDE_LATERAL, 30), c(TRICEPS, 20))),
        ex("Push Press", "HOMBROS", "PUSH", listOf(c(DELTOIDE_ANTERIOR, 55), c(DELTOIDE_LATERAL, 20), c(TRICEPS, 25))),
        ex("Elevaciones Laterales", "HOMBROS", "PUSH", listOf(c(DELTOIDE_LATERAL, 90), c(DELTOIDE_ANTERIOR, 10))),
        ex("Elevaciones Laterales Polea", "HOMBROS", "PUSH", listOf(c(DELTOIDE_LATERAL, 90), c(DELTOIDE_ANTERIOR, 10)), "layers"),
        ex("Elevaciones Laterales Máquina", "HOMBROS", "PUSH", listOf(c(DELTOIDE_LATERAL, 95), c(DELTOIDE_ANTERIOR, 5))),
        ex("Elevaciones Frontales Mancuernas", "HOMBROS", "PUSH", listOf(c(DELTOIDE_ANTERIOR, 90), c(DELTOIDE_LATERAL, 10))),
        ex("Elevaciones Frontales Polea", "HOMBROS", "PUSH", listOf(c(DELTOIDE_ANTERIOR, 90), c(DELTOIDE_LATERAL, 10)), "layers"),
        ex("Pájaro Mancuernas", "HOMBROS", "PULL", listOf(c(DELTOIDE_POSTERIOR, 85), c(ROMBOIDES, 15))),
        ex("Pájaro Máquina", "HOMBROS", "PULL", listOf(c(DELTOIDE_POSTERIOR, 85), c(ROMBOIDES, 15))),
        ex("Pájaro Polea", "HOMBROS", "PULL", listOf(c(DELTOIDE_POSTERIOR, 85), c(ROMBOIDES, 15)), "layers"),
        ex("Face Pull", "HOMBROS", "PULL", listOf(c(DELTOIDE_POSTERIOR, 55), c(ROMBOIDES, 25), c(TRAPECIO, 20)), "layers"),
        ex("Remo al Mentón", "HOMBROS", "PULL", listOf(c(DELTOIDE_LATERAL, 50), c(TRAPECIO, 30), c(BICEPS, 20))),
        ex("Remo al Mentón Polea", "HOMBROS", "PULL", listOf(c(DELTOIDE_LATERAL, 50), c(TRAPECIO, 30), c(BICEPS, 20)), "layers"),
        ex("Press Landmine Hombro", "HOMBROS", "PUSH", listOf(c(DELTOIDE_ANTERIOR, 65), c(TRICEPS, 20), c(DELTOIDE_LATERAL, 15))),
        ex("Elevación Lateral Inclinado", "HOMBROS", "PUSH", listOf(c(DELTOIDE_LATERAL, 90), c(DELTOIDE_POSTERIOR, 10))),
        ex("Elevación en Y", "HOMBROS", "PULL", listOf(c(DELTOIDE_POSTERIOR, 45), c(TRAPECIO, 30), c(DELTOIDE_LATERAL, 25))),
        ex("Press tras Nuca", "HOMBROS", "PUSH", listOf(c(DELTOIDE_ANTERIOR, 45), c(DELTOIDE_LATERAL, 30), c(TRICEPS, 25))),
        ex("Press Bradford", "HOMBROS", "PUSH", listOf(c(DELTOIDE_ANTERIOR, 45), c(DELTOIDE_LATERAL, 35), c(TRICEPS, 20))),
        ex("Flexiones Pike", "HOMBROS", "PUSH", listOf(c(DELTOIDE_ANTERIOR, 60), c(TRICEPS, 30), c(DELTOIDE_LATERAL, 10)), "body"),
        ex("Flexiones Pino", "HOMBROS", "PUSH", listOf(c(DELTOIDE_ANTERIOR, 55), c(TRICEPS, 35), c(DELTOIDE_LATERAL, 10)), "body"),
        ex("Elevación Frontal con Disco", "HOMBROS", "PUSH", listOf(c(DELTOIDE_ANTERIOR, 90), c(DELTOIDE_LATERAL, 10))),
        ex("Rotación Externa Polea", "HOMBROS", "PULL", listOf(c(DELTOIDE_POSTERIOR, 80), c(TRAPECIO, 20)), "layers")
    )

    // ─── BÍCEPS ──────────────────────────────────────────────────────────────
    private val biceps = listOf(
        ex("Curl Barra", "BÍCEPS", "PULL", listOf(c(BICEPS, 80), c(BRAQUIAL, 15), c(ANTEBRAZO, 5))),
        ex("Curl Barra Z", "BÍCEPS", "PULL", listOf(c(BICEPS, 80), c(BRAQUIAL, 15), c(ANTEBRAZO, 5))),
        ex("Curl Mancuernas", "BÍCEPS", "PULL", listOf(c(BICEPS, 80), c(BRAQUIAL, 15), c(ANTEBRAZO, 5))),
        ex("Curl Alterno Mancuernas", "BÍCEPS", "PULL", listOf(c(BICEPS, 80), c(BRAQUIAL, 15), c(ANTEBRAZO, 5))),
        ex("Curl Martillo", "BÍCEPS", "PULL", listOf(c(BRAQUIAL, 45), c(BICEPS, 35), c(ANTEBRAZO, 20))),
        ex("Curl Concentrado", "BÍCEPS", "PULL", listOf(c(BICEPS, 90), c(BRAQUIAL, 10))),
        ex("Curl Predicador", "BÍCEPS", "PULL", listOf(c(BICEPS, 85), c(BRAQUIAL, 15))),
        ex("Curl Predicador Máquina", "BÍCEPS", "PULL", listOf(c(BICEPS, 85), c(BRAQUIAL, 15))),
        ex("Curl Polea Baja", "BÍCEPS", "PULL", listOf(c(BICEPS, 85), c(BRAQUIAL, 10), c(ANTEBRAZO, 5)), "layers"),
        ex("Curl Polea con Cuerda", "BÍCEPS", "PULL", listOf(c(BICEPS, 80), c(BRAQUIAL, 15), c(ANTEBRAZO, 5)), "layers"),
        ex("Curl Inclinado Mancuernas", "BÍCEPS", "PULL", listOf(c(BICEPS, 90), c(BRAQUIAL, 10))),
        ex("Curl Araña", "BÍCEPS", "PULL", listOf(c(BICEPS, 90), c(BRAQUIAL, 10))),
        ex("Curl 21s", "BÍCEPS", "PULL", listOf(c(BICEPS, 90), c(BRAQUIAL, 10))),
        ex("Curl Máquina", "BÍCEPS", "PULL", listOf(c(BICEPS, 90), c(BRAQUIAL, 10))),
        ex("Curl Bayesian", "BÍCEPS", "PULL", listOf(c(BICEPS, 90), c(BRAQUIAL, 10)), "layers"),
        ex("Curl Inverso", "BÍCEPS", "PULL", listOf(c(ANTEBRAZO, 55), c(BRAQUIAL, 30), c(BICEPS, 15))),
        ex("Curl Zottman", "BÍCEPS", "PULL", listOf(c(BICEPS, 50), c(ANTEBRAZO, 30), c(BRAQUIAL, 20))),
        ex("Curl Predicador Mancuerna", "BÍCEPS", "PULL", listOf(c(BICEPS, 85), c(BRAQUIAL, 15))),
        ex("Curl Drag", "BÍCEPS", "PULL", listOf(c(BICEPS, 85), c(BRAQUIAL, 15))),
        ex("Curl Cable Alto", "BÍCEPS", "PULL", listOf(c(BICEPS, 90), c(BRAQUIAL, 10)), "layers")
    )

    // ─── TRÍCEPS ─────────────────────────────────────────────────────────────
    private val triceps = listOf(
        ex("Press Cerrado", "TRÍCEPS", "PUSH", listOf(c(TRICEPS, 60), c(PECHO_MEDIO, 25), c(DELTOIDE_ANTERIOR, 15))),
        ex("Press Francés", "TRÍCEPS", "PUSH", listOf(c(TRICEPS, 100))),
        ex("Press Francés Mancuerna", "TRÍCEPS", "PUSH", listOf(c(TRICEPS, 100))),
        ex("Extensión Polea Alta", "TRÍCEPS", "PUSH", listOf(c(TRICEPS, 100)), "layers"),
        ex("Extensión Polea con Cuerda", "TRÍCEPS", "PUSH", listOf(c(TRICEPS, 100)), "layers"),
        ex("Extensión Polea Unilateral", "TRÍCEPS", "PUSH", listOf(c(TRICEPS, 100)), "layers"),
        ex("Extensión Overhead Polea", "TRÍCEPS", "PUSH", listOf(c(TRICEPS, 100)), "layers"),
        ex("Extensión Mancuerna Overhead", "TRÍCEPS", "PUSH", listOf(c(TRICEPS, 100))),
        ex("Fondos en Banco", "TRÍCEPS", "PUSH", listOf(c(TRICEPS, 75), c(DELTOIDE_ANTERIOR, 15), c(PECHO_INFERIOR, 10)), "body"),
        ex("Patada de Tríceps", "TRÍCEPS", "PUSH", listOf(c(TRICEPS, 100))),
        ex("Patada Tríceps Polea", "TRÍCEPS", "PUSH", listOf(c(TRICEPS, 100)), "layers"),
        ex("Tríceps Máquina", "TRÍCEPS", "PUSH", listOf(c(TRICEPS, 100))),
        ex("Press JM", "TRÍCEPS", "PUSH", listOf(c(TRICEPS, 80), c(PECHO_MEDIO, 20))),
        ex("Flexiones Diamante", "TRÍCEPS", "PUSH", listOf(c(TRICEPS, 60), c(PECHO_MEDIO, 25), c(DELTOIDE_ANTERIOR, 15)), "body"),
        ex("Fondos Máquina Tríceps", "TRÍCEPS", "PUSH", listOf(c(TRICEPS, 80), c(PECHO_INFERIOR, 10), c(DELTOIDE_ANTERIOR, 10))),
        ex("Press Tate", "TRÍCEPS", "PUSH", listOf(c(TRICEPS, 100))),
        ex("Extensión Tríceps Banda", "TRÍCEPS", "PUSH", listOf(c(TRICEPS, 100))),
        ex("Extensión Overhead Barra", "TRÍCEPS", "PUSH", listOf(c(TRICEPS, 100))),
        ex("Extensión Polea Barra", "TRÍCEPS", "PUSH", listOf(c(TRICEPS, 100)), "layers"),
        ex("Extensión Polea Invertida", "TRÍCEPS", "PUSH", listOf(c(TRICEPS, 100)), "layers")
    )

    // ─── ANTEBRAZO ───────────────────────────────────────────────────────────
    private val antebrazo = listOf(
        ex("Curl de Muñeca", "ANTEBRAZO", "PULL", listOf(c(ANTEBRAZO, 100))),
        ex("Curl de Muñeca Invertido", "ANTEBRAZO", "PULL", listOf(c(ANTEBRAZO, 100))),
        ex("Rueda de Antebrazo", "ANTEBRAZO", "PULL", listOf(c(ANTEBRAZO, 100))),
        ex("Farmer Walk", "ANTEBRAZO", "PULL", listOf(c(ANTEBRAZO, 60), c(TRAPECIO, 40)), "run"),
        ex("Curl de Muñeca Polea", "ANTEBRAZO", "PULL", listOf(c(ANTEBRAZO, 100)), "layers"),
        ex("Agarre con Pinza", "ANTEBRAZO", "PULL", listOf(c(ANTEBRAZO, 100))),
        ex("Curl de Muñeca tras Espalda", "ANTEBRAZO", "PULL", listOf(c(ANTEBRAZO, 100)))
    )

    // ─── PIERNAS ─────────────────────────────────────────────────────────────
    private val piernas = listOf(
        ex("Sentadilla", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 50), c(GLUTEO, 30), c(ISQUIOTIBIALES, 15), c(LUMBAR, 5))),
        ex("Sentadilla Frontal", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 65), c(GLUTEO, 25), c(ISQUIOTIBIALES, 10))),
        ex("Sentadilla Hack", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 75), c(GLUTEO, 25))),
        ex("Sentadilla Búlgara", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 45), c(GLUTEO, 40), c(ISQUIOTIBIALES, 15))),
        ex("Sentadilla Goblet", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 55), c(GLUTEO, 30), c(ISQUIOTIBIALES, 15))),
        ex("Sentadilla Multipower", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 60), c(GLUTEO, 28), c(ISQUIOTIBIALES, 12))),
        ex("Sentadilla Sissy", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 100)), "body"),
        ex("Prensa de Piernas", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 65), c(GLUTEO, 25), c(ISQUIOTIBIALES, 10))),
        ex("Prensa Horizontal", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 65), c(GLUTEO, 25), c(ISQUIOTIBIALES, 10))),
        ex("Zancadas", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 45), c(GLUTEO, 40), c(ISQUIOTIBIALES, 15)), "run"),
        ex("Zancadas con Mancuernas", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 45), c(GLUTEO, 40), c(ISQUIOTIBIALES, 15))),
        ex("Zancadas Caminando", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 45), c(GLUTEO, 40), c(ISQUIOTIBIALES, 15)), "run"),
        ex("Step Up", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 45), c(GLUTEO, 45), c(ISQUIOTIBIALES, 10)), "run"),
        ex("Extensión de Cuádriceps", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 100))),
        ex("Curl Femoral Tumbado", "PIERNAS", "LEGS", listOf(c(ISQUIOTIBIALES, 100))),
        ex("Curl Femoral Sentado", "PIERNAS", "LEGS", listOf(c(ISQUIOTIBIALES, 100))),
        ex("Curl Femoral de Pie", "PIERNAS", "LEGS", listOf(c(ISQUIOTIBIALES, 100))),
        ex("Peso Muerto Rumano", "PIERNAS", "LEGS", listOf(c(ISQUIOTIBIALES, 45), c(GLUTEO, 35), c(LUMBAR, 20))),
        ex("Peso Muerto Rumano Mancuernas", "PIERNAS", "LEGS", listOf(c(ISQUIOTIBIALES, 45), c(GLUTEO, 35), c(LUMBAR, 20))),
        ex("Peso Muerto Piernas Rígidas", "PIERNAS", "LEGS", listOf(c(ISQUIOTIBIALES, 55), c(GLUTEO, 25), c(LUMBAR, 20))),
        ex("Hip Thrust", "PIERNAS", "LEGS", listOf(c(GLUTEO, 75), c(ISQUIOTIBIALES, 25))),
        ex("Puente de Glúteo", "PIERNAS", "LEGS", listOf(c(GLUTEO, 80), c(ISQUIOTIBIALES, 20)), "body"),
        ex("Patada de Glúteo Polea", "PIERNAS", "LEGS", listOf(c(GLUTEO, 85), c(ISQUIOTIBIALES, 15)), "layers"),
        ex("Abductor Máquina", "PIERNAS", "LEGS", listOf(c(GLUTEO, 100))),
        ex("Aductor Máquina", "PIERNAS", "LEGS", listOf(c(ADUCTORES, 100))),
        ex("Gemelo de Pie", "PIERNAS", "LEGS", listOf(c(GEMELOS, 100)), "body"),
        ex("Gemelo en Máquina de Pie", "PIERNAS", "LEGS", listOf(c(GEMELOS, 100))),
        ex("Gemelo en Prensa", "PIERNAS", "LEGS", listOf(c(GEMELOS, 100))),
        ex("Gemelo Burro", "PIERNAS", "LEGS", listOf(c(GEMELOS, 100)), "body"),
        ex("Gemelo a una Pierna", "PIERNAS", "LEGS", listOf(c(GEMELOS, 100)), "body"),
        ex("Sóleo Sentado", "PIERNAS", "LEGS", listOf(c(GEMELOS, 100)), "body"),
        ex("Zancadas Inversas", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 45), c(GLUTEO, 40), c(ISQUIOTIBIALES, 15)), "run"),
        ex("Prensa a una Pierna", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 60), c(GLUTEO, 30), c(ISQUIOTIBIALES, 10))),
        ex("Peso Muerto a una Pierna", "PIERNAS", "LEGS", listOf(c(ISQUIOTIBIALES, 45), c(GLUTEO, 40), c(LUMBAR, 15))),
        ex("Sentadilla Pistol", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 60), c(GLUTEO, 30), c(ISQUIOTIBIALES, 10)), "body"),
        ex("Curl Femoral Nórdico", "PIERNAS", "LEGS", listOf(c(ISQUIOTIBIALES, 100)), "body"),
        ex("Sentadilla Zercher", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 55), c(GLUTEO, 30), c(LUMBAR, 15))),
        ex("Sentadilla Cajón", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 45), c(GLUTEO, 35), c(ISQUIOTIBIALES, 15), c(LUMBAR, 5))),
        ex("Sentadilla Sumo", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 40), c(GLUTEO, 35), c(ADUCTORES, 15), c(ISQUIOTIBIALES, 10))),
        ex("Zancada Lateral", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 40), c(GLUTEO, 35), c(ADUCTORES, 25)), "run"),
        ex("Extensión de Cuádriceps Unilateral", "PIERNAS", "LEGS", listOf(c(CUADRICEPS, 100))),
        ex("Curl Femoral Unilateral", "PIERNAS", "LEGS", listOf(c(ISQUIOTIBIALES, 100))),
        ex("Hip Thrust a una Pierna", "PIERNAS", "LEGS", listOf(c(GLUTEO, 75), c(ISQUIOTIBIALES, 25))),
        ex("Hip Thrust Máquina", "PIERNAS", "LEGS", listOf(c(GLUTEO, 75), c(ISQUIOTIBIALES, 25)))
    )

    // ─── CORE ────────────────────────────────────────────────────────────────
    private val core = listOf(
        ex("Crunch", "CORE", "CORE", listOf(c(ABDOMEN, 100)), "body"),
        ex("Crunch en Máquina", "CORE", "CORE", listOf(c(ABDOMEN, 100))),
        ex("Crunch en Polea", "CORE", "CORE", listOf(c(ABDOMEN, 100)), "layers"),
        ex("Elevación de Piernas Colgado", "CORE", "CORE", listOf(c(ABDOMEN, 80), c(OBLICUOS, 20)), "body"),
        ex("Elevación de Rodillas", "CORE", "CORE", listOf(c(ABDOMEN, 80), c(OBLICUOS, 20)), "body"),
        ex("Plancha", "CORE", "CORE", listOf(c(ABDOMEN, 70), c(OBLICUOS, 30)), "body"),
        ex("Plancha Lateral", "CORE", "CORE", listOf(c(OBLICUOS, 80), c(ABDOMEN, 20)), "body"),
        ex("Rueda Abdominal", "CORE", "CORE", listOf(c(ABDOMEN, 80), c(OBLICUOS, 20))),
        ex("Giro Ruso", "CORE", "CORE", listOf(c(OBLICUOS, 80), c(ABDOMEN, 20)), "body"),
        ex("Oblicuos en Polea", "CORE", "CORE", listOf(c(OBLICUOS, 85), c(ABDOMEN, 15)), "layers"),
        ex("Sit Up", "CORE", "CORE", listOf(c(ABDOMEN, 85), c(OBLICUOS, 15)), "body"),
        ex("Elevación de Piernas Tumbado", "CORE", "CORE", listOf(c(ABDOMEN, 85), c(OBLICUOS, 15)), "body"),
        ex("Crunch Bicicleta", "CORE", "CORE", listOf(c(ABDOMEN, 60), c(OBLICUOS, 40)), "body"),
        ex("V-Up", "CORE", "CORE", listOf(c(ABDOMEN, 80), c(OBLICUOS, 20)), "body"),
        ex("Mountain Climber", "CORE", "CORE", listOf(c(ABDOMEN, 70), c(OBLICUOS, 30)), "run"),
        ex("Dragon Flag", "CORE", "CORE", listOf(c(ABDOMEN, 85), c(OBLICUOS, 15)), "body"),
        ex("Pallof Press", "CORE", "CORE", listOf(c(OBLICUOS, 70), c(ABDOMEN, 30)), "layers"),
        ex("Suitcase Carry", "CORE", "CORE", listOf(c(OBLICUOS, 60), c(ANTEBRAZO, 40)), "run"),
        ex("Crunch Inverso", "CORE", "CORE", listOf(c(ABDOMEN, 85), c(OBLICUOS, 15)), "body"),
        ex("Elevación de Piernas en Paralelas", "CORE", "CORE", listOf(c(ABDOMEN, 80), c(OBLICUOS, 20)), "body"),
        ex("Leñador en Polea", "CORE", "CORE", listOf(c(OBLICUOS, 80), c(ABDOMEN, 20)), "layers"),
        ex("Encogimiento Lateral con Mancuerna", "CORE", "CORE", listOf(c(OBLICUOS, 90), c(ABDOMEN, 10))),
        ex("Plancha con Peso", "CORE", "CORE", listOf(c(ABDOMEN, 70), c(OBLICUOS, 30)), "body"),
        ex("Hollow Hold", "CORE", "CORE", listOf(c(ABDOMEN, 80), c(OBLICUOS, 20)), "body")
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
