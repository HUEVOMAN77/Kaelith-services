package com.hcs.openai

import org.hcs.tasks.Tasks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class OpenAiUnitTest {

    @Test
    fun testSetAndGetApiKey() {
        val client = HcsOpenAiClient(DummyContext())
        client.setApiKey("OPENAI_KEY_999")
        assertEquals("OPENAI_KEY_999", client.getApiKey())
    }

    @Test
    fun testGenerateChatResponseDemoMode() {
        val client = HcsOpenAiClient(DummyContext())
        client.setApiKey("DEMO_KEY")
        val task = client.generateChatResponse("Hola ChatGPT")
        val result = Tasks.await(task, 1, TimeUnit.SECONDS)

        assertNotNull(result)
        assertTrue(result.contains("[ChatGPT gpt-4o-mini Demo Mode]"))
    }
}

private class DummyContext : android.content.ContextWrapper(null)
