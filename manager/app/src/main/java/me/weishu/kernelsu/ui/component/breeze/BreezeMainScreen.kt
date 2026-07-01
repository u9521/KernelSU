package me.weishu.kernelsu.ui.component.breeze

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.ui.LocalMainPagerState
import me.weishu.kernelsu.ui.component.bottombar.BottomBarBreeze
import me.weishu.kernelsu.ui.component.bottombar.MainPagerState
import me.weishu.kernelsu.ui.component.bottombar.NavigationRailBreeze
import me.weishu.kernelsu.ui.component.bottombar.rememberMainPagerState
import me.weishu.kernelsu.ui.navigation3.LocalNavigator
import me.weishu.kernelsu.ui.navigation3.Navigator
import me.weishu.kernelsu.ui.navigation3.Route
import me.weishu.kernelsu.ui.navigation3.breeze.getNavBarType
import me.weishu.kernelsu.ui.navigation3.breeze.isRailNavbar
import me.weishu.kernelsu.ui.navigation3.breeze.isTopRoute
import me.weishu.kernelsu.ui.screen.home.HomePager
import me.weishu.kernelsu.ui.screen.module.ModulePager
import me.weishu.kernelsu.ui.screen.settings.SettingPager
import me.weishu.kernelsu.ui.screen.superuser.SuperUserPager
import me.weishu.kernelsu.ui.util.bottomBarHazeStyle
import me.weishu.kernelsu.ui.util.defaultHazeEffect
import me.weishu.kernelsu.ui.util.onlyHorizontal
import me.weishu.kernelsu.ui.util.rememberContentReady
import me.weishu.kernelsu.ui.util.rootAvailable
import me.weishu.kernelsu.ui.viewmodel.MainPagerConfig

@Composable
fun MainScreenBreeze(
    initialPage: Int = 0,
    onPageChanged: (Int) -> Unit = {},
) {
    val navController = LocalNavigator.current
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { MainPagerConfig.PAGE_COUNT })
    val mainPagerState = rememberMainPagerState(pagerState)
    val isManager = Natives.isManager
    val isFullFeatured = isManager && !Natives.requireNewKernel() && rootAvailable()
    var userScrollEnabled by remember(isFullFeatured) { mutableStateOf(isFullFeatured) }
    val mainScreenHazeState = rememberHazeState()

    val isTopRoute = navController.isTopRoute()
    val navBarType = currentWindowAdaptiveInfo().getNavBarType()
    val useNavigationRail = isRailNavbar()
    var railExpandedOverride by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val navState = rememberBreezeNavLayoutState(
        initialValue = if (useNavigationRail) NavigationLayoutType.SIDE else NavigationLayoutType.BOTTOM
    )
    LaunchedEffect(isTopRoute, useNavigationRail) {
        if (!isTopRoute) {
            navState.hideNavController()
            return@LaunchedEffect
        }
        if (useNavigationRail) navState.showRail() else navState.showBar()
    }

    val settledPage = mainPagerState.pagerState.settledPage
    LaunchedEffect(settledPage) {
        onPageChanged(settledPage)
    }

    val currentPage = mainPagerState.pagerState.currentPage
    LaunchedEffect(currentPage) {
        mainPagerState.syncPage()
    }

    MainScreenBackHandler(mainPagerState, navController)

    CompositionLocalProvider(
        LocalMainPagerState provides mainPagerState
    ) {
        val contentReady = rememberContentReady()
        val pagerContent = @Composable { contentPadding: PaddingValues ->
            val bottomInnerPadding = contentPadding.calculateBottomPadding()
            HorizontalPager(
                modifier = Modifier
                    .hazeSource(mainScreenHazeState)
                    .padding(contentPadding.onlyHorizontal()),
                state = mainPagerState.pagerState,
                beyondViewportPageCount = if (contentReady) 3 else 0,
                userScrollEnabled = userScrollEnabled,
            ) { page ->
                val isCurrentPage = page == settledPage
                when (page) {
                    0 -> if (isCurrentPage || contentReady) HomePager(navController, bottomInnerPadding, isCurrentPage)
                    1 -> if (isCurrentPage || contentReady) SuperUserPager(navController, bottomInnerPadding, isCurrentPage)
                    2 -> if (isCurrentPage || contentReady) ModulePager(bottomInnerPadding, isCurrentPage)
                    3 -> if (isCurrentPage || contentReady) SettingPager(navController, bottomInnerPadding)
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
                        navBarType
                    )
                }
            },
            sideBar = {
                NavigationRailBreeze(
                    navBarType = navBarType,
                    expandedOverride = railExpandedOverride,
                    onExpandedOverrideChange = { railExpandedOverride = it },
                    modifier = Modifier,
                )
            },
        ) { contentPadding ->
            pagerContent(contentPadding)
        }
    }
}

@Composable
private fun MainScreenBackHandler(
    mainState: MainPagerState,
    navController: Navigator,
) {
    val isPagerBackHandlerEnabled by remember {
        derivedStateOf {
            navController.current() is Route.Main && navController.backStackSize() == 1 && mainState.selectedPage != 0
        }
    }

    val navEventState = rememberNavigationEventState(NavigationEventInfo.None)

    NavigationBackHandler(
        state = navEventState,
        isBackEnabled = isPagerBackHandlerEnabled,
        onBackCompleted = {
            mainState.animateToPage(0)
        }
    )
}
