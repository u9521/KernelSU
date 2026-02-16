package me.weishu.kernelsu.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import kotlinx.coroutines.flow.filter


fun slideHorizontal(isLtr: Boolean = true, animationSpec: FiniteAnimationSpec<IntOffset> = tween(250)): ContentTransform {
    val direction = if (isLtr) -1 else 1
    return slideInHorizontally(
        initialOffsetX = { it * direction }, animationSpec = animationSpec
    ) togetherWith slideOutHorizontally(
        targetOffsetX = { -it * direction }, animationSpec = animationSpec
    )
}


fun slideVertical(isUpToDown: Boolean = true, animationSpec: FiniteAnimationSpec<IntOffset> = tween(300)): ContentTransform {
    val direction = if (isUpToDown) -1 else 1
    return slideInVertically(
        initialOffsetY = { it * direction }, animationSpec = animationSpec
    ) togetherWith slideOutVertically(
        targetOffsetY = { -it * direction }, animationSpec = animationSpec
    )
}

/**
 * A specialized AnimatedVisibility composable that applies a 3D "spliced" flip effect
 * entering from the top.
 *
 * It manages Z-indexing automatically to ensure items stack correctly during the
 * 3D animation (items earlier in the list appear "above" later items).
 *
 * @param visible Whether the content should be visible.
 * @param loaded Indicates if the data has already been loaded initially. Used to determine
 * if the initial state should be visible immediately (skipping entrance animation)
 * or if it should animate in.
 * @param index The index of this item in the list (used for Z-indexing).
 * @param totalCount The total number of items in the list (used for Z-indexing).
 * @param onAfterExit Optional callback invoked when the exit animation is fully finished.
 * @param content The composable content to display.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SplicedAnimatedVisibility(
    modifier: Modifier = Modifier,
    visible: Boolean,
    loaded: Boolean,
    index: Int = 0,
    totalCount: Int = 0,
    onAfterExit: (() -> Unit)? = null,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    // Calculate Z-index to ensure correct draw order.
    // We want lower indices (top of the list) to render *above* higher indices
    // so that the "hinge" effect doesn't clip through the item below it.
    val zIndex = if (totalCount > 0) totalCount - index.toFloat() else 0f

    // If the data isn't 'loaded' yet, but we are 'visible', we assume this is
    // the initial composition and show it immediately (true).
    // If it is 'loaded', we start at false to allow the transition to animate to 'visible'.
    val visibleState = remember {
        MutableTransitionState(
            initialState = if (!loaded) visible else false
        )
    }.apply {
        targetState = visible
    }

    // Monitor the transition state to trigger the cleanup callback.
    if (onAfterExit != null) {
        LaunchedEffect(visibleState) {
            snapshotFlow { visibleState.isIdle && !visibleState.currentState }
                .filter { it } // Only emit when transition is idle AND invisible
                .collect {
                    onAfterExit()
                }
        }
    }

    val transition = rememberTransition(visibleState, label = "SplicedFlipTransition")

    // Animates the X-axis rotation.
    val rotationX by transition.animateFloat(
        transitionSpec = { MaterialTheme.motionScheme.defaultSpatialSpec() },
        label = "SplicedFlipRotation"
    ) { state ->
        if (state) 0f else -45f
    }

    val density = LocalDensity.current

    AnimatedVisibility(
        visibleState = visibleState,
        modifier = modifier
            .zIndex(zIndex)
            .graphicsLayer {
                this.rotationX = rotationX
                this.transformOrigin = TransformOrigin(0.5f, 0f)
                this.cameraDistance = 12f * density.density
            },
        enter = expandVertically(
            animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
            expandFrom = Alignment.Top
        ) + fadeIn(
            animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()
        ),
        exit = shrinkVertically(
            animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
            shrinkTowards = Alignment.Top
        ) + fadeOut(
            animationSpec = MaterialTheme.motionScheme.slowEffectsSpec()
        ),
        content = content
    )
}