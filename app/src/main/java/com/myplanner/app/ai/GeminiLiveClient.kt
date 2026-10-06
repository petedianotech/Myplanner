package com.myplanner.app.ai

/**
 * Gemini Live API contract for Pete.
 */
interface GeminiLiveClient {
    val isSessionActive: Boolean

    suspend fun startSession(systemPrompt: String = DEFAULT_SYSTEM_PROMPT)

    suspend fun sendUserMessage(text: String)

    suspend fun sendAudioChunk(bytes: ByteArray) {}

    suspend fun endSession()

    fun interface Listener {
        fun onPeteText(text: String)
        fun onToolCall(name: String, argsJson: String) {}
        fun onError(message: String) {}
        fun onSessionEnded() {}
    }

    fun setListener(listener: Listener?) {}

    companion object {
        val DEFAULT_SYSTEM_PROMPT: String =
            "You are Pete, a personal Android assistant. Address the user as boss when it fits. " +
            "Be concise, loyal, and practical. You manage tasks, reminders, notes, focus timers, " +
            "and daily briefs via tools. Prefer short spoken replies. Privacy-first: data stays on device unless the user exports it."
    }
}

class GeminiLiveClientStub : GeminiLiveClient {
    override val isSessionActive: Boolean = false
    override suspend fun startSession(systemPrompt: String) {}
    override suspend fun sendUserMessage(text: String) {}
    override suspend fun endSession() {}
}
