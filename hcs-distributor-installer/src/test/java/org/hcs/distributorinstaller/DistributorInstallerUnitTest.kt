package org.hcs.distributorinstaller

import org.hcs.tasks.Tasks
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class DistributorInstallerUnitTest {

    @Test
    fun testGetInstalledDistributorsIncludesEmbedded() {
        val manager = UnifiedPushDistributorManager(DummyContext())
        val task = manager.getInstalledDistributors()
        val distributors = Tasks.await(task, 1, TimeUnit.SECONDS)

        assertNotNull(distributors)
        assertTrue(distributors.any { it.packageName == UnifiedPushDistributorManager.HCS_EMBEDDED_PACKAGE && it.isInstalled })
    }
}

private class DummyContext : android.content.ContextWrapper(null)
