package me.weishu.kernelsu.ui.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import me.weishu.kernelsu.ui.theme.LocalEnableBlur

private const val TopBarMaxTintAlpha = 0.7f
private val DefaultBarBlurRadius = 15.dp

@Composable
@OptIn(ExperimentalHazeApi::class)
fun Modifier.defaultHazeEffect(
    hazeState: HazeState,
    hazeStyle: HazeStyle,
): Modifier = if (LocalEnableBlur.current) this.hazeEffect(
    state = hazeState,
    style = hazeStyle
) {
    inputScale = HazeInputScale.Fixed(0.35f)
    forceInvalidateOnPreDraw = false
} else this

@Composable
fun Modifier.topBarHazeEffect(
    hazeState: HazeState,
    scrollBehavior: TopAppBarScrollBehavior?,
): Modifier {
    val collapseFraction = (scrollBehavior?.state?.collapsedFraction ?: 0f).coerceIn(0f, 1f)
    return if (collapseFraction > 0f) {
        defaultHazeEffect(hazeState, topBarHazeStyle(collapseFraction))
    } else {
        this
    }
}

@Composable
fun topBarHazeStyle(collapseFraction: Float) = HazeStyle(
    backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
    blurRadius = DefaultBarBlurRadius,
    noiseFactor = 0f,
    tint = HazeTint(
        MaterialTheme.colorScheme.surfaceBright.copy(
            alpha = collapseFraction.coerceIn(0f, 1f) * TopBarMaxTintAlpha
        )
    )
)

@Composable
fun bottomBarHazeStyle() = HazeStyle(
    backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
    blurRadius = DefaultBarBlurRadius,
    noiseFactor = 0f,
    tint = HazeTint(
        MaterialTheme.colorScheme.surfaceBright.copy(alpha = TopBarMaxTintAlpha)
    )
)

