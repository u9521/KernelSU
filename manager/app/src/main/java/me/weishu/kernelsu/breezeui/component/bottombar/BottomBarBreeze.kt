package me.weishu.kernelsu.breezeui.component.bottombar

import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.breezeui.icons.MaterialSymbols
import me.weishu.kernelsu.breezeui.nav.NavigationBarType
import me.weishu.kernelsu.breezeui.screen.LocalBreezeMainPagerState
import me.weishu.kernelsu.ui.component.bottombar.NavigationBadgeState
import me.weishu.kernelsu.ui.component.bottombar.NavigationIconWithBadge
import me.weishu.kernelsu.ui.component.bottombar.badgeFor
import me.weishu.kernelsu.ui.theme.LocalEnableBlur

@Composable
fun BottomBarBreeze(
    modifier: Modifier = Modifier,
    navBarType: NavigationBarType = NavigationBarType.Bar,
    navigationBadge: NavigationBadgeState,
) {
    val fullFeatured = Natives.isFullFeatured()
    if (!fullFeatured) return

    val mainPagerState = LocalBreezeMainPagerState.current
    val blurEnabled = LocalEnableBlur.current

    val items = listOf(
        Triple(R.string.home, MaterialSymbols.Filled.Rounded.Cottage, MaterialSymbols.Rounded.Cottage),
        Triple(R.string.superuser, MaterialSymbols.Rounded.Security, MaterialSymbols.Rounded.Shield),
        Triple(R.string.module, MaterialSymbols.Filled.Rounded.Extension, MaterialSymbols.Rounded.Extension),
        Triple(R.string.settings, MaterialSymbols.Filled.Rounded.Settings, MaterialSymbols.Rounded.Settings)
    )
    val barHeight = 80.dp //NavigationBarTokens.TallContainerHeight
    val compactBarHeight = BottomAppBarDefaults.FlexibleBottomAppBarHeight

    FlexibleBottomAppBar(
        modifier = modifier,
        containerColor = if (blurEnabled) Color.Transparent else MaterialTheme.colorScheme.surfaceBright,
        expandedHeight = if (navBarType == NavigationBarType.Bar) barHeight else compactBarHeight
    ) {
        items.forEachIndexed { index, (label, selectedIcon, unselectedIcon) ->
            val selected = mainPagerState.selectedPage == index
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        mainPagerState.animateToPage(index)
                    }
                },
                icon = {
                    NavigationIconWithBadge(
                        icon = if (selected) selectedIcon else unselectedIcon,
                        contentDescription = stringResource(id = label),
                        badge = badgeFor(index, navigationBadge),
                    )
                },
                label = {
                    Text(
                        stringResource(label),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}
