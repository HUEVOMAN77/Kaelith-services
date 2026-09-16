package org.hcs.webview

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WebViewUnitTest {

    @Test
    fun testWebViewStatusDefaults() {
        val status = WebViewStatus(
            packageName = "com.huawei.webview",
            versionName = "12.0.0.300",
            isMultiProcessEnabled = true,
            isSufficientForHcs = true
        )

        assertNotNull(status.packageName)
        assertTrue(status.isSufficientForHcs)
    }
}
