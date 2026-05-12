package me.weishu.kernelsu.ui.component.bottombar

import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.LocalMainPagerState
import me.weishu.kernelsu.ui.navigation3.breeze.NavigationBarType
import me.weishu.kernelsu.ui.theme.LocalEnableBlur
import me.weishu.kernelsu.ui.util.rootAvailable

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BottomBarBreeze(modifier: Modifier = Modifier, navBarType: NavigationBarType = NavigationBarType.Bar) {
    val isManager = Natives.isManager
    val fullFeatured = isManager && !Natives.requireNewKernel() && rootAvailable()
    val mainPagerState = LocalMainPagerState.current
    val blurEnabled = LocalEnableBlur.current

    if (!fullFeatured) return

    val items = listOf(
        Triple(R.string.home, R.drawable.ic_cottage_rounded_filled, R.drawable.ic_cottage_rounded),
        Triple(R.string.superuser, R.drawable.ic_security_rounded, R.drawable.ic_shield_rounded),
        Triple(R.string.module, R.drawable.ic_extension_rounded_filled, R.drawable.ic_extension_rounded),
        Triple(R.string.settings, R.drawable.ic_settings_rounded_filled, R.drawable.ic_settings_rounded)
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
                    Icon(
                        painterResource(if (selected) selectedIcon else unselectedIcon),
                        stringResource(label)
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
