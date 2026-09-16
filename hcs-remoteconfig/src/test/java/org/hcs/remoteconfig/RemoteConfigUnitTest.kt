package org.hcs.remoteconfig

import org.hcs.tasks.Tasks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class RemoteConfigUnitTest {

    @Test
    fun testFetchAndActivateRemoteConfig() {
        val client = HcsRemoteConfigClient(DummyContext())
        val fetchTask = client.fetchAndActivate()
        val success = Tasks.await(fetchTask, 1, TimeUnit.SECONDS)

        assertTrue(success)
        assertEquals("Welcome to HCS (Huawei Compatibility Services)", client.getString("hcs_welcome_message"))
        assertTrue(client.getBoolean("hcs_enable_unified_push"))
    }
}

private class DummyContext : android.content.ContextWrapper(null)
