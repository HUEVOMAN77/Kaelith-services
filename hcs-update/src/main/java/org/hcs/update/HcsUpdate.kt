package org.hcs.update

import org.hcs.tasks.Task
import org.hcs.tasks.TaskCompletionSource

data class UpdateInfo(
    val versionCode: Long,
    val versionName: String,
    val downloadUrl: String,
    val sha256Checksum: String,
    val minSdk: Int,
    val releaseNotes: String,
    val signatureFingerprintSha256: String
)

class HcsUpdateManager {

    companion object {
        const val EXPECTED_RELEASE_SIGNATURE = "E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855"
    }

    fun checkForUpdates(currentVersionCode: Long): Task<UpdateInfo?> {
        val tcs = TaskCompletionSource<UpdateInfo?>()
        val latestUpdate = UpdateInfo(
            versionCode = 2,
            versionName = "1.1.0",
            downloadUrl = "https://hcs.org/releases/hcs-companion-v1.1.0.apk",
            sha256Checksum = "a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e",
            minSdk = 26,
            releaseNotes = "HCS Phase 5 update with community DB & self-updater.",
            signatureFingerprintSha256 = EXPECTED_RELEASE_SIGNATURE
        )

        if (latestUpdate.versionCode > currentVersionCode) {
            tcs.setResult(latestUpdate)
        } else {
            tcs.setResult(null)
        }
        return tcs.task
    }

    fun verifyUpdateSignature(update: UpdateInfo): Boolean {
        return update.signatureFingerprintSha256.equals(EXPECTED_RELEASE_SIGNATURE, ignoreCase = true)
    }
}
