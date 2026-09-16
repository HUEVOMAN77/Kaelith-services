package com.hcs.gemini

import android.content.Context
import org.hcs.tasks.Task
import org.hcs.tasks.TaskCompletionSource
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class HcsGeminiClient(private val context: Context) {

    private var apiKey: String? = null

    fun setApiKey(key: String) {
        this.apiKey = key
    }

    fun getApiKey(): String? = apiKey

    fun generateContent(prompt: String, base64Image: String? = null): Task<String> {
        val tcs = TaskCompletionSource<String>()
        val key = apiKey ?: "DEMO_KEY"

        thread {
            try {
                if (key == "DEMO_KEY") {
                    tcs.setResult("[Gemini Demo Mode] Respondiendo a: '$prompt'. Registra una API Key válida en Ajustes de Gemini para respuestas reales.")
                    return@thread
                }

                val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$key"
                val url = URL(endpoint)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val partsJson = if (base64Image != null) {
                    """[{"text": "$prompt"}, {"inline_data": {"mime_type": "image/jpeg", "data": "$base64Image"}}]"""
                } else {
                    """[{"text": "$prompt"}]"""
                }

                val jsonPayload = """
                    {
                      "contents": [{
                        "parts": $partsJson
                      }]
                    }
                """.trimIndent()

                OutputStreamWriter(conn.outputStream).use { writer ->
                    writer.write(jsonPayload)
                    writer.flush()
                }

                if (conn.responseCode == 200) {
                    val response = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                    tcs.setResult(parseTextFromResponse(response))
                } else {
                    val errorText = BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream)).use { it.readText() }
                    tcs.setException(RuntimeException("HTTP ${conn.responseCode}: $errorText"))
                }
            } catch (e: Exception) {
                tcs.setException(e)
            }
        }

        return tcs.task
    }

    private fun parseTextFromResponse(json: String): String {
        return try {
            val root = JSONObject(json)
            val candidates = root.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return parts.getJSONObject(0).optString("text", json)
                }
            }
            json
        } catch (e: Exception) {
            json
        }
    }
}
