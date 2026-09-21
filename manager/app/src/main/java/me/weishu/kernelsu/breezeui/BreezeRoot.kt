package me.weishu.kernelsu.breezeui

import android.content.Intent
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import kotlinx.coroutines.channels.ReceiveChannel
import me.weishu.kernelsu.breezeui.nav.BreezeIntentRouter
import me.weishu.kernelsu.breezeui.nav.BreezeListDetailScene
import me.weishu.kernelsu.breezeui.nav.BreezeMainHostContentKey
import me.weishu.kernelsu.breezeui.nav.BreezeRoute
import me.weishu.kernelsu.breezeui.nav.LocalBreezeNavigator
import me.weishu.kernelsu.breezeui.nav.NavDisplayBreeze
import me.weishu.kernelsu.breezeui.nav.currentPageIndex
import me.weishu.kernelsu.breezeui.nav.mainPageBindingKey
import me.weishu.kernelsu.breezeui.nav.rememberBreezeNavigator
import me.weishu.kernelsu.breezeui.screen.LocalBreezeMainPagerState
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
import me.weishu.kernelsu.ui.component.bottombar.rememberMainPagerState
import me.weishu.kernelsu.ui.theme.LocalColorMode
import me.weishu.kernelsu.ui.theme.LocalEnableBlur
import me.weishu.kernelsu.ui.theme.LocalEnableFloatingBottomBar
import me.weishu.kernelsu.ui.theme.LocalEnableFloatingBottomBarBlur
import me.weishu.kernelsu.ui.theme.LocalEnableNavigationBadge
import me.weishu.kernelsu.ui.theme.LocalModuleDescriptionMaxLines
import me.weishu.kernelsu.ui.theme.MaterialKernelSUTheme
import me.weishu.kernelsu.ui.viewmodel.MainActivityViewModel
import me.weishu.kernelsu.ui.viewmodel.MainPagerConfig

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
    val navigator = rememberBreezeNavigator(BreezeRoute.TabRoute.Home)
    // The pager is owned here, above the navigation host, and not by the entry that renders it:
    // the page on screen is the bottom entry of the back stack, so every page change swaps that
    // entry - and a pager rebuilt by such a swap would be initialized on the new page and appear
    // there without animating. Out here it keeps its layout and its animation across the swap, so
    // the page always slides in from the one on screen.
    val pagerState = rememberPagerState(
        initialPage = navigator.currentPageIndex() ?: 0,
        pageCount = { MainPagerConfig.PAGE_COUNT },
    )
    val mainPagerState = rememberMainPagerState(
        pagerState = pagerState,
        animatePageChanges = true,
    )
    val systemDensity = LocalDensity.current
    val density = remember(systemDensity, uiState.pageScale) {
        Density(systemDensity.density * uiState.pageScale, systemDensity.fontScale)
    }
    val blurController = remember { BlurController() }

    CompositionLocalProvider(
        LocalBreezeNavigator provides navigator,
        LocalBreezeMainPagerState provides mainPagerState,
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

            // The pager host is a single movable node that every page route renders. A page change
            // swaps the entry at the bottom of the back stack, and navigation3 composes the new
            // entry's content from scratch - which, without this, rebuilds the host and all four of
            // its pages for every page change, and makes a drag in flight lose the pager it was
            // dragging. Instead every page route renders *this* content, and a movable node that is
            // called again - here in the same place, with the new page - keeps its composition, its
            // state and its nodes, so the pager, its pages and the drag all survive the swap.
            //
            // It is created out here, and not by an entry or by navigation3's own decorators,
            // because everything navigation3 owns goes away with the entry it belongs to: it drops
            // the movable content of a popped entry, and it skips an entry that another scene is
            // already rendering. This instance is never popped and never excluded.
            val onPageChanged by rememberUpdatedState(viewModel::setSelectedMainPage)
            val mainHost = remember {
                movableContentOf<BreezeRoute.TabRoute> { page -> MainScreenBreeze(page, onPageChanged) }
            }

            val provider = entryProvider<NavKey>(
                // A page route has no `entry` of its own: all four of them render the same host, so
                // they are handled here together rather than as four registrations with four
                // identical bodies. The shared content key is what makes navigation3 treat them as
                // the same scene - and so the same content, which is the other half of why the host
                // is not torn down and no scene transition runs between two pages. See
                // `BreezeMainHostContentKey`.
                fallback = { key ->
                    val route = key as? BreezeRoute.TabRoute ?: error("Unknown screen $key")
                    NavEntry(
                        key,
                        BreezeMainHostContentKey,
                        BreezeListDetailScene.listPane(route.mainPageBindingKey()),
                    ) { mainHost(route) }
                },
            ) {
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
            }

            NavDisplayBreeze(entryProvider = provider)

            SideEffect { onContentReady() }
        }
    }
}
