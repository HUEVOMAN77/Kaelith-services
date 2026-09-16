package org.hcs.shizuku

import org.hcs.tasks.Task

interface ShizukuCapability {
    val isShizukuAvailable: Boolean
    val isShizukuPermissionGranted: Boolean
    fun requestShizukuPermission(requestCode: Int)
    fun getBatteryOptimizationDetail(packageName: String): Task<String>
    fun requestDeviceIdleWhitelist(packageName: String): Task<Boolean>
    fun getExpandedAppDetail(packageName: String): Task<String>
}
