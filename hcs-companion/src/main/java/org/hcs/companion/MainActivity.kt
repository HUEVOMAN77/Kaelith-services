package org.hcs.companion

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.hcs.companion.databinding.ActivityMainBinding
import org.hcs.diagnostics.HcsAppInspector
import org.hcs.diagnostics.RedactedLogExporter
import org.hcs.emui.EmuiCompatibilityProfile

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var emuiProfile: EmuiCompatibilityProfile
    private lateinit var appInspector: HcsAppInspector
    private lateinit var logExporter: RedactedLogExporter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        emuiProfile = EmuiCompatibilityProfile(this)
        appInspector = HcsAppInspector(this)
        logExporter = RedactedLogExporter(this)

        setupSelfCheck()
        setupListeners()
    }

    private fun setupSelfCheck() {
        val device = emuiProfile.getDeviceProfile()
        val spoofingStatus = emuiProfile.checkSignatureSpoofingStatus()

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
                val summary = StringBuilder().apply {
                    append("App Name: ").append(result.appName).append("\n")
                    append("Package: ").append(result.packageName).append("\n")
                    append("Version: ").append(result.versionName).append(" (").append(result.versionCode).append(")\n")
                    append("Compatibility Level: ").append(result.compatibilityLevel).append("\n")
                    append("Root Cause: ").append(result.primaryRootCause).append("\n")
                    append("GMS Libraries: ").append(if (result.detectedGmsLibraries.isEmpty()) "None" else result.detectedGmsLibraries.joinToString(", ")).append("\n")
                    append("Notes: ").append(result.summaryNotes)
                }.toString()

                binding.tvInspectionResult.text = summary
            }
        }

        binding.btnExportLog.setOnClickListener {
            val rawDiagnosticData = """
                HCS Self-Check Export
                Device: ${binding.tvDeviceInfo.text}
                Status: ${binding.tvSignatureSpoofing.text}
                Simulated User Log Entry: user=john_doe@example.com token=bearer_secret_token_123456789 phone=+1234567890
            """.trimIndent()

            val exportedFile = logExporter.exportSanitizedLog(rawDiagnosticData)
            val msg = getString(R.string.msg_log_exported, exportedFile.absolutePath)
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }
    }
}
