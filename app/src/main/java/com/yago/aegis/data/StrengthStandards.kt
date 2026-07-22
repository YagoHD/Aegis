package com.yago.aegis.data

/**
 * Estándar de fuerza de un ejercicio ANCLA: multiplicador de peso corporal (1RM / peso)
 * mínimo para alcanzar cada tier, por sexo. Sostiene el eje de FUERZA (el tier).
 *
 * Los valores son aproximados (estilo StrengthLevel) y están pensados para calibrarse.
 * [anchorName] es el nombre del ejercicio base en mayúsculas y sin el sufijo invisible.
 */
data class StrengthStandard(
    val anchorName: String,
    val subgroup: MuscleSubgroup,
    val male: Map<RankTier, Double>,
    val female: Map<RankTier, Double>
) {
    /** Umbrales para un sexo dado ("MALE"/"FEMALE"); por defecto masculino. */
    fun thresholds(sex: String): Map<RankTier, Double> =
        if (sex.equals("FEMALE", ignoreCase = true)) female else male

    /** Tier que corresponde a un ratio 1RM/peso dado, para el sexo indicado. */
    fun tierForRatio(ratio: Double, sex: String): RankTier {
        val t = thresholds(sex)
        // El tier más alto cuyo umbral se supera; si no llega ni a Bronce → SIN_RANGO.
        return RankTier.ladder.lastOrNull { ratio >= (t[it] ?: Double.MAX_VALUE) } ?: RankTier.SIN_RANGO
    }
}

/**
 * Catálogo de estándares por ejercicio ancla. Solo los subgrupos con un ancla fiable
 * obtienen un tier de fuerza "duro"; el resto se estimará por volumen (aprox.).
 */
object StrengthStandards {

    val all: List<StrengthStandard> = listOf(
        //  ancla                          subgrupo               Bronce Plata  Oro   Plat  Diam  Titán            factor♀
        std("PRESS BANCA",                 MuscleSubgroup.PECHO_MEDIO,        listOf(0.50, 0.75, 1.00, 1.25, 1.50, 2.00), 0.65),
        std("PRESS BANCA INCLINADO",       MuscleSubgroup.PECHO_SUPERIOR,     listOf(0.40, 0.60, 0.85, 1.10, 1.35, 1.75), 0.65),
        std("PRESS MILITAR BARRA",         MuscleSubgroup.DELTOIDE_ANTERIOR,  listOf(0.35, 0.55, 0.75, 0.90, 1.10, 1.40), 0.65),
        std("REMO CON BARRA",              MuscleSubgroup.DORSAL,             listOf(0.50, 0.70, 0.90, 1.15, 1.40, 1.75), 0.70),
        std("CURL BARRA",                  MuscleSubgroup.BICEPS,             listOf(0.25, 0.40, 0.55, 0.70, 0.85, 1.10), 0.65),
        std("PRESS CERRADO",               MuscleSubgroup.TRICEPS,            listOf(0.40, 0.60, 0.80, 1.00, 1.25, 1.60), 0.65),
        std("SENTADILLA",                  MuscleSubgroup.CUADRICEPS,         listOf(0.75, 1.00, 1.25, 1.75, 2.25, 2.75), 0.80),
        std("PESO MUERTO RUMANO",          MuscleSubgroup.ISQUIOTIBIALES,     listOf(0.75, 1.00, 1.30, 1.70, 2.10, 2.60), 0.80),
        std("HIP THRUST",                  MuscleSubgroup.GLUTEO,             listOf(1.00, 1.50, 2.00, 2.50, 3.00, 3.75), 0.85),
        std("PESO MUERTO",                 MuscleSubgroup.LUMBAR,             listOf(1.00, 1.25, 1.50, 2.00, 2.50, 3.00), 0.80),
        std("ELEVACIÓN DE TALONES DE PIE", MuscleSubgroup.GEMELOS,            listOf(0.75, 1.25, 1.75, 2.25, 2.75, 3.50), 0.85)
    )

    /** Estándar cuyo ancla coincide con [exerciseName] (normaliza sufijo invisible y mayúsculas). */
    fun forExercise(exerciseName: String): StrengthStandard? {
        val norm = exerciseName.replace("​", "").trim().uppercase()
        return all.find { it.anchorName.uppercase() == norm }
    }

    /** Subgrupos que tienen un ancla con estándar de fuerza (tier "duro"). */
    val anchoredSubgroups: Set<MuscleSubgroup> get() = all.map { it.subgroup }.toSet()

    private fun round2(v: Double): Double = kotlin.math.round(v * 100) / 100.0

    private fun std(
        anchor: String,
        sub: MuscleSubgroup,
        male: List<Double>,
        femaleFactor: Double
    ): StrengthStandard {
        val tiers = RankTier.ladder
        val m = tiers.zip(male).toMap()
        val f = tiers.zip(male.map { round2(it * femaleFactor) }).toMap()
        return StrengthStandard(anchor, sub, m, f)
    }
}
