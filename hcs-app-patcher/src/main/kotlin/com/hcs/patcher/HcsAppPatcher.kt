package com.hcs.patcher

import org.hcs.diagnostics.AppInspectionResult
import org.hcs.diagnostics.CompatibilityLevel
import org.hcs.offlineprofiles.OfflineProfile

class HcsAppPatcher {

    fun generateCompatibilityProfile(result: AppInspectionResult): OfflineProfile {
        val level = when {
            result.usesSafetyNetOrIntegrity || result.usesPlayBilling -> CompatibilityLevel.D
            result.usesFirebase || result.usesMaps || result.usesGms -> CompatibilityLevel.A
            else -> CompatibilityLevel.B
        }

        return OfflineProfile(
            packageName = result.packageName,
            appName = result.appName,
            compatibilityLevel = level.name,
            preferredPushTransport = if (result.usesFirebase) "UNIFIED_PUSH" else "NONE",
            preferredMapEngine = if (result.usesMaps) "MAPLIBRE" else "NONE",
            notes = "Auto-generated HCS compatibility patch profile for ${result.packageName}"
        )
    }

    fun analyzeAppFixability(result: AppInspectionResult): PatchFixability {
        return when {
            result.usesSafetyNetOrIntegrity -> PatchFixability(
                isFixable = false,
                reason = "App strictly requires Google Play Integrity or SafetyNet hardware attestation which cannot be spoofed."
            )
            result.usesPlayBilling -> PatchFixability(
                isFixable = false,
                reason = "App requires Google Play In-App Billing which cannot be simulated."
            )
            result.usesFirebase || result.usesMaps || result.usesGms -> PatchFixability(
                isFixable = true,
                reason = "App GMS calls can be handled by HCS UnifiedPush, OpenMaps, and Fused Location services."
            )
            else -> PatchFixability(
                isFixable = true,
                reason = "App has standard Android dependencies compatible with HCS core."
            )
        }
    }
}

data class PatchFixability(
    val isFixable: Boolean,
    val reason: String
)
