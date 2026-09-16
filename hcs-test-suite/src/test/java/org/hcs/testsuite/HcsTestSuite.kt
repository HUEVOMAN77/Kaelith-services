package org.hcs.testsuite

import org.hcs.auth.HcsAuthClient
import org.hcs.compatdb.CommunityReport
import org.hcs.compatdb.HcsCompatDbClient
import org.hcs.diagnostics.AppInspectionResult
import org.hcs.diagnostics.CompatibilityLevel
import org.hcs.diagnostics.FailureRootCause
import org.hcs.diagnostics.RedactedLogExporter
import org.hcs.fido.HcsFidoClient
import org.hcs.fido.WebAuthnOption
import org.hcs.gmsbridge.GmsServiceRouter
import org.hcs.gmsbridge.MicroGConflictChecker
import org.hcs.location.HcsLocation
import org.hcs.location.HcsLocationResult
import org.hcs.maps.MapEngineType
import org.hcs.maps.MapManager
import org.hcs.privileged.PrivilegedPatcher
import org.hcs.push.PushEngineManager
import org.hcs.push.PushTransportType
import org.hcs.shizuku.ShizukuAvailability
import org.hcs.shizuku.ShizukuCommands
import org.hcs.shizuku.ShizukuState
import org.hcs.tasks.TaskCompletionSource
import org.hcs.tasks.Tasks
import org.hcs.telemetry.HcsCrashReporter
import org.hcs.update.HcsUpdateManager
import org.hcs.webview.WebViewStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
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

    @Test
    fun testCompatDbUpdateAndTelemetryIntegration() {
        val compatDb = HcsCompatDbClient()
        val submitTask = compatDb.submitAnonymousReport(
            CommunityReport(
                packageName = "org.hcs.fulltest",
                appVersionCode = 1,
                appVersionName = "1.0",
                deviceModel = "NCO-LX3",
                emuiVersion = "13.0",
                androidSdk = 31,
                compatibilityLevel = "A",
                failingApis = emptyList(),
                summaryNotes = "Integration test OK"
            )
        )
        assertTrue(Tasks.await(submitTask, 1, TimeUnit.SECONDS))

        val updateManager = HcsUpdateManager()
        val updateTask = updateManager.checkForUpdates(1)
        val update = Tasks.await(updateTask, 1, TimeUnit.SECONDS)
        assertNotNull(update)
        assertTrue(updateManager.verifyUpdateSignature(update!!))

        val crashReporter = HcsCrashReporter(DummyContext())
        crashReporter.setOptInStatus(true)
        val reportTask = crashReporter.reportCrash(RuntimeException("HCS Test Crash"), "13.0", "1.0")
        assertTrue(Tasks.await(reportTask, 1, TimeUnit.SECONDS))
    }

    @Test
    fun testPrivilegedPatcherSimulationIntegration() {
        val patcher = PrivilegedPatcher(DummyContext())
        val patchTask = patcher.applySystemPatch("/tmp/hcs_patch.conf", "TEST_PATCH", isSimulation = true)
        val result = Tasks.await(patchTask, 1, TimeUnit.SECONDS)

        assertTrue(result.isSuccess)
        assertTrue(result.isSimulation)
    }

    @Test
    fun testShizukuExtraGracefulFallbackIntegration() {
        val state = ShizukuAvailability.getShizukuState()
        assertEquals(ShizukuState.NOT_INSTALLED_OR_RUNNING, state)

        val commands = ShizukuCommands()
        assertFalse(commands.isShizukuPermissionGranted)

        val task = commands.getBatteryOptimizationDetail("com.aurorastore.targetapp")
        val result = Tasks.await(task, 1, TimeUnit.SECONDS)
        assertNotNull(result)
    }

    @Test
    fun testGmsBridgeIntegration() {
        val mockChecker = object : MicroGConflictChecker(DummyContext()) {
            override fun checkMicroGInstalled(): Boolean = false
        }
        val router = GmsServiceRouter(DummyContext(), mockChecker)
        val enableTask = router.enableBridge(hasSignatureSpoofing = true)
        val enabled = Tasks.await(enableTask, 1, TimeUnit.SECONDS)

        assertTrue(enabled)
        val routeResult = router.routeGmsServiceQuery("com.google.android.gms.location.LOCATION_SERVICE")
        assertEquals("ROUTED_TO_HCS_LOCATION", routeResult)
    }
}

private class DummyContext : android.content.ContextWrapper(null) {
    override fun getFilesDir(): File {
        val f = File(System.getProperty("java.io.tmpdir"), "hcs_test_suite_tmp")
        if (!f.exists()) f.mkdirs()
        return f
    }
}
