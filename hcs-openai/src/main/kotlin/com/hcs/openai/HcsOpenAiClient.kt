package com.hcs.openai

import android.content.Context
import org.hcs.tasks.Task
import org.hcs.tasks.TaskCompletionSource
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class HcsOpenAiClient(private val context: Context) {

    private var apiKey: String? = null

    fun setApiKey(key: String) {
        this.apiKey = key
    }

    fun getApiKey(): String? = apiKey

    fun generateChatResponse(prompt: String, model: String = "gpt-4o-mini"): Task<String> {
        val tcs = TaskCompletionSource<String>()
        val key = apiKey ?: "DEMO_KEY"

        thread {
            try {
                if (key == "DEMO_KEY") {
                    tcs.setResult("[ChatGPT $model Demo Mode] Respondiendo a: '$prompt'. Configura tu API Key de OpenAI para respuestas reales.")
                    return@thread
                }

                val endpoint = "https://api.openai.com/v1/chat/completions"
                val url = URL(endpoint)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Authorization", "Bearer $key")
                conn.doOutput = true

                val payload = JSONObject().apply {
                    put("model", model)
                    val messages = JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", prompt)
                        })
                    }
                    put("messages", messages)
                }

                OutputStreamWriter(conn.outputStream).use { writer ->
                    writer.write(payload.toString())
                    writer.flush()
                }

                if (conn.responseCode == 200) {
                    val response = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                    tcs.setResult(parseContentFromResponse(response))
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

    private fun parseContentFromResponse(json: String): String {
        return try {
            val root = JSONObject(json)
            val choices = root.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val message = choices.getJSONObject(0).optJSONObject("message")
                return message?.optString("content", json) ?: json
            }
            json
        } catch (e: Exception) {
            json
        }
    }
}
