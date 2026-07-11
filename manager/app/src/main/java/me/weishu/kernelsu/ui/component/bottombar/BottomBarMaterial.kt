package me.weishu.kernelsu.ui.component.bottombar

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.LocalMainPagerState
import me.weishu.kernelsu.ui.util.rootAvailable

@Composable
fun BottomBarMaterial(moduleBadge: ModuleBadgeState) {
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

    ShortNavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        windowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout).only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
        )
    ) {
        items.forEachIndexed { index, (label, selectedIcon, unselectedIcon) ->
            val selected = mainPagerState.selectedPage == index
            ShortNavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        mainPagerState.animateToPage(index)
                    }
                },
                icon = {
                    NavigationIconWithBadge(
                        icon = painterResource(if (selected) selectedIcon else unselectedIcon),
                        contentDescription = stringResource(label),
                        badge = if (index == BottomBarDestination.Module.ordinal) moduleBadge else null,
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

@Composable
internal fun NavigationIconWithBadge(
    icon: Painter,
    contentDescription: String?,
    badge: ModuleBadgeState?,
) {
    if (badge != null && (badge.updatableCount > 0 || badge.enabledCount > 0)) {
        BadgedBox(
            badge = {
                // Pending updates take priority: default badge color (red) with the updatable
                // count; otherwise the theme-colored badge shows the enabled count.
                if (badge.updatableCount > 0) {
                    Badge {
                        Text(badge.updatableCount.toString())
                    }
                } else {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ) {
                        Text(badge.enabledCount.toString())
                    }
                }
            }
        ) {
            Icon(icon, contentDescription)
        }
    } else {
        Icon(icon, contentDescription)
    }
}
