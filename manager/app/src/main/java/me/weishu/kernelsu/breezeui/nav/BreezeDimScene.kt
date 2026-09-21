package me.weishu.kernelsu.breezeui.nav

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import me.weishu.kernelsu.breezeui.animation.IosCoveredPageEasing
import me.weishu.kernelsu.breezeui.animation.IosScrimEasing
import me.weishu.kernelsu.breezeui.animation.NAV_TRANSITION_DURATION

// Calibrated against Flutter's Cupertino page transition, the closest thing to a measurement of
// iOS. Flutter's own strengths are lighter still - a black barrier at 0x18 (0.094) and an edge
// shadow peaking at 0x04 (0.016) over 5% of the page width - but both are marked "eyeballed"
// there and read as barely visible on a device, so the strengths are nudged up while geometry,
// curves and proportions are kept.

/** How far the page underneath the top one is dimmed at full travel. Flutter: 0.094. */
private const val DIM_ALPHA = 0.12f

/** Peak alpha of the top page's edge shadow. Flutter: 0.016. */
private const val SHADOW_ALPHA = 0.05f

/** How far the top page's edge shadow bleeds onto the page underneath, as a fraction of the width. */
private const val SHADOW_WIDTH_FRACTION = 0.05f

/**
 * Wraps a [SceneStrategy] so the page underneath the top one is dimmed, mimicking iOS: pushing
 * covers a dimmed page, and popping reveals one that brightens as the top page leaves.
 *
 * The dimming used to live in the transition specs as `fadeOut`/`fadeIn`, which only reads as
 * dimming because something dark happens to be painted behind the navigation host. Keeping it
 * here makes it self-contained: the pages stay fully opaque, the dim follows the pages instead of
 * an animation of its own, and its color and strength are plain values rather than a hidden
 * dependency on the backdrop.
 *
 * `AnimatedContent` never clips its children (`navDisplay` passes a null `sizeTransform`, so not
 * even the container gets a `clipToBounds`), which is what lets the top page paint the page
 * underneath: the dim is a scrim drawn just before its own content, and the shadow is drawn just
 * to the left of its leading edge. Both are positioned in the top page's own coordinates, so they
 * ride along with it through the slide, and through a predictive back gesture, for free.
 *
 * Strengths, proportions and curves are calibrated against Flutter's Cupertino page transition -
 * see `breezeui.animation.NavTransition`.
 *
 * Everything here assumes Breeze's left-to-right slide specs: content only moves horizontally, and
 * the page underneath is always at a negative x offset.
 */
internal fun <T : Any> SceneStrategy<T>.withDimmedUnderneath(): SceneStrategy<T> =
    DimSceneStrategy(this)

private class DimSceneStrategy<T : Any>(
    private val delegate: SceneStrategy<T>
) : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? =
        with(delegate) { calculateScene(entries) }?.let { DimScene(it) }
}

private class DimScene<T : Any>(
    private val delegate: Scene<T>
) : Scene<T> by delegate {

    override val content: @Composable () -> Unit = {
        val transition = LocalNavAnimatedContentScope.current.transition

        // Both the dim and the shadow are painted to the left of this page's own origin, and
        // navigation3 only ever puts the page *underneath* at a negative x offset (the slide specs
        // move it by at most -W/3). So whatever a page underneath paints lands outside the window,
        // and painting from every page that is or has been on screen is enough: the only page that
        // can end up at a positive offset - and therefore paint somewhere visible - is the top one.
        // `PreEnter` is the one state that means "not on screen yet".
        val paints = transition.targetState != EnterExitState.PreEnter

        // How far the top page has travelled: 0f while it is still off screen, 1f once it has
        // settled. On a push it runs 0f -> 1f, on a pop 1f -> 0f. Both read the same visibility
        // state but carry their own curve, because that is how Flutter drives them: the scrim on
        // `Curves.ease`, the shadow on `Curves.linearToEaseOut`.
        //
        // Frozen at the boundary values while a predictive back gesture is dragging, where
        // navigation3 holds the enter/exit states and moves the content with a mutable transform
        // instead: the geometry still follows the finger, the strength only moves on release.
        val dimFraction by transition.animateFloat(
            transitionSpec = { tween(NAV_TRANSITION_DURATION, easing = IosScrimEasing) },
        ) { state -> if (state == EnterExitState.Visible) 1f else 0f }
        val shadowFraction by transition.animateFloat(
            transitionSpec = { tween(NAV_TRANSITION_DURATION, easing = IosCoveredPageEasing) },
        ) { state -> if (state == EnterExitState.Visible) 1f else 0f }

        val scrimColor = MaterialTheme.colorScheme.scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    if (paints) {
                        // Everything left of the top page is the page underneath: dim it, then let
                        // it brighten as the top page leaves.
                        val dim = dimFraction * DIM_ALPHA
                        if (dim > 0f) {
                            drawRect(
                                color = scrimColor,
                                alpha = dim,
                                topLeft = Offset(-size.width, 0f),
                                size = Size(size.width, size.height),
                            )
                        }
                        // Depth cue: the top page casts a shadow onto the page underneath. Its
                        // position comes from this page's own leading edge, so it stays correct at
                        // any offset - including mid gesture, where the states above are frozen -
                        // while its strength follows the incoming page. At rest it lands outside
                        // the window.
                        val shadow = size.width * SHADOW_WIDTH_FRACTION
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    scrimColor.copy(alpha = shadowFraction * SHADOW_ALPHA),
                                ),
                                startX = -shadow,
                                endX = 0f,
                            ),
                            topLeft = Offset(-shadow, 0f),
                            size = Size(shadow, size.height),
                        )
                    }
                    drawContent()
                }
        ) {
            delegate.content()
        }
    }

    // Scenes are looked up by equality while a transition is in flight; creating a new wrapper
    // every recomposition must not read as a different scene.
    override fun equals(other: Any?): Boolean =
        other is DimScene<*> && delegate == other.delegate

    override fun hashCode(): Int = delegate.hashCode()
}
