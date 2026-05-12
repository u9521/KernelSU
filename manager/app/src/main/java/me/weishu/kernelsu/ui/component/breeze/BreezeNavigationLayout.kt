package me.weishu.kernelsu.ui.component.breeze

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

enum class NavigationLayoutType {
    HIDDEN, BOTTOM, SIDE, BOTH
}

enum class SideBarPosition {
    START,
    END,
    LEFT,
    RIGHT
}

@Stable
class NavigationLayoutState(
    initialValue: NavigationLayoutType,
    private val defaultAnimationSpec: AnimationSpec<Float>
) {
    var currentValue: NavigationLayoutType by mutableStateOf(initialValue)
        private set

    var targetValue: NavigationLayoutType by mutableStateOf(initialValue)
        private set

    val isAnimating: Boolean
        get() = bottomAnim.isRunning || sideAnim.isRunning

    internal val bottomAnim = Animatable(if (initialValue.hasBottom) 1f else 0f)
    internal val sideAnim = Animatable(if (initialValue.hasSide) 1f else 0f)

    private val NavigationLayoutType.hasBottom
        get() = this == NavigationLayoutType.BOTTOM || this == NavigationLayoutType.BOTH
    private val NavigationLayoutType.hasSide
        get() = this == NavigationLayoutType.SIDE || this == NavigationLayoutType.BOTH

    suspend fun animateTo(
        target: NavigationLayoutType,
        animationSpec: AnimationSpec<Float> = defaultAnimationSpec
    ) {
        targetValue = target
        coroutineScope {
            launch { bottomAnim.animateTo(if (target.hasBottom) 1f else 0f, animationSpec) }
            launch { sideAnim.animateTo(if (target.hasSide) 1f else 0f, animationSpec) }
        }
        currentValue = target
    }

    suspend fun hideNavController(animationSpec: AnimationSpec<Float> = defaultAnimationSpec) {
        animateTo(NavigationLayoutType.HIDDEN, animationSpec)
    }

    suspend fun showBar(animationSpec: AnimationSpec<Float> = defaultAnimationSpec) {
        animateTo(NavigationLayoutType.BOTTOM, animationSpec)
    }

    suspend fun showRail(animationSpec: AnimationSpec<Float> = defaultAnimationSpec) {
        animateTo(NavigationLayoutType.SIDE, animationSpec)
    }

    suspend fun showBoth(animationSpec: AnimationSpec<Float> = defaultAnimationSpec) {
        animateTo(NavigationLayoutType.BOTH, animationSpec)
    }

    suspend fun snapTo(target: NavigationLayoutType) {
        targetValue = target
        coroutineScope {
            launch { bottomAnim.snapTo(if (target.hasBottom) 1f else 0f) }
            launch { sideAnim.snapTo(if (target.hasSide) 1f else 0f) }
        }
        currentValue = target
    }
}

@Composable
fun rememberBreezeNavLayoutState(
    initialValue: NavigationLayoutType = NavigationLayoutType.BOTTOM,
    animationSpec: AnimationSpec<Float> = spring(dampingRatio = 0.9f, stiffness = 700.0f)
): NavigationLayoutState {
    return rememberSaveable(
        inputs = arrayOf(animationSpec),
        saver = Saver(
            save = { state -> state.targetValue.name },
            restore = { savedString ->
                val restoredValue = NavigationLayoutType.valueOf(savedString)
                NavigationLayoutState(
                    initialValue = restoredValue,
                    defaultAnimationSpec = animationSpec
                )
            }
        )
    ) {
        NavigationLayoutState(initialValue, animationSpec)
    }
}

@Composable
fun BreezeNavigationLayout(
    state: NavigationLayoutState,
    sideBarPosition: SideBarPosition = SideBarPosition.START,
    sideBar: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit,
    content: @Composable (contentPadding: PaddingValues) -> Unit
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val effectiveIsStart = when (sideBarPosition) {
        SideBarPosition.START -> true
        SideBarPosition.END -> false
        SideBarPosition.LEFT -> !isRtl
        SideBarPosition.RIGHT -> isRtl
    }

    SubcomposeLayout { constraints ->
        val showBottom = state.bottomAnim.value > 0f || state.bottomAnim.targetValue > 0f
        val showSide = state.sideAnim.value > 0f || state.sideAnim.targetValue > 0f

        val bottomPlaceables = if (showBottom) {
            subcompose("bottomBar", bottomBar).map {
                it.measure(constraints.copy(minHeight = 0))
            }
        } else {
            emptyList()
        }
        val bottomHeight = bottomPlaceables.maxOfOrNull { it.height } ?: 0

        val sidePlaceables = if (showSide) {
            subcompose("sideBar", sideBar).map {
                it.measure(constraints.copy(minWidth = 0, minHeight = 0))
            }
        } else {
            emptyList()
        }
        val sideWidth = sidePlaceables.maxOfOrNull { it.width } ?: 0

        val currentBottomPadding = (bottomHeight * state.bottomAnim.value).toDp()
        val currentSidePadding = (sideWidth * state.sideAnim.value).toDp()

        val contentPadding = if (effectiveIsStart) {
            PaddingValues(start = currentSidePadding, bottom = currentBottomPadding)
        } else {
            PaddingValues(end = currentSidePadding, bottom = currentBottomPadding)
        }

        val contentPlaceables = subcompose("content") {
            Box(Modifier.consumeWindowInsets(contentPadding)) {
                content(contentPadding)
            }
        }.map { it.measure(constraints) }

        layout(constraints.maxWidth, constraints.maxHeight) {
            contentPlaceables.forEach { it.placeRelative(0, 0) }

            if (showBottom) {
                val bottomY = constraints.maxHeight - (bottomHeight * state.bottomAnim.value).toInt()
                bottomPlaceables.forEach { it.placeRelative(0, bottomY) }
            }

            if (showSide) {
                val sideX = if (effectiveIsStart) {
                    (-sideWidth * (1f - state.sideAnim.value)).toInt()
                } else {
                    constraints.maxWidth - (sideWidth * state.sideAnim.value).toInt()
                }
                sidePlaceables.forEach { it.placeRelative(sideX, 0) }
            }
        }
    }
}
