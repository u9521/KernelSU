package me.weishu.kernelsu.ui.navigation3.breeze

import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import me.weishu.kernelsu.ui.animation.breeze.navPopTransitionSpec
import me.weishu.kernelsu.ui.animation.breeze.navPredictivePopTransitionSpec
import me.weishu.kernelsu.ui.animation.breeze.navTransitionSpec
import me.weishu.kernelsu.ui.navigation3.LocalNavigator
import me.weishu.kernelsu.ui.util.blurOverlay


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NavDisplayBreeze(modifier: Modifier = Modifier, entryProvider: (NavKey) -> NavEntry<NavKey>) {
    val navigator = LocalNavigator.current
    val breezeListDetailSceneStrategy = rememberBreezeListDetailSceneStrategy<NavKey>()
    NavDisplay(
        modifier = modifier
            .blurOverlay()
            // Trick for dim the old page
            .background(Color.Black),
        backStack = navigator.backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        sceneStrategies = listOf(breezeListDetailSceneStrategy, SinglePaneSceneStrategy()),
        entryProvider = entryProvider,
        onBack = { navigator.pop() },
        transitionSpec = navTransitionSpec(),
        popTransitionSpec = navPopTransitionSpec(),
        predictivePopTransitionSpec = navPredictivePopTransitionSpec(),
    )
}

