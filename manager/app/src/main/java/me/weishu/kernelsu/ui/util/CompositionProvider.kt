package me.weishu.kernelsu.ui.util

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import me.weishu.kernelsu.ui.navigation.NavController

val LocalSnackbarHost = compositionLocalOf<SnackbarHostState> {
    error("CompositionLocal LocalSnackbarController not present")
}

val LocalNavController = staticCompositionLocalOf<NavController> {
    error("LocalNavigator AppNavigator not provided")
}