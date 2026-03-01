package me.weishu.kernelsu.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.BreezeSnackBarHost
import me.weishu.kernelsu.ui.component.LocalSnackbarHost
import me.weishu.kernelsu.ui.component.SegmentedListGroup
import me.weishu.kernelsu.ui.component.ksuIsValid
import me.weishu.kernelsu.ui.component.popUps.AboutDialog
import me.weishu.kernelsu.ui.component.popUps.sendLogBottomSheet
import me.weishu.kernelsu.ui.component.popUps.uninstallDialog
import me.weishu.kernelsu.ui.component.rememberCustomDialog
import me.weishu.kernelsu.ui.component.switchHapticFeedBack
import me.weishu.kernelsu.ui.navigation3.LocalNavController
import me.weishu.kernelsu.ui.navigation3.Route
import me.weishu.kernelsu.ui.theme.defaultTopAppBarColors
import me.weishu.kernelsu.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingScreen() {
    val navigator = LocalNavController.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val snackBarHost = LocalSnackbarHost.current
    val viewModel = viewModel<SettingsViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val resources = LocalResources.current
    val switchFeedback = switchHapticFeedBack()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                colors = defaultTopAppBarColors(), title = {
                    Text(stringResource(R.string.settings))
                }, scrollBehavior = scrollBehavior
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        snackbarHost = { BreezeSnackBarHost(snackBarHost) },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(all = 16.dp)
                .navigationBarsPadding()
                .imePadding(), verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            SegmentedListGroup {
                switchItem(
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.ic_update_rounded_filled), contentDescription = stringResource(id = R.string.settings_check_update)
                        )
                    },
                    title = resources.getString(R.string.settings_check_update),
                    summary = resources.getString(R.string.settings_check_update_summary),
                    checked = uiState.checkUpdate,
                    onCheckedChange = {
                        switchFeedback(it)
                        viewModel.setCheckUpdate(it)
                    })

                switchItem(
                    visible = ksuIsValid(),
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.ic_upload_file_rounded_filled), contentDescription = stringResource(id = R.string.settings_check_update)
                        )
                    },
                    title = resources.getString(R.string.settings_module_check_update),
                    summary = resources.getString(R.string.settings_check_update_summary),
                    checked = uiState.checkModuleUpdate
                ) {
                    switchFeedback(it)
                    viewModel.setCheckModuleUpdate(it)
                }
            }

            SegmentedListGroup {
                item(
                    visible = ksuIsValid(),
                    leadingContent = { Icon(painterResource(R.drawable.ic_fence_rounded), stringResource(id = R.string.settings_profile_template)) },
                    content = { Text(stringResource(id = R.string.settings_profile_template)) },
                    supportingContent = { Text(stringResource(id = R.string.settings_profile_template_summary)) },
                    onClick = {
                        navigator.navigateTo(Route.AppProfileTemplate)
                    })
            }

            val suSummary = when (uiState.suCompatStatus) {
                "unsupported" -> stringResource(id = R.string.feature_status_unsupported_summary)
                "managed" -> stringResource(id = R.string.feature_status_managed_summary)
                else -> stringResource(id = R.string.settings_sucompat_summary)
            }
            val suCompatModeItems = listOf(
                stringResource(id = R.string.settings_mode_enable_by_default),
                stringResource(id = R.string.settings_mode_disable_until_reboot),
                stringResource(id = R.string.settings_mode_disable_always),
            )

            val umountSummary = when (uiState.kernelUmountStatus) {
                "unsupported" -> stringResource(id = R.string.feature_status_unsupported_summary)
                "managed" -> stringResource(id = R.string.feature_status_managed_summary)
                else -> stringResource(id = R.string.settings_kernel_umount_summary)
            }

            SegmentedListGroup {
                menuItem(
                    visible = ksuIsValid(),
                    content = { Text(stringResource(id = R.string.settings_sucompat)) },
                    selected = suCompatModeItems.getOrNull(uiState.suCompatMode),
                    enabled = uiState.suCompatStatus == "supported",
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.ic_remove_moderator_outlined_filled), stringResource(id = R.string.settings_sucompat)
                        )
                    },
                    supportingContent = { Text(suSummary) }) { dismissMenu ->
                    suCompatModeItems.forEachIndexed { index, name ->
                        DropdownMenuItem(text = { Text(name) }, onClick = {
                            viewModel.setSuCompatMode(index)
                            dismissMenu()
                        })
                    }
                }

                switchItem(
                    visible = ksuIsValid(),
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.ic_do_not_disturb_on_rounded_filled), stringResource(id = R.string.settings_kernel_umount)
                        )
                    },
                    title = resources.getString(R.string.settings_kernel_umount),
                    summary = umountSummary,
                    checked = uiState.isKernelUmountEnabled,
                    enabled = uiState.kernelUmountStatus == "supported",
                ) { checked ->
                    viewModel.setKernelUmountEnabled(checked)
                    switchFeedback(checked)
                }
            }

            SegmentedListGroup {
                switchItem(
                    visible = ksuIsValid(),
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.ic_folder_delete_rounded_filled), stringResource(R.string.settings_umount_modules_default)
                        )
                    },
                    title = resources.getString(R.string.settings_umount_modules_default),
                    summary = resources.getString(R.string.settings_umount_modules_default_summary),
                    checked = uiState.isDefaultUmountModules
                ) {
                    viewModel.setDefaultUmountModules(it)
                    switchFeedback(it)
                }

                switchItem(
                    visible = ksuIsValid(),
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.ic_mobile_code_rounded_filled), stringResource(
                                id = R.string.enable_web_debugging
                            )
                        )
                    },
                    title = resources.getString(R.string.enable_web_debugging),
                    summary = resources.getString(R.string.enable_web_debugging_summary),
                    checked = uiState.enableWebDebugging
                ) {
                    switchFeedback(it)
                    viewModel.setEnableWebDebugging(it)
                }
            }

            if (Natives.isLkmMode) {
                val uninstallDialog = uninstallDialog()
                SegmentedListGroup {
                    item(
                        leadingContent = { Icon(Icons.Filled.Delete, contentDescription = stringResource(id = R.string.settings_uninstall)) },
                        onClick = { uninstallDialog.show() }) {
                        Text(stringResource(id = R.string.settings_uninstall))
                    }
                }
            }

            val sendLogBottomSheet = sendLogBottomSheet()
            val aboutDialog = rememberCustomDialog {
                AboutDialog(it)
            }

            SegmentedListGroup {
                item(leadingContent = {
                    Icon(
                        painterResource(R.drawable.ic_bug_report_rounded_filled), stringResource(id = R.string.send_log)
                    )
                }, onClick = { sendLogBottomSheet.show() }) { Text(stringResource(id = R.string.send_log)) }

                item(leadingContent = {
                    Icon(
                        painterResource(R.drawable.ic_contact_page_rounded_filled), stringResource(id = R.string.about)
                    )
                }, onClick = { aboutDialog.show() }) { Text(stringResource(id = R.string.about)) }
            }
        }
    }
}