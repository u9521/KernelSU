package me.weishu.kernelsu.ui.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.view.ViewParent
import android.view.Window
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import me.weishu.kernelsu.ui.theme.LocalEnableBlur

val LocalBlurController = staticCompositionLocalOf<BlurController> {
    error("CompositionLocal LocalBlurController not present")
}

/**
 * Describes a single blur request sent to [BlurController].
 *
 * @param radius target blur radius in dp
 * @param animationSpec transition spec for entering/leaving blur
 * @param backgroundColor background color behind the captured haze source,
 *   set via [HazeStyle.backgroundColor]. When the source composable has
 *   transparent regions, those pixels are captured as-is — causing wallpapers
 *   or launcher content to bleed through the blur. Providing a non-transparent
 *   [backgroundColor] fills those gaps. [Color.Unspecified] means no fill.
 */
data class BlurRequest(
    val radius: Dp,
    val animationSpec: AnimationSpec<Dp>,
    val backgroundColor: Color = Color.Unspecified
)

class BlurController {
    private val activeRequests = mutableStateListOf<BlurRequest>()

    fun show(request: BlurRequest) {
        activeRequests.add(request)
    }

    fun disappear(request: BlurRequest) {
        activeRequests.remove(request)
    }

    fun clearAll() {
        activeRequests.clear()
    }

    val currentRequest: BlurRequest?
        get() = activeRequests.lastOrNull()
}

@Composable
fun rememberBlurController() = remember { BlurController() }

/**
 * Applies a haze blur to the content of the composable this modifier is
 * attached to. The composable's own content is used as the blur source —
 * similar to [Modifier.blur] but with Haze styling.
 *
 * The blur is controlled by [BlurController]: it only renders when an
 * active [BlurRequest] is registered (e.g., via [windowBlurBehind]).
 * The composable must be inside a [LocalBlurController] scope:
 *
 * ```
 * val blurController = rememberBlurController()
 * CompositionLocalProvider(LocalBlurController provides blurController) {
 *     Box(Modifier.blurOverlay()) { ... }
 * }
 * ```
 *
 * @param backgroundColor fill color for transparent source regions.
 *   `null` (or omitted) falls back to [MaterialTheme.colorScheme.surface].
 * @see BlurRequest
 * @see windowBlurBehind
 */
@OptIn(ExperimentalHazeApi::class)
fun Modifier.blurOverlay(backgroundColor: Color? = null): Modifier = composed {
    if (!LocalEnableBlur.current) return@composed this
    val blurController = LocalBlurController.current
    val currentRequest = blurController.currentRequest

    var lastValidRequest by remember { mutableStateOf<BlurRequest?>(null) }

    if (currentRequest != null) {
        lastValidRequest = currentRequest
    }


    val targetRadius = currentRequest?.radius ?: 0.dp
    val activeAnimationSpec = (currentRequest ?: lastValidRequest)?.animationSpec ?: tween()

    val blurRadius by animateDpAsState(
        targetValue = targetRadius,
        animationSpec = activeAnimationSpec,
        label = "blurRadius"
    )

    if (blurRadius > 0.dp) {
        val requestBg = currentRequest?.backgroundColor ?: Color.Unspecified
        val effectiveBg = if (requestBg != Color.Unspecified) requestBg else backgroundColor ?: MaterialTheme.colorScheme.surface
        this.hazeEffect(
            style = HazeStyle(
                backgroundColor = effectiveBg,
                blurRadius = blurRadius,
                noiseFactor = 0.0065f,
                tint = null
            )
        ) {
            inputScale = HazeInputScale.Fixed(0.5f)
            forceInvalidateOnPreDraw = true
        }
    } else {
        this
    }
}


/**
 * Registers a blur request with [BlurController] for the lifetime of the
 * composable this modifier is attached to (typically a [Dialog]).
 *
 * Pair with [blurOverlay] on the same composable to render the blur. Sets
 * the parent window's `dimAmount` to `0f` to remove the system scrim.
 *
 * @param radius target blur intensity in dp (default 20.dp)
 * @param blurAnimationSpec transition spec for enter/exit animation
 * @see blurOverlay
 * @see BlurRequest
 */
fun Modifier.windowBlurBehind(radius: Dp = 20.dp, blurAnimationSpec: AnimationSpec<Dp> = tween(durationMillis = 300)) = composed {
    if (!LocalEnableBlur.current) return@composed this
    val view = LocalView.current
    val blurController = LocalBlurController.current
    val request = remember(radius, blurAnimationSpec) {
        BlurRequest(radius = radius, animationSpec = blurAnimationSpec)
    }

    DisposableEffect(view, request) {
        val window = view.findWindow()
        window?.setDimAmount(0f)
        blurController.show(request)
        onDispose {
            blurController.disappear(request)
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
