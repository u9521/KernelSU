package me.weishu.kernelsu.ui.navigation

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import me.weishu.kernelsu.ui.util.LocalNavController
import me.weishu.kernelsu.ui.util.isRailNavbar


@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainNavDisplay(modifier: Modifier = Modifier) {
    val isRail = isRailNavbar()
    val navigator = LocalNavController.current
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val currentWidth = LocalWindowInfo.current.containerDpSize.width
    val panelWidthScale = 0.5f
    val motionScheme = MaterialTheme.motionScheme
    val navAnimSpec = motionScheme.defaultEffectsSpec<IntOffset>()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val directive = remember(windowAdaptiveInfo) {
        calculatePaneScaffoldDirective(windowAdaptiveInfo).copy(
            horizontalPartitionSpacerSize = 0.dp,
            defaultPanePreferredWidth = currentWidth.times(panelWidthScale)
        )
    }
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive, backNavigationBehavior = BackNavigationBehavior.PopLatest)
    NavDisplay(
        modifier = modifier,
        sceneStrategy = listDetailStrategy,
        entries = navigator.genEntries(getMainEntryProvider()),
        onBack = { navigator.popBackStack() },
        transitionSpec = {
            val (navbarSwitch, reverseAnim) = calculateNavTransition(
                initialState.entries,
                targetState.entries,
                navigator
            )
            if (navbarSwitch) {
                if (isRail) {
                    slideVertical(reverseAnim,animationSpec = navAnimSpec)
                } else {
                    slideHorizontal(reverseAnim != isRtl,animationSpec = navAnimSpec)
                }
            } else {
                slideHorizontal(isRtl,animationSpec = navAnimSpec)
            }
        },
        popTransitionSpec = {
            val (navbarSwitch, reverseAnim) = calculateNavTransition(
                initialState.entries,
                targetState.entries,
                navigator
            )
            if (navbarSwitch) {
                if (isRail) {
                    slideVertical(!reverseAnim,animationSpec = navAnimSpec)
                } else {
                    slideHorizontal(reverseAnim == isRtl,animationSpec = navAnimSpec)
                }
            } else {
                slideHorizontal(!isRtl,animationSpec = navAnimSpec)
            }
        },
        predictivePopTransitionSpec = {
            val (navbarSwitch, reverseAnim) = calculateNavTransition(
                initialState.entries,
                targetState.entries,
                navigator
            )
            if (navbarSwitch) {
                if (isRail) {
                    slideVertical(!reverseAnim,animationSpec = navAnimSpec)
                } else {
                    slideHorizontal(reverseAnim == isRtl,animationSpec = navAnimSpec)
                }
            } else {
                slideHorizontal(!isRtl,animationSpec = navAnimSpec)
            }
        })
}

data class NavTransitionInfo(
    val isTabSwitch: Boolean,
    val isReverse: Boolean
)

fun calculateNavTransition(
    initialEntries: List<NavEntry<NavKey>>,
    targetEntries: List<NavEntry<NavKey>>,
    navController: NavController
): NavTransitionInfo {
    val initialNavKey = initialEntries.lastOrNull()?.metadata["navKey"] as? NavKey
    val targetNavKey = targetEntries.lastOrNull()?.metadata["navKey"] as? NavKey

    if (initialNavKey == null || targetNavKey == null) {
        return NavTransitionInfo(isTabSwitch = false, isReverse = false)
    }
    val initialTab = navController.getTopLevel(initialNavKey)
    val targetTab = navController.getTopLevel(targetNavKey)
    if (initialTab == null){
        return NavTransitionInfo(isTabSwitch = false, isReverse = false)
    }
    if (targetTab == null) {
        return NavTransitionInfo(isTabSwitch = false, isReverse = false)
    }
    // we don't need to reverse Home
    val isReverse = targetTab.ordinal < initialTab.ordinal && targetNavKey != navController.startKey
    return NavTransitionInfo(isTabSwitch = true, isReverse = isReverse)
}