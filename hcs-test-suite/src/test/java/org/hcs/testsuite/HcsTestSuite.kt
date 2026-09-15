package org.hcs.testsuite

import org.hcs.diagnostics.AppInspectionResult
import org.hcs.diagnostics.CompatibilityLevel
import org.hcs.diagnostics.FailureRootCause
import org.hcs.diagnostics.RedactedLogExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HcsTestSuite {

    @Test
    fun testRedactedLogExporterStripsImeiAndTokens() {
        val exporter = RedactedLogExporter(DummyContext())
        val input = "Log line: bearer=super_secret_auth_token_12345 imei=123456789012345 email=john@domain.com"
        val output = exporter.sanitizeLogText(input)

        assertFalse(output.contains("super_secret_auth_token_12345"))
        assertFalse(output.contains("123456789012345"))
        assertFalse(output.contains("john@domain.com"))
        assertTrue(output.contains("[REDACTED_EMAIL]"))
    }

    @Test
    fun testAppInspectionResultMapping() {
        val result = AppInspectionResult(
            packageName = "org.hcs.sample",
            appName = "Sample HCS App",
            versionName = "2.0.0",
            versionCode = 200,
            isSystemApp = false,
            usesGms = false,
            usesFirebase = false,
            usesMaps = false,
            usesFido = false,
            usesSafetyNetOrIntegrity = false,
            usesPlayBilling = false,
            usesHms = true,
            detectedGmsLibraries = emptyList(),
            detectedPermissions = emptyList(),
            compatibilityLevel = CompatibilityLevel.A,
            primaryRootCause = FailureRootCause.NONE,
            summaryNotes = "Native HMS support."
        )

        assertEquals(CompatibilityLevel.A, result.compatibilityLevel)
        assertEquals(FailureRootCause.NONE, result.primaryRootCause)
        assertTrue(result.usesHms)
    }
}

private class DummyContext : android.content.ContextWrapper(null)
