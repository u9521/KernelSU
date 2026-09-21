package me.weishu.kernelsu.breezeui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.breezeui.component.bottombar.BottomBarBreeze
import me.weishu.kernelsu.breezeui.component.bottombar.BreezeNavigationLayout
import me.weishu.kernelsu.breezeui.component.bottombar.NavigationLayoutType
import me.weishu.kernelsu.breezeui.component.bottombar.NavigationRailBreeze
import me.weishu.kernelsu.breezeui.component.bottombar.rememberBreezeNavLayoutState
import me.weishu.kernelsu.breezeui.nav.BreezeNavigator
import me.weishu.kernelsu.breezeui.nav.BreezeRoute
import me.weishu.kernelsu.breezeui.nav.LocalBreezeNavigator
import me.weishu.kernelsu.breezeui.nav.currentPageIndex
import me.weishu.kernelsu.breezeui.nav.getNavBarType
import me.weishu.kernelsu.breezeui.nav.isRailNavbar
import me.weishu.kernelsu.breezeui.nav.isTopRoute
import me.weishu.kernelsu.breezeui.nav.mainPageRoute
import me.weishu.kernelsu.breezeui.screen.home.HomeEntry
import me.weishu.kernelsu.breezeui.screen.module.ModuleEntry
import me.weishu.kernelsu.breezeui.screen.settings.SettingsEntry
import me.weishu.kernelsu.breezeui.screen.superuser.SuperUserEntry
import me.weishu.kernelsu.breezeui.util.bottomBarHazeStyle
import me.weishu.kernelsu.breezeui.util.defaultHazeEffect
import me.weishu.kernelsu.breezeui.util.onlyHorizontal
import me.weishu.kernelsu.ui.component.bottombar.MainPagerState
import me.weishu.kernelsu.ui.component.bottombar.NavigationBadgeState
import me.weishu.kernelsu.ui.theme.LocalEnableNavigationBadge
import me.weishu.kernelsu.ui.util.getSuperuserCount
import me.weishu.kernelsu.ui.viewmodel.MainPagerConfig
import me.weishu.kernelsu.ui.viewmodel.ModuleViewModel
import me.weishu.kernelsu.ui.viewmodel.SuperUserViewModel

/**
 * Breeze's main screen: the four pages, the bottom bar / navigation rail, and the pager that moves
 * between them.
 *
 * Which page is on screen is decided by the back stack - a page route is its bottom entry, see
 * [BreezeNavigator.push] - so this screen only follows it: [tabRoute] sends the pager to the page
 * it names, and a swipe sends the page it settled on back to the back stack.
 *
 * Every page route renders this same screen, and `BreezeRoot` renders it through a single
 * `movableContentOf`, so the entry a page change swaps is only a new call to that movable node:
 * this composition, the pager and a drag in flight all survive it. The pager state is owned by
 * `BreezeRoot` as well, so even a rebuild cannot make a page change arrive without animating.
 */
@Composable
fun MainScreenBreeze(
    tabRoute: BreezeRoute.TabRoute,
    onPageChanged: (Int) -> Unit = {},
) {
    val navController = LocalBreezeNavigator.current
    val navBarType = currentWindowAdaptiveInfoV2().getNavBarType()
    val useNavigationRail = isRailNavbar()
    val mainPagerState = LocalBreezeMainPagerState.current
    val isFullFeatured = Natives.isFullFeatured()
    val mainScreenHazeState = rememberHazeState()

    var railExpandedOverride by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val navState = rememberBreezeNavLayoutState(
        initialValue = if (useNavigationRail) NavigationLayoutType.SIDE else NavigationLayoutType.BOTTOM
    )
    val isTopRoute = navController.isTopRoute()
    val isNavVisible = isTopRoute && navState.targetValue != NavigationLayoutType.HIDDEN
    var userScrollEnabled by remember(isFullFeatured, isNavVisible) { mutableStateOf(isFullFeatured && isNavVisible) }

    val enableNavigationBadge = LocalEnableNavigationBadge.current
    val badgeEnabled = enableNavigationBadge && isFullFeatured
    val moduleViewModel = viewModel<ModuleViewModel>()
    val moduleUiState by moduleViewModel.uiState.collectAsStateWithLifecycle()

    val superUserViewModel = viewModel<SuperUserViewModel>()
    val grantedUidCount by remember(superUserViewModel) {
        superUserViewModel.uiState
            .map { state -> state.groupedApps.count { it.anyAllowSu } }
            .distinctUntilChanged()
    }.collectAsStateWithLifecycle(0)

    var startupPreloadStarted by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(isFullFeatured) {
        if (!isFullFeatured || startupPreloadStarted) {
            return@LaunchedEffect
        }

        moduleViewModel.initializePreferences()
        val moduleState = moduleViewModel.uiState.value
        if (!moduleState.hasLoaded) {
            if (!moduleState.isRefreshing) moduleViewModel.fetchModuleList()
            moduleViewModel.uiState.first { it.hasLoaded }
        }
        moduleViewModel.syncModuleUpdateInfo(moduleViewModel.uiState.value.modules)

        val superUserState = superUserViewModel.uiState.value
        if (!superUserState.hasLoaded) {
            superUserViewModel.initializePreferences()
            if (superUserState.isRefreshing) {
                superUserViewModel.uiState.first { it.hasLoaded }
            } else {
                superUserViewModel.loadAppList().join()
            }
        }

        startupPreloadStarted = true
    }

    // Loading the app list just for a badge is too expensive; read the kernel allowlist instead.
    var superuserCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(badgeEnabled, grantedUidCount) {
        superuserCount = if (badgeEnabled) withContext(Dispatchers.IO) { getSuperuserCount() } else 0
    }

    val navigationBadge = if (badgeEnabled) {
        NavigationBadgeState(
            superuserCount = superuserCount,
            moduleEnabledCount = moduleUiState.modules.count { it.enabled },
            moduleUpdatableCount = moduleUiState.updateInfo.count { it.value.downloadUrl.isNotBlank() },
        )
    } else {
        NavigationBadgeState()
    }

    LaunchedEffect(isTopRoute, useNavigationRail) {
        if (!isTopRoute) {
            navState.hideNavController()
            return@LaunchedEffect
        }
        if (useNavigationRail) navState.showRail() else navState.showBar()
    }

    // The back stack names the page on screen, so a page change is a route change: the pager is
    // sent to the page the stack now names. Tapping the bar or the rail therefore slides away from
    // the page on screen, and tapping again mid-slide only retargets that one animation - the route
    // is the only thing driving the pager, so nothing can pull it back.
    LaunchedEffect(tabRoute) {
        mainPagerState.animateToPage(tabRoute.ordinal)
    }

    // The other direction, for a swipe: the page the pager settled on is named by pushing it as a
    // page route, which owns the back stack. Both guards matter. `isNavigating`: a page the pager
    // was *sent* to is named already, and naming it again cancels the animation still running.
    // `isTopRoute()`: a page route push clears the stack, so a screen that was only *composed*
    // must not name anything - a predictive back composes the page it returns to *before* it pops,
    // so pushing here drops the detail being previewed and the back commits early, uncancellably.
    val settledPage = mainPagerState.pagerState.settledPage
    LaunchedEffect(settledPage) {
        onPageChanged(settledPage)
        if (!mainPagerState.isNavigating && navController.isTopRoute()) {
            navController.push(mainPageRoute(settledPage))
        }
    }

    val currentPage = mainPagerState.pagerState.currentPage
    LaunchedEffect(currentPage) {
        mainPagerState.syncPage()
    }

    // The page on screen, or the one the pager is heading to: while a page change is running, the
    // settled page is still the one being left. This is what decides which page's content is drawn
    // and which page is told it is the current one, so it must follow the page *arriving* as well -
    // a page that only counts as current once the pager has come to rest is a page that draws
    // nothing while it slides in, and whose data only starts loading once it is already there.
    val activePage = mainPagerState.selectedPage

    MainScreenBackHandler(mainPagerState, navController)

    // The frame this screen is composed in is the frame a navigation transition starts on (app
    // start, and every return from a detail), and four pages in that frame is a wait the user sees.
    // So the page on screen is composed alone while this page's enter transition is in flight, and
    // the other three are preloaded in one go once it has arrived, off screen.
    //
    // `isSeeking` is not redundant: a predictive back seeks this transition instead of running it,
    // and `Transition.isRunning` is false while seeking - exactly the window where the extra work
    // must not happen. The scope is readable only inside a NavEntry, which the page entry is.
    val pageTransition = LocalNavAnimatedContentScope.current.transition
    val pageTransitionInFlight = pageTransition.isRunning || pageTransition.isSeeking
    var preloadAllPages by remember { mutableStateOf(false) }
    LaunchedEffect(pageTransitionInFlight) {
        if (!pageTransitionInFlight && !preloadAllPages) {
            withFrameNanos { }
            preloadAllPages = true
        }
    }

    val pagerContent = @Composable { contentPadding: PaddingValues ->
        val bottomInnerPadding = contentPadding.calculateBottomPadding()
        HorizontalPager(
            modifier = Modifier
                .hazeSource(mainScreenHazeState)
                .padding(contentPadding.onlyHorizontal()),
            state = mainPagerState.pagerState,
            // All four pages are preloaded - so none is ever composed while it is sliding in, which
            // is a frame of blank page - but only once the transition above is over.
            beyondViewportPageCount = if (preloadAllPages) MainPagerConfig.LAST_PAGE_INDEX else 0,
            overscrollEffect = null,
            userScrollEnabled = userScrollEnabled,
        ) { page ->
            val isActivePage = page == activePage
            when (page) {
                0 -> HomeEntry(bottomInnerPadding, isActivePage)
                1 -> SuperUserEntry(bottomInnerPadding, isActivePage)
                2 -> ModuleEntry(bottomInnerPadding, isActivePage)
                3 -> SettingsEntry(bottomInnerPadding, isActivePage)
            }
        }
    }

    BreezeNavigationLayout(
        state = navState,
        bottomBar = {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                BottomBarBreeze(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .defaultHazeEffect(mainScreenHazeState, bottomBarHazeStyle()),
                    navBarType = navBarType,
                    navigationBadge = navigationBadge
                )
            }
        },
        sideBar = {
            NavigationRailBreeze(
                navBarType = navBarType,
                navigationBadge = navigationBadge,
                expandedOverride = railExpandedOverride,
                onExpandedOverrideChange = { railExpandedOverride = it },
                modifier = Modifier,
            )
        },
    ) { contentPadding ->
        pagerContent(contentPadding)
    }
}

@Composable
private fun MainScreenBackHandler(
    mainState: MainPagerState,
    navController: BreezeNavigator,
) {
    // The pager is the only entry left and it is not on the first page, so back returns to the
    // first page - by asking for the route of it, which the pager then follows. Any one of the
    // three saying so is enough: the route is the page the stack names, `selectedPage` is the page
    // a swipe is already heading for, and `isNavigating` is a return that is still running. Back
    // must not fall through to finishing the activity while any of them is true.
    val isPagerBackHandlerEnabled by remember {
        derivedStateOf {
            navController.backStackSize() == 1 &&
                    ((navController.currentPageIndex() ?: 0) != 0 ||
                            mainState.selectedPage != 0 ||
                            mainState.isNavigating)
        }
    }

    val navEventState = rememberNavigationEventState(NavigationEventInfo.None)

    NavigationBackHandler(
        state = navEventState,
        isBackEnabled = isPagerBackHandlerEnabled,
        onBackCompleted = {
            navController.push(BreezeRoute.TabRoute.Home)
        }
    )
}

/**
 * The pager state of Breeze's main screen.
 *
 * Provided by `BreezeRoot`, next to the movable host rather than inside it, so the pager outlives
 * both the page entry that renders the screen and the screen itself: a page change cannot be cut
 * short by swapping that entry, and cannot arrive without animating even if the screen is rebuilt.
 * The bottom bar and the rail read it to know which page is selected.
 */
val LocalBreezeMainPagerState = staticCompositionLocalOf<MainPagerState> { error("LocalBreezeMainPagerState not provided") }
