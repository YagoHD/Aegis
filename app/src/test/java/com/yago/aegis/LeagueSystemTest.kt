package com.yago.aegis

import com.yago.aegis.data.Exercise
import com.yago.aegis.data.ExerciseProgress
import com.yago.aegis.data.ExerciseSet
import com.yago.aegis.data.RankTier
import com.yago.aegis.data.WorkoutSession
import com.yago.aegis.data.league.LeagueSystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LeagueSystemTest {

    private fun ex() = Exercise(name = "X", type = "COMPOUND", muscleGroup = "CHEST")

    private fun session(date: Long, weight: Double, reps: Int, completed: Boolean = true) =
        WorkoutSession(
            routineName = "T",
            date = date,
            exercisesProgress = listOf(
                ExerciseProgress(
                    exercise = ex(),
                    sets = listOf(ExerciseSet(reps = reps, weight = weight, isCompleted = completed))
                )
            )
        )

    private val now = System.currentTimeMillis()
    private val bounds = LeagueSystem.seasonBounds(now)
    private val inMonth get() = bounds.first + 1000        // dentro del mes actual
    private val outMonth get() = bounds.first - 1000       // mes anterior

    @Test
    fun points_formula_relativeToBodyweight() {
        // 100kg x 10 = 1000 kg·reps; /100 kg peso = 10 de trabajo relativo.
        // puntos = 50*1 sesion + K(1.0)*10 + 30*0 racha = 60.
        val score = LeagueSystem.computeSeason(listOf(session(inMonth, 100.0, 10)), bodyweightKg = 100.0, streakWeeks = 0, now = now)
        assertEquals(1, score.sessions)
        assertEquals(10.0, score.relativeWork, 0.001)
        assertEquals(60L, score.points)
    }

    @Test
    fun streak_addsBonus() {
        val score = LeagueSystem.computeSeason(emptyList(), bodyweightKg = 80.0, streakWeeks = 3, now = now)
        assertEquals(0, score.sessions)
        assertEquals(90L, score.points)   // 3 * 30
    }

    @Test
    fun onlySessionsInCurrentMonthCount() {
        val history = listOf(session(inMonth, 100.0, 10), session(outMonth, 100.0, 10))
        val score = LeagueSystem.computeSeason(history, bodyweightKg = 100.0, streakWeeks = 0, now = now)
        assertEquals(1, score.sessions)      // la del mes anterior se ignora
        assertEquals(60L, score.points)
    }

    @Test
    fun onlyCompletedSetsCount() {
        val history = listOf(session(inMonth, 100.0, 10, completed = false))
        val score = LeagueSystem.computeSeason(history, bodyweightKg = 100.0, streakWeeks = 0, now = now)
        assertEquals(1, score.sessions)      // la sesión cuenta como sesión
        assertEquals(0.0, score.relativeWork, 0.001)  // ...pero la serie no completada no suma trabajo
        assertEquals(50L, score.points)
    }

    @Test
    fun bodyweightGuard_noDivideByZero() {
        // peso 0 (sin rellenar) -> usa 1.0; trabajo = 100*10/1 = 1000, no infinito ni NaN.
        val score = LeagueSystem.computeSeason(listOf(session(inMonth, 100.0, 10)), bodyweightKg = 0.0, streakWeeks = 0, now = now)
        assertEquals(1000.0, score.relativeWork, 0.001)
        assertTrue(score.points > 0)
    }

    @Test
    fun leagueThresholds() {
        assertEquals(RankTier.BRONCE, LeagueSystem.leagueFor(0))
        assertEquals(RankTier.BRONCE, LeagueSystem.leagueFor(999))
        assertEquals(RankTier.PLATA, LeagueSystem.leagueFor(1000))
        assertEquals(RankTier.DIAMANTE, LeagueSystem.leagueFor(14999))
        assertEquals(RankTier.TITAN, LeagueSystem.leagueFor(15000))
    }

    @Test
    fun progressAndNextThreshold() {
        assertEquals(0f, LeagueSystem.progressToNext(0), 0.001f)
        assertEquals(0.5f, LeagueSystem.progressToNext(500), 0.001f)   // 500 de 0..1000
        assertEquals(1000L, LeagueSystem.nextThreshold(0))
        // Titán: sin siguiente liga, progreso lleno.
        assertNull(LeagueSystem.nextThreshold(20000))
        assertEquals(1f, LeagueSystem.progressToNext(20000), 0.001f)
    }

    @Test
    fun seasonIdFormat() {
        // seasonId del inicio del mes actual coincide con el de "ahora".
        assertEquals(LeagueSystem.seasonIdFor(now), LeagueSystem.seasonIdFor(inMonth))
        assertTrue(LeagueSystem.seasonIdFor(now).matches(Regex("\\d{4}-\\d{2}")))
    }
}
