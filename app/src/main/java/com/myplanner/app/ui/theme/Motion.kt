package com.myplanner.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

/**
 * Restrained motion tokens for MyPlanner.
 * Prefer short, ease-out transitions. Avoid bounce and spin.
 */
object AppMotion {
    const val ShortMs = 150
    const val MediumMs = 250
    const val LongMs = 350

    val EaseOut = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
    val EaseInOut = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)

    fun <T> shortTween() = tween<T>(durationMillis = ShortMs, easing = EaseOut)
    fun <T> mediumTween() = tween<T>(durationMillis = MediumMs, easing = EaseOut)
    fun <T> longTween() = tween<T>(durationMillis = LongMs, easing = EaseInOut)

    val fadeInShort = fadeIn(animationSpec = shortTween())
    val fadeOutShort = fadeOut(animationSpec = shortTween())

    val sheetEnter = slideInVertically(
        animationSpec = mediumTween(),
        initialOffsetY = { it / 4 }
    ) + fadeIn(animationSpec = mediumTween())

    val sheetExit = slideOutVertically(
        animationSpec = mediumTween(),
        targetOffsetY = { it / 4 }
    ) + fadeOut(animationSpec = mediumTween())
}
