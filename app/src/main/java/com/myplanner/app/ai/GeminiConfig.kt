package com.myplanner.app.ai

import android.content.Context
import com.myplanner.app.BuildConfig

/**
 * Gemini catalog — text chat (generateContent) + Live voice (BidiGenerateContent).
 */
object GeminiConfig {
    data class ModelOption(
        val id: String,
        val label: String,
        val blurb: String
    )

    /** Text chat models. */
    val availableModels: List<ModelOption> = listOf(
        ModelOption("gemini-3.5-flash-lite", "3.5 Flash-Lite", "Fast · free-tier reliable"),
        ModelOption("gemini-3.1-flash-lite", "3.1 Flash-Lite", "Light · low latency"),
        ModelOption("gemini-3.6-flash", "3.6 Flash", "Balanced"),
        ModelOption("gemini-3.8-flash", "3.8 Flash", "Newest text"),
        ModelOption("gemini-3.5-flash", "3.5 Flash", "Strong chat")
    )

    /** Live voice models (WebSocket BidiGenerateContent). */
    val liveModels: List<ModelOption> = listOf(
        ModelOption("gemini-3.8-live", "3.8 Live", "Default live voice"),
        ModelOption("gemini-3.1-flash-live-preview", "3.1 Flash Live", "Stable live fallback"),
        ModelOption("gemini-3.8-live-extended-thinking", "3.8 Live Think", "Deeper reasoning"),
        ModelOption(
            "gemini-2.5-flash-native-audio-preview-12-2025",
            "2.5 Native Audio",
            "Legacy native audio"
        )
    )

    const val DEFAULT_MODEL = "gemini-3.5-flash-lite"
    const val DEFAULT_LIVE_MODEL = "gemini-3.8-live"

    private const val PREFS = "pete_gemini"
    private const val KEY_MODEL = "selected_model"
    private const val KEY_LIVE = "selected_live_model"

    val apiKey: String get() = BuildConfig.GEMINI_API_KEY.trim()
    val isConfigured: Boolean get() = apiKey.isNotEmpty()

    val buildDefaultModel: String
        get() {
            val baked = BuildConfig.GEMINI_MODEL.trim()
            return if (baked.isNotEmpty() && availableModels.any { it.id == baked }) baked
            else DEFAULT_MODEL
        }

    fun getSelectedModel(context: Context): String {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_MODEL, null)
        if (saved != null && availableModels.any { it.id == saved }) return saved
        return buildDefaultModel
    }

    fun setSelectedModel(context: Context, modelId: String) {
        if (availableModels.none { it.id == modelId }) return
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_MODEL, modelId).apply()
    }

    fun getSelectedLiveModel(context: Context): String {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_LIVE, null)
        if (saved != null && liveModels.any { it.id == saved }) return saved
        return DEFAULT_LIVE_MODEL
    }

    fun setSelectedLiveModel(context: Context, modelId: String) {
        if (liveModels.none { it.id == modelId }) return
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_LIVE, modelId).apply()
    }

    fun labelFor(modelId: String): String =
        availableModels.firstOrNull { it.id == modelId }?.label
            ?: liveModels.firstOrNull { it.id == modelId }?.label
            ?: modelId
}
