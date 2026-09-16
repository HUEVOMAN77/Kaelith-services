package org.hcs.tasks

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class TasksUnitTest {

    @Test
    fun testTaskCompletionSuccess() {
        val tcs = TaskCompletionSource<String>()
        var successResult: String? = null

        tcs.task.addOnSuccessListener { result ->
            successResult = result
        }

        tcs.setResult("HCS_TASKS_OK")

        assertTrue(tcs.task.isComplete)
        assertTrue(tcs.task.isSuccessful)
        assertEquals("HCS_TASKS_OK", successResult)
    }

    @Test
    fun testNullableTaskCompletionSuccess() {
        val tcs = TaskCompletionSource<Void?>()
        var callbackCalled = false

        tcs.task.addOnSuccessListener { res ->
            callbackCalled = true
            assertNull(res)
        }

        tcs.setResult(null)

        assertTrue(tcs.task.isComplete)
        assertTrue(tcs.task.isSuccessful)
        assertTrue(callbackCalled)

        val awaited = Tasks.await(tcs.task, 1, TimeUnit.SECONDS)
        assertNull(awaited)
    }

    @Test
    fun testTaskFailure() {
        val tcs = TaskCompletionSource<String>()
        var failureException: Exception? = null

        tcs.task.addOnFailureListener { e ->
            failureException = e
        }

        val testException = RuntimeException("Test Exception")
        tcs.setException(testException)

        assertTrue(tcs.task.isComplete)
        assertFalse(tcs.task.isSuccessful)
        assertEquals(testException, failureException)
    }

    @Test
    fun testTasksAwaitHelper() {
        val task = Tasks.forResult("AWAIT_RESULT")
        val result = Tasks.await(task, 1, TimeUnit.SECONDS)
        assertEquals("AWAIT_RESULT", result)
    }
}
