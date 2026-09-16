package org.hcs.diagnostics

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import org.hcs.emui.EmuiCompatibilityProfile
import org.hcs.emui.SignatureSpoofingStatus
import org.hcs.shizuku.ShizukuCapability
import org.hcs.shizuku.ShizukuCommands

enum class CompatibilityLevel {
    A, // Fully compatible
    B, // Functional with minor documented limitations
    C, // Functional under manual setup / EMUI battery adjustment
    D, // Unimplementable without proprietary Google binaries, root, or custom ROM
    E  // Untested or unknown
}

enum class FailureRootCause {
    NONE,
    API_NOT_IMPLEMENTED,
    SIGNATURE_SPOOFING_MISSING,
    RESTRICTED_BY_EMUI_BATTERY,
    WEBVIEW_DEPENDENCY_MISSING,
    NETWORK_FAIL
}

data class AppInspectionResult(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val versionCode: Long,
    val isSystemApp: Boolean,
    val usesGms: Boolean,
    val usesFirebase: Boolean,
    val usesMaps: Boolean,
    val usesFido: Boolean,
    val usesSafetyNetOrIntegrity: Boolean,
    val usesPlayBilling: Boolean,
    val usesHms: Boolean,
    val detectedGmsLibraries: List<String>,
    val detectedPermissions: List<String>,
    val compatibilityLevel: CompatibilityLevel,
    val primaryRootCause: FailureRootCause,
    val summaryNotes: String,
    val shizukuShellDetails: String? = null
)

class HcsAppInspector(
    private val context: Context,
    private val shizukuCapability: ShizukuCapability = ShizukuCommands()
) {

    private val emuiProfile = EmuiCompatibilityProfile(context)

    companion object {
        private val GMS_PACKAGE_KEYWORDS = listOf(
            "com.google.android.gms",
            "com.google.firebase",
            "com.google.android.play"
        )

        private const val PLAY_INTEGRITY_SERVICE = "com.google.android.play.core.integrity"
        private const val SAFETYNET_SERVICE = "com.google.android.gms.safetynet"
        private const val GOOGLE_BILLING_PERMISSION = "com.android.vending.BILLING"
        private const val MAPS_METADATA_KEY = "com.google.android.geo.API_KEY"
    }

    /**
     * Inspects an installed package and determines its compatibility profile and limitations.
     */
    fun inspectPackage(packageName: String): AppInspectionResult {
        val pm = context.packageManager
        val flags = PackageManager.GET_PERMISSIONS or PackageManager.GET_SERVICES or PackageManager.GET_RECEIVERS or PackageManager.GET_META_DATA

        val pkgInfo: PackageInfo = try {
            pm.getPackageInfo(packageName, flags)
        } catch (e: PackageManager.NameNotFoundException) {
            return createUnknownResult(packageName, "Package not found")
        }

        val appInfo = pkgInfo.applicationInfo
        val appName = pm.getApplicationLabel(appInfo).toString()
        val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pkgInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            pkgInfo.versionCode.toLong()
        }

        val detectedGmsLibs = mutableListOf<String>()
        val requestedPermissions = pkgInfo.requestedPermissions?.toList() ?: emptyList()

        var usesGms = false
        var usesFirebase = false
        var usesMaps = false
        var usesFido = false
        var usesSafetyNetOrIntegrity = false
        var usesPlayBilling = false
        var usesHms = false

        // Check metadata
        appInfo.metaData?.let { meta ->
            if (meta.containsKey(MAPS_METADATA_KEY) || meta.keySet().any { it.contains("maps", ignoreCase = true) }) {
                usesMaps = true
                detectedGmsLibs.add("Google Maps API")
            }
            if (meta.keySet().any { it.contains("firebase", ignoreCase = true) }) {
                usesFirebase = true
                detectedGmsLibs.add("Firebase Services")
            }
            if (meta.keySet().any { it.contains("hms", ignoreCase = true) || it.contains("huawei", ignoreCase = true) }) {
                usesHms = true
            }
        }

        // Check declared services & receivers
        val servicesAndReceivers = (pkgInfo.services?.map { it.name } ?: emptyList()) + (pkgInfo.receivers?.map { it.name } ?: emptyList())
        for (item in servicesAndReceivers) {
            if (item.contains("firebase", ignoreCase = true)) {
                usesFirebase = true
                if (!detectedGmsLibs.contains("Firebase Services")) detectedGmsLibs.add("Firebase Services")
            }
            if (item.contains("safetynet", ignoreCase = true) || item.contains("integrity", ignoreCase = true)) {
                usesSafetyNetOrIntegrity = true
                if (!detectedGmsLibs.contains("Play Integrity / SafetyNet")) detectedGmsLibs.add("Play Integrity / SafetyNet")
            }
            if (item.contains("fido", ignoreCase = true) || item.contains("webauthn", ignoreCase = true)) {
                usesFido = true
                if (!detectedGmsLibs.contains("FIDO / WebAuthn")) detectedGmsLibs.add("FIDO / WebAuthn")
            }
            if (GMS_PACKAGE_KEYWORDS.any { item.contains(it) }) {
                usesGms = true
            }
        }

        // Check permissions
        if (requestedPermissions.contains(GOOGLE_BILLING_PERMISSION)) {
            usesPlayBilling = true
            detectedGmsLibs.add("Google Play Billing")
        }

        if (detectedGmsLibs.isNotEmpty() || usesGms) {
            usesGms = true
        }

        // Evaluate Compatibility Level & Failure Root Cause
        val spoofingStatus = emuiProfile.checkSignatureSpoofingStatus()
        val (level, cause, notes) = evaluateCompatibility(
            usesGms = usesGms,
            usesSafetyNetOrIntegrity = usesSafetyNetOrIntegrity,
            usesPlayBilling = usesPlayBilling,
            usesHms = usesHms,
            spoofingStatus = spoofingStatus
        )

        val shizukuDetail = if (shizukuCapability.isShizukuPermissionGranted) {
            "[Shizuku Shell Active] Querying extended package info for $packageName"
        } else {
            null
        }

        return AppInspectionResult(
            packageName = packageName,
            appName = appName,
            versionName = pkgInfo.versionName ?: "1.0",
            versionCode = versionCode,
            isSystemApp = isSystem,
            usesGms = usesGms,
            usesFirebase = usesFirebase,
            usesMaps = usesMaps,
            usesFido = usesFido,
            usesSafetyNetOrIntegrity = usesSafetyNetOrIntegrity,
            usesPlayBilling = usesPlayBilling,
            usesHms = usesHms,
            detectedGmsLibraries = detectedGmsLibs,
            detectedPermissions = requestedPermissions,
            compatibilityLevel = level,
            primaryRootCause = cause,
            summaryNotes = notes,
            shizukuShellDetails = shizukuDetail
        )
    }

    private fun evaluateCompatibility(
        usesGms: Boolean,
        usesSafetyNetOrIntegrity: Boolean,
        usesPlayBilling: Boolean,
        usesHms: Boolean,
        spoofingStatus: SignatureSpoofingStatus
    ): Triple<CompatibilityLevel, FailureRootCause, String> {
        if (usesHms && !usesGms) {
            return Triple(CompatibilityLevel.A, FailureRootCause.NONE, "App uses Huawei Mobile Services natively.")
        }

        if (usesSafetyNetOrIntegrity || usesPlayBilling) {
            return Triple(
                CompatibilityLevel.D,
                FailureRootCause.API_NOT_IMPLEMENTED,
                "Requires Google Play Integrity / SafetyNet or Google Play Billing which cannot be implemented without Google hardware attestation."
            )
        }

        if (!usesGms) {
            return Triple(CompatibilityLevel.A, FailureRootCause.NONE, "No GMS dependencies detected. Fully compatible.")
        }

        return when (spoofingStatus) {
            SignatureSpoofingStatus.SUPPORTED_AND_GRANTED -> {
                Triple(CompatibilityLevel.B, FailureRootCause.NONE, "GMS dependencies detected. Signature spoofing is active.")
            }
            SignatureSpoofingStatus.SUPPORTED_BUT_NOT_GRANTED -> {
                Triple(CompatibilityLevel.C, FailureRootCause.SIGNATURE_SPOOFING_MISSING, "Signature spoofing supported but not granted. Manual setup required.")
            }
            else -> {
                Triple(CompatibilityLevel.C, FailureRootCause.RESTRICTED_BY_EMUI_BATTERY, "GMS dependency present; manual EMUI battery/autostart configuration required.")
            }
        }
    }

    private fun createUnknownResult(packageName: String, reason: String): AppInspectionResult {
        return AppInspectionResult(
            packageName = packageName,
            appName = packageName,
            versionName = "0.0",
            versionCode = 0,
            isSystemApp = false,
            usesGms = false,
            usesFirebase = false,
            usesMaps = false,
            usesFido = false,
            usesSafetyNetOrIntegrity = false,
            usesPlayBilling = false,
            usesHms = false,
            detectedGmsLibraries = emptyList(),
            detectedPermissions = emptyList(),
            compatibilityLevel = CompatibilityLevel.E,
            primaryRootCause = FailureRootCause.NONE,
            summaryNotes = reason,
            shizukuShellDetails = null
        )
    }
}
