package com.yago.aegis.data

/**
 * Costura que convierte un SCORE (ratio 1RM/peso) en un TIER.
 * Hoy: [StandardsTierProvider] (tabla fija). Mañana, con backend:
 * un CommunityTierProvider por percentiles de la comunidad — drop-in, sin tocar el motor.
 */
interface TierProvider {
    /** ¿Este subgrupo tiene un estándar/ancla fiable (tier "duro")? */
    fun isAnchored(subgroup: MuscleSubgroup): Boolean

    /** Tier para un subgrupo dado su ratio 1RM/peso y el sexo ("MALE"/"FEMALE"). */
    fun tierFor(subgroup: MuscleSubgroup, ratio: Double, sex: String): RankTier

    /** Progreso 0..1 dentro del tier actual hacia el siguiente (para la barra). */
    fun progressToNext(subgroup: MuscleSubgroup, ratio: Double, sex: String): Float
}

/** Proveedor v1: usa la tabla fija de [StrengthStandards]. */
class StandardsTierProvider(
    standards: List<StrengthStandard> = StrengthStandards.all
) : TierProvider {

    private val bySub = standards.associateBy { it.subgroup }

    override fun isAnchored(subgroup: MuscleSubgroup): Boolean = bySub.containsKey(subgroup)

    override fun tierFor(subgroup: MuscleSubgroup, ratio: Double, sex: String): RankTier {
        val std = bySub[subgroup] ?: return RankTier.SIN_RANGO
        if (ratio <= 0.0) return RankTier.SIN_RANGO
        return std.tierForRatio(ratio, sex)
    }

    override fun progressToNext(subgroup: MuscleSubgroup, ratio: Double, sex: String): Float {
        val std = bySub[subgroup] ?: return 0f
        if (ratio <= 0.0) return 0f
        val t = std.thresholds(sex)
        return when (val current = std.tierForRatio(ratio, sex)) {
            RankTier.TITAN -> 1f
            RankTier.SIN_RANGO -> {
                val bronze = t[RankTier.BRONCE] ?: return 0f
                if (bronze <= 0.0) 0f else (ratio / bronze).coerceIn(0.0, 1.0).toFloat()
            }
            else -> {
                val idx = RankTier.ladder.indexOf(current)
                val next = RankTier.ladder[idx + 1]
                val lo = t[current] ?: return 0f
                val hi = t[next] ?: return 1f
                if (hi <= lo) 1f else ((ratio - lo) / (hi - lo)).coerceIn(0.0, 1.0).toFloat()
            }
        }
    }
}
