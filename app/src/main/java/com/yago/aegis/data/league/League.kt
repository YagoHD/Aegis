package com.yago.aegis.data.league

import com.yago.aegis.data.RankTier
import com.yago.aegis.data.WorkoutSession
import java.util.Calendar
import java.util.TimeZone

/**
 * LIGA mensual global (US-LIGA, Fase 1).
 *
 * A diferencia del Panteón (fuerza/identidad, lento), la Liga mide el **esfuerzo del mes**
 * relativo al peso corporal y se reinicia cada mes natural: todo el mundo puede subir entrenando.
 *
 * Fase 1 = solo cliente + Firestore: puntos calculados aquí, tabla mundial ordenada por puntos y
 * liga (tier) PROVISIONAL por umbrales. El ascenso/descenso real y el cierre mensual llegan en la
 * Fase 2 (Cloud Functions). Ver docs/us-liga.md.
 */

/** Mi entrada en la liga de la temporada actual (doc global `leagueMembers/{uid}`). */
data class LeagueEntry(
    val uid: String = "",
    val seasonId: String = "",          // "2026-09"
    val username: String = "",
    val avatar: String = "",            // thumbnail JPEG en base64 (<=128px); vacío = sin foto
    val points: Long = 0,
    val daysTrained: Int = 0,                  // días distintos entrenados este mes (fórmula de frecuencia)
    val tier: String = RankTier.BRONCE.name,   // liga provisional por umbrales (Fase 1)
    // Fijados por el SERVIDOR en Fase 2 (Cloud Functions): liga real (ganada por ascenso) y grupo.
    // Vacíos mientras las Functions no estén desplegadas → el cliente cae a la tabla mundial (Fase 1).
    val league: String = "",
    val groupId: String = "",
    val updatedAt: Long = 0L
)

/** Medalla de una temporada cerrada (doc `leagueMedals/{uid}/seasons/{seasonId}`). Solo la escribe el servidor. */
data class LeagueMedal(
    val seasonId: String = "",
    val league: String = "",            // nombre de RankTier
    val position: Int = 0,
    val groupSize: Int = 0,
    val movement: String = "stay"       // "up" | "down" | "stay"
)

/** Puntos de esfuerzo de una temporada + su desglose. */
data class LeagueScore(
    val points: Long,
    val daysTrained: Int,
    val completedSets: Int
)

object LeagueSystem {

    // Filosofía: premiar IR A ENTRENAR. Manda la FRECUENCIA (días distintos entrenados), no la
    // fuerza ni el volumen: ni kilos, ni 1RM, ni % graso, ni peso corporal entran en la cuenta.
    // Un día bestia de 1.000.000 kg NO puede con varios días constantes.
    private const val PTS_PER_DAY = 100          // lo que MÁS pesa: cada día distinto entrenado
    private const val PTS_PER_STREAK_WEEK = 30   // constancia semana a semana
    private const val PTS_PER_SET = 2            // "ganas" (series completadas)...
    private const val SETS_CAP_PER_SESSION = 10  // ...pero topadas por sesión (un día no se dispara)

    /** Umbrales de liga por puntos (solo para el display del fallback Fase 1; en grupo manda el servidor). */
    private val THRESHOLDS: List<Pair<Long, RankTier>> = listOf(
        0L to RankTier.BRONCE,
        700L to RankTier.PLATA,
        1400L to RankTier.ORO,
        2200L to RankTier.PLATINO,
        3200L to RankTier.DIAMANTE,
        4500L to RankTier.TITAN
    )

    /** seasonId (mes natural, UTC) de una fecha: "2026-09". */
    fun seasonIdFor(epochMillis: Long): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.timeInMillis = epochMillis
        return "%04d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    /** Rango [inicio, fin) del mes natural (UTC) que contiene [epochMillis]. */
    fun seasonBounds(epochMillis: Long): Pair<Long, Long> {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.timeInMillis = epochMillis
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        return start to cal.timeInMillis
    }

    /**
     * Puntos de la temporada que contiene [now], desde el historial. Premia la FRECUENCIA:
     * días distintos entrenados (con al menos una serie completada) + racha + series completadas
     * (topadas por sesión). NO usa peso, 1RM, % graso ni peso corporal.
     */
    fun computeSeason(
        history: List<WorkoutSession>,
        streakWeeks: Int,
        now: Long = System.currentTimeMillis()
    ): LeagueScore {
        val (start, end) = seasonBounds(now)
        val monthSessions = history.filter { it.date in start until end }
        // Un día cuenta solo si tuvo al menos una serie completada (no premia abrir sesiones vacías).
        val daysTrained = monthSessions
            .filter { s -> s.exercisesProgress.any { p -> p.sets.any { it.isCompleted } } }
            .map { it.date / 86_400_000L }   // índice de día (UTC)
            .toSet().size
        // "Ganas": series completadas por sesión, con tope para que un día no se dispare.
        var cappedSets = 0
        for (s in monthSessions) {
            val completed = s.exercisesProgress.sumOf { p -> p.sets.count { it.isCompleted } }
            cappedSets += completed.coerceAtMost(SETS_CAP_PER_SESSION)
        }
        val pts = PTS_PER_DAY * daysTrained +
                  PTS_PER_STREAK_WEEK * streakWeeks.coerceAtLeast(0) +
                  PTS_PER_SET * cappedSets
        return LeagueScore(pts.toLong(), daysTrained, cappedSets)
    }

    /** Liga (tier) provisional a partir de los puntos, por umbrales (Fase 1). */
    fun leagueFor(points: Long): RankTier =
        THRESHOLDS.last { points >= it.first }.second

    /** Umbral de la liga actual (piso). */
    fun currentThreshold(points: Long): Long =
        THRESHOLDS.last { points >= it.first }.first

    /** Puntos del siguiente umbral, o null si ya está en la liga más alta (Titán). */
    fun nextThreshold(points: Long): Long? =
        THRESHOLDS.firstOrNull { it.first > points }?.first

    /** Progreso [0,1] hacia la siguiente liga (1 si ya es Titán), para la barra. */
    fun progressToNext(points: Long): Float {
        val cur = currentThreshold(points)
        val next = nextThreshold(points) ?: return 1f
        val span = (next - cur).coerceAtLeast(1)
        return ((points - cur).toFloat() / span).coerceIn(0f, 1f)
    }

    // --- Zonas de ascenso/descenso del grupo (Fase 2). Debe COINCIDIR con functions/index.js. ---
    const val GROUP_SIZE = 30
    private const val PROMOTE_TOP = 7
    private const val RELEGATE_BOTTOM = 5

    /** Cuántos suben (arriba) y bajan (abajo) en un grupo de [size] de la liga [league]. */
    private fun cutoffs(size: Int, league: RankTier): Pair<Int, Int> {
        if (size <= 1) return 0 to 0
        var promote = if (league == RankTier.TITAN) 0 else minOf(PROMOTE_TOP, size / 3)
        var relegate = if (league == RankTier.BRONCE) 0 else minOf(RELEGATE_BOTTOM, size / 4)
        while (promote + relegate > size - 1 && (promote > 0 || relegate > 0)) {
            if (relegate >= promote && relegate > 0) relegate-- else if (promote > 0) promote-- else break
        }
        return promote to relegate
    }

    /** Nº de puestos de ascenso (los N primeros del grupo suben de liga). */
    fun promoteCount(size: Int, league: RankTier): Int = cutoffs(size, league).first

    /** Nº de puestos de descenso (los N últimos del grupo bajan de liga). */
    fun relegateCount(size: Int, league: RankTier): Int = cutoffs(size, league).second
}
