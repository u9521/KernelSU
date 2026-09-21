package me.weishu.kernelsu.breezeui.animation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.geometry.Offset
import androidx.navigation3.scene.Scene
import androidx.navigationevent.NavigationEvent

internal const val NAV_TRANSITION_DURATION = 500

/**
 * Curves taken from Flutter's Cupertino page transition, which is the closest thing to a
 * measurement of iOS: `fastEaseInToSlowEaseOut` was fit to iOS 16.3 transition frames captured on
 * an iPhone 14 Pro Max ("transition animation positions were measured every frame and plotted
 * against time, then a cubic curve was strictly fit to the measured data points"), and
 * `linearToEaseOut` is the curve Flutter drives the covered page with.
 */
internal val IosPageEasing: Easing = ThreePointCubicEasing(
    a1 = Offset(0.056f, 0.024f),
    b1 = Offset(0.108f, 0.3085f),
    mid = Offset(0.198f, 0.541f),
    a2 = Offset(0.3655f, 1f),
    b2 = Offset(0.5465f, 0.989f),
)

/** Flutter `Curves.linearToEaseOut`; also drives the edge shadow there. */
internal val IosCoveredPageEasing: Easing = CubicBezierEasing(0.35f, 0.91f, 0.33f, 0.97f)

/** Flutter `Curves.ease`, the curve the transition barrier (our scrim) fades in on. */
internal val IosScrimEasing: Easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)

/**
 * Port of Flutter's `ThreePointCubic`: two cubic beziers sharing [mid], which is how Flutter stores
 * `Curves.fastEaseInToSlowEaseOut`. Compose only ships single-cubic easings, and one cubic cannot
 * carry the shoulder of the fitted curve.
 */
private class ThreePointCubicEasing(
    a1: Offset,
    b1: Offset,
    mid: Offset,
    a2: Offset,
    b2: Offset,
) : Easing {
    private val midX = mid.x
    private val midY = mid.y
    private val first = CubicBezierEasing(a1.x / midX, a1.y / midY, b1.x / midX, b1.y / midY)
    private val tailX = 1f - midX
    private val tailY = 1f - midY
    private val second = CubicBezierEasing(
        (a2.x - midX) / tailX,
        (a2.y - midY) / tailY,
        (b2.x - midX) / tailX,
        (b2.y - midY) / tailY,
    )

    override fun transform(fraction: Float): Float = if (fraction < midX) {
        first.transform(fraction / midX) * midY
    } else {
        second.transform((fraction - midX) / tailX) * tailY + midY
    }
}

// These specs only slide. Breeze mimics iOS: the page underneath the top one is dimmed by a
// scrim (and the top page casts a shadow on it) instead of fading the page itself, so that the
// dimming does not depend on whatever is painted behind the navigation host.
// See `breezeui.nav.BreezeDimScene`.

// Forward navigation (Push): the incoming page covers the old one, which parallaxes a third of its
// width toward the leading edge
fun <T : Any> navTransitionSpec(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    val enter = slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(durationMillis = NAV_TRANSITION_DURATION, easing = IosPageEasing),
    )
    val exit = slideOutHorizontally(
        targetOffsetX = { -it / 3 },
        animationSpec = tween(durationMillis = NAV_TRANSITION_DURATION, easing = IosCoveredPageEasing)
    )

    (enter togetherWith exit)
}

// Back navigation (Pop): the top page leaves, revealing the page underneath
fun <T : Any> navPopTransitionSpec(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    val enter = slideInHorizontally(
        initialOffsetX = { -it / 3 },
        animationSpec = tween(durationMillis = NAV_TRANSITION_DURATION, easing = IosCoveredPageEasing),
    )
    val exit = slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(durationMillis = NAV_TRANSITION_DURATION, easing = IosPageEasing)
    )

    (enter togetherWith exit).apply {
        // Ensure the revealed page is at the lowest z-index during pop, otherwise it covers the
        // exiting new page
        targetContentZIndex = -1f
    }
}

// Predictive back gesture: same as pop
fun <T : Any> navPredictivePopTransitionSpec(): AnimatedContentTransitionScope<Scene<T>>.(@NavigationEvent.SwipeEdge Int) -> ContentTransform = {
    val enter = slideInHorizontally(
        initialOffsetX = { -it / 3 },
        animationSpec = tween(durationMillis = NAV_TRANSITION_DURATION + 50, easing = IosCoveredPageEasing),
    )
    val exit = slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(durationMillis = NAV_TRANSITION_DURATION + 50, easing = IosPageEasing),
    )

    (enter togetherWith exit).apply {
        targetContentZIndex = -1f
    }
}
