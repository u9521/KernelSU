package me.weishu.kernelsu.ui.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.view.View
import android.view.ViewParent
import android.view.Window
import android.view.WindowManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider

/**
 * Applies a background blur effect to the current Window (Activity or Compose Dialog).
 *
 * **Note:** This effect is only visible on Android 12 (API level 31) and above.
 * On older versions, this modifier effectively does nothing.
 *
 * Do NOT use this modifier if you are embedding Compose content inside a legacy
 * `android.app.DialogFragment` or `androidx.fragment.app.DialogFragment`.
 *
 * In such hybrid scenarios, this logic may incorrectly identify the underlying
 * Activity's window instead of the floating Dialog's window, causing the blur
 * to appear on the Activity layer rather than the Dialog itself.
 *
 * @param radius The radius of the blur effect.
 */
fun Modifier.windowBlurBehind(radius: Dp = 16.dp, blurAnimationSpec: AnimationSpec<Float> = tween()) = composed {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return@composed this
    }

    val view = LocalView.current
    val density = LocalDensity.current

    val targetRadiusPx = with(density) { radius.roundToPx().toFloat() }

    val blurRadiusAnim = remember { Animatable(0f) }

    LaunchedEffect(targetRadiusPx) {
        blurRadiusAnim.animateTo(
            targetValue = targetRadiusPx,
            animationSpec = blurAnimationSpec
        )
    }

    DisposableEffect(view) {
        val window = view.findWindow()

        if (window != null) {
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)

            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

            window.attributes.dimAmount = 0f
            window.attributes = window.attributes
        }

        onDispose {}
    }

    // Sync the animated value to the Window attributes
    LaunchedEffect(view) {
        val window = view.findWindow() ?: return@LaunchedEffect

        snapshotFlow { blurRadiusAnim.value }.collect { currentRadius ->
            val newRadius = currentRadius.toInt().coerceAtLeast(0)
            if (window.attributes.blurBehindRadius != newRadius) {
                window.attributes.blurBehindRadius = newRadius
                window.attributes = window.attributes
            }
        }
    }
    this
}


private fun View.findWindow(): Window? {
    findDialogWindowProvider()?.let { return it.window }

    return context.findWindow()
}

private tailrec fun Context.findWindow(): Window? = when (this) {
    is Activity -> window
    is ContextWrapper -> baseContext.findWindow()
    else -> null
}

private fun View.findDialogWindowProvider(): DialogWindowProvider? = this as? DialogWindowProvider ?: parent?.findDialogWindowProvider()

private fun ViewParent.findDialogWindowProvider(): DialogWindowProvider? = this as? DialogWindowProvider ?: parent?.findDialogWindowProvider()