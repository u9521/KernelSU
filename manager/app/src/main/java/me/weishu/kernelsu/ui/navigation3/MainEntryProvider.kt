package me.weishu.kernelsu.ui.navigation3

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import me.weishu.kernelsu.ui.screen.about.AboutScreen
import me.weishu.kernelsu.ui.screen.appprofile.AppProfileScreen
import me.weishu.kernelsu.ui.screen.executemoduleaction.ExecuteModuleActionScreen
import me.weishu.kernelsu.ui.screen.flash.FlashScreen
import me.weishu.kernelsu.ui.screen.home.HomeScreen
import me.weishu.kernelsu.ui.screen.install.InstallScreen
import me.weishu.kernelsu.ui.screen.module.ModuleScreen
import me.weishu.kernelsu.ui.screen.settings.SettingScreen
import me.weishu.kernelsu.ui.screen.superuser.SuperUserScreen
import me.weishu.kernelsu.ui.screen.template.AppProfileTemplateScreen
import me.weishu.kernelsu.ui.screen.templateeditor.TemplateEditorScreen


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
        entry<Route.About> {
            AboutScreen()
        }
        entry<Route.AppProfile>(metadata = BreezeListDetailScene.detailPane("navbar")) {
            AppProfileScreen(uid = it.uid, packageName = it.packageName)
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