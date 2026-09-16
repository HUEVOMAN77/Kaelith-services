package org.hcs.compatdb

import org.hcs.tasks.Task
import org.hcs.tasks.TaskCompletionSource

data class CommunityReport(
    val packageName: String,
    val appVersionCode: Long,
    val appVersionName: String,
    val deviceModel: String,
    val emuiVersion: String,
    val androidSdk: Int,
    val compatibilityLevel: String, // A, B, C, D, E
    val failingApis: List<String>,
    val summaryNotes: String,
    val timestamp: Long = System.currentTimeMillis()
)

class HcsCompatDbClient {

    private val localDatabase = mutableMapOf<String, CommunityReport>()

    init {
        // Seed default known community reports
        localDatabase["com.aurorastore.targetapp"] = CommunityReport(
            packageName = "com.aurorastore.targetapp",
            appVersionCode = 100,
            appVersionName = "1.0.0",
            deviceModel = "NCO-LX3",
            emuiVersion = "13.0.0",
            androidSdk = 31,
            compatibilityLevel = "A",
            failingApis = emptyList(),
            summaryNotes = "Fully functional with HCS Fused Location & UnifiedPush."
        )
    }

    fun getReportForApp(packageName: String): Task<CommunityReport?> {
        val tcs = TaskCompletionSource<CommunityReport?>()
        tcs.setResult(localDatabase[packageName])
        return tcs.task
    }

    fun submitAnonymousReport(report: CommunityReport): Task<Boolean> {
        val tcs = TaskCompletionSource<Boolean>()
        if (report.packageName.isNotBlank()) {
            localDatabase[report.packageName] = report
            tcs.setResult(true)
        } else {
            tcs.setResult(false)
        }
        return tcs.task
    }
}
