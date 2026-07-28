package com.yago.aegis.data

import kotlin.math.roundToInt

/** Rango de un subgrupo muscular. */
data class SubgroupRank(
    val subgroup: MuscleSubgroup,
    val tier: RankTier,
    val ratio: Double,          // 1RM/peso del mejor ejercicio de ese músculo (0 si no hay dato)
    val progressToNext: Float,  // 0..1 dentro del tier
    val approx: Boolean,        // true si no hay estándar (ya no ocurre: todos lo tienen)
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
 * Eje FUERZA (tier): para cada ejercicio BASE hecho en la ventana de 28 días se calcula
 * su mejor 1RM (Epley); se enruta al MÚSCULO PRINCIPAL del ejercicio (la contribución %
 * más alta) y el tier del subgrupo sale de su estándar (StrengthStandards, uno por músculo).
 * Así CUALQUIER ejercicio base da rango a su músculo. Solo cuentan los BASE (anti-trampas).
 *
 * Eje VOLUMEN: reparte peso·reps por subgrupo según las contribuciones % (para mostrar).
 * La ventana de 28 días actúa de decay natural.
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
        // Contribuciones CANÓNICAS por nombre (no dependemos de las guardadas, que Gson
        // puede dejar null en datos antiguos).
        val baseByName = DefaultExercises.getAll().associateBy { normalize(it.name) }
        val libraryById = library.associateBy { it.id }
        val libraryByName = library.associateBy { normalize(it.name) }

        val best1RM = HashMap<String, Double>()               // ejercicio -> mejor 1RM
        val subgroupVolume = HashMap<MuscleSubgroup, Double>() // subgrupo -> volumen

        for (session in history) {
            if (session.date < cutoff) continue
            for (prog in session.exercisesProgress) {
                val ex = libraryById[prog.exercise.id]
                    ?: libraryByName[normalize(prog.exercise.name)]
                    ?: prog.exercise
                // ANTI-TRAMPAS: solo puntúan los ejercicios BASE.
                if (DefaultExercises.BASE_TAG !in (ex.tags ?: emptyList())) continue
                val name = normalize(ex.name)
                val canonical = baseByName[name] ?: continue
                val contribs = canonical.muscleContributions ?: emptyList()
                for (set in prog.sets) {
                    if (!set.isCompleted) continue
                    if (set.weight > 0.0) {
                        val e1rm = set.weight * (1 + set.reps / 30.0)   // Epley
                        if (e1rm > (best1RM[name] ?: 0.0)) best1RM[name] = e1rm
                    }
                    val load = (if (set.weight > 0.0) set.weight else bodyweight) * set.reps
                    for (c in contribs) {
                        val sub = c.subgroup ?: continue
                        subgroupVolume[sub] = (subgroupVolume[sub] ?: 0.0) + load * (c.percent / 100.0)
                    }
                }
            }
        }

        // Cada ejercicio suma a CADA músculo que trabaja (directa o indirectamente):
        // score del músculo = mejor de (1RM/peso × contribución%) entre sus ejercicios.
        val subgroupScore = HashMap<MuscleSubgroup, Double>()
        for ((name, best) in best1RM) {
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
            GroupRank(grp, tier, progress, subs)
        }

        val strongest = groups.filter { it.tier != RankTier.SIN_RANGO }
            .maxByOrNull { RankTier.ladder.indexOf(it.tier) }
        val weakest = groups.minByOrNull { tierRank(it.tier) }

        return PanteonResult(groups, strongest, weakest)
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
