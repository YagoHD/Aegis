package com.yago.aegis.data

/**
 * Cálculos PUROS de entrenamiento (volumen, 1RM), sin dependencias de Android, para poder
 * testearlos y reutilizarlos (finalizar sesión, Stats, XP, Panteón).
 */
object WorkoutStats {

    /** 1RM estimado por la fórmula de Epley. */
    fun epley1RM(weight: Double, reps: Int): Double = weight * (1 + reps / 30.0)

    /** Mejor 1RM (Epley) entre las series completadas con peso de un progreso. 0 si no hay. */
    fun best1RM(progress: ExerciseProgress): Double =
        progress.sets.filter { it.isCompleted && it.weight > 0.0 }
            .maxOfOrNull { epley1RM(it.weight, it.reps) } ?: 0.0

    /** Volumen (peso × reps) de todas las series COMPLETADAS de una sesión. */
    fun sessionVolume(session: WorkoutSession): Double =
        session.exercisesProgress.sumOf { volumeOf(it) }

    /** Volumen (peso × reps) de las series completadas de un progreso. */
    fun volumeOf(progress: ExerciseProgress): Double =
        progress.sets.filter { it.isCompleted }.sumOf { it.weight * it.reps }
}
