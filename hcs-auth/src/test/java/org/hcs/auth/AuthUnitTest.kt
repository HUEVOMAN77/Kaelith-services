package org.hcs.auth

import org.hcs.tasks.Tasks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class AuthUnitTest {

    @Test
    fun testHandleAuthorizationResponseCodeSuccess() {
        val authClient = HcsAuthClient(DummyContext())
        val mockUrl = "https://hcs.local/callback?code=AUTH_CODE_123&email=test@hcs.local"
        val task = authClient.handleAuthorizationUrl(mockUrl)
        val result = Tasks.await(task, 1, TimeUnit.SECONDS)

        assertTrue(result.isAuthenticated)
        assertEquals("test@hcs.local", result.accountEmail)
    }

    @Test
    fun testHandleAuthorizationResponseFailure() {
        val authClient = HcsAuthClient(DummyContext())
        val mockUrl = "https://hcs.local/callback?error=access_denied"
        val task = authClient.handleAuthorizationUrl(mockUrl)
        val result = Tasks.await(task, 1, TimeUnit.SECONDS)

        assertFalse(result.isAuthenticated)
        assertEquals("access_denied", result.errorMessage)
    }
}

private class DummyContext : android.content.ContextWrapper(null)
