package org.hcs.companion

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hcs.gemini.HcsGeminiClient

class GeminiChatActivity : AppCompatActivity() {

    private lateinit var geminiClient: HcsGeminiClient
    private lateinit var tvChatLog: TextView
    private lateinit var etPrompt: EditText
    private lateinit var btnSend: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gemini_chat)

        geminiClient = HcsGeminiClient(applicationContext)
        tvChatLog = findViewById(R.id.tvChatLog)
        etPrompt = findViewById(R.id.etPrompt)
        btnSend = findViewById(R.id.btnSend)

        btnSend.setOnClickListener {
            val prompt = etPrompt.text.toString().trim()
            if (prompt.isNotEmpty()) {
                appendChat("Usuario", prompt)
                etPrompt.setText("")

                val task = geminiClient.generateContent(prompt)
                task.addOnCompleteListener { completedTask ->
                    runOnUiThread {
                        if (completedTask.isSuccessful) {
                            appendChat("Gemini", completedTask.result ?: "Sin respuesta")
                        } else {
                            appendChat("Gemini (Error)", completedTask.exception?.message ?: "Error desconocido")
                        }
                    }
                }
            }
        }
    }

    private fun appendChat(sender: String, message: String) {
        val currentText = tvChatLog.text.toString()
        val newEntry = "\n\n[$sender]: $message"
        tvChatLog.text = currentText + newEntry
    }
}
