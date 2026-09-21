package me.weishu.kernelsu.breezeui.screen.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.weishu.kernelsu.breezeui.BreezeMode
import me.weishu.kernelsu.breezeui.nav.BreezeRoute
import me.weishu.kernelsu.breezeui.nav.LocalBreezeNavigator
import me.weishu.kernelsu.ui.screen.settings.SettingsScreenActions
import me.weishu.kernelsu.ui.viewmodel.SettingsViewModel

/**
 * Breeze's settings page.
 *
 * The UI mode entry lists all three themes; picking Material or Miuix leaves Breeze,
 * picking Breeze comes back here. The mode itself is Breeze's `ui_mode` value.
 */
@Composable
fun SettingsEntry(
    bottomInnerPadding: Dp,
    isCurrentPage: Boolean = true,
) {
    val navigator = LocalBreezeNavigator.current
    val viewModel = viewModel<SettingsViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val latestIsCurrentPage by rememberUpdatedState(isCurrentPage)
    val initialResumeHandled = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isCurrentPage) {
        if (isCurrentPage) {
            viewModel.refresh()
        }
    }

    LifecycleResumeEffect(Unit) {
        if (initialResumeHandled.value && latestIsCurrentPage) {
            viewModel.refresh()
        }
        initialResumeHandled.value = true
        onPauseOrDispose { }
    }

    val actions = SettingsScreenActions(
        onSetCheckUpdate = viewModel::setCheckUpdate,
        onSetCheckModuleUpdate = viewModel::setCheckModuleUpdate,
        onOpenTheme = { navigator.push(BreezeRoute.ColorPalette) },
        onSetUiModeIndex = { index ->
            BreezeMode.select(
                when (index) {
                    UiModeIndex.MIUIX -> BreezeMode.MIUIX
                    UiModeIndex.MATERIAL -> BreezeMode.MATERIAL
                    else -> BreezeMode.VALUE
                }
            )
        },
        onOpenProfileTemplate = { navigator.push(BreezeRoute.AppProfileTemplate) },
        onSetSuCompatMode = viewModel::setSuCompatMode,
        onSetKernelUmountEnabled = viewModel::setKernelUmountEnabled,
        onSetSelinuxHideEnabled = viewModel::setSelinuxHideEnabled,
        onSetSulogEnabled = viewModel::setSulogEnabled,
        onSetAdbRootEnabled = viewModel::setAdbRootEnabled,
        onSetDefaultUmountModules = viewModel::setDefaultUmountModules,
        onSetEnableWebDebugging = viewModel::setEnableWebDebugging,
        onSetAutoJailbreak = viewModel::setAutoJailbreak,
        onSetUseSoftReboot = viewModel::setUseSoftReboot,
        onOpenAbout = { navigator.push(BreezeRoute.About) },
    )

    SettingPagerBreeze(uiState, actions, bottomInnerPadding)
}

/** Indices of the UI mode dropdown in [SettingPagerBreeze]. */
internal object UiModeIndex {
    const val MIUIX = 0
    const val MATERIAL = 1
    const val BREEZE = 2
}
