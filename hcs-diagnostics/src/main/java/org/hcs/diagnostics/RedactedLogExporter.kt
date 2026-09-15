package org.hcs.diagnostics

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generates sanitized log diagnostic exports that automatically strip sensitive PII
 * such as tokens, email addresses, phone numbers, IMEI, and MAC addresses.
 */
class RedactedLogExporter(private val context: Context) {

    companion object {
        private val EMAIL_REGEX = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}")
        private val AUTH_TOKEN_REGEX = Regex("(bearer|token|auth|key)[=:\\s]+[A-Za-z0-9._~+/-]{16,}", RegexOption.IGNORE_CASE)
        private val PHONE_REGEX = Regex("\\+?[0-9]{10,15}")
        private val IMEI_MAC_REGEX = Regex("([0-9a-fA-F]{2}[:.-]){5}[0-9a-fA-F]{2}|[0-9]{15}")
    }

    /**
     * Sanitizes raw text input by replacing identified sensitive data patterns with [REDACTED].
     */
    fun sanitizeLogText(input: String): String {
        var redacted = input
        redacted = EMAIL_REGEX.replace(redacted, "[REDACTED_EMAIL]")
        redacted = AUTH_TOKEN_REGEX.replace(redacted, "$1=[REDACTED_TOKEN]")
        redacted = PHONE_REGEX.replace(redacted, "[REDACTED_PHONE]")
        redacted = IMEI_MAC_REGEX.replace(redacted, "[REDACTED_DEVICE_ID]")
        return redacted
    }

    /**
     * Exports redacted system diagnostic logs to a local sanitized text file.
     */
    fun exportSanitizedLog(rawLogContent: String): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "HCS_Diagnostic_Log_$timeStamp.txt"
        val exportDir = File(context.filesDir, "diagnostics_logs")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }

        val sanitizedContent = sanitizeLogText(rawLogContent)
        val file = File(exportDir, fileName)
        file.writeText(sanitizedContent)
        return file
    }
}
