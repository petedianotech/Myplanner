package com.myplanner.app.ai

import com.myplanner.app.BuildConfig

/**
 * Where the API key comes from at runtime:
 * - BuildConfig.GEMINI_API_KEY baked at compile time from:
 *     1) root `local.properties` → GEMINI_API_KEY=...
 *     2) environment variable GEMINI_API_KEY (GitHub Actions secret)
 *     3) empty string if unset (local router still works)
 */
object GeminiConfig {
    val apiKey: String get() = BuildConfig.GEMINI_API_KEY.trim()
    val model: String get() = BuildConfig.GEMINI_MODEL
    val isConfigured: Boolean get() = apiKey.isNotEmpty()
}
