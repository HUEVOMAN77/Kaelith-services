package org.hcs.distributorinstaller

import android.content.Context
import org.hcs.tasks.Task
import org.hcs.tasks.TaskCompletionSource

data class DistributorInfo(
    val packageName: String,
    val name: String,
    val isInstalled: Boolean,
    val isRecommended: Boolean
)

class UnifiedPushDistributorManager(private val context: Context) {

    companion object {
        const val NTFY_PACKAGE = "org.ntfy.android"
        const val GOTIFY_PACKAGE = "com.github.gotify"
        const val HCS_EMBEDDED_PACKAGE = "org.hcs.companion"
    }

    fun getInstalledDistributors(): Task<List<DistributorInfo>> {
        val tcs = TaskCompletionSource<List<DistributorInfo>>()
        val distributors = mutableListOf<DistributorInfo>()

        var ntfyInstalled = false
        var gotifyInstalled = false

        try {
            val pm = context.packageManager
            if (pm != null) {
                try {
                    pm.getPackageInfo(NTFY_PACKAGE, 0)
                    ntfyInstalled = true
                } catch (ignored: Exception) { }

                try {
                    pm.getPackageInfo(GOTIFY_PACKAGE, 0)
                    gotifyInstalled = true
                } catch (ignored: Exception) { }
            }
        } catch (ignored: Exception) { }

        distributors.add(
            DistributorInfo(
                packageName = NTFY_PACKAGE,
                name = "ntfy.sh (Recommended)",
                isInstalled = ntfyInstalled,
                isRecommended = true
            )
        )

        distributors.add(
            DistributorInfo(
                packageName = GOTIFY_PACKAGE,
                name = "Gotify",
                isInstalled = gotifyInstalled,
                isRecommended = false
            )
        )

        distributors.add(
            DistributorInfo(
                packageName = HCS_EMBEDDED_PACKAGE,
                name = "HCS Embedded Push Server",
                isInstalled = true,
                isRecommended = true
            )
        )

        tcs.setResult(distributors)
        return tcs.task
    }
}
