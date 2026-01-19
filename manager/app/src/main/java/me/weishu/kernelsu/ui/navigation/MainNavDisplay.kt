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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import me.weishu.kernelsu.ui.util.LocalNavController
import me.weishu.kernelsu.ui.util.isRailNavbar


@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainNavDisplay(modifier: Modifier = Modifier) {
    val isRail = isRailNavbar()
    val navigator = LocalNavController.current
    val navbarSwitch = navigator.isTopLevel()
    val reverseAnim = navigator.getResult<Boolean>(REVERSE_ANIM) ?: false
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val currentWidth = LocalWindowInfo.current.containerDpSize.width
    val panelWidthScale = 0.5f
    val motionScheme = MaterialTheme.motionScheme
    val navAnimSpec = motionScheme.defaultEffectsSpec<IntOffset>()
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
            // Slide in from right when navigating forward
            if (navbarSwitch) {
                if (isRail) {
                    if (reverseAnim) slidePopDown(animationSpec = navAnimSpec) else slidePushUp(animationSpec = navAnimSpec)
                } else {
                    if (reverseAnim) slideInFromLeft(animationSpec = navAnimSpec) else slideInFromRight(animationSpec = navAnimSpec)
                }
            } else {
                slideInFromRight(animationSpec = navAnimSpec)
            }
        },
        popTransitionSpec = {
            // Slide in from left when navigating back
            if (navbarSwitch) {
                if (isRail) {
                    if (!reverseAnim) slidePopDown(animationSpec = navAnimSpec) else slidePushUp(animationSpec = navAnimSpec)
                } else {
                    if (!reverseAnim) slideInFromLeft(animationSpec = navAnimSpec) else slideInFromRight(animationSpec = navAnimSpec)
                }
            } else {
                slideInFromLeft(animationSpec = navAnimSpec)
            }
        },
        predictivePopTransitionSpec = {
            // Slide in from left when navigating back
            if (navbarSwitch) {
                if (isRail) {
                    if (!reverseAnim) slidePopDown(animationSpec = navAnimSpec) else slidePushUp(animationSpec = navAnimSpec)
                } else {
                    if (!reverseAnim) slideInFromLeft(animationSpec = navAnimSpec) else slideInFromRight(animationSpec = navAnimSpec)
                }
            } else {
                slideInFromLeft(animationSpec = navAnimSpec)
            }
        })
}
