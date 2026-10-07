package com.myplanner.app.ai

import android.content.Context
import com.myplanner.app.BuildConfig

/**
 * Gemini model catalog (Oct 2026).
 * Free-tier friendly defaults; 2.0 models are shut down.
 * Prefer Flash-Lite when 3.8 hits capacity (503) on free tier.
 */
object GeminiConfig {
    data class ModelOption(
        val id: String,
        val label: String,
        val blurb: String
    )

    /** Models available for chat (generateContent). */
    val availableModels: List<ModelOption> = listOf(
        ModelOption(
            id = "gemini-3.5-flash-lite",
            label = "3.5 Flash-Lite",
            blurb = "Fast · best free-tier reliability"
        ),
        ModelOption(
            id = "gemini-3.1-flash-lite",
            label = "3.1 Flash-Lite",
            blurb = "Light · low latency"
        ),
        ModelOption(
            id = "gemini-3.6-flash",
            label = "3.6 Flash",
            blurb = "Balanced speed & quality"
        ),
        ModelOption(
            id = "gemini-3.8-flash",
            label = "3.8 Flash",
            blurb = "Newest · may hit free-tier limits"
        ),
        ModelOption(
            id = "gemini-3.5-flash",
            label = "3.5 Flash",
            blurb = "Strong general chat"
        )
    )

    const val DEFAULT_MODEL = "gemini-3.5-flash-lite"
    private const val PREFS = "pete_gemini"
    private const val KEY_MODEL = "selected_model"

    val apiKey: String get() = BuildConfig.GEMINI_API_KEY.trim()
    val isConfigured: Boolean get() = apiKey.isNotEmpty()

    /** Build-time fallback if user has not chosen yet. */
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
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MODEL, modelId)
            .apply()
    }

    fun labelFor(modelId: String): String =
        availableModels.firstOrNull { it.id == modelId }?.label ?: modelId
}
