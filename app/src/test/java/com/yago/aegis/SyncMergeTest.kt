package com.yago.aegis

import com.yago.aegis.data.BodySnapshot
import com.yago.aegis.data.ExerciseProgress
import com.yago.aegis.data.PhotoRecord
import com.yago.aegis.data.SyncMerge
import com.yago.aegis.data.WorkoutSession
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncMergeTest {

    private fun session(id: String, date: Long) =
        WorkoutSession(id = id, routineName = "R", date = date, exercisesProgress = emptyList<ExerciseProgress>())

    @Test
    fun mergeHistory_dedupeByIdSortedByDate() {
        val local = listOf(session("A", 100), session("B", 300))
        val cloud = listOf(session("B", 300), session("C", 200))
        val merged = SyncMerge.mergeHistory(local, cloud)
        assertEquals(listOf("A", "C", "B"), merged.map { it.id })  // dedupe + orden por fecha
    }

    @Test
    fun mergeBodyHistory_dedupeByDate() {
        val local = listOf(BodySnapshot(date = 100), BodySnapshot(date = 200))
        val cloud = listOf(BodySnapshot(date = 200), BodySnapshot(date = 300))
        val merged = SyncMerge.mergeBodyHistory(local, cloud)
        assertEquals(listOf(100L, 200L, 300L), merged.map { it.date })
    }

    @Test
    fun mergePhotoHistory_keepsLocalUriAndAddsCloudDates() {
        val local = listOf(PhotoRecord(date = 100, uri = "content://local"))
        val cloud = listOf(PhotoRecord(date = 100, uri = ""), PhotoRecord(date = 200, uri = ""))
        val merged = SyncMerge.mergePhotoHistory(local, cloud)
        assertEquals(2, merged.size)
        assertEquals("content://local", merged.first { it.date == 100L }.uri) // local gana
        assertEquals("", merged.first { it.date == 200L }.uri)                // fecha solo-nube añadida
    }

    @Test
    fun photoHistoryForCloud_stripsUris() {
        val stripped = SyncMerge.photoHistoryForCloud(listOf(PhotoRecord(date = 1, uri = "x"), PhotoRecord(date = 2, uri = "y")))
        assertEquals(listOf("", ""), stripped.map { it.uri })
        assertEquals(listOf(1L, 2L), stripped.map { it.date })  // conserva las fechas
    }
}
