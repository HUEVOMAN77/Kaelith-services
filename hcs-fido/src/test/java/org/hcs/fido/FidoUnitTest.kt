package org.hcs.fido

import org.hcs.tasks.Tasks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class FidoUnitTest {

    @Test
    fun testPasskeyCreationSuccess() {
        val client = HcsFidoClient(DummyContext())
        val option = WebAuthnOption("example.com", "CHALLENGE_123")
        val task = client.createPasskey(option)
        val result = Tasks.await(task, 1, TimeUnit.SECONDS)

        assertTrue(result.isSuccess)
        assertTrue(result.rawAttestationOrAssertion!!.contains("example.com"))
    }
}

private class DummyContext : android.content.ContextWrapper(null)
