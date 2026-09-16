package org.hcs.push

import org.hcs.tasks.Tasks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class PushUnitTest {

    @Test
    fun testUnifiedPushTokenGeneration() {
        val connector = UnifiedPushConnector(DummyContext())
        assertTrue(connector.isAvailable)
        val tokenTask = connector.registerPushToken("com.example.app")
        val token = Tasks.await(tokenTask, 1, TimeUnit.SECONDS)
        assertTrue(token.startsWith("up_token_com.example.app_"))
    }

    @Test
    fun testPushEnginePreferredTransportPriority() {
        val manager = PushEngineManager(DummyContext())
        val transport = manager.getPreferredTransport()
        assertEquals(PushTransportType.UNIFIED_PUSH, transport.transportType)
    }
}

private class DummyContext : android.content.ContextWrapper(null)
