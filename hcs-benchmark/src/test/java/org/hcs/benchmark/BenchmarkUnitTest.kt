package org.hcs.benchmark

import org.hcs.tasks.Tasks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class BenchmarkUnitTest {

    @Test
    fun testMeasureOverhead() {
        val profiler = HcsPerformanceProfiler(DummyContext())
        val task = profiler.measureOverhead()
        val metrics = Tasks.await(task, 1, TimeUnit.SECONDS)

        assertNotNull(metrics)
        assertTrue(metrics.memoryUsageMb >= 0)
        assertEquals(45, metrics.estimatedBatterySavingsVsGmsPercent)
    }
}

private class DummyContext : android.content.ContextWrapper(null)
