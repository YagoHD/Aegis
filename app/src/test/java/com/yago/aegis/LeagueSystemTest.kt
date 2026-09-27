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

    /** Sesión en [date] con [nSets] series (todas completadas salvo [completed]=false). */
    private fun session(date: Long, nSets: Int, completed: Boolean = true) =
        WorkoutSession(
            routineName = "T",
            date = date,
            exercisesProgress = listOf(
                ExerciseProgress(
                    exercise = ex(),
                    sets = List(nSets) { ExerciseSet(reps = 10, weight = 100.0, isCompleted = completed) }
                )
            )
        )

    private val now = System.currentTimeMillis()
    private val bounds = LeagueSystem.seasonBounds(now)
    private val inMonth get() = bounds.first + 1000                 // dentro del mes actual
    private val nextDay get() = bounds.first + 86_400_000L + 1000   // día siguiente (mismo mes)
    private val outMonth get() = bounds.first - 1000                // mes anterior

    @Test
    fun points_daysAndSets_noWeight() {
        // 1 día, 5 series completadas, sin racha -> 100*1 + 2*5 = 110. El PESO no influye.
        val sc = LeagueSystem.computeSeason(listOf(session(inMonth, 5)), streakWeeks = 0, now = now)
        assertEquals(1, sc.daysTrained)
        assertEquals(5, sc.completedSets)
        assertEquals(110L, sc.points)
    }

    @Test
    fun streak_addsBonus() {
        val sc = LeagueSystem.computeSeason(emptyList(), streakWeeks = 3, now = now)
        assertEquals(0, sc.daysTrained)
        assertEquals(90L, sc.points)   // 3 * 30
    }

    @Test
    fun distinctDays_multipleSessionsSameDayCountOnce() {
        val same = LeagueSystem.computeSeason(listOf(session(inMonth, 3), session(inMonth, 3)), 0, now)
        assertEquals(1, same.daysTrained)   // 2 sesiones el mismo día = 1 día
        val diff = LeagueSystem.computeSeason(listOf(session(inMonth, 3), session(nextDay, 3)), 0, now)
        assertEquals(2, diff.daysTrained)   // 2 días distintos = 2
    }

    @Test
    fun noCompletedSets_dayDoesNotCount() {
        val sc = LeagueSystem.computeSeason(listOf(session(inMonth, 4, completed = false)), 0, now)
        assertEquals(0, sc.daysTrained)     // abrir una sesión sin completar nada no cuenta
        assertEquals(0, sc.completedSets)
        assertEquals(0L, sc.points)
    }

    @Test
    fun setsCappedPerSession() {
        // 20 series en una sesión -> topan a 10 -> 100 (1 día) + 2*10 = 120.
        val sc = LeagueSystem.computeSeason(listOf(session(inMonth, 20)), 0, now)
        assertEquals(1, sc.daysTrained)
        assertEquals(10, sc.completedSets)
        assertEquals(120L, sc.points)
    }

    @Test
    fun onlySessionsInCurrentMonthCount() {
        val sc = LeagueSystem.computeSeason(listOf(session(inMonth, 5), session(outMonth, 5)), 0, now)
        assertEquals(1, sc.daysTrained)     // la del mes anterior se ignora
        assertEquals(110L, sc.points)
    }

    @Test
    fun consistentBeatsMonster() {
        // Constante: 4 días distintos, 8 series/día. Bestia: 1 día, 30 series (topan a 10).
        val consistent = (0 until 4).map { session(bounds.first + 1000 + it * 86_400_000L, 8) }
        val monster = listOf(session(inMonth, 30))
        val cPts = LeagueSystem.computeSeason(consistent, 0, now).points
        val mPts = LeagueSystem.computeSeason(monster, 0, now).points
        assertTrue("constante ($cPts) debe superar a bestia ($mPts)", cPts > mPts)
    }

    @Test
    fun leagueThresholds() {
        assertEquals(RankTier.BRONCE, LeagueSystem.leagueFor(0))
        assertEquals(RankTier.BRONCE, LeagueSystem.leagueFor(699))
        assertEquals(RankTier.PLATA, LeagueSystem.leagueFor(700))
        assertEquals(RankTier.TITAN, LeagueSystem.leagueFor(4500))
    }

    @Test
    fun progressAndNextThreshold() {
        assertEquals(0f, LeagueSystem.progressToNext(0), 0.001f)
        assertEquals(0.5f, LeagueSystem.progressToNext(350), 0.001f)   // 350 de 0..700
        assertEquals(700L, LeagueSystem.nextThreshold(0))
        assertNull(LeagueSystem.nextThreshold(9999))
        assertEquals(1f, LeagueSystem.progressToNext(9999), 0.001f)
    }

    @Test
    fun groupCutoffs_matchFunction() {
        assertEquals(7, LeagueSystem.promoteCount(30, RankTier.ORO))
        assertEquals(5, LeagueSystem.relegateCount(30, RankTier.ORO))
        assertEquals(0, LeagueSystem.relegateCount(30, RankTier.BRONCE))
        assertEquals(0, LeagueSystem.promoteCount(30, RankTier.TITAN))
        assertEquals(0, LeagueSystem.promoteCount(1, RankTier.ORO))
        val p = LeagueSystem.promoteCount(6, RankTier.ORO)
        val r = LeagueSystem.relegateCount(6, RankTier.ORO)
        assertTrue(p + r <= 5)
    }

    @Test
    fun seasonIdFormat() {
        assertEquals(LeagueSystem.seasonIdFor(now), LeagueSystem.seasonIdFor(inMonth))
        assertTrue(LeagueSystem.seasonIdFor(now).matches(Regex("\\d{4}-\\d{2}")))
    }
}
