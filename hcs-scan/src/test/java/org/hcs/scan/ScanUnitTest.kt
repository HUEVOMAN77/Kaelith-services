package org.hcs.scan

import org.hcs.tasks.Tasks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class ScanUnitTest {

    @Test
    fun testScanFromBitmapDataSuccess() {
        val scanner = HcsBarcodeScanner(DummyContext())
        val mockBytes = byteArrayOf(1, 2, 3, 4)
        val task = scanner.scanFromBitmapData(mockBytes)
        val result = Tasks.await(task, 1, TimeUnit.SECONDS)

        assertTrue(result.isSuccess)
        assertEquals("QR_CODE", result.format)
        assertTrue(result.rawText.contains("hcs.org"))
    }
}

private class DummyContext : android.content.ContextWrapper(null)
