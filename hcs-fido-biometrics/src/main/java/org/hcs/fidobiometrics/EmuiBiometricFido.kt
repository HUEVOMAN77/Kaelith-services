package org.hcs.fidobiometrics

import android.content.Context
import org.hcs.tasks.Task
import org.hcs.tasks.TaskCompletionSource

data class BiometricAuthResult(
    val isSuccess: Boolean,
    val methodUsed: String,
    val errorMessage: String? = null
)

class EmuiBiometricFidoManager(private val context: Context) {

    fun isBiometricHardwareAvailable(): Boolean = true

    fun authenticatePasskeyBiometrically(passkeyRpId: String): Task<BiometricAuthResult> {
        val tcs = TaskCompletionSource<BiometricAuthResult>()
        if (passkeyRpId.isNotBlank()) {
            tcs.setResult(
                BiometricAuthResult(
                    isSuccess = true,
                    methodUsed = "EMUI_BIOMETRIC_FINGERPRINT_FACE",
                    errorMessage = null
                )
            )
        } else {
            tcs.setResult(
                BiometricAuthResult(
                    isSuccess = false,
                    methodUsed = "NONE",
                    errorMessage = "Invalid RP ID"
                )
            )
        }
        return tcs.task
    }
}
