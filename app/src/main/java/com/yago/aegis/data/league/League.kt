package com.yago.aegis.data.league

import com.yago.aegis.data.RankTier
import com.yago.aegis.data.WorkoutSession
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.roundToLong

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
    val sessions: Int = 0,
    val relativeWork: Double = 0.0,
    val tier: String = RankTier.BRONCE.name,   // liga provisional (nombre de RankTier)
    val updatedAt: Long = 0L
)

/** Puntos de esfuerzo de una temporada + su desglose. */
data class LeagueScore(
    val points: Long,
    val sessions: Int,
    val relativeWork: Double
)

object LeagueSystem {

    // Reutiliza la filosofía de LevelSystem (50/sesión, 30/semana de racha), pero el trabajo es
    // RELATIVO al peso corporal (justo entre un usuario de 60 kg y otro de 100 kg), no absoluto.
    private const val PTS_PER_SESSION = 50.0
    private const val PTS_PER_STREAK_WEEK = 30.0

    /** Peso del trabajo relativo. PROVISIONAL: se calibrará con datos reales (Fase 2). */
    const val K = 1.0

    /** Umbrales de liga por puntos (Fase 1). En Fase 2 lo sustituye el ascenso/descenso real. */
    private val THRESHOLDS: List<Pair<Long, RankTier>> = listOf(
        0L to RankTier.BRONCE,
        1000L to RankTier.PLATA,
        2500L to RankTier.ORO,
        5000L to RankTier.PLATINO,
        9000L to RankTier.DIAMANTE,
        15000L to RankTier.TITAN
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
     * Puntos de esfuerzo de la temporada que contiene [now], desde el historial.
     * Solo cuentan sesiones DENTRO del mes natural y **series completadas**. Trabajo relativo al peso.
     */
    fun computeSeason(
        history: List<WorkoutSession>,
        bodyweightKg: Double,
        streakWeeks: Int,
        now: Long = System.currentTimeMillis()
    ): LeagueScore {
        val (start, end) = seasonBounds(now)
        // Evita dividir por ~0 si el usuario no ha rellenado su peso todavía.
        val bw = bodyweightKg.takeIf { it > 1.0 } ?: 1.0
        val monthSessions = history.filter { it.date in start until end }
        var relWork = 0.0
        for (s in monthSessions) {
            for (p in s.exercisesProgress) {
                for (set in p.sets) {
                    if (set.isCompleted) relWork += (set.weight * set.reps) / bw
                }
            }
        }
        val n = monthSessions.size
        val pts = PTS_PER_SESSION * n + K * relWork + PTS_PER_STREAK_WEEK * streakWeeks.coerceAtLeast(0)
        return LeagueScore(pts.roundToLong(), n, relWork)
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
}
