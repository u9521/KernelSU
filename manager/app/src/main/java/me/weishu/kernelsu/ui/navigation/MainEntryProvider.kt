package me.weishu.kernelsu.ui.navigation

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

internal val mainEntryProvider: (NavKey) -> NavEntry<NavKey> = entryProvider {
    entry<TopLevelRoute> {
        when (it) {
            TopLevelRoute.Home -> HomeScreen()
            TopLevelRoute.SuperUser -> SuperUserScreen()
            TopLevelRoute.Module -> ModuleScreen()
            TopLevelRoute.Setting -> SettingScreen()
        }
    }

    entry<AppProfileScreenNavKey> {
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
    entry<AppProfileTemplateNavKey> {
        AppProfileTemplateScreen()
    }
    entry<TemplateEditorNavKey> {
        TemplateEditorScreen(it.initialTemplate, it.readOnly)
    }

}