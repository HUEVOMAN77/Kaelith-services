package org.hcs.offlineprofiles

import org.hcs.tasks.Task
import org.hcs.tasks.TaskCompletionSource
import java.io.File

data class OfflineProfile(
    val packageName: String,
    val appName: String,
    val compatibilityLevel: String,
    val preferredPushTransport: String,
    val preferredMapEngine: String,
    val notes: String
)

class HcsProfileSerializer {

    fun exportProfileToFile(profile: OfflineProfile, targetFile: File): Task<Boolean> {
        val tcs = TaskCompletionSource<Boolean>()
        try {
            val jsonContent = """
                {
                  "package_name": "${profile.packageName}",
                  "app_name": "${profile.appName}",
                  "compatibility_level": "${profile.compatibilityLevel}",
                  "preferred_push_transport": "${profile.preferredPushTransport}",
                  "preferred_map_engine": "${profile.preferredMapEngine}",
                  "notes": "${profile.notes}"
                }
            """.trimIndent()

            targetFile.writeText(jsonContent)
            tcs.setResult(true)
        } catch (e: Exception) {
            tcs.setResult(false)
        }
        return tcs.task
    }

    fun importProfileFromFile(sourceFile: File): Task<OfflineProfile?> {
        val tcs = TaskCompletionSource<OfflineProfile?>()
        try {
            if (!sourceFile.exists()) {
                tcs.setResult(null)
                return tcs.task
            }

            val text = sourceFile.readText()
            val pkg = extractJsonValue(text, "package_name") ?: "unknown"
            val app = extractJsonValue(text, "app_name") ?: "unknown"
            val level = extractJsonValue(text, "compatibility_level") ?: "A"
            val push = extractJsonValue(text, "preferred_push_transport") ?: "UNIFIED_PUSH"
            val map = extractJsonValue(text, "preferred_map_engine") ?: "OPEN_STREET_MAP"
            val notes = extractJsonValue(text, "notes") ?: ""

            val profile = OfflineProfile(
                packageName = pkg,
                appName = app,
                compatibilityLevel = level,
                preferredPushTransport = push,
                preferredMapEngine = map,
                notes = notes
            )
            tcs.setResult(profile)
        } catch (e: Exception) {
            tcs.setResult(null)
        }
        return tcs.task
    }

    private fun extractJsonValue(json: String, key: String): String? {
        val regex = Regex("\"$key\"\\s*:\\s*\"([^\"]+)\"")
        val match = regex.find(json)
        return match?.groupValues?.get(1)
    }
}
