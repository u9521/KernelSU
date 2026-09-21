package me.weishu.kernelsu.breezeui.component.bottombar

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.breezeui.icons.MaterialSymbols
import me.weishu.kernelsu.breezeui.nav.NavigationBarType
import me.weishu.kernelsu.breezeui.nav.isRail
import me.weishu.kernelsu.breezeui.screen.LocalBreezeMainPagerState
import me.weishu.kernelsu.ui.component.bottombar.NavigationBadgeState
import me.weishu.kernelsu.ui.component.bottombar.NavigationIconWithBadge
import me.weishu.kernelsu.ui.component.bottombar.badgeFor

private data class RailItem(
    val label: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val railItems = listOf(
    RailItem(R.string.home, MaterialSymbols.Filled.Rounded.Cottage, MaterialSymbols.Rounded.Cottage),
    RailItem(R.string.superuser, MaterialSymbols.Rounded.Security, MaterialSymbols.Rounded.Shield),
    RailItem(R.string.module, MaterialSymbols.Filled.Rounded.Extension, MaterialSymbols.Rounded.Extension),
    RailItem(R.string.settings, MaterialSymbols.Filled.Rounded.Settings, MaterialSymbols.Rounded.Settings),
)

@Composable
fun NavigationRailBreeze(
    modifier: Modifier = Modifier,
    navigationBadge: NavigationBadgeState,
    navBarType: NavigationBarType = NavigationBarType.Rail,
    expandedOverride: Boolean? = null,
    onExpandedOverrideChange: (Boolean?) -> Unit = {},
) {
    if (!navBarType.isRail()) return
    val fullFeatured = Natives.isFullFeatured()
    if (!fullFeatured) return
    val mainPagerState = LocalBreezeMainPagerState.current

    val defaultExpanded = navBarType != NavigationBarType.Rail
    val isExpanded = expandedOverride ?: defaultExpanded
    val state = rememberWideNavigationRailState(
        initialValue = if (defaultExpanded) WideNavigationRailValue.Expanded else WideNavigationRailValue.Collapsed
    )

    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            state.expand()
        } else {
            state.collapse()
        }
    }

    val resources = LocalResources.current
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelLarge
    val layoutDirection = LocalLayoutDirection.current

    val insetWidth = WideNavigationRailDefaults.windowInsets.asPaddingValues().calculateStartPadding(layoutDirection)

    val expandedWidthDp = remember(railItems, density, insetWidth) {
        val maxTextWidthPx = railItems.maxOfOrNull { item ->
            textMeasurer.measure(resources.getString(item.label), style = labelStyle).size.width
        } ?: 0
        val maxTextWidthDp = with(density) { maxTextWidthPx.toDp() }

        val targetWidth = maxTextWidthDp + 104.dp + insetWidth
        targetWidth
    }

    val animatedWidth by animateDpAsState(
        targetValue = if (isExpanded) expandedWidthDp else 96.dp + insetWidth,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "RailWidthAnimation"
    )

    WideNavigationRail(
        modifier = modifier
            .fillMaxHeight()
            .requiredWidth(animatedWidth),
        state = state,
        header = {
            IconButton(
                modifier = Modifier.padding(start = 24.dp),
                onClick = {
                    onExpandedOverrideChange(!isExpanded)
                },
            ) {
                Icon(
                    imageVector = if (isExpanded) {
                        MaterialSymbols.Rounded.MenuOpen
                    } else {
                        MaterialSymbols.Rounded.Menu
                    },
                    contentDescription = stringResource(
                        if (isExpanded) R.string.nav_rail_collapse else R.string.nav_rail_expand
                    ),
                )
            }
        },
    ) {
        railItems.forEachIndexed { index, (label, selectedIcon, unselectedIcon) ->
            val selected = mainPagerState.selectedPage == index
            WideNavigationRailItem(
                selected = selected,
                onClick = {
                    if (!selected) mainPagerState.animateToPage(index)
                },
                icon = {
                    NavigationIconWithBadge(
                        icon = if (selected) selectedIcon else unselectedIcon,
                        contentDescription = stringResource(id = label),
                        badge = badgeFor(index, navigationBadge),
                    )
                },
                label = { Text(stringResource(id = label)) },
                railExpanded = isExpanded,
            )
        }
    }
}
