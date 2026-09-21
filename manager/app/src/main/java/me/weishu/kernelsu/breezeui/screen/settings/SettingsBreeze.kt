package me.weishu.kernelsu.breezeui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SelectableDropdownMenuItem
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.R.string.settings
import me.weishu.kernelsu.breezeui.BreezeMode
import me.weishu.kernelsu.breezeui.component.bar.BreezeTopAppBar
import me.weishu.kernelsu.breezeui.component.dialog.UninstallDialogBreeze
import me.weishu.kernelsu.breezeui.component.feedback.BreezeSnackBarHost
import me.weishu.kernelsu.breezeui.component.list.SegmentedListGroup
import me.weishu.kernelsu.breezeui.component.sheet.SendLogBottomSheetBreeze
import me.weishu.kernelsu.breezeui.icons.MaterialSymbols
import me.weishu.kernelsu.breezeui.nav.isRailNavbar
import me.weishu.kernelsu.breezeui.util.onlyHorizontal
import me.weishu.kernelsu.ui.component.material.ExpressiveScaffold
import me.weishu.kernelsu.ui.screen.settings.SettingsScreenActions
import me.weishu.kernelsu.ui.screen.settings.SettingsUiState

@Composable
fun SettingPagerBreeze(
    uiState: SettingsUiState,
    actions: SettingsScreenActions,
    bottomInnerPadding: Dp,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val hazeState = rememberHazeState()
    val snackBarHost = remember { SnackbarHostState() }
    val ksuVersion = if (Natives.isManager) Natives.version else null
    val isKsuValid = ksuVersion != null
    var showUninstallDialog by rememberSaveable { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }

    UninstallDialogBreeze(
        show = showUninstallDialog,
        onDismissRequest = { showUninstallDialog = false },
    )

    ExpressiveScaffold(
        topBar = {
            BreezeTopAppBar(
                title = { Text(stringResource(settings)) },
                scrollBehavior = scrollBehavior,
                hazeState = hazeState
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
            val uninstall = stringResource(id = R.string.settings_uninstall)

            SegmentedListGroup {
                switchItem(
                    leadingContent = {
                        Icon(
                            imageVector = MaterialSymbols.Filled.Rounded.SystemUpdate,
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
                            imageVector = MaterialSymbols.Filled.Rounded.SystemUpdateAlt,
                            contentDescription = settingsModuleCheckUpdate
                        )
                    },
                    title = settingsModuleCheckUpdate,
                    summary = settingsCheckUpdateSummary,
                    checked = { uiState.checkModuleUpdate },
                    onCheckedChange = actions.onSetCheckModuleUpdate
                )
            }

            val currentUiModeName = when (BreezeMode.currentMode()) {
                BreezeMode.MIUIX -> UiModeOrder[UiModeIndex.MIUIX]
                BreezeMode.MATERIAL -> UiModeOrder[UiModeIndex.MATERIAL]
                else -> UiModeOrder[UiModeIndex.BREEZE]
            }
            val uiModes = UiModeOrder
            SegmentedListGroup {
                menuItem(
                    content = { Text(stringResource(id = R.string.settings_ui_mode)) },
                    selected = { currentUiModeName },
                    leadingContent = {
                        Icon(
                            MaterialSymbols.Filled.Rounded.DisplaySettings,
                            contentDescription = stringResource(id = R.string.settings_ui_mode)
                        )
                    },
                    supportingContent = { Text(stringResource(id = R.string.settings_ui_mode_summary)) },
                    menuContent = { dismissMenu ->
                        DropdownMenuGroup(shapes = MenuDefaults.groupShapes()) {
                            uiModes.forEachIndexed { index, name ->
                                SelectableDropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        actions.onSetUiModeIndex(index)
                                        dismissMenu()
                                    },
                                    shapes = MenuDefaults.itemShape(index = index, count = uiModes.size),
                                    selected = name == currentUiModeName,
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
                            MaterialSymbols.Filled.Rounded.Palette,
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
                        leadingContent = { Icon(MaterialSymbols.Filled.Rounded.Description, profileTemplate) },
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

                val settingsSoftReboot = stringResource(id = R.string.settings_soft_reboot)
                val settingsSoftRebootSummary = stringResource(id = R.string.settings_soft_reboot_summary)

                SegmentedListGroup {
                    menuItem(
                        content = { Text(stringResource(id = R.string.settings_sucompat)) },
                        selected = { suCompatModeItems.getOrNull(uiState.suCompatMode) },
                        enabled = uiState.suCompatStatus == "supported",
                        leadingContent = {
                            Icon(
                                imageVector = MaterialSymbols.Filled.Rounded.AdminPanelSettings,
                                contentDescription = stringResource(id = R.string.settings_sucompat)
                            )
                        },
                        supportingContent = { Text(suSummary) },
                        menuContent = { dismissMenu ->
                            DropdownMenuGroup(shapes = MenuDefaults.groupShapes()) {
                                suCompatModeItems.forEachIndexed { index, name ->
                                    SelectableDropdownMenuItem(
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
                                MaterialSymbols.Filled.Rounded.LayersClear,
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
                                MaterialSymbols.Filled.Rounded.Security,
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
                                imageVector = MaterialSymbols.Filled.Rounded.Article,
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
                                imageVector = MaterialSymbols.Filled.Rounded.Adb,
                                contentDescription = settingsAdbRoot
                            )
                        },
                        title = settingsAdbRoot,
                        summary = adbRootSummary,
                        checked = { uiState.isAdbRootEnabled },
                        onCheckedChange = actions.onSetAdbRootEnabled
                    )

                    switchItem(
                        leadingContent = {
                            Icon(
                                imageVector = MaterialSymbols.Filled.Rounded.RestartAlt,
                                contentDescription = settingsSoftReboot
                            )
                        },
                        title = settingsSoftReboot,
                        summary = settingsSoftRebootSummary,
                        enabled = !uiState.isLateLoadMode,
                        checked = { uiState.isLateLoadMode || uiState.useSoftReboot },
                        onCheckedChange = actions.onSetUseSoftReboot
                    )

                }

                SegmentedListGroup {
                    switchItem(
                        leadingContent = {
                            Icon(
                                imageVector = MaterialSymbols.Filled.Rounded.Rule,
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
                                imageVector = MaterialSymbols.Filled.Rounded.DeveloperMode,
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
                                imageVector = MaterialSymbols.Filled.Rounded.FlashOn,
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

            SegmentedListGroup {
                item(
                    onClick = { showUninstallDialog = true },
                    enabled = !uiState.isLateLoadMode && uiState.isLkmMode,
                    leadingContent = {
                        Icon(
                            imageVector = MaterialSymbols.Filled.Rounded.Delete,
                            contentDescription = uninstall
                        )
                    },
                    content = { Text(uninstall) }
                )
            }

            SegmentedListGroup {
                item(
                    onClick = { showBottomSheet = true },
                    leadingContent = {
                        Icon(
                            imageVector = MaterialSymbols.Filled.Rounded.BugReport,
                            contentDescription = stringResource(id = R.string.send_log)
                        )
                    },
                    content = { Text(stringResource(id = R.string.send_log)) }
                )

                item(
                    onClick = actions.onOpenAbout,
                    leadingContent = {
                        Icon(
                            imageVector = MaterialSymbols.Filled.Rounded.Info,
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

/** Dropdown order of the UI mode selector; indices match [UiModeIndex]. */
private val UiModeOrder = listOf("Miuix", "Material", "Breeze")
