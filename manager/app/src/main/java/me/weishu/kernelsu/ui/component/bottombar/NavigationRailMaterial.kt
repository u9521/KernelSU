package me.weishu.kernelsu.ui.component.bottombar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.LocalMainPagerState
import me.weishu.kernelsu.ui.util.rootAvailable

@Composable
fun NavigationRailMaterial(
    modifier: Modifier = Modifier,
) {
    val isManager = Natives.isManager
    val fullFeatured = isManager && !Natives.requireNewKernel() && rootAvailable()
    val mainPagerState = LocalMainPagerState.current

    if (!fullFeatured) return

    val items = listOf(
        Triple(R.string.home, R.drawable.ic_cottage_rounded_filled, R.drawable.ic_cottage_rounded),
        Triple(R.string.superuser, R.drawable.ic_security_rounded, R.drawable.ic_shield_rounded),
        Triple(R.string.module, R.drawable.ic_extension_rounded_filled, R.drawable.ic_extension_rounded),
        Triple(R.string.settings, R.drawable.ic_settings_rounded_filled, R.drawable.ic_settings_rounded)
    )

    val state = rememberWideNavigationRailState()
    val scope = rememberCoroutineScope()
    val expanded = state.targetValue == WideNavigationRailValue.Expanded

    WideNavigationRail(
        modifier = modifier.fillMaxHeight(),
        state = state,
        colors = WideNavigationRailDefaults.colors().copy(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        windowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout).only(
            WindowInsetsSides.Start + WindowInsetsSides.Vertical
        ),
        contentPadding = PaddingValues(vertical = 20.dp),
        header = {
            IconButton(
                modifier = Modifier.padding(start = 24.dp),
                onClick = {
                    scope.launch {
                        if (expanded) state.collapse() else state.expand()
                    }
                },
            ) {
                Icon(
                    painter = painterResource(if (expanded) R.drawable.ic_menu_open_rounded else R.drawable.ic_menu_rounded),
                    contentDescription = null
                )
            }
        },
    ) {
        items.forEachIndexed { index, (label, selectedIcon, unselectedIcon) ->
            val selected = mainPagerState.selectedPage == index
            WideNavigationRailItem(
                railExpanded = expanded,
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
                label = { Text(stringResource(label)) }
            )
        }
    }
}
