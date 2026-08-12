package com.yago.aegis.data.db

import com.yago.aegis.data.SettingsStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * US-03 Fase 2: vuelca las colecciones COMPLEJAS de DataStore a Room UNA sola vez.
 *
 * - Idempotente: no hace nada si el flag `roomMigrated` ya está puesto.
 * - DEDUPLICA por PK (distinctBy id/fecha) ANTES de escribir, respetando la semántica del sync
 *   (históricamente hubo ids de sesión duplicados). Sin esto, el upsert colapsaría filas
 *   silenciosamente y se perderían datos.
 * - NO borra DataStore: sigue siendo la fuente de verdad hasta la Fase 3 (y queda como backup).
 * - Si algo falla, el flag NO se marca (se pone al final) → se reintenta el próximo arranque;
 *   `replaceAll` limpia antes de insertar, así que reintentar es idempotente. DataStore intacto.
 *
 * Nota: tags (List<String>) y las preferencias simples se quedan en DataStore (no van a Room).
 */
class RoomMigrator(
    private val settingsStore: SettingsStore,
    private val db: AegisDatabase
) {
    // Serializa la migración: aunque la disparen a la vez el arranque y la 1ª lectura del repo,
    // se ejecuta UNA sola vez (doble-check del flag dentro del lock).
    private val mutex = Mutex()

    suspend fun migrateIfNeeded() {
        if (settingsStore.roomMigrated.first()) return
        mutex.withLock {
            if (settingsStore.roomMigrated.first()) return

            val exercises = settingsStore.exerciseLibrary.first().distinctBy { it.id }
            val routines = settingsStore.routines.first().distinctBy { it.id }
            val history = settingsStore.workoutHistory.first().distinctBy { it.id }.sortedBy { it.date }
            val body = settingsStore.bodyHistory.first().distinctBy { it.date }
            val photos = settingsStore.photoHistory.first().distinctBy { it.date }

            db.exerciseDao().replaceAll(exercises.map { it.toEntity() })
            db.routineDao().replaceAll(routines.map { it.toEntity() })
            db.workoutSessionDao().replaceAll(history.map { it.toEntity() })
            db.bodySnapshotDao().replaceAll(body.map { it.toEntity() })
            db.photoRecordDao().replaceAll(photos.map { it.toEntity() })

            settingsStore.saveRoomMigrated(true)
        }
    }
}
