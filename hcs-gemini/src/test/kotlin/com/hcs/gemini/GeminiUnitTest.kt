package com.hcs.gemini

import org.hcs.tasks.Tasks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class GeminiUnitTest {

    @Test
    fun testSetAndGetApiKey() {
        val client = HcsGeminiClient(DummyContext())
        client.setApiKey("TEST_KEY_123")
        assertEquals("TEST_KEY_123", client.getApiKey())
    }

    @Test
    fun testGenerateContentDemoMode() {
        val client = HcsGeminiClient(DummyContext())
        client.setApiKey("DEMO_KEY")
        val task = client.generateContent("Hola Gemini")
        val result = Tasks.await(task, 1, TimeUnit.SECONDS)
        assertNotNull(result)
        assertTrue(result.contains("[Gemini Demo Mode]"))
    }
}

private class DummyContext : android.content.ContextWrapper(null)
