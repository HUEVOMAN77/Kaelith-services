package org.hcs.telemetry

import org.hcs.tasks.Tasks
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class TelemetryUnitTest {

    @Test
    fun testCrashReportOptInRequirement() {
        val reporter = HcsCrashReporter(DummyContext())
        reporter.setOptInStatus(false)

        val taskOptOut = reporter.reportCrash(RuntimeException("Test Crash"), "13.0", "1.0")
        val resultOptOut = Tasks.await(taskOptOut, 1, TimeUnit.SECONDS)
        assertFalse(resultOptOut)

        reporter.setOptInStatus(true)
        val taskOptIn = reporter.reportCrash(RuntimeException("Test Crash"), "13.0", "1.0")
        val resultOptIn = Tasks.await(taskOptIn, 1, TimeUnit.SECONDS)
        assertTrue(resultOptIn)
    }
}

private class DummyContext : android.content.ContextWrapper(null) {
    override fun getFilesDir(): java.io.File {
        val f = java.io.File(System.getProperty("java.io.tmpdir"), "hcs_test_telemetry")
        if (!f.exists()) f.mkdirs()
        return f
    }
}
