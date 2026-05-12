package me.weishu.kernelsu.ui.component.breeze

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AccessibilityManager
import androidx.compose.ui.platform.LocalAccessibilityManager
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private const val ANIMATION_DURATION = 450
private const val ANIMATION_FADE_DURATION = 200


class OvershootEasing(private val tension: Float = 2.0f) : Easing {
    override fun transform(fraction: Float): Float {
        val t = fraction - 1.0f
        return t * t * ((tension + 1) * t + tension) + 1.0f
    }
}

private fun getSnackBarEnterTransition() = slideInVertically(
    initialOffsetY = { fullHeight -> fullHeight }, animationSpec = tween(
        durationMillis = ANIMATION_DURATION, easing = OvershootEasing()
    )
) + scaleIn(
    initialScale = 1.1f, animationSpec = tween(
        durationMillis = ANIMATION_DURATION, easing = FastOutSlowInEasing
    )
) + fadeIn(
    animationSpec = tween(durationMillis = ANIMATION_FADE_DURATION)
)

private fun getSnackBarExitTransition() = slideOutVertically(
    targetOffsetY = { fullHeight -> fullHeight }, animationSpec = tween(durationMillis = ANIMATION_DURATION, easing = FastOutSlowInEasing)
) + fadeOut(
    animationSpec = tween(durationMillis = ANIMATION_FADE_DURATION)
)


@Composable
private fun FadeInFadeOutWithScale(
    current: SnackbarData?,
    modifier: Modifier = Modifier,
    content: @Composable (SnackbarData) -> Unit,
) {
    AnimatedContent(
        targetState = current,
        transitionSpec = {
            (getSnackBarEnterTransition() togetherWith getSnackBarExitTransition()).using(sizeTransform = null)
        },
        modifier = modifier,
        label = "breeze_snackbar_animation"
    ) { data ->
        if (data != null) {
            content(data)
        }
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
            delay(duration.milliseconds)
            currentSnackBarData.dismiss()
        }
    }
    val state = rememberSwipeToDismissBoxState()

    LaunchedEffect(hostState.currentSnackbarData) {
        hostState.currentSnackbarData ?: return@LaunchedEffect
        state.snapTo(SwipeToDismissBoxValue.Settled)
    }
    SwipeToDismissBox(
        state = state,
        backgroundContent = {},
        onDismiss = {
            hostState.currentSnackbarData?.dismiss()
        }
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            FadeInFadeOutWithScale(
                current = hostState.currentSnackbarData,
                modifier = modifier,
                content = snackBar,
            )
        }
    }
}

// TODO: magic numbers adjustment
private fun SnackbarDuration.toMillis(
    hasAction: Boolean,
    accessibilityManager: AccessibilityManager?,
): Long {
    val original = when (this) {
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
