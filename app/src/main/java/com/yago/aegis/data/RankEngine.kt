package com.yago.aegis.data

import kotlin.math.roundToInt

/** Rango de un subgrupo muscular. */
data class SubgroupRank(
    val subgroup: MuscleSubgroup,
    val tier: RankTier,
    val ratio: Double,          // 1RM/peso del ancla (0 si no hay dato)
    val progressToNext: Float,  // 0..1 dentro del tier
    val approx: Boolean,        // true si no proviene de un ancla fiable
    val windowVolume: Double    // volumen del músculo en la ventana (kg·reps)
)

/** Rango agregado de un grupo muscular. */
data class GroupRank(
    val group: MuscleGroup,
    val tier: RankTier,
    val progressToNext: Float,
    val subgroups: List<SubgroupRank>
)

/** Resultado completo del Panteón para un usuario. */
data class PanteonResult(
    val groups: List<GroupRank>,
    val strongest: GroupRank?,
    val weakest: GroupRank?
) {
    companion object { val EMPTY = PanteonResult(emptyList(), null, null) }
}

/**
 * Motor de rango del Panteón.
 *
 * Eje FUERZA (tier): mejor 1RM (Epley) del ejercicio ancla en la ventana de 28 días,
 * dividido por el peso corporal → ratio → tier vía [TierProvider].
 * Eje VOLUMEN: reparte peso·reps por subgrupo según las contribuciones % (para mostrar
 * y para futuros afinados de decay). La propia ventana de 28 días ya actúa como decay:
 * si dejas de entrenar un músculo, su ancla sale de la ventana y cae a SIN_RANGO.
 */
object RankEngine {

    const val WINDOW_DAYS = 28
    private const val ZWS = "​"

    fun compute(
        history: List<WorkoutSession>,
        library: List<Exercise>,
        bodyweight: Double,
        sex: String,
        provider: TierProvider = StandardsTierProvider(),
        nowMillis: Long = System.currentTimeMillis()
    ): PanteonResult {
        if (bodyweight <= 0.0) return PanteonResult.EMPTY

        val cutoff = nowMillis - WINDOW_DAYS.toLong() * 24L * 60L * 60L * 1000L
        val libraryById = library.associateBy { it.id }
        val libraryByName = library.associateBy { normalize(it.name) }

        val best1RM = HashMap<String, Double>()               // ejercicio normalizado -> mejor 1RM
        val subgroupVolume = HashMap<MuscleSubgroup, Double>() // subgrupo -> volumen en ventana

        for (session in history) {
            if (session.date < cutoff) continue
            for (prog in session.exercisesProgress) {
                // Resuelve contribuciones frescas desde la librería (sesiones antiguas no las tienen).
                val ex = libraryById[prog.exercise.id]
                    ?: libraryByName[normalize(prog.exercise.name)]
                    ?: prog.exercise
                val name = normalize(ex.name)
                for (set in prog.sets) {
                    if (!set.isCompleted) continue
                    if (set.weight > 0.0) {
                        val e1rm = set.weight * (1 + set.reps / 30.0)   // Epley
                        if (e1rm > (best1RM[name] ?: 0.0)) best1RM[name] = e1rm
                    }
                    // Peso corporal como carga en ejercicios sin peso externo (calistenia).
                    val load = (if (set.weight > 0.0) set.weight else bodyweight) * set.reps
                    // Gson deja null los campos nuevos en datos antiguos → protegemos.
                    val contribs = ex.muscleContributions ?: emptyList()
                    for (contrib in contribs) {
                        val sub = contrib.subgroup ?: continue
                        subgroupVolume[sub] = (subgroupVolume[sub] ?: 0.0) + load * (contrib.percent / 100.0)
                    }
                }
            }
        }

        val stdBySub = StrengthStandards.all.associateBy { it.subgroup }

        val subRanks = MuscleSubgroup.entries.map { sub ->
            val std = stdBySub[sub]
            val volume = subgroupVolume[sub] ?: 0.0
            if (std != null) {
                val best = best1RM[normalize(std.anchorName)] ?: 0.0
                val ratio = if (best > 0.0) best / bodyweight else 0.0
                SubgroupRank(
                    subgroup = sub,
                    tier = provider.tierFor(sub, ratio, sex),
                    ratio = ratio,
                    progressToNext = provider.progressToNext(sub, ratio, sex),
                    approx = false,
                    windowVolume = volume
                )
            } else {
                // Sin ancla fiable: por ahora sin tier (aprox), pero se traza el volumen.
                SubgroupRank(sub, RankTier.SIN_RANGO, 0.0, 0f, approx = true, windowVolume = volume)
            }
        }

        val groups = MuscleGroup.entries.map { grp ->
            val subs = subRanks.filter { it.subgroup.group == grp }
            val ranked = subs.filter { it.tier != RankTier.SIN_RANGO }
            val tier = avgTier(ranked.map { it.tier })
            val progress = if (ranked.isEmpty()) 0f else ranked.map { it.progressToNext }.average().toFloat()
            GroupRank(grp, tier, progress, subs)
        }

        val strongest = groups.filter { it.tier != RankTier.SIN_RANGO }
            .maxByOrNull { RankTier.ladder.indexOf(it.tier) }
        val weakest = groups.minByOrNull { tierRank(it.tier) }

        return PanteonResult(groups, strongest, weakest)
    }

    /** Orden para comparar tiers; SIN_RANGO es el más bajo. */
    private fun tierRank(t: RankTier): Int =
        if (t == RankTier.SIN_RANGO) -1 else RankTier.ladder.indexOf(t)

    /** Tier medio (redondeado) de una lista de tiers, ignorando SIN_RANGO. */
    private fun avgTier(tiers: List<RankTier>): RankTier {
        val idx = tiers.filter { it != RankTier.SIN_RANGO }.map { RankTier.ladder.indexOf(it) }
        if (idx.isEmpty()) return RankTier.SIN_RANGO
        val avg = idx.average().roundToInt().coerceIn(0, RankTier.ladder.lastIndex)
        return RankTier.ladder[avg]
    }

    private fun normalize(name: String): String = name.replace(ZWS, "").trim().uppercase()
}
