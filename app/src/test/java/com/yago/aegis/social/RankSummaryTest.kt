package com.yago.aegis.social

import com.yago.aegis.data.GroupRank
import com.yago.aegis.data.MuscleGroup
import com.yago.aegis.data.PanteonResult
import com.yago.aegis.data.RankTier
import com.yago.aegis.data.social.toRankSummary
import org.junit.Assert.assertEquals
import org.junit.Test

/** Social: el resumen compartible extrae los tiers por grupo + el global (más fuerte). */
class RankSummaryTest {

    @Test
    fun toRankSummary_extrae_tiers_por_grupo_y_global() {
        val pecho = GroupRank(MuscleGroup.PECHO, RankTier.ORO, 0.5f, emptyList())
        val pierna = GroupRank(MuscleGroup.PIERNA, RankTier.PLATA, 0.2f, emptyList())
        val result = PanteonResult(groups = listOf(pecho, pierna), strongest = pecho, weakest = pierna)

        val s = result.toRankSummary()

        assertEquals("ORO", s.overall)
        assertEquals("ORO", s.byGroup["PECHO"])
        assertEquals("PLATA", s.byGroup["PIERNA"])
        assertEquals(2, s.byGroup.size)
    }

    @Test
    fun toRankSummary_vacio_da_sin_rango() {
        val s = PanteonResult.EMPTY.toRankSummary()

        assertEquals("SIN_RANGO", s.overall)
        assertEquals(0, s.byGroup.size)
    }
}
