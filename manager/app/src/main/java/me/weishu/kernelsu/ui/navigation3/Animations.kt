package me.weishu.kernelsu.ui.navigation3

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset


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
