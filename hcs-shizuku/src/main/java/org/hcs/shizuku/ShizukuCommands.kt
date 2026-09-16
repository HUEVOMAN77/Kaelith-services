package org.hcs.shizuku

import org.hcs.tasks.Task
import org.hcs.tasks.TaskCompletionSource

class ShizukuCommands : ShizukuCapability {

    override val isShizukuAvailable: Boolean
        get() = ShizukuAvailability.getShizukuState() != ShizukuState.NOT_INSTALLED_OR_RUNNING

    override val isShizukuPermissionGranted: Boolean
        get() = ShizukuAvailability.getShizukuState() == ShizukuState.RUNNING_AND_AUTHORIZED

    override fun requestShizukuPermission(requestCode: Int) {
        ShizukuPermissionFlow.requestPermission(requestCode)
    }

    override fun getBatteryOptimizationDetail(packageName: String): Task<String> {
        val tcs = TaskCompletionSource<String>()
        if (isShizukuPermissionGranted) {
            val result = executeShellCommand("dumpsys deviceidle whitelist")
            if (result.contains(packageName)) {
                tcs.setResult("[Shizuku Dumpsys] App $packageName is whitelisted in deviceidle.")
            } else {
                tcs.setResult("[Shizuku Dumpsys] App $packageName is not whitelisted in deviceidle.")
            }
        } else {
            tcs.setResult("[Shizuku Unavailable] Unable to query dumpsys deviceidle. Falling back to standard Android/EMUI intent.")
        }
        return tcs.task
    }

    override fun requestDeviceIdleWhitelist(packageName: String): Task<Boolean> {
        val tcs = TaskCompletionSource<Boolean>()
        if (isShizukuPermissionGranted) {
            val output = executeShellCommand("dumpsys deviceidle whitelist +$packageName")
            tcs.setResult(output.isNotBlank())
        } else {
            tcs.setResult(false)
        }
        return tcs.task
    }

    override fun getExpandedAppDetail(packageName: String): Task<String> {
        val tcs = TaskCompletionSource<String>()
        if (isShizukuPermissionGranted) {
            val output = executeShellCommand("dumpsys package $packageName")
            val summary = if (output.length > 500) output.substring(0, 500) + "..." else output
            tcs.setResult("[Shizuku Shell PM] Extended dump for $packageName:\n$summary")
        } else {
            tcs.setResult("[Shizuku Unavailable] Standard package manager inspection active.")
        }
        return tcs.task
    }

    private fun executeShellCommand(cmd: String): String {
        return try {
            val newProcessMethod = Class.forName("rikka.shizuku.Shizuku")
                .getMethod("newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java)
            val process = newProcessMethod.invoke(null, arrayOf("sh", "-c", cmd), null, null) as Process
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            output.ifBlank { "Executed $cmd" }
        } catch (e: Throwable) {
            "Error executing $cmd: ${e.message}"
        }
    }
}
