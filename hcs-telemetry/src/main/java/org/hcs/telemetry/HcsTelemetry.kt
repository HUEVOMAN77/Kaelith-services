package org.hcs.telemetry

import android.content.Context
import org.hcs.tasks.Task
import org.hcs.tasks.TaskCompletionSource
import java.io.File

data class AnonymizedCrashReport(
    val appVersion: String,
    val emuiVersion: String,
    val androidSdk: Int,
    val redactedStackTrace: String,
    val timestamp: Long = System.currentTimeMillis()
)

class HcsCrashReporter(private val context: Context) {

    private var isOptIn: Boolean = false

    fun setOptInStatus(enabled: Boolean) {
        this.isOptIn = enabled
    }

    fun reportCrash(throwable: Throwable, emuiVersion: String, appVersion: String): Task<Boolean> {
        val tcs = TaskCompletionSource<Boolean>()
        if (!isOptIn) {
            tcs.setResult(false)
            return tcs.task
        }

        val rawStack = throwable.stackTraceToString()
        val sanitizedStack = sanitizeStackTrace(rawStack)

        val report = AnonymizedCrashReport(
            appVersion = appVersion,
            emuiVersion = emuiVersion,
            androidSdk = android.os.Build.VERSION.SDK_INT,
            redactedStackTrace = sanitizedStack
        )

        // Save crash report locally without calling Google services
        tcs.setResult(saveReportLocally(report))
        return tcs.task
    }

    private fun sanitizeStackTrace(stackTrace: String): String {
        return stackTrace.replace(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}"), "[REDACTED_EMAIL]")
            .replace(Regex("\\+?[0-9]{10,15}"), "[REDACTED_PHONE]")
    }

    private fun saveReportLocally(report: AnonymizedCrashReport): Boolean {
        return try {
            val dir = File(context.filesDir, "telemetry_reports")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "crash_${report.timestamp}.txt")
            file.writeText("AppVer: ${report.appVersion}\nEMUI: ${report.emuiVersion}\nStack:\n${report.redactedStackTrace}")
            true
        } catch (e: Exception) {
            false
        }
    }
}
