package me.weishu.kernelsu.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith

fun slideInFromRight(duration: Int = 250): ContentTransform {
    return slideInHorizontally(
        initialOffsetX = { it }, animationSpec = tween(duration)
    ) togetherWith slideOutHorizontally(
        targetOffsetX = { -it }, animationSpec = tween(duration)
    )
}

fun slideInFromLeft(duration: Int = 250): ContentTransform {
    return slideInHorizontally(
        initialOffsetX = { -it }, animationSpec = tween(duration)
    ) togetherWith slideOutHorizontally(
        targetOffsetX = { it }, animationSpec = tween(duration)
    )
}

fun slidePushUp(
    duration: Int = 300
): ContentTransform {
    return slideInVertically(
        initialOffsetY = { it }, animationSpec = tween(duration)
    ) togetherWith slideOutVertically(
        targetOffsetY = { -it }, animationSpec = tween(duration)
    )
}

fun slidePopDown(
    duration: Int = 300
): ContentTransform {
    return slideInVertically(
        initialOffsetY = { -it }, animationSpec = tween(duration)
    ) togetherWith slideOutVertically(
        targetOffsetY = { it }, animationSpec = tween(duration)
    )
}