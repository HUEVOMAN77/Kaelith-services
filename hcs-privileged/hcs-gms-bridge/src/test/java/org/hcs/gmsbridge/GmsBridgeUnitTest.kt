package org.hcs.gmsbridge

import org.hcs.tasks.Tasks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class GmsBridgeUnitTest {

    @Test
    fun testGmsServiceRouterEnableAndRoute() {
        val mockChecker = object : MicroGConflictChecker(DummyContext()) {
            override fun checkMicroGInstalled(): Boolean = false
        }
        val router = GmsServiceRouter(DummyContext(), mockChecker)
        val enableTask = router.enableBridge(hasSignatureSpoofing = true)
        val enabled = Tasks.await(enableTask, 1, TimeUnit.SECONDS)

        assertTrue(enabled)
        val routeResult = router.routeGmsServiceQuery("com.google.android.gms.location.LOCATION_SERVICE")
        assertEquals("ROUTED_TO_HCS_LOCATION", routeResult)
    }

    @Test
    fun testGmsBridgeDisabledWhenMicroGDetected() {
        val mockChecker = object : MicroGConflictChecker(DummyContext()) {
            override fun checkMicroGInstalled(): Boolean = true
        }

        val router = GmsServiceRouter(DummyContext(), mockChecker)
        val status = router.getStatus(hasSignatureSpoofing = true)

        assertTrue(status.isMicroGInstalled)
        assertNotNull(status.warningMessage)

        val enableTask = router.enableBridge(hasSignatureSpoofing = true)
        val enabled = Tasks.await(enableTask, 1, TimeUnit.SECONDS)
        assertFalse(enabled)
    }
}

private class DummyContext : android.content.ContextWrapper(null)
