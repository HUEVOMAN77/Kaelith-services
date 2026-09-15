package org.hcs.emui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import java.io.File

/**
 * Data class representing the device hardware, OS, EMUI profile, and HMS status.
 */
data class DeviceProfile(
    val manufacturer: String,
    val model: String,
    val codeName: String,
    val androidVersion: String,
    val sdkInt: Int,
    val emuiVersion: String,
    val isHuaweiDevice: Boolean,
    val hasHmsCore: Boolean,
    val hmsCoreVersionCode: Long,
    val hasPushAgent: Boolean,
    val displayLanguage: String,
    val countryRegion: String
)

/**
 * Classifies the exact status of Signature Spoofing on the current device.
 */
enum class SignatureSpoofingStatus {
    SUPPORTED_AND_GRANTED,
    SUPPORTED_BUT_NOT_GRANTED,
    UNSUPPORTED_BY_SYSTEM,
    HUAWEISPECIFIC_RESTRICTION,
    UNKNOWN_ERROR
}

/**
 * Inspection and helper utilities for Huawei EMUI environment detection and safe intent launching.
 */
class EmuiCompatibilityProfile(private val context: Context) {

    companion object {
        const val FAKE_SIGNATURE_PERMISSION = "android.permission.FAKE_PACKAGE_SIGNATURE"
        const val HMS_CORE_PACKAGE = "com.huawei.hwid"
        const val HUAWEI_PUSH_AGENT_PACKAGE = "com.huawei.android.pushagent"

        // EMUI Power/Startup Activities
        const val EMUI_AUTOSTART_ACTIVITY_1 = "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
        const val EMUI_AUTOSTART_ACTIVITY_2 = "com.huawei.systemmanager.optimize.bootcom.BootStartActivity"
        const val EMUI_BATTERY_GOVERNING_ACTIVITY = "com.huawei.systemmanager.power.ui.HwPowerManagerActivity"
        const val EMUI_PROTECTED_APPS_ACTIVITY = "com.huawei.systemmanager.optimize.process.ProtectActivity"
    }

    /**
     * Reads system build properties to accurately identify EMUI version without elevated permissions.
     */
    fun getEmuiVersion(): String {
        return try {
            val systemPropertiesClass = Class.forName("android.os.SystemProperties")
            val getMethod = systemPropertiesClass.getMethod("get", String::class.java, String::class.java)
            val emuiVer = getMethod.invoke(null, "ro.build.version.emui", "") as String
            if (emuiVer.isNotBlank()) emuiVer else "UNKNOWN"
        } catch (e: Exception) {
            "UNKNOWN"
        }
    }

    /**
     * Generates a full immutable DeviceProfile snapshot.
     */
    fun getDeviceProfile(): DeviceProfile {
        val manufacturer = Build.MANUFACTURER ?: "UNKNOWN"
        val model = Build.MODEL ?: "UNKNOWN"
        val codeName = Build.DEVICE ?: "UNKNOWN"
        val isHuawei = manufacturer.equals("Huawei", ignoreCase = true) || manufacturer.equals("HONOR", ignoreCase = true)

        val pm = context.packageManager
        var hmsCoreInstalled = false
        var hmsVersionCode = 0L
        try {
            val pkgInfo = pm.getPackageInfo(HMS_CORE_PACKAGE, 0)
            hmsCoreInstalled = true
            hmsVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pkgInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pkgInfo.versionCode.toLong()
            }
        } catch (ignored: PackageManager.NameNotFoundException) { }

        var pushAgentInstalled = false
        try {
            pm.getPackageInfo(HUAWEI_PUSH_AGENT_PACKAGE, 0)
            pushAgentInstalled = true
        } catch (ignored: PackageManager.NameNotFoundException) { }

        val locale = context.resources.configuration.locales[0]

        return DeviceProfile(
            manufacturer = manufacturer,
            model = model,
            codeName = codeName,
            androidVersion = Build.VERSION.RELEASE ?: "UNKNOWN",
            sdkInt = Build.VERSION.SDK_INT,
            emuiVersion = getEmuiVersion(),
            isHuaweiDevice = isHuawei,
            hasHmsCore = hmsCoreInstalled,
            hmsCoreVersionCode = hmsVersionCode,
            hasPushAgent = pushAgentInstalled,
            displayLanguage = locale.language,
            countryRegion = locale.country
        )
    }

    /**
     * Inspects actual Signature Spoofing state on the device without assuming system behavior.
     */
    fun checkSignatureSpoofingStatus(): SignatureSpoofingStatus {
        return try {
            val pm = context.packageManager
            val permissionGranted = pm.checkPermission(FAKE_SIGNATURE_PERMISSION, context.packageName) == PackageManager.PERMISSION_GRANTED
            if (permissionGranted) {
                return SignatureSpoofingStatus.SUPPORTED_AND_GRANTED
            }

            // Check if permission is declared in system permission group / manifest
            val permInfo = try {
                pm.getPermissionInfo(FAKE_SIGNATURE_PERMISSION, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }

            if (permInfo != null) {
                SignatureSpoofingStatus.SUPPORTED_BUT_NOT_GRANTED
            } else {
                val profile = getDeviceProfile()
                if (profile.isHuaweiDevice && profile.emuiVersion.startsWith("EmotionUI_13") || profile.emuiVersion.startsWith("EmotionUI_12")) {
                    SignatureSpoofingStatus.HUAWEISPECIFIC_RESTRICTION
                } else {
                    SignatureSpoofingStatus.UNSUPPORTED_BY_SYSTEM
                }
            }
        } catch (e: Exception) {
            SignatureSpoofingStatus.UNKNOWN_ERROR
        }
    }

    /**
     * Resolves an Intent safely to open EMUI autostart / launch manager settings.
     * Returns true if launched successfully or intent was found.
     */
    fun createAutoStartIntent(): Intent? {
        val intents = listOf(
            Intent().setClassName("com.huawei.systemmanager", EMUI_AUTOSTART_ACTIVITY_1),
            Intent().setClassName("com.huawei.systemmanager", EMUI_AUTOSTART_ACTIVITY_2),
            Intent().setClassName("com.huawei.systemmanager", EMUI_PROTECTED_APPS_ACTIVITY)
        )
        val pm = context.packageManager
        for (intent in intents) {
            if (intent.resolveActivity(pm) != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                return intent
            }
        }
        return null
    }

    /**
     * Resolves an Intent safely to open EMUI battery management settings.
     */
    fun createBatterySettingsIntent(): Intent {
        val pm = context.packageManager
        val emuiIntent = Intent().setClassName("com.huawei.systemmanager", EMUI_BATTERY_GOVERNING_ACTIVITY)
        emuiIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        if (emuiIntent.resolveActivity(pm) != null) {
            return emuiIntent
        }

        // Fallback to standard Android battery optimization settings
        return Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
