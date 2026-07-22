package com.yago.aegis.data

/**
 * Etiquetas canónicas de la app: un vocabulario CERRADO y fijo.
 * El usuario NO crea ni borra tags — solo elige de esta lista al crear ejercicios.
 * Así los ejercicios base y los del usuario comparten las mismas etiquetas y el
 * filtro de la librería es coherente (antes: un tag propio no mostraba los base).
 */
object AppTags {
    val ALL: List<String> = listOf(
        "PECHO",
        "ESPALDA",
        "HOMBROS",
        "BÍCEPS",
        "TRÍCEPS",
        "ANTEBRAZO",
        "PIERNAS",
        "CORE",
        "PUSH",
        "PULL"
    )
}
