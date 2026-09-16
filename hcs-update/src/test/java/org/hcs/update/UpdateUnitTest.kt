package org.hcs.update

import org.hcs.tasks.Tasks
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class UpdateUnitTest {

    @Test
    fun testCheckForUpdatesAvailable() {
        val manager = HcsUpdateManager()
        val task = manager.checkForUpdates(1)
        val update = Tasks.await(task, 1, TimeUnit.SECONDS)

        assertNotNull(update)
        assertTrue(update!!.versionCode > 1)
        assertTrue(manager.verifyUpdateSignature(update))
    }
}
