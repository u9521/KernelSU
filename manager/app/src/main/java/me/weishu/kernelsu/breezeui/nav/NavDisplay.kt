package me.weishu.kernelsu.breezeui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import me.weishu.kernelsu.breezeui.animation.navPopTransitionSpec
import me.weishu.kernelsu.breezeui.animation.navPredictivePopTransitionSpec
import me.weishu.kernelsu.breezeui.animation.navTransitionSpec
import me.weishu.kernelsu.breezeui.util.blurOverlay


@Composable
fun NavDisplayBreeze(modifier: Modifier = Modifier, entryProvider: (NavKey) -> NavEntry<NavKey>) {
    val navigator = LocalBreezeNavigator.current
    val breezeListDetailSceneStrategy = rememberBreezeListDetailSceneStrategy<NavKey>()

    val sceneStrategies = remember(breezeListDetailSceneStrategy) {
        listOf(
            breezeListDetailSceneStrategy.withDimmedUnderneath(),
            SinglePaneSceneStrategy<NavKey>().withDimmedUnderneath(),
        )
    }

    NavDisplay(
        modifier = modifier.blurOverlay(),
        backStack = navigator.backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        sceneStrategies = sceneStrategies,
        entryProvider = entryProvider,
        onBack = { navigator.pop() },
        transitionSpec = navTransitionSpec(),
        popTransitionSpec = navPopTransitionSpec(),
        predictivePopTransitionSpec = navPredictivePopTransitionSpec(),
    )
}
