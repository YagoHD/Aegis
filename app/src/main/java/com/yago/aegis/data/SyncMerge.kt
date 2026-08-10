package com.yago.aegis.data

/**
 * Lógica PURA de fusión local↔nube (US-02/US-08). Sin dependencias de Android/Firebase,
 * para poder testearla directamente. La estrategia por entidad se documenta aquí.
 */
object SyncMerge {

    /** Historial de entrenos: unión deduplicada por id, ordenada por fecha. */
    fun mergeHistory(local: List<WorkoutSession>, cloud: List<WorkoutSession>): List<WorkoutSession> =
        (local + cloud).distinctBy { it.id }.sortedBy { it.date }

    /** Historial corporal: unión deduplicada por fecha, ordenada. */
    fun mergeBodyHistory(local: List<BodySnapshot>, cloud: List<BodySnapshot>): List<BodySnapshot> =
        (local + cloud).distinctBy { it.date }.sortedBy { it.date }

    /**
     * Registro de fotos: merge por fecha CONSERVANDO la URI local (la nube trae uri vacía porque
     * las imágenes no se sincronizan). Local gana; se añaden las fechas que solo están en la nube.
     */
    fun mergePhotoHistory(local: List<PhotoRecord>, cloud: List<PhotoRecord>): List<PhotoRecord> {
        val byDate = local.associateBy { it.date }.toMutableMap()
        for (p in cloud) if (!byDate.containsKey(p.date)) byDate[p.date] = p
        return byDate.values.sortedBy { it.date }
    }

    /** Quita las imágenes (URIs locales) antes de subir el registro de fotos a la nube. */
    fun photoHistoryForCloud(list: List<PhotoRecord>): List<PhotoRecord> = list.map { it.copy(uri = "") }
}
