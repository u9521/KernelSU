package me.weishu.kernelsu.breezeui.screen.module

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import me.weishu.kernelsu.R
import me.weishu.kernelsu.breezeui.component.dialog.InstallModuleDialog
import me.weishu.kernelsu.breezeui.nav.BreezeRoute
import me.weishu.kernelsu.breezeui.nav.LocalBreezeNavigator
import me.weishu.kernelsu.ui.component.SearchStatus
import me.weishu.kernelsu.ui.screen.flash.FlashIt
import me.weishu.kernelsu.ui.screen.module.ModuleActions
import me.weishu.kernelsu.ui.screen.module.ModuleEffect
import me.weishu.kernelsu.ui.screen.module.ShortcutType
import me.weishu.kernelsu.ui.util.download
import me.weishu.kernelsu.ui.util.module.Shortcut
import me.weishu.kernelsu.ui.viewmodel.ModuleViewModel
import me.weishu.kernelsu.ui.webui.WebUIActivity

/**
 * Breeze's module page.
 *
 * A finished module update is not pushed straight to the flash screen here: Breeze
 * parks the downloaded URI and shows its own install dialog first, so the user sees
 * what the archive contains before flashing.
 */
@Composable
fun ModuleEntry(
    bottomInnerPadding: Dp,
    isCurrentPage: Boolean = true,
) {
    val navigator = LocalBreezeNavigator.current
    val context = LocalContext.current
    val resource = LocalResources.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel = viewModel<ModuleViewModel>()
    val scope = rememberCoroutineScope()
    val rawUiState by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingInstallModuleUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val webUILauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { viewModel.fetchModuleList(resort = false) }

    // Request notification permission for download progress notifications
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Download works regardless of result */ }
    val latestIsCurrentPage by rememberUpdatedState(isCurrentPage)
    val initialResumeHandled = rememberSaveable { mutableStateOf(false) }

    var hasActivated by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(isCurrentPage) {
        if (isCurrentPage) {
            if (!hasActivated) {
                hasActivated = true
                viewModel.refreshEnvironmentState()
                viewModel.initializePreferences()
                val state = viewModel.uiState.value
                if (!state.hasLoaded && !state.isRefreshing) {
                    viewModel.fetchModuleList()
                }
                if (Build.VERSION.SDK_INT >= 33) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else if (!rawUiState.searchStatus.isCollapsed()) {
            viewModel.updateSearchStatus(rawUiState.searchStatus.copy(searchText = "", current = SearchStatus.Status.COLLAPSED))
        }
    }

    LifecycleResumeEffect(Unit) {
        if (initialResumeHandled.value && latestIsCurrentPage) {
            val state = viewModel.uiState.value
            if (!state.isRefreshing) {
                viewModel.fetchModuleList(
                    checkUpdate = !state.hasLoaded || viewModel.isNeedRefresh,
                    resort = !state.hasLoaded,
                )
            }
        }
        initialResumeHandled.value = true
        onPauseOrDispose {}
    }

    val actions = ModuleActions(
        onRefresh = {
            viewModel.fetchModuleList(checkUpdate = true)
        },
        onSearchStatusChange = {
            viewModel.updateSearchStatus(it)
        },
        onSearchTextChange = { text ->
            viewModel.updateSearchText(text)
        },
        onClearSearch = {
            viewModel.updateSearchText("")
        },
        onRequestUpdateConfirmation = { module, updateInfo ->
            viewModel.requestUpdateConfirmation(module, updateInfo)
        },
        onRequestUninstallConfirmation = { module ->
            viewModel.requestUninstallConfirmation(module)
        },
        onDismissConfirmRequest = {
            viewModel.dismissConfirmRequest()
        },
        onConfirmUpdate = { request ->
            scope.launch {
                download(
                    url = request.downloadUrl,
                    fileName = request.fileName,
                    onDownloaded = { uri ->
                        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                            pendingInstallModuleUri = uri
                        } else {
                            navigator.push(BreezeRoute.Flash(FlashIt.FlashModules(listOf(uri))))
                            viewModel.markNeedRefresh()
                        }
                    },
                    onDownloading = {
                        viewModel.emitEffect(
                            ModuleEffect.Toast(
                                resource.getString(R.string.module_downloading).format(request.module.name)
                            )
                        )
                    },
                )
            }
            viewModel.dismissConfirmRequest()
        },
        onOpenRepo = { },
        onToggleSortActionFirst = {
            viewModel.toggleSortActionFirst()
        },
        onToggleSortEnabledFirst = {
            viewModel.toggleSortEnabledFirst()
        },
        onOpenWebUi = { module ->
            webUILauncher.launch(
                Intent(context, WebUIActivity::class.java)
                    .setData(
                        Shortcut.buildShortcutUri(module.id, ShortcutType.WebUI)
                    )
            )
        },
        onToggleModule = { module ->
            viewModel.toggleModule(module)
        },
        onUninstallModule = { module ->
            viewModel.uninstallModule(module)
        },
        onUndoUninstallModule = { module ->
            viewModel.undoUninstallModule(module)
        },
        onOpenFlash = { uris ->
            if (uris.isNotEmpty()) {
                navigator.push(BreezeRoute.Flash(FlashIt.FlashModules(uris)))
                viewModel.markNeedRefresh()
            }
        },
        onExecuteModuleAction = { module ->
            navigator.push(BreezeRoute.ExecuteModuleAction(module.id))
            viewModel.markNeedRefresh()
        },
    )

    ModulePagerBreeze(
        uiState = rawUiState,
        confirmDialogState = rawUiState.confirmDialogState,
        moduleEvent = viewModel.moduleEvent,
        actions = actions,
        bottomInnerPadding = bottomInnerPadding,
    )

    InstallModuleDialog(
        uris = pendingInstallModuleUri?.let { listOf(it) } ?: emptyList(),
        getInstalledModules = { rawUiState.moduleList },
        onConfirmInstall = {
            pendingInstallModuleUri?.let { uri ->
                navigator.push(BreezeRoute.Flash(FlashIt.FlashModules(listOf(uri))))
                viewModel.markNeedRefresh()
            }
            pendingInstallModuleUri = null
        },
        onDismiss = { pendingInstallModuleUri = null }
    )
}
