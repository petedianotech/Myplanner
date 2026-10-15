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

class GeminiApiClient(private val apiKey: String = GeminiConfig.apiKey) {
    data class Turn(val role: String, val text: String)
    data class Source(val title: String, val uri: String)
    data class ChatResult(
        val text: String,
        val sources: List<Source> = emptyList(),
        val imageBase64: String? = null,
        val imageMime: String? = null,
        val usedSearch: Boolean = false
    )

    companion object {
        val SYSTEM_PROMPT = """
You are Pete, a sharp personal AI assistant inside the petediano Android app.
You help with planning, tasks, reminders, habits, focus, and general knowledge.
Be clear, direct, and useful — like a capable colleague, not a corporate bot.
Use short paragraphs. Prefer concrete next steps.
When the user asks about current events, news, prices, weather, sports, or anything time-sensitive, rely on Google Search results if available.
Never invent local app actions you cannot perform; the app handles task/reminder commands separately.
If you generate or describe an image, be specific and vivid.
""".trimIndent()
    }

    suspend fun chat(
        userMessage: String,
        history: List<Turn> = emptyList(),
        model: String = GeminiConfig.DEFAULT_MODEL,
        systemPrompt: String = SYSTEM_PROMPT,
        enableSearch: Boolean = true
    ): ChatResult = withContext(Dispatchers.IO) {
        require(apiKey.isNotBlank()) { "GEMINI_API_KEY is empty — add it in GitHub Secrets or local.properties" }
        val safeModel = model.ifBlank { GeminiConfig.DEFAULT_MODEL }
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$safeModel:generateContent?key=$apiKey")
        val contents = JSONArray()
        history.takeLast(16).forEach { turn ->
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
            .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemPrompt))))
            .put("generationConfig", JSONObject().put("temperature", 0.8).put("topP", 0.95).put("maxOutputTokens", 4096))
        if (enableSearch) {
            body.put("tools", JSONArray().put(JSONObject().put("google_search", JSONObject())))
        }
        post(url, body, safeModel)
    }

    suspend fun generateImage(
        prompt: String,
        model: String = GeminiConfig.DEFAULT_IMAGE_MODEL
    ): ChatResult = withContext(Dispatchers.IO) {
        require(apiKey.isNotBlank()) { "GEMINI_API_KEY is empty" }
        val safeModel = model.ifBlank { GeminiConfig.DEFAULT_IMAGE_MODEL }
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$safeModel:generateContent?key=$apiKey")
        val body = JSONObject()
            .put("contents", JSONArray().put(
                JSONObject().put("role", "user").put("parts", JSONArray().put(JSONObject().put("text", prompt)))
            ))
            .put("generationConfig", JSONObject()
                .put("responseModalities", JSONArray().put("TEXT").put("IMAGE"))
                .put("temperature", 1.0))
        post(url, body, safeModel)
    }

    private fun post(url: URL, body: JSONObject, safeModel: String): ChatResult {
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            doOutput = true
            connectTimeout = 45_000
            readTimeout = 120_000
        }
        try {
            OutputStreamWriter(conn.outputStream, StandardCharsets.UTF_8).use { it.write(body.toString()) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val response = stream?.bufferedReader(StandardCharsets.UTF_8)?.use(BufferedReader::readText).orEmpty()
            if (code !in 200..299) {
                val hint = when (code) {
                    404 -> "Model not found. Switch to 3.5 Flash-Lite in the model picker."
                    429 -> "Rate limited (free-tier quota). Wait a minute or switch to Flash-Lite."
                    503 -> "Model temporarily busy. Retry or switch model."
                    403 -> "Access denied. Check API key, or enable billing for image models."
                    400 -> "Bad request: ${response.take(200)}"
                    else -> response.take(280)
                }
                throw IllegalStateException("Gemini HTTP $code ($safeModel): $hint")
            }
            return parseResult(response)
        } finally {
            conn.disconnect()
        }
    }

    private fun parseResult(json: String): ChatResult {
        val root = JSONObject(json)
        val candidates = root.optJSONArray("candidates")
        if (candidates == null || candidates.length() == 0) return ChatResult("I couldn't form a reply.")
        val candidate = candidates.getJSONObject(0)
        val content = candidate.optJSONObject("content") ?: return ChatResult("Empty reply.")
        val parts = content.optJSONArray("parts") ?: return ChatResult("Empty reply.")
        val textSb = StringBuilder()
        var imageB64: String? = null
        var imageMime: String? = null
        for (i in 0 until parts.length()) {
            val part = parts.getJSONObject(i)
            val t = part.optString("text")
            if (t.isNotBlank()) {
                if (textSb.isNotEmpty()) textSb.append('\n')
                textSb.append(t)
            }
            val inline = part.optJSONObject("inlineData")
            if (inline != null) {
                imageMime = inline.optString("mimeType", "image/png")
                imageB64 = inline.optString("data")
            }
        }
        val sources = mutableListOf<Source>()
        var usedSearch = false
        val grounding = candidate.optJSONObject("groundingMetadata")
        if (grounding != null) {
            usedSearch = true
            val chunks = grounding.optJSONArray("groundingChunks")
            if (chunks != null) {
                for (i in 0 until chunks.length()) {
                    val web = chunks.getJSONObject(i).optJSONObject("web") ?: continue
                    val uri = web.optString("uri")
                    val title = web.optString("title").ifBlank { uri }
                    if (uri.isNotBlank()) sources.add(Source(title, uri))
                }
            }
            val queries = grounding.optJSONArray("webSearchQueries")
            if (queries != null && queries.length() > 0) usedSearch = true
        }
        val text = textSb.toString().ifBlank {
            if (imageB64 != null) "Here's the image." else "Empty reply."
        }
        return ChatResult(
            text = text,
            sources = sources.distinctBy { it.uri }.take(6),
            imageBase64 = imageB64,
            imageMime = imageMime,
            usedSearch = usedSearch
        )
    }
}
