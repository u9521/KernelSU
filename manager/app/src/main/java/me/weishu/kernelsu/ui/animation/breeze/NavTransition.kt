package me.weishu.kernelsu.ui.animation.breeze

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.navigation3.scene.Scene
import androidx.navigationevent.NavigationEvent

private const val DEFAULT_TRANSITION_DURATION = 500

// Dimming level: 0.0f is pure black, 1.0f is no dimming
private const val DIM_ALPHA = 0.5f

// Forward navigation (Push): old page dims as it exits
fun <T : Any> navTransitionSpec(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    val enter = slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(durationMillis = DEFAULT_TRANSITION_DURATION),
    )
    // With fadeOut combined, page dims while sliding left
    val exit = slideOutHorizontally(
        targetOffsetX = { -it / 4 },
        animationSpec = tween(durationMillis = DEFAULT_TRANSITION_DURATION)
    ) + fadeOut(
        targetAlpha = DIM_ALPHA,
        animationSpec = tween(durationMillis = DEFAULT_TRANSITION_DURATION)
    )

    (enter togetherWith exit)
}

// Back navigation (Pop): old page brightens from dim as it enters
fun <T : Any> navPopTransitionSpec(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    // With fadeIn combined, page brightens from dim while sliding right
    val enter = slideInHorizontally(
        initialOffsetX = { -it / 4 },
        animationSpec = tween(durationMillis = DEFAULT_TRANSITION_DURATION),
    ) + fadeIn(
        initialAlpha = DIM_ALPHA,
        animationSpec = tween(durationMillis = DEFAULT_TRANSITION_DURATION)
    )
    val exit = slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(durationMillis = DEFAULT_TRANSITION_DURATION)
    )

    (enter togetherWith exit).apply {
        // Ensure the restored old page is at the lowest z-index during pop, otherwise it covers the exiting new page
        targetContentZIndex = -1f
    }
}

// Predictive back gesture: same as pop
fun <T : Any> navPredictivePopTransitionSpec(): AnimatedContentTransitionScope<Scene<T>>.(@NavigationEvent.SwipeEdge Int) -> ContentTransform = {
    val enter = slideInHorizontally(
        initialOffsetX = { -it / 4 },
        animationSpec = tween(durationMillis = DEFAULT_TRANSITION_DURATION + 50),
    ) + fadeIn(
        initialAlpha = DIM_ALPHA,
        animationSpec = tween(durationMillis = DEFAULT_TRANSITION_DURATION + 50)
    )
    val exit = slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(durationMillis = DEFAULT_TRANSITION_DURATION + 50),
    )

    (enter togetherWith exit).apply {
        targetContentZIndex = -1f
    }
}