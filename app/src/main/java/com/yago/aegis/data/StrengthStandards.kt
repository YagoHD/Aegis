package com.yago.aegis.data

/**
 * Estándar de fuerza de un SUBGRUPO muscular, en el espacio PONDERADO del Panteón.
 *
 * El motor alimenta cada músculo con `1RM/peso × contribución%` de CADA ejercicio que lo
 * trabaja (directa o indirectamente). Por eso los umbrales están calibrados en ese espacio:
 * se toma la curva "cruda" del ejercicio principal del músculo y se multiplica por su % de
 * contribución (dom). Así, hacer el ejercicio principal da el mismo tier que antes y los
 * ejercicios secundarios suman crédito proporcional. Valores aproximados, calibrables.
 * Los aislamientos con mancuerna asumen peso POR MANCUERNA (como se registra en la app).
 */
data class StrengthStandard(
    val subgroup: MuscleSubgroup,
    val male: Map<RankTier, Double>,
    val female: Map<RankTier, Double>
) {
    fun thresholds(sex: String): Map<RankTier, Double> =
        if (sex.equals("FEMALE", ignoreCase = true)) female else male

    /** Tier para un score ponderado dado, para el sexo indicado. */
    fun tierForRatio(ratio: Double, sex: String): RankTier {
        val t = thresholds(sex)
        return RankTier.ladder.lastOrNull { ratio >= (t[it] ?: Double.MAX_VALUE) } ?: RankTier.SIN_RANGO
    }
}

/** Catálogo de estándares por subgrupo (los 21). */
object StrengthStandards {

    val all: List<StrengthStandard> = listOf(
        //   subgrupo                          curva CRUDA del ejercicio principal          ♀     dom (% del principal)
        std(MuscleSubgroup.PECHO_MEDIO,        listOf(0.50, 0.75, 1.00, 1.25, 1.50, 2.00), 0.65, 0.65), // Press Banca
        std(MuscleSubgroup.PECHO_SUPERIOR,     listOf(0.40, 0.60, 0.85, 1.10, 1.35, 1.75), 0.65, 0.60), // Press Inclinado
        std(MuscleSubgroup.PECHO_INFERIOR,     listOf(0.50, 0.75, 1.00, 1.25, 1.50, 2.00), 0.65, 0.60), // Press Declinado
        std(MuscleSubgroup.DORSAL,             listOf(0.50, 0.70, 0.90, 1.15, 1.40, 1.75), 0.70, 0.55), // Remo/Jalón
        std(MuscleSubgroup.TRAPECIO,           listOf(0.75, 1.10, 1.50, 2.00, 2.50, 3.25), 0.75, 1.00), // Encogimientos
        std(MuscleSubgroup.ROMBOIDES,          listOf(0.50, 0.70, 0.90, 1.15, 1.40, 1.75), 0.70, 0.30), // Remo (secundario)
        std(MuscleSubgroup.LUMBAR,             listOf(1.00, 1.25, 1.50, 2.00, 2.50, 3.00), 0.80, 0.25), // Peso Muerto
        std(MuscleSubgroup.DELTOIDE_ANTERIOR,  listOf(0.35, 0.55, 0.75, 0.90, 1.10, 1.40), 0.65, 0.55), // Press Militar
        std(MuscleSubgroup.DELTOIDE_LATERAL,   listOf(0.08, 0.12, 0.16, 0.22, 0.28, 0.36), 0.70, 0.90), // Elevaciones Lat.
        std(MuscleSubgroup.DELTOIDE_POSTERIOR, listOf(0.07, 0.11, 0.15, 0.20, 0.26, 0.34), 0.70, 0.85), // Pájaros
        std(MuscleSubgroup.BICEPS,             listOf(0.25, 0.40, 0.55, 0.70, 0.85, 1.10), 0.65, 0.80), // Curl Barra
        std(MuscleSubgroup.BRAQUIAL,           listOf(0.15, 0.22, 0.30, 0.38, 0.47, 0.58), 0.65, 0.45), // Curl Martillo
        std(MuscleSubgroup.TRICEPS,            listOf(0.40, 0.60, 0.80, 1.00, 1.25, 1.60), 0.65, 0.60), // Press Cerrado
        std(MuscleSubgroup.ANTEBRAZO,          listOf(0.20, 0.35, 0.50, 0.65, 0.82, 1.00), 0.60, 1.00), // Curl Muñeca
        std(MuscleSubgroup.CUADRICEPS,         listOf(0.75, 1.00, 1.25, 1.75, 2.25, 2.75), 0.80, 0.50), // Sentadilla
        std(MuscleSubgroup.ISQUIOTIBIALES,     listOf(0.75, 1.00, 1.30, 1.70, 2.10, 2.60), 0.80, 0.45), // PM Rumano
        std(MuscleSubgroup.GLUTEO,             listOf(1.00, 1.50, 2.00, 2.50, 3.00, 3.75), 0.85, 0.75), // Hip Thrust
        std(MuscleSubgroup.GEMELOS,            listOf(0.75, 1.25, 1.75, 2.25, 2.75, 3.50), 0.85, 1.00), // Gemelo de Pie
        std(MuscleSubgroup.ADUCTORES,          listOf(0.30, 0.50, 0.75, 1.00, 1.30, 1.65), 0.85, 1.00), // Aductor Máquina
        std(MuscleSubgroup.ABDOMEN,            listOf(0.25, 0.45, 0.65, 0.90, 1.15, 1.50), 0.75, 1.00), // Crunch lastrado
        std(MuscleSubgroup.OBLICUOS,           listOf(0.20, 0.35, 0.55, 0.75, 1.00, 1.30), 0.75, 0.85)  // Oblicuos
    )

    private fun round2(v: Double): Double = kotlin.math.round(v * 100) / 100.0

    /** [rawMale] = curva cruda del ejercicio principal; se pondera por [dom] (% de contribución). */
    private fun std(
        sub: MuscleSubgroup,
        rawMale: List<Double>,
        femaleFactor: Double,
        dom: Double
    ): StrengthStandard {
        val tiers = RankTier.ladder
        val m = tiers.zip(rawMale.map { round2(it * dom) }).toMap()
        val f = tiers.zip(rawMale.map { round2(it * dom * femaleFactor) }).toMap()
        return StrengthStandard(sub, m, f)
    }
}
