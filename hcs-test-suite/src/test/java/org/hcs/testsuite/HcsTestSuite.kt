package org.hcs.testsuite

import org.hcs.auth.HcsAuthClient
import org.hcs.diagnostics.AppInspectionResult
import org.hcs.diagnostics.CompatibilityLevel
import org.hcs.diagnostics.FailureRootCause
import org.hcs.diagnostics.RedactedLogExporter
import org.hcs.fido.HcsFidoClient
import org.hcs.fido.WebAuthnOption
import org.hcs.location.HcsLocation
import org.hcs.location.HcsLocationResult
import org.hcs.maps.MapEngineType
import org.hcs.maps.MapManager
import org.hcs.push.PushEngineManager
import org.hcs.push.PushTransportType
import org.hcs.tasks.TaskCompletionSource
import org.hcs.tasks.Tasks
import org.hcs.webview.WebViewStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

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

    @Test
    fun testAsyncTasksPipelineIntegration() {
        val tcs = TaskCompletionSource<HcsLocationResult>()
        val loc = HcsLocation("gps", 40.7128, -74.0060)
        val locResult = HcsLocationResult(listOf(loc))

        tcs.setResult(locResult)
        val awaited = Tasks.await(tcs.task, 1, TimeUnit.SECONDS)

        assertNotNull(awaited.lastLocation)
        assertEquals(40.7128, awaited.lastLocation!!.latitude, 0.0001)
    }

    @Test
    fun testPushTransportEngineIntegration() {
        val pushManager = PushEngineManager(DummyContext())
        val regTask = pushManager.registerApp("com.aurorastore.targetapp")
        val (transportType, token) = Tasks.await(regTask, 1, TimeUnit.SECONDS)

        assertEquals(PushTransportType.UNIFIED_PUSH, transportType)
        assertTrue(token.contains("com.aurorastore.targetapp"))
    }

    @Test
    fun testAuthAndFidoIntegration() {
        val authClient = HcsAuthClient(DummyContext())
        val mockCallback = "https://hcs.local/callback?code=VALID_CODE&email=user@hcs.local"
        val authResult = Tasks.await(authClient.handleAuthorizationUrl(mockCallback), 1, TimeUnit.SECONDS)

        assertTrue(authResult.isAuthenticated)
        assertEquals("user@hcs.local", authResult.accountEmail)

        val fidoClient = HcsFidoClient(DummyContext())
        val fidoResult = Tasks.await(fidoClient.createPasskey(WebAuthnOption("hcs.local", "CHALLENGE_1")), 1, TimeUnit.SECONDS)
        assertTrue(fidoResult.isSuccess)
    }

    @Test
    fun testMapsAndWebViewIntegration() {
        val mapManager = MapManager()
        val osmProvider = mapManager.getProvider(MapEngineType.OPEN_STREET_MAP)
        val tileUrl = osmProvider.renderMapTile(12.0, 34.0, 15f)

        assertTrue(tileUrl.contains("tile.openstreetmap.org"))

        val webViewStatus = WebViewStatus(
            packageName = "com.huawei.webview",
            versionName = "12.0.0.300",
            isMultiProcessEnabled = true,
            isSufficientForHcs = true
        )
        assertTrue(webViewStatus.isSufficientForHcs)
    }
}

private class DummyContext : android.content.ContextWrapper(null)
