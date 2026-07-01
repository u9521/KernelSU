package me.weishu.kernelsu.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.UiMode
import me.weishu.kernelsu.ui.component.breeze.BreezeSnackBarHost
import me.weishu.kernelsu.ui.component.breeze.SegmentedListGroup
import me.weishu.kernelsu.ui.component.material.ExpressiveScaffold
import me.weishu.kernelsu.ui.component.material.SendLogBottomSheetBreeze
import me.weishu.kernelsu.ui.component.material.disableDrag
import me.weishu.kernelsu.ui.component.material.expressiveTopBarColors
import me.weishu.kernelsu.ui.component.uninstalldialog.UninstallDialog
import me.weishu.kernelsu.ui.navigation3.breeze.isRailNavbar
import me.weishu.kernelsu.ui.util.onlyHorizontal
import me.weishu.kernelsu.ui.util.topBarHazeEffect

/**
 * @author weishu
 * @date 2023/1/1.
 */
@Composable
fun SettingPagerBreeze(
    uiState: SettingsUiState,
    actions: SettingsScreenActions,
    bottomInnerPadding: Dp,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior().disableDrag()
    val hazeState = rememberHazeState()
    val snackBarHost = remember { SnackbarHostState() }
    val ksuVersion = if (Natives.isManager) Natives.version else null
    val isKsuValid = ksuVersion != null
    val showUninstallDialog = rememberSaveable { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }

    UninstallDialog(
        show = showUninstallDialog.value,
        onDismissRequest = { showUninstallDialog.value = false }
    )

    ExpressiveScaffold(
        topBar = {
            TopBar(
                scrollBehavior = scrollBehavior,
                hazeState = hazeState,
            )
        },
        snackbarHost = {
            BreezeSnackBarHost(
                hostState = snackBarHost, modifier = Modifier.padding(
                    bottom = bottomInnerPadding
                ).let { if (isRailNavbar()) it.safeDrawingPadding() else it }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding.onlyHorizontal())
                .hazeSource(hazeState)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))

            val suCompatModeItems = listOf(
                stringResource(id = R.string.settings_mode_enable_by_default),
                stringResource(id = R.string.settings_mode_disable_until_reboot),
                stringResource(id = R.string.settings_mode_disable_always),
            )
            val settingsCheckUpdate = stringResource(id = R.string.settings_check_update)
            val settingsCheckUpdateSummary = stringResource(id = R.string.settings_check_update_summary)
            val settingsModuleCheckUpdate = stringResource(id = R.string.settings_module_check_update)
            val settingsKernelUmount = stringResource(id = R.string.settings_kernel_umount)
            val settingsSelinuxHide = stringResource(id = R.string.settings_selinux_hide)
            val settingsSULog = stringResource(id = R.string.settings_sulog)
            val settingsAdbRoot = stringResource(id = R.string.settings_adb_root)
            val settingsUmountModules = stringResource(id = R.string.settings_umount_modules_default)
            val settingsUmountModulesDefaultSummary = stringResource(id = R.string.settings_umount_modules_default_summary)
            val enableWebDebugging = stringResource(id = R.string.enable_web_debugging)
            val enableWebDebuggingSummary = stringResource(id = R.string.enable_web_debugging_summary)
            val settingsAutoJailbreak = stringResource(id = R.string.settings_auto_jailbreak)
            val settingsAutoJailbreakSummary = stringResource(id = R.string.settings_auto_jailbreak_summary)

            SegmentedListGroup {
                switchItem(
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_bug_report_rounded_filled),
                            contentDescription = settingsCheckUpdate
                        )
                    },
                    title = settingsCheckUpdate,
                    summary = settingsCheckUpdateSummary,
                    checked = { uiState.checkUpdate },
                    onCheckedChange = actions.onSetCheckUpdate
                )

                switchItem(
                    visible = isKsuValid,
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_upload_file_rounded_filled),
                            contentDescription = settingsModuleCheckUpdate
                        )
                    },
                    title = settingsModuleCheckUpdate,
                    summary = settingsCheckUpdateSummary,
                    checked = { uiState.checkModuleUpdate },
                    onCheckedChange = actions.onSetCheckModuleUpdate
                )
            }

            val uiModes = UiMode.entries.map { it.name }
            SegmentedListGroup {
                menuItem(
                    content = { Text(stringResource(id = R.string.settings_ui_mode)) },
                    selected = { UiMode.fromValue(uiState.uiMode).name },
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.ic_dashboard_rounded_filled),
                            contentDescription = stringResource(id = R.string.settings_ui_mode)
                        )
                    },
                    supportingContent = { Text(stringResource(id = R.string.settings_ui_mode_summary)) },
                    menuContent = { dismissMenu ->
                        DropdownMenuGroup(shapes = MenuDefaults.groupShapes()) {
                            uiModes.forEachIndexed { index, name ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        actions.onSetUiModeIndex(index)
                                        dismissMenu()
                                    },
                                    shapes = MenuDefaults.itemShape(index = index, count = uiModes.size),
                                    selected = name == UiMode.fromValue(uiState.uiMode).name,
                                    selectedLeadingIcon = {
                                        Icon(
                                            Icons.Filled.Check,
                                            modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                                            contentDescription = null,
                                        )
                                    }
                                )
                            }
                        }
                    }
                )

                item(
                    onClick = actions.onOpenTheme,
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.ic_palette_rounded_filled),
                            contentDescription = stringResource(id = R.string.settings_theme)
                        )
                    },
                    supportingContent = { Text(stringResource(id = R.string.settings_theme_summary)) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
                    content = { Text(stringResource(id = R.string.settings_theme)) }
                )
            }

            val profileTemplate = stringResource(id = R.string.settings_profile_template)
            if (isKsuValid) {
                SegmentedListGroup {
                    item(
                        onClick = actions.onOpenProfileTemplate,
                        leadingContent = { Icon(painterResource(R.drawable.ic_fence_rounded), profileTemplate) },
                        supportingContent = { Text(stringResource(id = R.string.settings_profile_template_summary)) },
                        trailingContent = {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                null
                            )
                        },
                        content = { Text(profileTemplate) }
                    )
                }
            }

            if (isKsuValid) {
                val suSummary = when (uiState.suCompatStatus) {
                    "unsupported" -> stringResource(id = R.string.feature_status_unsupported_summary)
                    "managed" -> stringResource(id = R.string.feature_status_managed_summary)
                    else -> stringResource(id = R.string.settings_sucompat_summary)
                }

                val umountSummary = when (uiState.kernelUmountStatus) {
                    "unsupported" -> stringResource(id = R.string.feature_status_unsupported_summary)
                    "managed" -> stringResource(id = R.string.feature_status_managed_summary)
                    else -> stringResource(id = R.string.settings_kernel_umount_summary)
                }
                val selinuxHideSummary = when (uiState.selinuxHideStatus) {
                    "unsupported" -> stringResource(id = R.string.feature_status_unsupported_summary)
                    "managed" -> stringResource(id = R.string.feature_status_managed_summary)
                    else -> stringResource(id = R.string.settings_selinux_hide_summary)
                }

                val sulogSummary = when (uiState.sulogStatus) {
                    "unsupported" -> stringResource(id = R.string.feature_status_unsupported_summary)
                    "managed" -> stringResource(id = R.string.feature_status_managed_summary)
                    else -> stringResource(id = R.string.settings_sulog_summary)
                }

                val adbRootSummary = when (uiState.adbRootStatus) {
                    "unsupported" -> stringResource(id = R.string.feature_status_unsupported_summary)
                    "managed" -> stringResource(id = R.string.feature_status_managed_summary)
                    else -> stringResource(id = R.string.settings_adb_root_summary)
                }

                SegmentedListGroup {
                    menuItem(
                        content = { Text(stringResource(id = R.string.settings_sucompat)) },
                        selected = { suCompatModeItems.getOrNull(uiState.suCompatMode) },
                        enabled = uiState.suCompatStatus == "supported",
                        leadingContent = {
                            Icon(
                                painter = painterResource(R.drawable.ic_remove_moderator_outlined_filled),
                                contentDescription = stringResource(id = R.string.settings_sucompat)
                            )
                        },
                        supportingContent = { Text(suSummary) },
                        menuContent = { dismissMenu ->
                            DropdownMenuGroup(shapes = MenuDefaults.groupShapes()) {
                                suCompatModeItems.forEachIndexed { index, name ->
                                    DropdownMenuItem(
                                        text = { Text(name) },
                                        onClick = {
                                            actions.onSetSuCompatMode(index)
                                            dismissMenu()
                                        },
                                        shapes = MenuDefaults.itemShape(index = index, count = suCompatModeItems.size),
                                        selected = index == uiState.suCompatMode,
                                        selectedLeadingIcon = {
                                            Icon(
                                                Icons.Filled.Check,
                                                modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                                                contentDescription = null,
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    )

                    switchItem(
                        leadingContent = {
                            Icon(
                                painterResource(R.drawable.ic_do_not_disturb_on_rounded_filled),
                                contentDescription = settingsKernelUmount
                            )
                        },
                        title = settingsKernelUmount,
                        summary = umountSummary,
                        enabled = uiState.kernelUmountStatus == "supported",
                        checked = { uiState.isKernelUmountEnabled },
                        onCheckedChange = actions.onSetKernelUmountEnabled
                    )

                    switchItem(
                        leadingContent = {
                            Icon(
                                painterResource(R.drawable.ic_policy_rounded_filled),
                                contentDescription = settingsSelinuxHide
                            )
                        },
                        title = settingsSelinuxHide,
                        summary = selinuxHideSummary,
                        enabled = uiState.selinuxHideStatus == "supported",
                        checked = { uiState.isSelinuxHideEnabled },
                        onCheckedChange = actions.onSetSelinuxHideEnabled
                    )

                    switchItem(
                        leadingContent = {
                            Icon(
                                painter = painterResource(R.drawable.ic_article_rounded_filled),
                                contentDescription = settingsSULog
                            )
                        },
                        title = settingsSULog,
                        summary = sulogSummary,
                        enabled = uiState.sulogStatus == "supported",
                        checked = { uiState.isSulogEnabled },
                        onCheckedChange = actions.onSetSulogEnabled
                    )

                    switchItem(
                        leadingContent = {
                            Icon(
                                painter = painterResource(R.drawable.ic_adb_rounded),
                                contentDescription = settingsAdbRoot
                            )
                        },
                        title = settingsAdbRoot,
                        summary = adbRootSummary,
                        checked = { uiState.isAdbRootEnabled },
                        onCheckedChange = actions.onSetAdbRootEnabled
                    )

                }

                SegmentedListGroup {
                    switchItem(
                        leadingContent = {
                            Icon(
                                painter = painterResource(R.drawable.ic_folder_delete_rounded_filled),
                                contentDescription = settingsUmountModules
                            )
                        },
                        title = settingsUmountModules,
                        summary = settingsUmountModulesDefaultSummary,
                        checked = { uiState.isDefaultUmountModules },
                        onCheckedChange = actions.onSetDefaultUmountModules
                    )

                    switchItem(
                        leadingContent = {
                            Icon(
                                painter = painterResource(R.drawable.ic_mobile_code_rounded_filled),
                                contentDescription = enableWebDebugging
                            )
                        },
                        title = enableWebDebugging,
                        summary = enableWebDebuggingSummary,
                        checked = { uiState.enableWebDebugging },
                        onCheckedChange = actions.onSetEnableWebDebugging
                    )

                    switchItem(
                        leadingContent = {
                            Icon(
                                painter = painterResource(R.drawable.ic_electrical_services_rounded),
                                contentDescription = settingsAutoJailbreak
                            )
                        },
                        title = settingsAutoJailbreak,
                        summary = settingsAutoJailbreakSummary,
                        enabled = uiState.isLateLoadMode,
                        checked = { uiState.autoJailbreak },
                        onCheckedChange = actions.onSetAutoJailbreak
                    )
                }
            }

            if (uiState.isLkmMode) {
                val uninstall = stringResource(id = R.string.settings_uninstall)
                SegmentedListGroup(
                    modifier = Modifier.padding(vertical = 8.dp),
                ) {
                    item(
                        onClick = { showUninstallDialog.value = true },
                        enabled = !uiState.isLateLoadMode,
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = uninstall
                            )
                        },
                        content = { Text(uninstall) }
                    )
                }
            }

            SegmentedListGroup {
                item(
                    onClick = { showBottomSheet = true },
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_bug_report_rounded_filled),
                            contentDescription = stringResource(id = R.string.send_log)
                        )
                    },
                    content = { Text(stringResource(id = R.string.send_log)) }
                )

                item(
                    onClick = actions.onOpenAbout,
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_contact_page_rounded_filled),
                            contentDescription = stringResource(id = R.string.about)
                        )
                    },
                    content = { Text(stringResource(id = R.string.about)) }
                )
            }

            if (showBottomSheet) {
                SendLogBottomSheetBreeze(
                    snackBarHost = snackBarHost
                ) { showBottomSheet = false }
            }

            Spacer(
                modifier = Modifier
                    .navigationBarsPadding()
                    .height(bottomInnerPadding)
            )
        }
    }
}

@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior? = null,
    hazeState: HazeState,
) {
    LargeFlexibleTopAppBar(
        modifier = Modifier.topBarHazeEffect(hazeState, scrollBehavior),
        title = { Text(stringResource(R.string.settings)) },
        colors = expressiveTopBarColors(),
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        scrollBehavior = scrollBehavior
    )
}
