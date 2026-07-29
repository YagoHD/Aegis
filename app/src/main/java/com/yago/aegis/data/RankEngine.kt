package com.yago.aegis.data

import kotlin.math.roundToInt

/** Fatiga estimada de un músculo (no recuperación real: heurística por recencia). */
enum class Fatigue { ALTA, MEDIA, BAJA, DESCANSADO, SIN_DATOS }

/** Rango de un subgrupo muscular. */
data class SubgroupRank(
    val subgroup: MuscleSubgroup,
    val tier: RankTier,
    val ratio: Double,
    val progressToNext: Float,
    val approx: Boolean,
    val windowVolume: Double
)

/** Rango agregado de un grupo muscular. */
data class GroupRank(
    val group: MuscleGroup,
    val tier: RankTier,
    val progressToNext: Float,
    val subgroups: List<SubgroupRank>,
    val daysSinceTrained: Int = -1,      // -1 = sin datos en la ventana
    val fatigue: Fatigue = Fatigue.SIN_DATOS
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
 * Eje FUERZA (tier): cada ejercicio base suma a CADA músculo que trabaja
 * (score = 1RM Epley/peso × contribución%). El tier del subgrupo sale del mejor score
 * vs su estándar (por sexo). DECAIMIENTO PROGRESIVO: las marcas pierden peso con la edad
 * (100% ≤28d, 95% ≤35d, 90% ≤42d, 80% ≤56d, 65% ≤70d, 50% ≤84d, 0 después) para no
 * castigar de golpe una pausa/lesión.
 *
 * FATIGA: por grupo, días desde el último entreno de ese músculo → nivel estimado.
 * Solo cuentan los ejercicios BASE (anti-trampas).
 */
object RankEngine {

    const val DECAY_WINDOW_DAYS = 84
    private const val DAY_MS = 24L * 60L * 60L * 1000L
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

        val cutoff = nowMillis - DECAY_WINDOW_DAYS.toLong() * DAY_MS
        val baseByName = DefaultExercises.getAll().associateBy { normalize(it.name) }
        val libraryById = library.associateBy { it.id }
        val libraryByName = library.associateBy { normalize(it.name) }

        val bestDecayed1RM = HashMap<String, Double>()          // ejercicio -> mejor 1RM decaído
        val subgroupVolume = HashMap<MuscleSubgroup, Double>()  // volumen (últimos 28d)
        val subgroupLastTrained = HashMap<MuscleSubgroup, Int>() // días desde último entreno

        for (session in history) {
            if (session.date < cutoff) continue
            val daysAgo = ((nowMillis - session.date) / DAY_MS).toInt().coerceAtLeast(0)
            val recency = recencyFactor(daysAgo.toLong())
            if (recency <= 0.0) continue
            for (prog in session.exercisesProgress) {
                val ex = libraryById[prog.exercise.id]
                    ?: libraryByName[normalize(prog.exercise.name)]
                    ?: prog.exercise
                if (DefaultExercises.BASE_TAG !in (ex.tags ?: emptyList())) continue
                val name = normalize(ex.name)
                val contribs = baseByName[name]?.muscleContributions ?: emptyList()
                for (set in prog.sets) {
                    if (!set.isCompleted) continue
                    if (set.weight > 0.0) {
                        val e1rm = set.weight * (1 + set.reps / 30.0) * recency
                        if (e1rm > (bestDecayed1RM[name] ?: 0.0)) bestDecayed1RM[name] = e1rm
                    }
                    val load = (if (set.weight > 0.0) set.weight else bodyweight) * set.reps
                    for (c in contribs) {
                        val sub = c.subgroup ?: continue
                        // Volumen solo de los últimos 28 días (para la fatiga/actividad reciente)
                        if (daysAgo <= 28) {
                            subgroupVolume[sub] = (subgroupVolume[sub] ?: 0.0) + load * (c.percent / 100.0)
                        }
                        val prev = subgroupLastTrained[sub]
                        if (prev == null || daysAgo < prev) subgroupLastTrained[sub] = daysAgo
                    }
                }
            }
        }

        // Score por músculo = mejor (1RM decaído/peso × contribución%) entre sus ejercicios.
        val subgroupScore = HashMap<MuscleSubgroup, Double>()
        for ((name, best) in bestDecayed1RM) {
            if (best <= 0.0) continue
            val contribs = baseByName[name]?.muscleContributions ?: continue
            val ratio = best / bodyweight
            for (c in contribs) {
                val sub = c.subgroup ?: continue
                val score = ratio * (c.percent / 100.0)
                if (score > (subgroupScore[sub] ?: 0.0)) subgroupScore[sub] = score
            }
        }

        val subRanks = MuscleSubgroup.entries.map { sub ->
            val score = subgroupScore[sub] ?: 0.0
            SubgroupRank(
                subgroup = sub,
                tier = provider.tierFor(sub, score, sex),
                ratio = score,
                progressToNext = provider.progressToNext(sub, score, sex),
                approx = !provider.isAnchored(sub),
                windowVolume = subgroupVolume[sub] ?: 0.0
            )
        }

        val groups = MuscleGroup.entries.map { grp ->
            val subs = subRanks.filter { it.subgroup.group == grp }
            val ranked = subs.filter { it.tier != RankTier.SIN_RANGO }
            val tier = avgTier(ranked.map { it.tier })
            val progress = if (ranked.isEmpty()) 0f else ranked.map { it.progressToNext }.average().toFloat()
            // Fatiga: días desde el entreno más reciente de cualquier músculo del grupo.
            val daysSince = subs.mapNotNull { subgroupLastTrained[it.subgroup] }.minOrNull() ?: -1
            GroupRank(grp, tier, progress, subs, daysSince, fatigueFor(daysSince))
        }

        val strongest = groups.filter { it.tier != RankTier.SIN_RANGO }
            .maxByOrNull { RankTier.ladder.indexOf(it.tier) }
        val weakest = groups.minByOrNull { tierRank(it.tier) }

        return PanteonResult(groups, strongest, weakest)
    }

    /** Peso de una marca según su antigüedad (decaimiento progresivo). */
    fun recencyFactor(daysAgo: Long): Double = when {
        daysAgo <= 28 -> 1.0
        daysAgo <= 35 -> 0.95
        daysAgo <= 42 -> 0.90
        daysAgo <= 56 -> 0.80
        daysAgo <= 70 -> 0.65
        daysAgo <= 84 -> 0.50
        else -> 0.0
    }

    private fun fatigueFor(daysSince: Int): Fatigue = when {
        daysSince < 0 -> Fatigue.SIN_DATOS
        daysSince <= 1 -> Fatigue.ALTA
        daysSince <= 3 -> Fatigue.MEDIA
        daysSince <= 6 -> Fatigue.BAJA
        else -> Fatigue.DESCANSADO
    }

    private fun tierRank(t: RankTier): Int =
        if (t == RankTier.SIN_RANGO) -1 else RankTier.ladder.indexOf(t)

    private fun avgTier(tiers: List<RankTier>): RankTier {
        val idx = tiers.filter { it != RankTier.SIN_RANGO }.map { RankTier.ladder.indexOf(it) }
        if (idx.isEmpty()) return RankTier.SIN_RANGO
        val avg = idx.average().roundToInt().coerceIn(0, RankTier.ladder.lastIndex)
        return RankTier.ladder[avg]
    }

    private fun normalize(name: String): String = name.replace(ZWS, "").trim().uppercase()
}
