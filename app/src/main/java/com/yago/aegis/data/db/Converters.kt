package com.yago.aegis.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yago.aegis.data.BodyMeasure
import com.yago.aegis.data.ExerciseProgress
import com.yago.aegis.data.ExerciseRecord
import com.yago.aegis.data.ExerciseSlot
import com.yago.aegis.data.MuscleContribution

/**
 * US-03 (Room). Modelo AGREGADO: cada fila es un agregado (ejercicio, rutina, sesión…) y las
 * listas anidadas se guardan como JSON en una columna vía estos TypeConverters. Es el diseño
 * correcto para estos datos tipo "snapshot" (una sesión conserva copias de los ejercicios de
 * ese día); normalizar rompería esa semántica. Room aporta acceso granular y trabajo fuera de Main.
 */
class Converters {
    private val gson = Gson()

    @TypeConverter fun stringListToJson(v: List<String>?): String = gson.toJson(v ?: emptyList<String>())
    @TypeConverter fun jsonToStringList(s: String?): List<String> =
        if (s.isNullOrEmpty()) emptyList() else gson.fromJson(s, object : TypeToken<List<String>>() {}.type)

    @TypeConverter fun longListToJson(v: List<Long>?): String = gson.toJson(v ?: emptyList<Long>())
    @TypeConverter fun jsonToLongList(s: String?): List<Long> =
        if (s.isNullOrEmpty()) emptyList() else gson.fromJson(s, object : TypeToken<List<Long>>() {}.type)

    @TypeConverter fun recordsToJson(v: List<ExerciseRecord>?): String = gson.toJson(v ?: emptyList<ExerciseRecord>())
    @TypeConverter fun jsonToRecords(s: String?): List<ExerciseRecord> =
        if (s.isNullOrEmpty()) emptyList() else gson.fromJson(s, object : TypeToken<List<ExerciseRecord>>() {}.type)

    @TypeConverter fun contributionsToJson(v: List<MuscleContribution>?): String = gson.toJson(v ?: emptyList<MuscleContribution>())
    @TypeConverter fun jsonToContributions(s: String?): List<MuscleContribution> =
        if (s.isNullOrEmpty()) emptyList() else gson.fromJson(s, object : TypeToken<List<MuscleContribution>>() {}.type)

    @TypeConverter fun slotsToJson(v: List<ExerciseSlot>?): String = gson.toJson(v ?: emptyList<ExerciseSlot>())
    @TypeConverter fun jsonToSlots(s: String?): List<ExerciseSlot> =
        if (s.isNullOrEmpty()) emptyList() else gson.fromJson(s, object : TypeToken<List<ExerciseSlot>>() {}.type)

    @TypeConverter fun progressToJson(v: List<ExerciseProgress>?): String = gson.toJson(v ?: emptyList<ExerciseProgress>())
    @TypeConverter fun jsonToProgress(s: String?): List<ExerciseProgress> =
        if (s.isNullOrEmpty()) emptyList() else gson.fromJson(s, object : TypeToken<List<ExerciseProgress>>() {}.type)

    @TypeConverter fun measuresToJson(v: List<BodyMeasure>?): String = gson.toJson(v ?: emptyList<BodyMeasure>())
    @TypeConverter fun jsonToMeasures(s: String?): List<BodyMeasure> =
        if (s.isNullOrEmpty()) emptyList() else gson.fromJson(s, object : TypeToken<List<BodyMeasure>>() {}.type)
}
