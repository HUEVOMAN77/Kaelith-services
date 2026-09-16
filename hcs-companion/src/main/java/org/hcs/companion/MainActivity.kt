package org.hcs.companion

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.hcs.benchmark.HcsPerformanceProfiler
import org.hcs.companion.databinding.ActivityMainBinding
import org.hcs.compatdb.HcsCompatDbClient
import org.hcs.diagnostics.HcsAppInspector
import org.hcs.diagnostics.RedactedLogExporter
import org.hcs.distributorinstaller.UnifiedPushDistributorManager
import org.hcs.emui.EmuiCompatibilityProfile
import org.hcs.gmsbridge.GmsServiceRouter
import org.hcs.maps.MapEngineType
import org.hcs.maps.MapManager
import org.hcs.offlineprofiles.HcsProfileSerializer
import org.hcs.offlineprofiles.OfflineProfile
import org.hcs.privileged.PrivilegedPatcher
import org.hcs.proxy.HcsLocalProxyServer
import org.hcs.push.PushEngineManager
import org.hcs.shizuku.ShizukuAvailability
import org.hcs.shizuku.ShizukuCommands
import org.hcs.shizuku.ShizukuPermissionFlow
import org.hcs.tasks.Tasks
import org.hcs.update.HcsUpdateManager
import java.io.File
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var emuiProfile: EmuiCompatibilityProfile
    private lateinit var appInspector: HcsAppInspector
    private lateinit var logExporter: RedactedLogExporter
    private lateinit var pushManager: PushEngineManager
    private lateinit var mapManager: MapManager
    private lateinit var compatDbClient: HcsCompatDbClient
    private lateinit var updateManager: HcsUpdateManager
    private lateinit var privilegedPatcher: PrivilegedPatcher
    private lateinit var proxyServer: HcsLocalProxyServer
    private lateinit var profiler: HcsPerformanceProfiler
    private lateinit var distributorManager: UnifiedPushDistributorManager
    private lateinit var profileSerializer: HcsProfileSerializer
    private lateinit var shizukuCommands: ShizukuCommands
    private lateinit var gmsRouter: GmsServiceRouter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        emuiProfile = EmuiCompatibilityProfile(this)
        appInspector = HcsAppInspector(this)
        logExporter = RedactedLogExporter(this)
        pushManager = PushEngineManager(this)
        mapManager = MapManager()
        compatDbClient = HcsCompatDbClient()
        updateManager = HcsUpdateManager()
        privilegedPatcher = PrivilegedPatcher(this)
        proxyServer = HcsLocalProxyServer()
        profiler = HcsPerformanceProfiler(this)
        distributorManager = UnifiedPushDistributorManager(this)
        profileSerializer = HcsProfileSerializer()
        shizukuCommands = ShizukuCommands()
        gmsRouter = GmsServiceRouter(this)

        setupSelfCheck()
        setupListeners()
    }

    private fun setupSelfCheck() {
        val shizukuState = ShizukuAvailability.getShizukuState()
        val isShizukuActive = shizukuCommands.isShizukuPermissionGranted
        val device = emuiProfile.getDeviceProfile(isShizukuActive)
        val spoofingStatus = emuiProfile.checkSignatureSpoofingStatus()
        val preferredPush = pushManager.getPreferredTransport()
        val defaultMapProvider = mapManager.getProvider(MapEngineType.OPEN_STREET_MAP)

        val infoText = StringBuilder().apply {
            append("Manufacturer: ").append(device.manufacturer).append("\n")
            append("Model: ").append(device.model).append(" (").append(device.codeName).append(")\n")
            append("Android: ").append(device.androidVersion).append(" (SDK ").append(device.sdkInt).append(")\n")
            append("EMUI Version: ").append(device.emuiVersion).append("\n")
            append("HMS Core Installed: ").append(if (device.hasHmsCore) "Yes (v${device.hmsCoreVersionCode})" else "No").append("\n")
            append("Push Agent Present: ").append(if (device.hasPushAgent) "Yes" else "No").append("\n")
            append("Shizuku Status: ").append(shizukuState)
        }.toString()

        binding.tvDeviceInfo.text = infoText
        binding.tvSignatureSpoofing.text = "Signature Spoofing: $spoofingStatus"
        binding.tvPushTransport.text = "Active Push: ${preferredPush.transportType}"
        binding.tvMapProvider.text = "Map Engine: ${defaultMapProvider.engineType}"
    }

    private fun setupListeners() {
        binding.btnBatterySettings.setOnClickListener {
            val intent = emuiProfile.createBatterySettingsIntent()
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, getString(R.string.msg_intent_failed), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnAutoStart.setOnClickListener {
            val intent = emuiProfile.createAutoStartIntent()
            if (intent != null) {
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, getString(R.string.msg_intent_failed), Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, getString(R.string.msg_intent_failed), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnToggleGmsBridge.setOnClickListener {
            val status = gmsRouter.getStatus(hasSignatureSpoofing = true)
            if (status.isMicroGInstalled) {
                Toast.makeText(this, status.warningMessage ?: "microG collision warning", Toast.LENGTH_LONG).show()
            } else {
                val toggleTask = if (status.isBridgeActive) gmsRouter.disableBridge() else gmsRouter.enableBridge(hasSignatureSpoofing = true)
                try {
                    val active = Tasks.await(toggleTask, 1, TimeUnit.SECONDS)
                    val msg = if (active) "HCS GMS Package Identity Bridge ENABLED" else "HCS GMS Bridge DISABLED"
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Failed to toggle GMS Bridge", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnCheckShizuku.setOnClickListener {
            if (shizukuCommands.isShizukuAvailable) {
                shizukuCommands.requestShizukuPermission(1001)
                Toast.makeText(this, ShizukuPermissionFlow.EXPLANATION_TEXT_ES, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Shizuku service is not running or not installed.", Toast.LENGTH_SHORT).show()
            }
            setupSelfCheck()
        }

        binding.btnRunBenchmark.setOnClickListener {
            val task = profiler.measureOverhead()
            try {
                val metrics = Tasks.await(task, 1, TimeUnit.SECONDS)
                Toast.makeText(this, metrics.summaryReport, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Benchmark failed", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnManageDistributors.setOnClickListener {
            val task = distributorManager.getInstalledDistributors()
            try {
                val dists = Tasks.await(task, 1, TimeUnit.SECONDS)
                val summary = dists.joinToString("\n") { "${it.name}: ${if (it.isInstalled) "Installed" else "Not installed"}" }
                Toast.makeText(this, summary, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Distributor query failed", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnExportOfflineProfile.setOnClickListener {
            val profile = OfflineProfile(
                packageName = "org.hcs.companion",
                appName = "HCS Companion",
                compatibilityLevel = "A",
                preferredPushTransport = "UNIFIED_PUSH",
                preferredMapEngine = "OPEN_STREET_MAP",
                notes = "Exported from HCS Companion Dashboard"
            )

            val exportFile = File(filesDir, "hcs_companion.hcsjson")
            val task = profileSerializer.exportProfileToFile(profile, exportFile)
            try {
                val success = Tasks.await(task, 1, TimeUnit.SECONDS)
                if (success) {
                    Toast.makeText(this, "Offline profile exported to: ${exportFile.absolutePath}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Profile export failed", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnInspect.setOnClickListener {
            val targetPkg = binding.etPackageName.text.toString().trim()
            if (targetPkg.isNotBlank()) {
                val result = appInspector.inspectPackage(targetPkg)
                val communityReportTask = compatDbClient.getReportForApp(targetPkg)

                var communityNotes = "No community report found."
                try {
                    val communityReport = Tasks.await(communityReportTask, 1, TimeUnit.SECONDS)
                    if (communityReport != null) {
                        communityNotes = "Community Level: ${communityReport.compatibilityLevel} - ${communityReport.summaryNotes}"
                    }
                } catch (ignored: Exception) { }

                val summary = StringBuilder().apply {
                    append("App Name: ").append(result.appName).append("\n")
                    append("Package: ").append(result.packageName).append("\n")
                    append("Version: ").append(result.versionName).append(" (").append(result.versionCode).append(")\n")
                    append("Local Compatibility Level: ").append(result.compatibilityLevel).append("\n")
                    append("Root Cause: ").append(result.primaryRootCause).append("\n")
                    append("GMS Libraries: ").append(if (result.detectedGmsLibraries.isEmpty()) "None" else result.detectedGmsLibraries.joinToString(", ")).append("\n")
                    if (targetPkg == "com.google.android.youtube") {
                        append("YouTube Feature Breakdown:\n")
                        append(" - Basic Playback: Level B (Functional)\n")
                        append(" - Account/Channel Sync: Level C (OAuth2/OpenID)\n")
                        append(" - Cast / 4K DRM Hardware Attestation: Level D (Unimplementable)\n")
                    }
                    append("Community DB: ").append(communityNotes)
                }.toString()

                binding.tvInspectionResult.text = summary
            }
        }

        binding.btnCheckUpdates.setOnClickListener {
            val updateTask = updateManager.checkForUpdates(1)
            try {
                val update = Tasks.await(updateTask, 1, TimeUnit.SECONDS)
                if (update != null) {
                    val msg = getString(R.string.msg_update_found, update.versionName, update.releaseNotes)
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, getString(R.string.msg_up_to_date), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, getString(R.string.msg_up_to_date), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSimulatePatch.setOnClickListener {
            val patchTask = privilegedPatcher.applySystemPatch("/tmp/simulated_hcs.conf", "MOCK_PATCH", isSimulation = true)
            try {
                val patchResult = Tasks.await(patchTask, 1, TimeUnit.SECONDS)
                Toast.makeText(this, patchResult.message, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Patch simulation failed", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnOpenGemini.setOnClickListener {
            val intent = android.content.Intent(this, GeminiChatActivity::class.java)
            startActivity(intent)
        }

        binding.btnTestGameServices.setOnClickListener {
            val gameClient = com.hcs.games.HcsGameServicesClient(this)
            gameClient.unlockAchievement("ach_first_game", "Primer Juego Local")
            gameClient.saveGameSnapshot("Partida 1 - Nivel 5", "score=5000;level=5")
            val snapshotsTask = gameClient.getSavedGameSnapshots()
            val list = Tasks.await(snapshotsTask, 1, TimeUnit.SECONDS)
            Toast.makeText(this, "HCS Game Services: ${list.size} partidas guardadas localmente.", Toast.LENGTH_LONG).show()
        }

        binding.btnTestNavigation.setOnClickListener {
            val navEngine = com.hcs.navigation.HcsNavigationEngine(this)
            val origin = org.hcs.maps.LatLng(19.4326, -99.1332)
            val destination = org.hcs.maps.LatLng(19.4350, -99.1400)
            val routeTask = navEngine.calculateRoute(origin, destination)
            val route = Tasks.await(routeTask, 1, TimeUnit.SECONDS)
            Toast.makeText(this, "Navegación HCS: Ruta calculada (${route.totalDistanceMeters}m).", Toast.LENGTH_LONG).show()
        }

        binding.btnTestOpenAi.setOnClickListener {
            val openAiClient = com.hcs.openai.HcsOpenAiClient(this)
            val responseTask = openAiClient.generateChatResponse("Hola ChatGPT")
            val response = Tasks.await(responseTask, 1, TimeUnit.SECONDS)
            Toast.makeText(this, response, Toast.LENGTH_LONG).show()
        }

        binding.btnExportLog.setOnClickListener {
            val rawDiagnosticData = """
                HCS Self-Check Export
                Device: ${binding.tvDeviceInfo.text}
                Status: ${binding.tvSignatureSpoofing.text}
                Push: ${binding.tvPushTransport.text}
                Simulated User Log Entry: user=john_doe@example.com token=bearer_secret_token_123456789 phone=+1234567890
            """.trimIndent()

            val exportedFile = logExporter.exportSanitizedLog(rawDiagnosticData)
            val msg = getString(R.string.msg_log_exported, exportedFile.absolutePath)
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }
    }
}
