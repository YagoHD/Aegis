package com.yago.aegis.data

/**
 * Nivel y XP del usuario. INDEPENDIENTE del Panteón: premia constancia y trabajo,
 * no solo la fuerza. Así hasta un principiante progresa aunque no suba de rango.
 */
data class LevelState(
    val level: Int = 1,
    val totalXp: Long = 0,
    val xpIntoLevel: Long = 0,
    val xpForLevel: Long = 100,
    val progress: Float = 0f
)

object LevelSystem {

    private const val XP_PER_SESSION = 50.0
    private const val XP_PER_1000KG = 20.0    // 20 XP por cada 1000 kg de volumen
    private const val XP_PER_STREAK_WEEK = 30.0

    /** Calcula nivel y XP a partir del historial y la racha (en semanas). */
    fun compute(history: List<WorkoutSession>, streakWeeks: Int): LevelState {
        var xp = 0.0
        for (s in history) {
            val volume = s.exercisesProgress.sumOf { p ->
                p.sets.filter { it.isCompleted }.sumOf { it.weight * it.reps }
            }
            xp += XP_PER_SESSION + (volume / 1000.0) * XP_PER_1000KG
        }
        xp += streakWeeks.coerceAtLeast(0) * XP_PER_STREAK_WEEK
        return fromTotalXp(xp.toLong())
    }

    // XP acumulada necesaria para ESTAR en el nivel L (nivel 1 = 0 XP).
    private fun cumXpForLevel(level: Int): Long = 50L * (level - 1) * level

    private fun fromTotalXp(total: Long): LevelState {
        var level = 1
        while (cumXpForLevel(level + 1) <= total) level++
        val floor = cumXpForLevel(level)
        val ceil = cumXpForLevel(level + 1)
        val into = total - floor
        val need = ceil - floor
        val progress = if (need > 0) (into.toDouble() / need).toFloat().coerceIn(0f, 1f) else 0f
        return LevelState(level, total, into, need, progress)
    }
}
