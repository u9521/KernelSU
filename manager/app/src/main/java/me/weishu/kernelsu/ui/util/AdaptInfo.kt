package me.weishu.kernelsu.ui.util

import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

fun navigationSuiteType(adaptiveInfo: WindowAdaptiveInfo): NavigationSuiteType {
    return with(adaptiveInfo) {
        if (windowSizeClass.minWidthDp.dp == 0.dp) {
            NavigationSuiteType.ShortNavigationBarCompact
        } else if (windowPosture.isTabletop || windowSizeClass.minHeightDp.dp == 0.dp) {
            NavigationSuiteType.ShortNavigationBarMedium
        } else if (windowSizeClass.minWidthDp.dp == 800.dp) {
            NavigationSuiteType.WideNavigationRailExpanded
        } else {
            NavigationSuiteType.WideNavigationRailCollapsed
        }
    }
}

@Composable
fun isRailNavbar(): Boolean {
    return when (navigationSuiteType(currentWindowAdaptiveInfo())) {
        NavigationSuiteType.WideNavigationRailExpanded -> true
        NavigationSuiteType.WideNavigationRailCollapsed -> true
        else -> false
    }
}