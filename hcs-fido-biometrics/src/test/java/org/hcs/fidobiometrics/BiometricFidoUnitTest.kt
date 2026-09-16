package org.hcs.fidobiometrics

import org.hcs.tasks.Tasks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class BiometricFidoUnitTest {

    @Test
    fun testAuthenticatePasskeyBiometrically() {
        val manager = EmuiBiometricFidoManager(DummyContext())
        val task = manager.authenticatePasskeyBiometrically("example.com")
        val result = Tasks.await(task, 1, TimeUnit.SECONDS)

        assertTrue(result.isSuccess)
        assertEquals("EMUI_BIOMETRIC_FINGERPRINT_FACE", result.methodUsed)
    }
}

private class DummyContext : android.content.ContextWrapper(null)
