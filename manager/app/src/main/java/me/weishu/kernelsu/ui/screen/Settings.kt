package me.weishu.kernelsu.ui.screen

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.edit
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.SegmentedListGroup
import me.weishu.kernelsu.ui.component.ksuIsValid
import me.weishu.kernelsu.ui.component.popUps.AboutDialog
import me.weishu.kernelsu.ui.component.popUps.sendLogBottomSheet
import me.weishu.kernelsu.ui.component.popUps.uninstallDialog
import me.weishu.kernelsu.ui.component.rememberCustomDialog
import me.weishu.kernelsu.ui.navigation3.Route
import me.weishu.kernelsu.ui.theme.defaultTopAppBarColors
import me.weishu.kernelsu.ui.util.LocalNavController
import me.weishu.kernelsu.ui.util.LocalSnackbarHost
import me.weishu.kernelsu.ui.util.execKsud
import me.weishu.kernelsu.ui.util.getFeaturePersistValue
import me.weishu.kernelsu.ui.util.getFeatureStatus

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingScreen() {
    val navigator = LocalNavController.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val snackBarHost = LocalSnackbarHost.current
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

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
        snackbarHost = { SnackbarHost(snackBarHost) },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {

            var checkUpdate by rememberSaveable {
                mutableStateOf(
                    prefs.getBoolean("check_update", true)
                )
            }

            var checkModuleUpdate by rememberSaveable {
                mutableStateOf(prefs.getBoolean("module_check_update", true))
            }

            SegmentedListGroup {
                switchItem(
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.ic_update_rounded_filled), contentDescription = stringResource(id = R.string.settings_check_update)
                        )
                    },
                    title = { stringResource(id = R.string.settings_check_update) },
                    summary = { stringResource(id = R.string.settings_check_update_summary) },
                    checked = checkUpdate,
                    onCheckedChange = {
                        prefs.edit { putBoolean("check_update", it) }
                        checkUpdate = it
                    })

                switchItem(
                    visible = ksuIsValid(),
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.ic_upload_file_rounded_filled), contentDescription = stringResource(id = R.string.settings_check_update)
                        )
                    },
                    title = { stringResource(id = R.string.settings_module_check_update) },
                    summary = { stringResource(id = R.string.settings_check_update_summary) },
                    checked = checkModuleUpdate
                ) {
                    prefs.edit {
                        putBoolean("module_check_update", it)
                    }
                    checkModuleUpdate = it
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

            val currentSuEnabled = Natives.isSuEnabled()
            var suCompatMode by rememberSaveable { mutableIntStateOf(if (!currentSuEnabled) 1 else 0) }
            val suPersistValue by produceState(initialValue = null as Long?) {
                value = getFeaturePersistValue("su_compat")
            }
            LaunchedEffect(suPersistValue) {
                suPersistValue?.let { v ->
                    suCompatMode = if (v == 0L) 2 else if (!currentSuEnabled) 1 else 0
                }
            }

            val suStatus by produceState(initialValue = "supported") {
                value = getFeatureStatus("su_compat")
            }
            val suSummary = when (suStatus) {
                "unsupported" -> stringResource(id = R.string.feature_status_unsupported_summary)
                "managed" -> stringResource(id = R.string.feature_status_managed_summary)
                else -> stringResource(id = R.string.settings_sucompat_summary)
            }
            val suCompatModeItems = listOf(
                stringResource(id = R.string.settings_mode_enable_by_default),
                stringResource(id = R.string.settings_mode_disable_until_reboot),
                stringResource(id = R.string.settings_mode_disable_always),
            )

            var isKernelUmountEnabled by rememberSaveable { mutableStateOf(Natives.isKernelUmountEnabled()) }
            val umountStatus by produceState(initialValue = "supported") {
                value = getFeatureStatus("kernel_umount")
            }

            val umountSummary = when (umountStatus) {
                "unsupported" -> stringResource(id = R.string.feature_status_unsupported_summary)
                "managed" -> stringResource(id = R.string.feature_status_managed_summary)
                else -> stringResource(id = R.string.settings_kernel_umount_summary)
            }

            SegmentedListGroup {
                menuItem(
                    visible = ksuIsValid(),
                    content = { Text(stringResource(id = R.string.settings_sucompat)) },
                    selected = suCompatModeItems.getOrNull(suCompatMode),
                    enabled = suStatus == "supported",
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.ic_remove_moderator_outlined_filled), stringResource(id = R.string.settings_sucompat)
                        )
                    },
                    supportingContent = { Text(suSummary) }) { dismissMenu ->
                    suCompatModeItems.forEachIndexed { index, name ->
                        DropdownMenuItem(text = { Text(name) }, onClick = {
                            when (index) {
                                // Default: enable and save to persist
                                0 -> if (Natives.setSuEnabled(true)) {
                                    execKsud("feature save", true)
                                    prefs.edit { putInt("su_compat_mode", 0) }
                                    suCompatMode = 0
                                }

                                // Temporarily disable: save enabled state first, then disable
                                1 -> if (Natives.setSuEnabled(true)) {
                                    execKsud("feature save", true)
                                    if (Natives.setSuEnabled(false)) {
                                        prefs.edit { putInt("su_compat_mode", 0) }
                                        suCompatMode = 1
                                    }
                                }

                                // Permanently disable: disable and save
                                2 -> if (Natives.setSuEnabled(false)) {
                                    execKsud("feature save", true)
                                    prefs.edit { putInt("su_compat_mode", 2) }
                                    suCompatMode = 2
                                }
                            }
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
                    title = { stringResource(id = R.string.settings_kernel_umount) },
                    summary = { umountSummary },
                    checked = isKernelUmountEnabled,
                    enabled = umountStatus == "supported",
                ) { checked ->
                    if (Natives.setKernelUmountEnabled(checked)) {
                        execKsud("feature save", true)
                        isKernelUmountEnabled = checked
                    }
                }
            }

            var umountChecked by rememberSaveable {
                mutableStateOf(Natives.isDefaultUmountModules())
            }

            var enableWebDebugging by rememberSaveable {
                mutableStateOf(
                    prefs.getBoolean("enable_web_debugging", false)
                )
            }

            SegmentedListGroup {
                switchItem(
                    visible = ksuIsValid(),
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.ic_folder_delete_rounded_filled), stringResource(R.string.settings_umount_modules_default)
                        )
                    },
                    title = { stringResource(R.string.settings_umount_modules_default) },
                    summary = { stringResource(R.string.settings_umount_modules_default_summary) },
                    checked = umountChecked
                ) {
                    if (Natives.setDefaultUmountModules(it)) {
                        umountChecked = it
                    }
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
                    title = { stringResource(id = R.string.enable_web_debugging) },
                    summary = { stringResource(id = R.string.enable_web_debugging_summary) },
                    checked = enableWebDebugging
                ) {
                    prefs.edit { putBoolean("enable_web_debugging", it) }
                    enableWebDebugging = it
                }
            }

            val uninstallDialog = uninstallDialog()

            SegmentedListGroup {
                item(
                    visible = Natives.isLkmMode,
                    leadingContent = { Icon(Icons.Filled.Delete, contentDescription = stringResource(id = R.string.settings_uninstall)) },
                    onClick = { uninstallDialog.show() }) {
                    Text(stringResource(id = R.string.settings_uninstall))
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