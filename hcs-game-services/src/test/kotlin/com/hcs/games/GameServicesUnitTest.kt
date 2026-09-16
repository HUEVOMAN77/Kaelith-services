package com.hcs.games

import org.hcs.tasks.Tasks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class GameServicesUnitTest {

    @Test
    fun testUnlockAchievement() {
        val client = HcsGameServicesClient(DummyContext())
        val task = client.unlockAchievement("ach_test", "Test Achievement")
        val result = Tasks.await(task, 1, TimeUnit.SECONDS)
        assertTrue(result)

        val achievementsTask = client.getAchievements()
        val list = Tasks.await(achievementsTask, 1, TimeUnit.SECONDS)
        assertEquals(1, list.size)
        assertEquals("ach_test", list[0].id)
    }

    @Test
    fun testSaveGameSnapshot() {
        val client = HcsGameServicesClient(DummyContext())
        val saveTask = client.saveGameSnapshot("Level 1 Save", "data_payload")
        val snapshot = Tasks.await(saveTask, 1, TimeUnit.SECONDS)
        assertNotNull(snapshot)
        assertEquals("Level 1 Save", snapshot.title)

        val snapshotsTask = client.getSavedGameSnapshots()
        val list = Tasks.await(snapshotsTask, 1, TimeUnit.SECONDS)
        assertEquals(1, list.size)
    }
}

private class DummyContext : android.content.ContextWrapper(null)
