package me.weishu.kernelsu.ui.component.bottombar

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.LocalMainPagerState
import me.weishu.kernelsu.ui.navigation3.breeze.NavigationBarType
import me.weishu.kernelsu.ui.navigation3.breeze.isRail
import me.weishu.kernelsu.ui.util.rootAvailable

private data class RailItem(
    val label: Int,
    val selectedIcon: Int,
    val unselectedIcon: Int,
)

private val railItems = listOf(
    RailItem(R.string.home, R.drawable.ic_cottage_rounded_filled, R.drawable.ic_cottage_rounded),
    RailItem(R.string.superuser, R.drawable.ic_security_rounded, R.drawable.ic_shield_rounded),
    RailItem(R.string.module, R.drawable.ic_extension_rounded_filled, R.drawable.ic_extension_rounded),
    RailItem(R.string.settings, R.drawable.ic_settings_rounded_filled, R.drawable.ic_settings_rounded),
)

@Composable
fun NavigationRailBreeze(
    modifier: Modifier = Modifier,
    navBarType: NavigationBarType = NavigationBarType.Rail,
    expandedOverride: Boolean? = null,
    onExpandedOverrideChange: (Boolean?) -> Unit = {},
) {
    if (!navBarType.isRail()) return
    val isManager = Natives.isManager
    val fullFeatured = isManager && !Natives.requireNewKernel() && rootAvailable()
    if (!fullFeatured) return
    val mainPagerState = LocalMainPagerState.current

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
        arrangement = Arrangement.Center,
        header = {
            IconButton(
                modifier = Modifier.padding(start = 24.dp),
                onClick = {
                    onExpandedOverrideChange(!isExpanded)
                },
            ) {
                Icon(
                    painter = painterResource(
                        if (isExpanded) R.drawable.ic_menu_open_rounded else R.drawable.ic_menu_rounded
                    ),
                    contentDescription = null,
                )
            }
        },
    ) {
        railItems.forEachIndexed { index, item ->
            val selected = mainPagerState.selectedPage == index
            WideNavigationRailItem(
                selected = selected,
                onClick = {
                    if (!selected) mainPagerState.animateToPage(index)
                },
                icon = {
                    Icon(
                        painterResource(if (selected) item.selectedIcon else item.unselectedIcon),
                        stringResource(item.label),
                    )
                },
                label = { Text(stringResource(item.label)) },
                railExpanded = isExpanded,
            )
        }
    }
}
