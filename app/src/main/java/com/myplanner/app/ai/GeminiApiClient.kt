package com.myplanner.app.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * Production Gemini chat client (Google AI Studio API key).
 * Uses generateContent for reliable conversational replies.
 */
class GeminiApiClient(
    private val apiKey: String = GeminiConfig.apiKey,
    private val model: String = GeminiConfig.model
) {
    data class Turn(val role: String, val text: String)

    suspend fun chat(
        userMessage: String,
        history: List<Turn> = emptyList(),
        systemPrompt: String = GeminiLiveClient.DEFAULT_SYSTEM_PROMPT
    ): String = withContext(Dispatchers.IO) {
        require(apiKey.isNotBlank()) { "GEMINI_API_KEY is empty" }

        val url = URL(
            "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        )

        val contents = JSONArray()
        contents.put(
            JSONObject()
                .put("role", "user")
                .put("parts", JSONArray().put(JSONObject().put("text", "System instructions:\n$systemPrompt")))
        )
        contents.put(
            JSONObject()
                .put("role", "model")
                .put("parts", JSONArray().put(JSONObject().put("text", "Understood. I'm Pete. Ready when you are, boss.")))
        )
        history.takeLast(12).forEach { turn ->
            contents.put(
                JSONObject()
                    .put("role", if (turn.role == "model") "model" else "user")
                    .put("parts", JSONArray().put(JSONObject().put("text", turn.text)))
            )
        }
        contents.put(
            JSONObject()
                .put("role", "user")
                .put("parts", JSONArray().put(JSONObject().put("text", userMessage)))
        )

        val body = JSONObject()
            .put("contents", contents)
            .put("generationConfig", JSONObject().put("temperature", 0.7).put("maxOutputTokens", 1024))

        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            doOutput = true
            connectTimeout = 30_000
            readTimeout = 60_000
        }

        try {
            OutputStreamWriter(conn.outputStream, StandardCharsets.UTF_8).use { it.write(body.toString()) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val response = stream.bufferedReader(StandardCharsets.UTF_8).use(BufferedReader::readText)
            if (code !in 200..299) {
                throw IllegalStateException("Gemini HTTP $code: ${response.take(400)}")
            }
            parseText(response)
        } finally {
            conn.disconnect()
        }
    }

    private fun parseText(json: String): String {
        val root = JSONObject(json)
        val candidates = root.optJSONArray("candidates") ?: return "I couldn't form a reply."
        if (candidates.length() == 0) return "I couldn't form a reply."
        val content = candidates.getJSONObject(0).optJSONObject("content") ?: return "Empty reply."
        val parts = content.optJSONArray("parts") ?: return "Empty reply."
        val sb = StringBuilder()
        for (i in 0 until parts.length()) {
            val t = parts.getJSONObject(i).optString("text")
            if (t.isNotBlank()) {
                if (sb.isNotEmpty()) sb.append('\n')
                sb.append(t)
            }
        }
        return sb.toString().ifBlank { "Empty reply." }
    }
}
