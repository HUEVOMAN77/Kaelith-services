package org.hcs.webview

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.webkit.WebView

data class WebViewStatus(
    val packageName: String,
    val versionName: String,
    val isMultiProcessEnabled: Boolean,
    val isSufficientForHcs: Boolean
)

class HcsWebViewInspector(private val context: Context) {

    fun inspectSystemWebView(): WebViewStatus {
        val pm = context.packageManager
        val webViewPackage = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WebView.getCurrentWebViewPackage()?.packageName ?: "com.google.android.webview"
            } else {
                "com.google.android.webview"
            }
        } catch (e: Exception) {
            "com.huawei.webview"
        }

        var verName = "1.0.0"
        try {
            val info = pm.getPackageInfo(webViewPackage, 0)
            verName = info.versionName ?: "1.0.0"
        } catch (ignored: PackageManager.NameNotFoundException) { }

        val isSufficient = verName.isNotBlank() && !verName.startsWith("0.")

        return WebViewStatus(
            packageName = webViewPackage,
            versionName = verName,
            isMultiProcessEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O,
            isSufficientForHcs = isSufficient
        )
    }
}
