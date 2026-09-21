package me.weishu.kernelsu.breezeui

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import kotlinx.coroutines.channels.ReceiveChannel
import me.weishu.kernelsu.breezeui.nav.BreezeIntentRouter
import me.weishu.kernelsu.breezeui.nav.BreezeListDetailScene
import me.weishu.kernelsu.breezeui.nav.BreezeRoute
import me.weishu.kernelsu.breezeui.nav.LocalBreezeNavigator
import me.weishu.kernelsu.breezeui.nav.NavDisplayBreeze
import me.weishu.kernelsu.breezeui.nav.rememberBreezeNavigator
import me.weishu.kernelsu.breezeui.screen.MainScreenBreeze
import me.weishu.kernelsu.breezeui.screen.about.AboutEntry
import me.weishu.kernelsu.breezeui.screen.appprofile.AppProfileEntry
import me.weishu.kernelsu.breezeui.screen.colorpalette.ColorPaletteEntry
import me.weishu.kernelsu.breezeui.screen.executemoduleaction.ExecuteModuleActionEntry
import me.weishu.kernelsu.breezeui.screen.flash.FlashEntry
import me.weishu.kernelsu.breezeui.screen.install.InstallEntry
import me.weishu.kernelsu.breezeui.screen.sulog.SulogEntry
import me.weishu.kernelsu.breezeui.screen.template.TemplateEntry
import me.weishu.kernelsu.breezeui.screen.templateeditor.TemplateEditorEntry
import me.weishu.kernelsu.breezeui.util.BlurController
import me.weishu.kernelsu.breezeui.util.LocalBlurController
import me.weishu.kernelsu.ui.LocalUiMode
import me.weishu.kernelsu.ui.UiMode
import me.weishu.kernelsu.ui.theme.LocalColorMode
import me.weishu.kernelsu.ui.theme.LocalEnableBlur
import me.weishu.kernelsu.ui.theme.LocalEnableFloatingBottomBar
import me.weishu.kernelsu.ui.theme.LocalEnableFloatingBottomBarBlur
import me.weishu.kernelsu.ui.theme.LocalEnableNavigationBadge
import me.weishu.kernelsu.ui.theme.LocalModuleDescriptionMaxLines
import me.weishu.kernelsu.ui.theme.MaterialKernelSUTheme
import me.weishu.kernelsu.ui.viewmodel.MainActivityViewModel

/**
 * The whole Breeze UI: its own theme, state providers and navigation graph.
 *
 * This is the only Breeze entry point `MainActivity` knows about. Breeze reads the
 * same settings through the upstream view model but owns everything above it, so
 * upstream can reshape its own screens and navigation without touching Breeze.
 */
@Composable
fun BreezeRoot(
    intentChannel: ReceiveChannel<Intent>,
    onContentReady: () -> Unit = {},
) {
    val viewModel = viewModel<MainActivityViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedMainPage by viewModel.selectedMainPage.collectAsStateWithLifecycle()
    val navigator = rememberBreezeNavigator(BreezeRoute.Main)
    val systemDensity = LocalDensity.current
    val density = remember(systemDensity, uiState.pageScale) {
        Density(systemDensity.density * uiState.pageScale, systemDensity.fontScale)
    }
    val blurController = remember { BlurController() }

    CompositionLocalProvider(
        LocalBreezeNavigator provides navigator,
        LocalDensity provides density,
        // Breeze is a Material3 theme; shared components still dispatching on the UI
        // mode therefore render their Material variant, which is what Breeze wants.
        LocalUiMode provides UiMode.Material,
        LocalColorMode provides uiState.appSettings.colorMode.value,
        LocalEnableBlur provides uiState.enableBlur,
        LocalEnableFloatingBottomBar provides uiState.enableFloatingBottomBar,
        LocalEnableFloatingBottomBarBlur provides uiState.enableFloatingBottomBarBlur,
        LocalEnableNavigationBadge provides uiState.enableNavigationBadge,
        LocalModuleDescriptionMaxLines provides uiState.moduleDescriptionMaxLines,
        LocalBlurController provides blurController,
    ) {
        MaterialKernelSUTheme(appSettings = uiState.appSettings) {
            BreezeIntentRouter(intentChannel = intentChannel)

            val mainScreenEntry = @Composable {
                MainScreenBreeze(
                    initialPage = selectedMainPage,
                    onPageChanged = viewModel::setSelectedMainPage,
                )
            }

            NavDisplayBreeze(entryProvider = entryProvider {
                // that sucks. TODO : bind navEntry to pages
                entry<BreezeRoute.Main>(metadata = BreezeListDetailScene.listPane("superuser")) { mainScreenEntry() }
                entry<BreezeRoute.About> { AboutEntry() }
                entry<BreezeRoute.Sulog> { SulogEntry() }
                entry<BreezeRoute.ColorPalette> { ColorPaletteEntry() }
                entry<BreezeRoute.AppProfileTemplate>(metadata = BreezeListDetailScene.listPane("temple")) { TemplateEntry() }
                entry<BreezeRoute.TemplateEditor>(metadata = BreezeListDetailScene.detailPane("temple")) { key ->
                    TemplateEditorEntry(key.template, key.readOnly)
                }
                entry<BreezeRoute.AppProfile>(metadata = BreezeListDetailScene.detailPane("superuser")) { key -> AppProfileEntry(key.uid) }
                entry<BreezeRoute.Install> { InstallEntry() }
                entry<BreezeRoute.Flash> { key -> FlashEntry(key.flashIt) }
                entry<BreezeRoute.ExecuteModuleAction> { key -> ExecuteModuleActionEntry(key.moduleId, key.fromShortcut) }
                entry<BreezeRoute.Home> { mainScreenEntry() }
                entry<BreezeRoute.SuperUser> { mainScreenEntry() }
                entry<BreezeRoute.Module> { mainScreenEntry() }
                entry<BreezeRoute.Settings> { mainScreenEntry() }
            })

            SideEffect { onContentReady() }
        }
    }
}
