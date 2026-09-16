package org.hcs.gmsbridge

import android.content.Context
import android.content.pm.PackageManager
import org.hcs.tasks.Task
import org.hcs.tasks.TaskCompletionSource

data class BridgeStatus(
    val isSignatureSpoofingAvailable: Boolean,
    val isMicroGInstalled: Boolean,
    val isBridgeActive: Boolean,
    val warningMessage: String?
)

open class MicroGConflictChecker(private val context: Context) {

    companion object {
        const val MICROG_GMS_PACKAGE = "com.google.android.gms"
        const val MICROG_GSF_PACKAGE = "com.google.android.gsf"
    }

    open fun checkMicroGInstalled(): Boolean {
        val pm = context.packageManager ?: return false
        return try {
            val info = pm.getPackageInfo(MICROG_GMS_PACKAGE, 0)
            val appInfo = info.applicationInfo ?: return false
            val label = pm.getApplicationLabel(appInfo).toString()
            label.contains("microG", ignoreCase = true) || label.contains("GmsCore", ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }
}

class GmsServiceRouter(
    private val context: Context,
    private val microGChecker: MicroGConflictChecker = MicroGConflictChecker(context)
) {

    private var isBridgeActive: Boolean = false

    fun getStatus(hasSignatureSpoofing: Boolean): BridgeStatus {
        val isMicroG = microGChecker.checkMicroGInstalled()
        val warning = if (isMicroG) {
            "WARNING: microG / GmsCore detected! You MUST uninstall microG before activating HCS GMS Bridge to prevent package identity collision."
        } else if (!hasSignatureSpoofing) {
            "Signature spoofing environment not detected. GMS Bridge requires signature spoofing support."
        } else {
            null
        }

        return BridgeStatus(
            isSignatureSpoofingAvailable = hasSignatureSpoofing,
            isMicroGInstalled = isMicroG,
            isBridgeActive = isBridgeActive && !isMicroG && hasSignatureSpoofing,
            warningMessage = warning
        )
    }

    fun enableBridge(hasSignatureSpoofing: Boolean): Task<Boolean> {
        val tcs = TaskCompletionSource<Boolean>()
        val status = getStatus(hasSignatureSpoofing)

        if (status.isMicroGInstalled || !status.isSignatureSpoofingAvailable) {
            isBridgeActive = false
            tcs.setResult(false)
        } else {
            isBridgeActive = true
            tcs.setResult(true)
        }
        return tcs.task
    }

    fun disableBridge(): Task<Boolean> {
        val tcs = TaskCompletionSource<Boolean>()
        isBridgeActive = false
        tcs.setResult(true)
        return tcs.task
    }

    /**
     * Routes incoming com.google.android.gms service queries to existing HCS core modules.
     */
    fun routeGmsServiceQuery(serviceName: String): String {
        if (!isBridgeActive) return "GMS_BRIDGE_INACTIVE"

        return when {
            serviceName.contains("location", ignoreCase = true) -> "ROUTED_TO_HCS_LOCATION"
            serviceName.contains("push", ignoreCase = true) || serviceName.contains("c2dm", ignoreCase = true) -> "ROUTED_TO_HCS_PUSH"
            serviceName.contains("auth", ignoreCase = true) -> "ROUTED_TO_HCS_AUTH"
            serviceName.contains("fido", ignoreCase = true) -> "ROUTED_TO_HCS_FIDO"
            serviceName.contains("maps", ignoreCase = true) -> "ROUTED_TO_HCS_MAPS"
            else -> "ROUTED_TO_HCS_TASKS"
        }
    }
}
