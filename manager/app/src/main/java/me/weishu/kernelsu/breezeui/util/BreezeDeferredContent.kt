package me.weishu.kernelsu.breezeui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.navigation3.ui.LocalNavAnimatedContentScope

/**
 * Breeze's own twin of upstream `ui.util.rememberContentReady`.
 *
 * Since miuix 0.9.4 the upstream helper reads miuix-nav's `LocalNavTransitionScope`, which is only
 * provided by miuix's `NavDisplay` and throws when read anywhere else. Breeze's navigation host is
 * androidx.navigation3 (`breezeui.nav.NavDisplayBreeze`), which provides
 * [LocalNavAnimatedContentScope] instead, so Breeze must keep its own copy.
 *
 * Returns true only after the navigation transition animation has completed and an additional
 * buffer frame has passed.
 *
 * Timeline:
 * - During animation: returns false → page shows lightweight placeholder (smooth animation)
 * - Animation ends + 1 frame: returns true → heavy content composes
 *   (stutter is invisible because the page is already static)
 *
 * The value is sticky — once true it never reverts to false,
 * so content stays visible during exit transitions.
 */
@Composable
fun rememberBreezeContentReady(): Boolean {
    val transitionRunning = LocalNavAnimatedContentScope.current.transition.isRunning
    val ready = remember { mutableStateOf(false) }

    LaunchedEffect(transitionRunning) {
        if (!transitionRunning && !ready.value) {
            withFrameNanos { }
            ready.value = true
        }
    }

    return ready.value
}
