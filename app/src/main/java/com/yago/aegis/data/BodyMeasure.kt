package com.yago.aegis.data

data class BodyMeasure(
    val id: String,    // Un identificador único (ej: "CHEST")
    val name: String,  // Lo que verá el usuario (ej: "Pecho")
    val value: String  // El número que escriba
)