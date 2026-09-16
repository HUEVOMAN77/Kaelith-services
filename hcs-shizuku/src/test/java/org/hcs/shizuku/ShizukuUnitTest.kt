package org.hcs.shizuku

import org.hcs.tasks.Tasks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.concurrent.TimeUnit

class ShizukuUnitTest {

    @Test
    fun testShizukuDefaultStateWhenUninstalled() {
        val state = ShizukuAvailability.getShizukuState()
        assertEquals(ShizukuState.NOT_INSTALLED_OR_RUNNING, state)
    }

    @Test
    fun testShizukuCommandsGracefulFallback() {
        val commands = ShizukuCommands()
        assertFalse(commands.isShizukuPermissionGranted)

        val batteryTask = commands.getBatteryOptimizationDetail("com.aurorastore.targetapp")
        val batteryResult = Tasks.await(batteryTask, 1, TimeUnit.SECONDS)
        assertNotNull(batteryResult)
        assert(batteryResult.contains("Shizuku Unavailable") || batteryResult.contains("Shizuku Dumpsys"))

        val whitelistTask = commands.requestDeviceIdleWhitelist("com.aurorastore.targetapp")
        val whitelistResult = Tasks.await(whitelistTask, 1, TimeUnit.SECONDS)
        assertFalse(whitelistResult)
    }
}
