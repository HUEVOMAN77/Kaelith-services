package org.hcs.scan

import android.content.Context
import org.hcs.tasks.Task
import org.hcs.tasks.TaskCompletionSource

data class ScanResult(
    val rawText: String,
    val format: String,
    val isSuccess: Boolean,
    val errorMessage: String? = null
)

class HcsBarcodeScanner(private val context: Context) {

    fun scanFromBitmapData(bitmapBytes: ByteArray): Task<ScanResult> {
        val tcs = TaskCompletionSource<ScanResult>()
        if (bitmapBytes.isNotEmpty()) {
            tcs.setResult(
                ScanResult(
                    rawText = "https://hcs.org/qr_code_sample",
                    format = "QR_CODE",
                    isSuccess = true
                )
            )
        } else {
            tcs.setResult(
                ScanResult(
                    rawText = "",
                    format = "UNKNOWN",
                    isSuccess = false,
                    errorMessage = "Empty bitmap data provided"
                )
            )
        }
        return tcs.task
    }
}
