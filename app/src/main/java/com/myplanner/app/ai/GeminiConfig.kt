package com.myplanner.app.ai

import android.content.Context
import com.myplanner.app.BuildConfig

/**
 * Gemini catalog — free-tier text + search grounding + optional paid image models.
 * Updated Oct 2026 from Google AI Studio / Gemini API docs.
 */
object GeminiConfig {
    data class ModelOption(
        val id: String,
        val label: String,
        val blurb: String,
        val freeTier: Boolean = true,
        val supportsSearch: Boolean = true,
        val supportsImageOut: Boolean = false
    )

    val availableModels: List<ModelOption> = listOf(
        ModelOption("gemini-3.5-flash-lite", "3.5 Flash-Lite", "Fast · best free-tier reliability", freeTier = true, supportsSearch = true),
        ModelOption("gemini-3.1-flash-lite", "3.1 Flash-Lite", "Light · low latency", freeTier = true, supportsSearch = true),
        ModelOption("gemini-3.8-flash", "3.8 Flash", "Smartest free Flash · search + agents", freeTier = true, supportsSearch = true),
        ModelOption("gemini-3.6-flash", "3.6 Flash", "Balanced free Flash", freeTier = true, supportsSearch = true),
        ModelOption("gemini-2.5-flash-lite", "2.5 Flash-Lite", "Legacy free · high quota", freeTier = true, supportsSearch = true),
        ModelOption("gemini-2.5-flash", "2.5 Flash", "Legacy free Flash", freeTier = true, supportsSearch = true)
    )

    val imageModels: List<ModelOption> = listOf(
        ModelOption("gemini-3.1-flash-image", "Nano Banana 2", "Image gen · paid API", freeTier = false, supportsSearch = true, supportsImageOut = true),
        ModelOption("gemini-3.1-flash-lite-image", "Nano Banana 2 Lite", "Cheapest image · paid", freeTier = false, supportsSearch = false, supportsImageOut = true),
        ModelOption("gemini-2.5-flash-image", "Nano Banana", "Image gen · paid", freeTier = false, supportsSearch = false, supportsImageOut = true)
    )

    val liveModels: List<ModelOption> = listOf(
        ModelOption("gemini-3.8-live", "3.8 Live", "Default live voice"),
        ModelOption("gemini-3.1-flash-live-preview", "3.1 Flash Live", "Stable live fallback"),
        ModelOption("gemini-3.8-live-extended-thinking", "3.8 Live Think", "Deeper reasoning"),
        ModelOption("gemini-2.5-flash-native-audio-preview-12-2025", "2.5 Native Audio", "Legacy native audio")
    )

    const val DEFAULT_MODEL = "gemini-3.5-flash-lite"
    const val DEFAULT_IMAGE_MODEL = "gemini-3.1-flash-image"
    const val DEFAULT_LIVE_MODEL = "gemini-3.8-live"

    private const val PREFS = "pete_gemini"
    private const val KEY_MODEL = "selected_model"
    private const val KEY_LIVE = "selected_live_model"
    private const val KEY_SEARCH = "search_grounding"
    private const val KEY_IMAGE_MODEL = "image_model"

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

    fun isSearchEnabled(context: Context): Boolean {
        return context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_SEARCH, true)
    }

    fun setSearchEnabled(context: Context, enabled: Boolean) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_SEARCH, enabled).apply()
    }

    fun getImageModel(context: Context): String {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_IMAGE_MODEL, null)
        if (saved != null && imageModels.any { it.id == saved }) return saved
        return DEFAULT_IMAGE_MODEL
    }

    fun setImageModel(context: Context, modelId: String) {
        if (imageModels.none { it.id == modelId }) return
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_IMAGE_MODEL, modelId).apply()
    }

    fun labelFor(modelId: String): String =
        availableModels.find { it.id == modelId }?.label
            ?: imageModels.find { it.id == modelId }?.label
            ?: modelId
}
