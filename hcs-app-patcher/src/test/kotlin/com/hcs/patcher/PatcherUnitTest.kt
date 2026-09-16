package com.hcs.patcher

import org.hcs.diagnostics.AppInspectionResult
import org.hcs.diagnostics.CompatibilityLevel
import org.hcs.diagnostics.FailureRootCause
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PatcherUnitTest {

    @Test
    fun testGenerateCompatibilityProfileLevelA() {
        val patcher = HcsAppPatcher()
        val mockResult = AppInspectionResult(
            packageName = "com.example.app",
            appName = "Example App",
            versionName = "1.0.0",
            versionCode = 100L,
            isSystemApp = false,
            usesGms = true,
            usesFirebase = true,
            usesMaps = true,
            usesFido = false,
            usesSafetyNetOrIntegrity = false,
            usesPlayBilling = false,
            usesHms = false,
            detectedGmsLibraries = listOf("firebase-messaging"),
            detectedPermissions = emptyList(),
            compatibilityLevel = CompatibilityLevel.A,
            primaryRootCause = FailureRootCause.NONE,
            summaryNotes = "Test"
        )

        val profile = patcher.generateCompatibilityProfile(mockResult)
        assertEquals("com.example.app", profile.packageName)
        assertEquals("A", profile.compatibilityLevel)
        assertEquals("UNIFIED_PUSH", profile.preferredPushTransport)
    }

    @Test
    fun testAnalyzeAppFixabilityUnfixableIntegrity() {
        val patcher = HcsAppPatcher()
        val mockResult = AppInspectionResult(
            packageName = "com.bank.app",
            appName = "Bank App",
            versionName = "2.0.0",
            versionCode = 200L,
            isSystemApp = false,
            usesGms = true,
            usesFirebase = false,
            usesMaps = false,
            usesFido = false,
            usesSafetyNetOrIntegrity = true,
            usesPlayBilling = false,
            usesHms = false,
            detectedGmsLibraries = listOf("play-integrity"),
            detectedPermissions = emptyList(),
            compatibilityLevel = CompatibilityLevel.D,
            primaryRootCause = FailureRootCause.API_NOT_IMPLEMENTED,
            summaryNotes = "Test"
        )

        val fixability = patcher.analyzeAppFixability(mockResult)
        assertFalse(fixability.isFixable)
    }
}
