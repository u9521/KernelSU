package me.weishu.kernelsu.ui.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.ui.navigation3.LocalNavigator
import me.weishu.kernelsu.ui.navigation3.breeze.NavigationBarType
import me.weishu.kernelsu.ui.navigation3.breeze.getNavBarType
import me.weishu.kernelsu.ui.navigation3.breeze.isRail
import me.weishu.kernelsu.ui.navigation3.breeze.isTopRoute

@Composable
fun PaddingValues.onlyHorizontal(): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(top = 0.dp, bottom = 0.dp, start = this.calculateStartPadding(layoutDirection), end = this.calculateEndPadding(layoutDirection))
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun fABBottomPadding(): Dp {
    val navigator = LocalNavigator.current
    val isTopRoute = navigator.isTopRoute()
    val barType = currentWindowAdaptiveInfo().getNavBarType()
    val isRail = barType.isRail()
    if (isTopRoute && !isRail) return 0.dp
    val barInsertHeight = NavigationBarDefaults.windowInsets.asPaddingValues().calculateBottomPadding()
    val barHeight = 80.dp //NavigationBarTokens.TallContainerHeight
    val compactBarHeight = BottomAppBarDefaults.FlexibleBottomAppBarHeight
    return barInsertHeight + if (barType == NavigationBarType.Bar) barHeight else compactBarHeight
}