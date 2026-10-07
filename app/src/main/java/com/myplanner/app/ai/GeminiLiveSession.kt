package com.myplanner.app.ai

import android.util.Base64
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Real Gemini Live API session over WebSocket (BidiGenerateContent).
 * Models: gemini-3.8-live, gemini-3.1-flash-live-preview, etc.
 *
 * Input audio: 16-bit PCM LE mono 16 kHz
 * Output audio: 16-bit PCM LE mono 24 kHz
 */
class GeminiLiveSession(
    private val apiKey: String,
    private val model: String,
    private val systemPrompt: String = DEFAULT_LIVE_PROMPT,
    private val listener: Listener
) {
    interface Listener {
        fun onSetupComplete()
        fun onUserTranscript(text: String)
        fun onModelTranscript(text: String)
        fun onAudioOut(pcm24k: ByteArray)
        fun onInterrupted()
        fun onError(message: String)
        fun onClosed()
    }

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    private var socket: WebSocket? = null
    private val open = AtomicBoolean(false)
    private val setupDone = AtomicBoolean(false)

    fun connect() {
        if (apiKey.isBlank()) {
            listener.onError("GEMINI_API_KEY missing")
            return
        }
        val url =
            "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent?key=$apiKey"
        val request = Request.Builder().url(url).build()
        socket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                open.set(true)
                sendSetup(webSocket)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleServerMessage(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                handleServerMessage(bytes.utf8())
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                open.set(false)
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                open.set(false)
                setupDone.set(false)
                listener.onClosed()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                open.set(false)
                setupDone.set(false)
                Log.e(TAG, "Live WS failure", t)
                listener.onError(t.message ?: "Live connection failed")
                listener.onClosed()
            }
        })
    }

    private fun sendSetup(ws: WebSocket) {
        val setup = JSONObject()
            .put(
                "setup",
                JSONObject()
                    .put("model", "models/$model")
                    .put(
                        "generationConfig",
                        JSONObject().put(
                            "responseModalities",
                            JSONArray().put("AUDIO")
                        )
                    )
                    .put(
                        "systemInstruction",
                        JSONObject().put(
                            "parts",
                            JSONArray().put(JSONObject().put("text", systemPrompt))
                        )
                    )
            )
        ws.send(setup.toString())
    }

    fun sendAudioPcm16k(pcm: ByteArray) {
        if (!open.get() || !setupDone.get()) return
        val b64 = Base64.encodeToString(pcm, Base64.NO_WRAP)
        val msg = JSONObject()
            .put(
                "realtimeInput",
                JSONObject().put(
                    "audio",
                    JSONObject()
                        .put("data", b64)
                        .put("mimeType", "audio/pcm;rate=16000")
                )
            )
        socket?.send(msg.toString())
    }

    fun sendText(text: String) {
        if (!open.get() || !setupDone.get() || text.isBlank()) return
        val msg = JSONObject()
            .put(
                "clientContent",
                JSONObject()
                    .put(
                        "turns",
                        JSONArray().put(
                            JSONObject()
                                .put("role", "user")
                                .put(
                                    "parts",
                                    JSONArray().put(JSONObject().put("text", text))
                                )
                        )
                    )
                    .put("turnComplete", true)
            )
        socket?.send(msg.toString())
    }

    fun close() {
        try {
            socket?.close(1000, "client end")
        } catch (_: Exception) {
        }
        socket = null
        open.set(false)
        setupDone.set(false)
    }

    private fun handleServerMessage(raw: String) {
        try {
            val root = JSONObject(raw)
            if (root.has("setupComplete")) {
                setupDone.set(true)
                listener.onSetupComplete()
                return
            }
            val server = root.optJSONObject("serverContent") ?: return
            if (server.optBoolean("interrupted", false)) {
                listener.onInterrupted()
            }
            server.optJSONObject("inputTranscription")?.optString("text")?.takeIf { it.isNotBlank() }?.let {
                listener.onUserTranscript(it)
            }
            server.optJSONObject("outputTranscription")?.optString("text")?.takeIf { it.isNotBlank() }?.let {
                listener.onModelTranscript(it)
            }
            val modelTurn = server.optJSONObject("modelTurn") ?: return
            val parts = modelTurn.optJSONArray("parts") ?: return
            for (i in 0 until parts.length()) {
                val part = parts.optJSONObject(i) ?: continue
                val inline = part.optJSONObject("inlineData") ?: continue
                val mime = inline.optString("mimeType")
                val data = inline.optString("data")
                if (data.isBlank()) continue
                if (mime.startsWith("audio/") || mime.isBlank()) {
                    val pcm = Base64.decode(data, Base64.DEFAULT)
                    if (pcm.isNotEmpty() && pcm.size > 4) {
                        listener.onAudioOut(pcm)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Parse error: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "GeminiLiveSession"

        const val DEFAULT_LIVE_PROMPT =
            "You are Pete, the personal voice assistant for petediano. " +
                "Speak briefly and naturally like a helpful friend. Call the user boss. " +
                "If the user says stop, goodbye, end call, or hang up, reply briefly and stop talking."

        val END_PHRASES = listOf(
            "stop", "goodbye", "good bye", "end call", "hang up", "bye pete", "bye peter"
        )

        val START_PHRASES = listOf(
            "start call", "hey peter", "hey pete", "wakeup", "wake up", "call pete", "live call"
        )

        fun containsEndPhrase(text: String): Boolean {
            val t = text.lowercase()
            return END_PHRASES.any { t.contains(it) }
        }

        fun containsStartPhrase(text: String): Boolean {
            val t = text.lowercase()
            return START_PHRASES.any { t.contains(it) }
        }
    }
}
