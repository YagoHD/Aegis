package com.yago.aegis.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Base de datos Room de Aegis (US-03). Guarda los tipos COMPLEJOS (ejercicios, rutinas, historial,
 * cuerpo, fotos). Las preferencias simples (toggles, timer, sesión activa en curso) siguen en
 * DataStore. version=1: primera versión; la "migración" real es un volcado one-shot desde DataStore
 * en el primer arranque (fase siguiente), no una migración de esquema Room.
 */
@Database(
    entities = [
        ExerciseEntity::class,
        RoutineEntity::class,
        WorkoutSessionEntity::class,
        BodySnapshotEntity::class,
        PhotoRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AegisDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun routineDao(): RoutineDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun bodySnapshotDao(): BodySnapshotDao
    abstract fun photoRecordDao(): PhotoRecordDao

    companion object {
        @Volatile private var INSTANCE: AegisDatabase? = null

        fun get(context: Context): AegisDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AegisDatabase::class.java,
                    "aegis.db"
                ).build().also { INSTANCE = it }
            }
    }
}
