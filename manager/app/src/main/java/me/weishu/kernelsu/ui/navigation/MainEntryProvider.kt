package me.weishu.kernelsu.ui.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import me.weishu.kernelsu.ui.screen.AppProfileScreen
import me.weishu.kernelsu.ui.screen.AppProfileTemplateScreen
import me.weishu.kernelsu.ui.screen.ExecuteModuleActionScreen
import me.weishu.kernelsu.ui.screen.FlashScreen
import me.weishu.kernelsu.ui.screen.HomeScreen
import me.weishu.kernelsu.ui.screen.InstallScreen
import me.weishu.kernelsu.ui.screen.ModuleScreen
import me.weishu.kernelsu.ui.screen.SettingScreen
import me.weishu.kernelsu.ui.screen.SuperUserScreen
import me.weishu.kernelsu.ui.screen.TemplateEditorScreen


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun getMainEntryProvider(): (NavKey) -> NavEntry<NavKey> {

    val mainEntryProvider: (NavKey) -> NavEntry<NavKey> = entryProvider {
        entry<HomeNavKey> {
            HomeScreen()
        }
        entry<SuperUserNavKey>(metadata = ListDetailSceneStrategy.listPane("navbar")) {
            SuperUserScreen()
        }
        entry<ModuleNavKey> {
            ModuleScreen()
        }
        entry<SettingNavKey> {
            SettingScreen()
        }
        entry<AppProfileScreenNavKey>(metadata = ListDetailSceneStrategy.detailPane("navbar")) {
            AppProfileScreen(appInfo = it.appInfo)
        }
        entry<ExecuteModuleActionNavKey> {
            ExecuteModuleActionScreen(it.moduleId)
        }
        entry<FlashScreenNavKey> {
            FlashScreen(it.flashIt)
        }
        entry<InstallScreenNavKey> {
            InstallScreen()
        }
        entry<AppProfileTemplateNavKey>(metadata = ListDetailSceneStrategy.listPane("appProfile")) {
            AppProfileTemplateScreen()
        }
        entry<TemplateEditorNavKey>(metadata = ListDetailSceneStrategy.detailPane("appProfile")) {
            TemplateEditorScreen(it.initialTemplate, it.readOnly)
        }
    }
    return mainEntryProvider.injectMetadata()
}

private fun <T : Any> ((T) -> NavEntry<T>).injectMetadata(): (T) -> NavEntry<T> {
    return { key ->
        val originalEntry = this(key)
        val newMetadata = originalEntry.metadata + ("navKey" to key)

        NavEntry(
            key = key,
            contentKey = originalEntry.contentKey,
            metadata = newMetadata,
            content = {
                originalEntry.Content()
            }
        )
    }
}