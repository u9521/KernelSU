package me.weishu.kernelsu.ui.navigation3.breeze

import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass


fun WindowAdaptiveInfo.getNavBarType(): NavigationBarType {
    val sizeClass = this.windowSizeClass
    val isTabletop = this.windowPosture.isTabletop
    val isCompactHeight = !sizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)

    return when {
        isTabletop ->
            NavigationBarType.Bar

        sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND) ->
            NavigationBarType.Drawer

        sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) ->
            if (isCompactHeight) NavigationBarType.Rail else NavigationBarType.WideRail

        sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) ->
            NavigationBarType.Rail

        else ->
            if (isCompactHeight) NavigationBarType.ShortBar else NavigationBarType.Bar
    }
}

fun WindowAdaptiveInfo.isRailNavbar(): Boolean {
    return this.getNavBarType().isRail()
}

@Composable
fun isRailNavbar(): Boolean {
    return currentWindowAdaptiveInfoV2().isRailNavbar()
}

enum class NavigationBarType {
    ShortBar,
    Bar,
    Rail,
    WideRail,
    Drawer
}

fun NavigationBarType.isRail(): Boolean {
    return when (this) {
        NavigationBarType.Rail, NavigationBarType.WideRail, NavigationBarType.Drawer -> true
        else -> false
    }
}