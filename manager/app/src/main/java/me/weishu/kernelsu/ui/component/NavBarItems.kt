package me.weishu.kernelsu.ui.component

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.ui.navigation3.NavController
import me.weishu.kernelsu.ui.navigation3.TopLevelRoute
import me.weishu.kernelsu.ui.util.LocalNavController
import me.weishu.kernelsu.ui.util.isRailNavbar
import me.weishu.kernelsu.ui.util.navigationSuiteType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavBarItems() {
    val navigator = LocalNavController.current
    val haptic = LocalHapticFeedback.current
    val isRail = isRailNavbar()
    val isManager = Natives.isManager
    TopLevelRoute.entries.forEach { route ->
        if (!isManager && route.rootRequired) return@forEach
        val interactionSource = remember { MutableInteractionSource() }
        val isSelected = NavController.getTopLevel(navigator.currentTopLevel) == route
        val onItemClick = {
            if (!isSelected) {
                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                navigator.navigateTo(route.navKey)
            }
        }
        val scope = rememberCoroutineScope()
        val tooltipState = rememberTooltipState()
        LaunchedEffect(tooltipState.isVisible) {
            if (tooltipState.isVisible) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        NavigationSuiteItem(
            modifier = Modifier.combinedClickable(
                interactionSource, indication = null, onClick = onItemClick, onLongClick = {
                    scope.launch { tooltipState.show() }
                }),
            navigationSuiteType = navigationSuiteType(currentWindowAdaptiveInfo()),
            interactionSource = interactionSource,
            selected = isSelected,
            onClick = onItemClick,
            label = { Text(stringResource(route.labelId)) },
            icon = {
                TooltipBox(
                    state = tooltipState,
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                        if (isRail) TooltipAnchorPosition.End else TooltipAnchorPosition.Above
                    ),
                    tooltip = {
                        val toolTipPadding =
                            if (isRail) PaddingValues(start = 15.dp) else PaddingValues(bottom = 15.dp)
                        PlainTooltip(modifier = Modifier.padding(toolTipPadding)) {
                            Text(
                                stringResource(route.labelId)
                            )
                        }
                    },
                ) {
                    Icon(
                        painter = painterResource(if (isSelected) route.selectedIcon else route.defaultIcon),
                        contentDescription = stringResource(route.labelId)
                    )
                }
            })
    }
}