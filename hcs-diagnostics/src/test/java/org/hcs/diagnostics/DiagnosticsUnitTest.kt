package org.hcs.diagnostics

import org.hcs.emui.SignatureSpoofingStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticsUnitTest {

    @Test
    fun testRedactedLogExporterSanitizesSensitiveData() {
        val exporter = RedactedLogExporter(DummyContext())
        val rawLog = "User email user@example.com with token bearer=abc123def456ghi789jkl and phone +15551234567"
        val sanitized = exporter.sanitizeLogText(rawLog)

        assertFalse(sanitized.contains("user@example.com"))
        assertFalse(sanitized.contains("abc123def456ghi789jkl"))
        assertFalse(sanitized.contains("+15551234567"))
        assertTrue(sanitized.contains("[REDACTED_EMAIL]"))
        assertTrue(sanitized.contains("[REDACTED_PHONE]"))
    }

    @Test
    fun testCompatibilityLevelClassification() {
        val result = AppInspectionResult(
            packageName = "com.example.bank",
            appName = "Example Bank",
            versionName = "1.0.0",
            versionCode = 100,
            isSystemApp = false,
            usesGms = true,
            usesFirebase = true,
            usesMaps = false,
            usesFido = false,
            usesSafetyNetOrIntegrity = true,
            usesPlayBilling = true,
            usesHms = false,
            detectedGmsLibraries = listOf("Play Integrity / SafetyNet", "Google Play Billing"),
            detectedPermissions = listOf("com.android.vending.BILLING"),
            compatibilityLevel = CompatibilityLevel.D,
            primaryRootCause = FailureRootCause.API_NOT_IMPLEMENTED,
            summaryNotes = "Requires Google Play Integrity or Play Billing."
        )

        assertEquals(CompatibilityLevel.D, result.compatibilityLevel)
        assertEquals(FailureRootCause.API_NOT_IMPLEMENTED, result.primaryRootCause)
        assertTrue(result.usesPlayBilling)
        assertTrue(result.usesSafetyNetOrIntegrity)
    }
}

private class DummyContext : android.content.ContextWrapper(null)
