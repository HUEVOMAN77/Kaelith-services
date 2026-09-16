package org.hcs.companion

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.hcs.companion.databinding.ActivityMainBinding
import org.hcs.compatdb.HcsCompatDbClient
import org.hcs.diagnostics.HcsAppInspector
import org.hcs.diagnostics.RedactedLogExporter
import org.hcs.emui.EmuiCompatibilityProfile
import org.hcs.maps.MapEngineType
import org.hcs.maps.MapManager
import org.hcs.privileged.PrivilegedPatcher
import org.hcs.push.PushEngineManager
import org.hcs.tasks.Tasks
import org.hcs.update.HcsUpdateManager
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

        setupSelfCheck()
        setupListeners()
    }

    private fun setupSelfCheck() {
        val device = emuiProfile.getDeviceProfile()
        val spoofingStatus = emuiProfile.checkSignatureSpoofingStatus()
        val preferredPush = pushManager.getPreferredTransport()
        val defaultMapProvider = mapManager.getProvider(MapEngineType.OPEN_STREET_MAP)

        val infoText = StringBuilder().apply {
            append("Manufacturer: ").append(device.manufacturer).append("\n")
            append("Model: ").append(device.model).append(" (").append(device.codeName).append(")\n")
            append("Android: ").append(device.androidVersion).append(" (SDK ").append(device.sdkInt).append(")\n")
            append("EMUI Version: ").append(device.emuiVersion).append("\n")
            append("HMS Core Installed: ").append(if (device.hasHmsCore) "Yes (v${device.hmsCoreVersionCode})" else "No").append("\n")
            append("Push Agent Present: ").append(if (device.hasPushAgent) "Yes" else "No")
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
