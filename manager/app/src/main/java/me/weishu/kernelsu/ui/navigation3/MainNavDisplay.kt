package me.weishu.kernelsu.ui.navigation3

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import me.weishu.kernelsu.ui.util.LocalNavController
import me.weishu.kernelsu.ui.util.isRailNavbar


@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainNavDisplay(modifier: Modifier = Modifier) {
    val isRail = isRailNavbar()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val navigator = LocalNavController.current
    val motionScheme = MaterialTheme.motionScheme
    val navAnimSpec = motionScheme.defaultEffectsSpec<IntOffset>()
    val breezeListDetailSceneStrategy = rememberBreezeListDetailSceneStrategy<NavKey>(isRail, isRtl, navAnimSpec)
    val breezeStrategy = rememberBreezeSinglePaneSceneStrategy<NavKey>(isRail, isRtl, navAnimSpec)
    NavDisplay(
        modifier = modifier,
        sceneStrategy = breezeListDetailSceneStrategy.then(breezeStrategy),
        entries = navigator.genEntries(getMainEntryProvider()),
        onBack = { navigator.popBackStack() },
        transitionSpec = {
            slideHorizontal(isRtl, animationSpec = navAnimSpec)
        },
        popTransitionSpec = {
            slideHorizontal(!isRtl, animationSpec = navAnimSpec)
        },
        predictivePopTransitionSpec = {
            slideHorizontal(!isRtl, animationSpec = navAnimSpec)
        })
}