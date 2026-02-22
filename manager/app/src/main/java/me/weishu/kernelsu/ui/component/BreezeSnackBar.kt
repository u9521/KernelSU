package me.weishu.kernelsu.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalAccessibilityManager
import kotlinx.coroutines.delay

private const val ANIMATION_DURATION = 450
private const val ANIMATION_FADE_DURATION = 200

class OvershootEasing(private val tension: Float = 2.0f) : Easing {
    override fun transform(fraction: Float): Float {
        val t = fraction - 1.0f
        return t * t * ((tension + 1) * t + tension) + 1.0f
    }
}

@Composable
fun getSnackBarEnterTransition() =
    slideInVertically(
        initialOffsetY = { fullHeight -> fullHeight },
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION,
            easing = OvershootEasing()
        )
    ) + scaleIn(
        initialScale = 1.1f,
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION,
            easing = FastOutSlowInEasing
        )
    ) + fadeIn(
        animationSpec = tween(durationMillis = ANIMATION_FADE_DURATION)
    )

@Composable
fun getSnackBarExitTransition() =
    slideOutVertically(
        targetOffsetY = { fullHeight -> fullHeight },
        animationSpec = tween(durationMillis = ANIMATION_DURATION, easing = FastOutSlowInEasing)
    ) + fadeOut(
        animationSpec = tween(durationMillis = ANIMATION_FADE_DURATION)
    )


@Composable
private fun FadeInFadeOutWithScale(
    current: SnackbarData?,
    modifier: Modifier = Modifier,
    content: @Composable (SnackbarData) -> Unit,
) {
    var visibleItem by remember { mutableStateOf<SnackbarData?>(null) }
    var isExiting by remember { mutableStateOf(false) }

    LaunchedEffect(current) {
        if (current != null) {
            visibleItem = current
            isExiting = false
        } else if (visibleItem != null && !isExiting) {
            isExiting = true
            delay(ANIMATION_DURATION.toLong())
            visibleItem = null
            isExiting = false
        }
    }

    AnimatedVisibility(
        visible = visibleItem != null && !isExiting,
        enter = getSnackBarEnterTransition(),
        exit = getSnackBarExitTransition(),
        modifier = modifier
    ) {
        visibleItem?.let { content(it) }
    }
}

@Composable
fun BreezeSnackBarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    snackBar: @Composable (SnackbarData) -> Unit = { Snackbar(it) },
) {
    val currentSnackBarData = hostState.currentSnackbarData
    val accessibilityManager = LocalAccessibilityManager.current
    LaunchedEffect(currentSnackBarData) {
        if (currentSnackBarData != null) {
            val duration = currentSnackBarData.visuals.duration.toMillis(
                currentSnackBarData.visuals.actionLabel != null,
                accessibilityManager,
            )
            delay(duration)
            currentSnackBarData.dismiss()
        }
    }
    FadeInFadeOutWithScale(
        current = hostState.currentSnackbarData,
        modifier = modifier,
        content = snackBar,
    )
}

// TODO: magic numbers adjustment
internal fun SnackbarDuration.toMillis(
    hasAction: Boolean,
    accessibilityManager: androidx.compose.ui.platform.AccessibilityManager?,
): Long {
    val original =
        when (this) {
            SnackbarDuration.Indefinite -> Long.MAX_VALUE
            SnackbarDuration.Long -> 10000L
            SnackbarDuration.Short -> 4000L
        }
    if (accessibilityManager == null) {
        return original
    }
    return accessibilityManager.calculateRecommendedTimeoutMillis(
        original,
        containsIcons = true,
        containsText = true,
        containsControls = hasAction,
    )
}
