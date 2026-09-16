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
import org.hcs.update.HcsUpdateManager
import java.io.File

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
                toggleTask.addOnCompleteListener { completed ->
                    val active = completed.result ?: false
                    val msg = if (active) "HCS GMS Package Identity Bridge ENABLED" else "HCS GMS Bridge DISABLED"
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
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
            profiler.measureOverhead().addOnCompleteListener { completed ->
                val metrics = completed.result
                Toast.makeText(this, metrics?.summaryReport ?: "Benchmark completado", Toast.LENGTH_LONG).show()
            }
        }

        binding.btnManageDistributors.setOnClickListener {
            distributorManager.getInstalledDistributors().addOnCompleteListener { completed ->
                val dists = completed.result ?: emptyList()
                val summary = dists.joinToString("\n") { "${it.name}: ${if (it.isInstalled) "Installed" else "Not installed"}" }
                Toast.makeText(this, summary, Toast.LENGTH_LONG).show()
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
            profileSerializer.exportProfileToFile(profile, exportFile).addOnCompleteListener { completed ->
                if (completed.result == true) {
                    Toast.makeText(this, "Offline profile exported to: ${exportFile.absolutePath}", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.btnInspect.setOnClickListener {
            val targetPkg = binding.etPackageName.text.toString().trim()
            if (targetPkg.isNotBlank()) {
                val result = appInspector.inspectPackage(targetPkg)
                compatDbClient.getReportForApp(targetPkg).addOnCompleteListener { completed ->
                    val communityReport = completed.result
                    val communityNotes = if (communityReport != null) {
                        "Community Level: ${communityReport.compatibilityLevel} - ${communityReport.summaryNotes}"
                    } else {
                        "No community report found."
                    }

                    val summary = StringBuilder().apply {
                        append("App Name: ").append(result.appName).append("\n")
                        append("Package: ").append(result.packageName).append("\n")
                        append("Version: ").append(result.versionName).append(" (").append(result.versionCode).append(")\n")
                        append("Local Compatibility Level: ").append(result.compatibilityLevel).append("\n")
                        append("Root Cause: ").append(result.primaryRootCause).append("\n")
                        append("GMS Libraries: ").append(if (result.detectedGmsLibraries.isEmpty()) "None" else result.detectedGmsLibraries.joinToString(", ")).append("\n")
                        append("Community DB: ").append(communityNotes)
                    }.toString()

                    binding.tvInspectionResult.text = summary
                }
            }
        }

        binding.btnCheckUpdates.setOnClickListener {
            updateManager.checkForUpdates(1).addOnCompleteListener { completed ->
                val update = completed.result
                if (update != null) {
                    val msg = getString(R.string.msg_update_found, update.versionName, update.releaseNotes)
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, getString(R.string.msg_up_to_date), Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnSimulatePatch.setOnClickListener {
            privilegedPatcher.applySystemPatch("/tmp/simulated_hcs.conf", "MOCK_PATCH", isSimulation = true).addOnCompleteListener { completed ->
                val patchResult = completed.result
                Toast.makeText(this, patchResult?.message ?: "Simulación completada", Toast.LENGTH_LONG).show()
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
            gameClient.getSavedGameSnapshots().addOnCompleteListener { completed ->
                val list = completed.result ?: emptyList()
                Toast.makeText(this, "HCS Game Services: ${list.size} partidas guardadas localmente.", Toast.LENGTH_LONG).show()
            }
        }

        binding.btnTestNavigation.setOnClickListener {
            val navEngine = com.hcs.navigation.HcsNavigationEngine(this)
            val origin = org.hcs.maps.LatLng(19.4326, -99.1332)
            val destination = org.hcs.maps.LatLng(19.4350, -99.1400)
            navEngine.calculateRoute(origin, destination).addOnCompleteListener { completed ->
                val route = completed.result
                Toast.makeText(this, "Navegación HCS: Ruta calculada (${route?.totalDistanceMeters ?: 0.0}m).", Toast.LENGTH_LONG).show()
            }
        }

        binding.btnTestOpenAi.setOnClickListener {
            val openAiClient = com.hcs.openai.HcsOpenAiClient(this)
            openAiClient.generateChatResponse("Hola ChatGPT").addOnCompleteListener { completed ->
                val response = completed.result ?: "Sin respuesta"
                Toast.makeText(this, response, Toast.LENGTH_LONG).show()
            }
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
