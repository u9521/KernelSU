package me.weishu.kernelsu.ui.navigation3

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
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
fun getMainEntryProvider(): (NavKey) -> NavEntry<NavKey> {
    val mainEntryProvider: (NavKey) -> NavEntry<NavKey> = entryProvider {
        topRouteEntry<Route.Home>(homepage = true) {
            HomeScreen()
        }
        topRouteEntry<Route.SuperUser>(metadata = BreezeListDetailScene.listPane("navbar")) {
            SuperUserScreen()
        }
        topRouteEntry<Route.Module> {
            ModuleScreen()
        }
        topRouteEntry<Route.Settings> {
            SettingScreen()
        }
        entry<Route.AppProfile>(metadata = BreezeListDetailScene.detailPane("navbar")) {
            AppProfileScreen(it.packageName)
        }
        entry<Route.ExecuteModuleAction> {
            ExecuteModuleActionScreen(it.moduleId)
        }
        entry<Route.Flash> {
            FlashScreen(it.flashIt)
        }
        entry<Route.Install> {
            InstallScreen()
        }
        entry<Route.AppProfileTemplate>(metadata = BreezeListDetailScene.listPane("appProfile")) {
            AppProfileTemplateScreen()
        }
        entry<Route.TemplateEditor>(metadata = BreezeListDetailScene.detailPane("appProfile")) {
            TemplateEditorScreen(it.initialTemplate, it.readOnly)
        }
    }
    return mainEntryProvider
}

inline fun <reified K : NavKey> EntryProviderScope<NavKey>.topRouteEntry(
    metadata: Map<String, Any> = emptyMap(),
    homepage: Boolean = false,
    noinline content: @Composable (K) -> Unit
) {
    val ordinal: ((NavKey) -> Int) = { NavController.getTopLevel(it)?.ordinal ?: 0 }
    this.addEntryProvider(
        clazz = K::class,
        metadata = { metadata + (TopLevelRouteOrdinal to ordinal(it)) + (TopLevelRouteHome to homepage) },
        content = content
    )
}