package me.weishu.kernelsu.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset


fun slideInFromRight(duration: Int = 250, animationSpec: FiniteAnimationSpec<IntOffset> = tween(duration)): ContentTransform {
    return slideInHorizontally(
        initialOffsetX = { it }, animationSpec = animationSpec
    ) togetherWith slideOutHorizontally(
        targetOffsetX = { -it }, animationSpec = animationSpec
    )
}

fun slideInFromLeft(duration: Int = 250, animationSpec: FiniteAnimationSpec<IntOffset> = tween(duration)): ContentTransform {
    return slideInHorizontally(
        initialOffsetX = { -it }, animationSpec = animationSpec
    ) togetherWith slideOutHorizontally(
        targetOffsetX = { it }, animationSpec = animationSpec
    )
}

fun slidePushUp(duration: Int = 300, animationSpec: FiniteAnimationSpec<IntOffset> = tween(duration)): ContentTransform {
    return slideInVertically(
        initialOffsetY = { it }, animationSpec = animationSpec
    ) togetherWith slideOutVertically(
        targetOffsetY = { -it }, animationSpec = animationSpec
    )
}

fun slidePopDown(duration: Int = 300, animationSpec: FiniteAnimationSpec<IntOffset> = tween(duration)): ContentTransform {
    return slideInVertically(
        initialOffsetY = { -it }, animationSpec = animationSpec
    ) togetherWith slideOutVertically(
        targetOffsetY = { it }, animationSpec = animationSpec
    )
}