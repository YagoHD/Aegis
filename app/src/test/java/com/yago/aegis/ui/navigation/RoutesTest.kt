package com.yago.aegis.ui.navigation

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * US-07 (fix review): el builder de rutas con nombre de ejercicio debe codificar caracteres
 * reservados para no romper el matching de `edit_exercise/{exerciseName}` (un solo segmento).
 * Robolectric porque Uri.encode/decode son APIs de Android.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoutesTest {

    @Test
    fun editExercise_codifica_reservados_y_hace_round_trip() {
        val name = "PRESS/INCLINADO? A"
        val route = Routes.editExercise(name)
        val arg = route.removePrefix("edit_exercise/")

        // No debe quedar ningún separador crudo que rompa el segmento de la ruta.
        assertFalse("no debe haber '/' crudo", arg.contains("/"))
        assertFalse("no debe haber '?' crudo", arg.contains("?"))

        // Navigation decodifica el argumento al leerlo -> debe recuperarse el nombre original.
        assertEquals(name, Uri.decode(arg))
    }
}
