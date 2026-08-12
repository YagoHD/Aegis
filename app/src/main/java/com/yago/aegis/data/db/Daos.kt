package com.yago.aegis.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * DAOs (US-03). Cada uno expone:
 *  - observeAll(): Flow reactivo, ejecutado FUERA de Main por Room.
 *  - getAll(): lectura puntual (para el sync/merge).
 *  - upsert/delete de UNA fila: escritura granular (editar 1 ejercicio no reescribe los 201).
 *  - replaceAll(): reemplazo transaccional del conjunto (equivalente al saveX actual).
 */

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises") fun observeAll(): Flow<List<ExerciseEntity>>
    @Query("SELECT * FROM exercises") suspend fun getAll(): List<ExerciseEntity>
    @Upsert suspend fun upsert(item: ExerciseEntity)
    @Upsert suspend fun upsertAll(items: List<ExerciseEntity>)
    @Delete suspend fun delete(item: ExerciseEntity)
    @Query("DELETE FROM exercises") suspend fun clear()
    @Transaction suspend fun replaceAll(items: List<ExerciseEntity>) { clear(); upsertAll(items) }
}

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines") fun observeAll(): Flow<List<RoutineEntity>>
    @Query("SELECT * FROM routines") suspend fun getAll(): List<RoutineEntity>
    @Upsert suspend fun upsert(item: RoutineEntity)
    @Upsert suspend fun upsertAll(items: List<RoutineEntity>)
    @Delete suspend fun delete(item: RoutineEntity)
    @Query("DELETE FROM routines") suspend fun clear()
    @Transaction suspend fun replaceAll(items: List<RoutineEntity>) { clear(); upsertAll(items) }
}

@Dao
interface WorkoutSessionDao {
    @Query("SELECT * FROM workout_sessions ORDER BY date ASC") fun observeAll(): Flow<List<WorkoutSessionEntity>>
    @Query("SELECT * FROM workout_sessions ORDER BY date ASC") suspend fun getAll(): List<WorkoutSessionEntity>
    @Upsert suspend fun upsert(item: WorkoutSessionEntity)
    @Upsert suspend fun upsertAll(items: List<WorkoutSessionEntity>)
    @Delete suspend fun delete(item: WorkoutSessionEntity)
    @Query("DELETE FROM workout_sessions") suspend fun clear()
    @Transaction suspend fun replaceAll(items: List<WorkoutSessionEntity>) { clear(); upsertAll(items) }
}

@Dao
interface BodySnapshotDao {
    @Query("SELECT * FROM body_snapshots ORDER BY date ASC") fun observeAll(): Flow<List<BodySnapshotEntity>>
    @Query("SELECT * FROM body_snapshots ORDER BY date ASC") suspend fun getAll(): List<BodySnapshotEntity>
    @Upsert suspend fun upsert(item: BodySnapshotEntity)
    @Upsert suspend fun upsertAll(items: List<BodySnapshotEntity>)
    @Query("DELETE FROM body_snapshots") suspend fun clear()
    @Transaction suspend fun replaceAll(items: List<BodySnapshotEntity>) { clear(); upsertAll(items) }
}

@Dao
interface PhotoRecordDao {
    @Query("SELECT * FROM photo_records ORDER BY date ASC") fun observeAll(): Flow<List<PhotoRecordEntity>>
    @Query("SELECT * FROM photo_records ORDER BY date ASC") suspend fun getAll(): List<PhotoRecordEntity>
    @Upsert suspend fun upsert(item: PhotoRecordEntity)
    @Upsert suspend fun upsertAll(items: List<PhotoRecordEntity>)
    @Query("DELETE FROM photo_records") suspend fun clear()
    @Transaction suspend fun replaceAll(items: List<PhotoRecordEntity>) { clear(); upsertAll(items) }
}
