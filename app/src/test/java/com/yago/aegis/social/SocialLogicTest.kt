package com.yago.aegis.social

import com.yago.aegis.data.social.Friendship
import com.yago.aegis.data.social.UsernameRules
import com.yago.aegis.data.social.bucketsFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Social: validación de @usuario y separación de amistades (lógica pura). */
class SocialLogicTest {

    @Test
    fun username_valida_formato() {
        assertTrue(UsernameRules.isValid("yago_92"))
        assertTrue(UsernameRules.isValid("abc"))
        assertFalse("muy corto", UsernameRules.isValid("ab"))
        assertFalse("espacios", UsernameRules.isValid("con espacios"))
        assertFalse("mayúsculas", UsernameRules.isValid("Mayus"))
        assertFalse("tildes/ñ", UsernameRules.isValid("añejo"))
    }

    @Test
    fun username_normaliza_arroba_espacios_y_mayus() {
        assertEquals("yago", UsernameRules.normalize("  @Yago "))
    }

    @Test
    fun buckets_separa_aceptadas_recibidas_y_enviadas() {
        val me = "ME"
        val list = listOf(
            Friendship(users = listOf(me, "A"), requestedBy = me, status = "accepted"),
            Friendship(users = listOf(me, "B"), requestedBy = me, status = "pending"),   // yo pedí -> outgoing
            Friendship(users = listOf("C", me), requestedBy = "C", status = "pending")   // me pidieron -> incoming
        )

        val b = list.bucketsFor(me)

        assertEquals(listOf("A"), b.friends)
        assertEquals(listOf("B"), b.outgoing)
        assertEquals(listOf("C"), b.incoming)
    }
}
