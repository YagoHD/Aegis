package com.yago.aegis.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yago.aegis.data.BodyMeasure
import com.yago.aegis.data.BodySnapshot
import com.yago.aegis.data.Exercise
import com.yago.aegis.data.ExerciseProgress
import com.yago.aegis.data.ExerciseRecord
import com.yago.aegis.data.ExerciseSlot
import com.yago.aegis.data.MuscleContribution
import com.yago.aegis.data.PhotoRecord
import com.yago.aegis.data.Routine
import com.yago.aegis.data.WorkoutSession
import com.yago.aegis.data.effectiveSlots

// Entidades Room (US-03). Una por agregado. Las listas se serializan vía [Converters].
// Los campos de lista son NON-NULL en la entidad (la nulabilidad del dominio se resuelve en el mapper).

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val type: String,
    val muscleGroup: String,
    val iconName: String,
    val notes: String,
    val isBodyweight: Boolean,
    val lastPerformance: String,
    val oneRepMax: Double,
    val bestSet: String?,
    val loadType: String?,
    val tags: List<String>,
    val history: List<ExerciseRecord>,
    val muscleContributions: List<MuscleContribution>
)

fun ExerciseEntity.toDomain(): Exercise = Exercise(
    name = name, type = type, muscleGroup = muscleGroup, id = id, tags = tags,
    iconName = iconName, notes = notes, isBodyweight = isBodyweight,
    lastPerformance = lastPerformance, oneRepMax = oneRepMax, bestSet = bestSet,
    history = history, muscleContributions = muscleContributions, loadType = loadType
)

fun Exercise.toEntity(): ExerciseEntity = ExerciseEntity(
    id = id, name = name, type = type, muscleGroup = muscleGroup, iconName = iconName,
    notes = notes, isBodyweight = isBodyweight, lastPerformance = lastPerformance,
    oneRepMax = oneRepMax, bestSet = bestSet, loadType = loadType, tags = tags,
    history = history, muscleContributions = muscleContributions ?: emptyList()
)

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val iconName: String?,
    val exerciseSlots: List<ExerciseSlot>,
    val lastCompletedDates: List<Long>
)

// Al leer reconstruimos la lista plana legacy `exercises` (1ª variante por slot), IGUAL que hace
// el guardado (RoutinesViewModel): la leen RoutineCard (nº), RoutineSelectionCard (preview) y
// SelectRoutineScreen (tags). Si no, tras migrar mostrarían "0 ejercicios".
fun RoutineEntity.toDomain(): Routine = Routine(
    id = id, name = name,
    exercises = exerciseSlots.mapNotNull { it.variants.firstOrNull() },
    exerciseSlots = exerciseSlots, iconName = iconName, lastCompletedDates = lastCompletedDates
)

// Al escribir, normalizamos legacy `exercises` -> slots con effectiveSlots() (no se pierde nada).
fun Routine.toEntity(): RoutineEntity = RoutineEntity(
    id = id, name = name, iconName = iconName,
    exerciseSlots = effectiveSlots(), lastCompletedDates = lastCompletedDates ?: emptyList()
)

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey val id: String,
    val routineName: String,
    val date: Long,
    val notes: String,
    val exercisesProgress: List<ExerciseProgress>
)

fun WorkoutSessionEntity.toDomain(): WorkoutSession = WorkoutSession(
    id = id, routineName = routineName, date = date, exercisesProgress = exercisesProgress, notes = notes
)

fun WorkoutSession.toEntity(): WorkoutSessionEntity = WorkoutSessionEntity(
    id = id, routineName = routineName, date = date, notes = notes, exercisesProgress = exercisesProgress
)

@Entity(tableName = "body_snapshots")
data class BodySnapshotEntity(
    @PrimaryKey val date: Long,     // el historial corporal se deduplica por fecha (SyncMerge)
    val mass: String,
    val bodyFat: String,
    val customMeasures: List<BodyMeasure>
)

fun BodySnapshotEntity.toDomain(): BodySnapshot = BodySnapshot(date, mass, bodyFat, customMeasures)
fun BodySnapshot.toEntity(): BodySnapshotEntity = BodySnapshotEntity(date, mass, bodyFat, customMeasures)

@Entity(tableName = "photo_records")
data class PhotoRecordEntity(
    @PrimaryKey val date: Long,     // el registro de fotos se deduplica por fecha (SyncMerge)
    val uri: String,
    val dateLabel: String
)

fun PhotoRecordEntity.toDomain(): PhotoRecord = PhotoRecord(date, uri, dateLabel)
fun PhotoRecord.toEntity(): PhotoRecordEntity = PhotoRecordEntity(date, uri, dateLabel)
