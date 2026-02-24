package me.weishu.kernelsu.ui.screen

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ksuApp
import me.weishu.kernelsu.ui.component.BreezeSnackBarHost
import me.weishu.kernelsu.ui.component.ConfirmResult
import me.weishu.kernelsu.ui.component.SearchAppBar
import me.weishu.kernelsu.ui.component.SearchStatus
import me.weishu.kernelsu.ui.component.StatusTag
import me.weishu.kernelsu.ui.component.module.ActionButton
import me.weishu.kernelsu.ui.component.module.ButtonGroup
import me.weishu.kernelsu.ui.component.module.ButtonSpec
import me.weishu.kernelsu.ui.component.module.ButtonType
import me.weishu.kernelsu.ui.component.module.EnumeratedPriorityButtonRow
import me.weishu.kernelsu.ui.component.popUps.RebootListPopup
import me.weishu.kernelsu.ui.component.rememberConfirmDialog
import me.weishu.kernelsu.ui.component.rememberLoadingDialog
import me.weishu.kernelsu.ui.navigation3.NavController
import me.weishu.kernelsu.ui.navigation3.Route
import me.weishu.kernelsu.ui.util.DownloadListener
import me.weishu.kernelsu.ui.util.LocalNavController
import me.weishu.kernelsu.ui.util.LocalSnackbarHost
import me.weishu.kernelsu.ui.util.ModuleParser
import me.weishu.kernelsu.ui.util.download
import me.weishu.kernelsu.ui.util.getFileName
import me.weishu.kernelsu.ui.util.hasMagisk
import me.weishu.kernelsu.ui.util.reboot
import me.weishu.kernelsu.ui.util.toggleModule
import me.weishu.kernelsu.ui.util.undoUninstallModule
import me.weishu.kernelsu.ui.util.uninstallModule
import me.weishu.kernelsu.ui.viewmodel.ModuleViewModel
import me.weishu.kernelsu.ui.viewmodel.ModuleViewModel.ModuleInfo
import me.weishu.kernelsu.ui.viewmodel.ModuleViewModel.ModuleUpdateInfo
import me.weishu.kernelsu.ui.webui.WebUIActivity
import okhttp3.Request

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleScreen() {
    val navigator = LocalNavController.current
    val viewModel = viewModel<ModuleViewModel>()
    val context = LocalContext.current
    val snackBarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val searchStatus by viewModel.searchStatus


    LaunchedEffect(Unit) {
        when {
            viewModel.moduleList.isEmpty() -> {
                viewModel.checkModuleUpdate = prefs.getBoolean("module_check_update", true)
                viewModel.sortEnabledFirst = prefs.getBoolean("module_sort_enabled_first", false)
                viewModel.sortActionFirst = prefs.getBoolean("module_sort_action_first", false)
                viewModel.fetchModuleList()
            }

            viewModel.isNeedRefresh -> {
                viewModel.fetchModuleList()
            }
        }
    }

    LaunchedEffect(searchStatus.searchText, viewModel.moduleList) {
        viewModel.updateSearchText(searchStatus.searchText)
    }

    val isSafeMode = Natives.isSafeMode
    val magiskInstalled by produceState(initialValue = false) {
        value = withContext(Dispatchers.IO) { hasMagisk() }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val webUILauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { viewModel.fetchModuleList() }

    val loadingDialog = rememberLoadingDialog()

    Scaffold(
        topBar = {
            SearchAppBar(
                title = { Text(stringResource(R.string.module)) },
                searchStatus = searchStatus,
                dropdownContent = {
                    RebootListPopup()
                    ShortByMenuButton(viewModel, prefs)
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = { InstallModuleFAB(visible = !(isSafeMode || magiskInstalled), viewModel = viewModel) },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        snackbarHost = { BreezeSnackBarHost(hostState = snackBarHost) },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { innerPadding ->
        if (magiskInstalled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp), contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.module_magisk_conflict),
                    textAlign = TextAlign.Center,
                )
            }
            return@Scaffold
        }

        var zipUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
        val confirmDialog = rememberConfirmDialog(onConfirm = {
            navigator.navigateTo(Route.Flash(FlashIt.FlashModules(zipUris)))
            viewModel.markNeedRefresh()
        })
        val confirmTitle = stringResource(R.string.module)
        ModuleList(
            navigator,
            viewModel = viewModel,
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            boxModifier = Modifier.padding(innerPadding),
            onInstallModule = {
                zipUris = listOf(it)
                scope.launch {
                    val moduleInstallDesc = loadingDialog.withLoading {
                        withContext(Dispatchers.IO) {
                            ModuleParser.getModuleInstallDesc(context, it, viewModel.moduleList)
                        }
                    }
                    confirmDialog.showConfirm(
                        title = confirmTitle, content = moduleInstallDesc, markdown = true
                    )
                }
            },
            onClickModule = { id, name, hasWebUi ->
                if (hasWebUi) {
                    webUILauncher.launch(
                        Intent(context, WebUIActivity::class.java).setData("kernelsu://webui/$id".toUri()).putExtra("id", id).putExtra("name", name)
                    )
                }
            },
            context = context,
            snackBarHost = snackBarHost
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ModuleList(
    navigator: NavController,
    viewModel: ModuleViewModel,
    modifier: Modifier = Modifier,
    boxModifier: Modifier = Modifier,
    onInstallModule: (Uri) -> Unit,
    onClickModule: (id: String, name: String, hasWebUi: Boolean) -> Unit,
    context: Context,
    snackBarHost: SnackbarHostState
) {
    val failedEnable = stringResource(R.string.module_failed_to_enable)
    val failedDisable = stringResource(R.string.module_failed_to_disable)
    val failedUndoUninstall = stringResource(R.string.module_undo_uninstall_failed)
    val successUndoUninstall = stringResource(R.string.module_undo_uninstall_success)
    val failedUninstall = stringResource(R.string.module_uninstall_failed)
    val successUninstall = stringResource(R.string.module_uninstall_success)
    val reboot = stringResource(R.string.reboot)
    val rebootToApply = stringResource(R.string.reboot_to_apply)
    val moduleStr = stringResource(R.string.module)
    val uninstall = stringResource(R.string.uninstall)
    val cancel = stringResource(android.R.string.cancel)
    val moduleUninstallConfirm = stringResource(R.string.module_uninstall_confirm)
    val metaModuleUninstallConfirm = stringResource(R.string.metamodule_uninstall_confirm)
    val updateText = stringResource(R.string.module_update)
    val changelogText = stringResource(R.string.module_changelog)
    val downloadingText = stringResource(R.string.module_downloading)
    val startDownloadingText = stringResource(R.string.module_start_downloading)

    val loadingDialog = rememberLoadingDialog()
    val confirmDialog = rememberConfirmDialog()

    suspend fun onModuleUpdate(
        module: ModuleInfo, changelogUrl: String, downloadUrl: String, fileName: String
    ) {
        val changelogResult = loadingDialog.withLoading {
            withContext(Dispatchers.IO) {
                runCatching {
                    ksuApp.okhttpClient.newCall(
                        Request.Builder().url(changelogUrl).build()
                    ).execute().body.string()
                }
            }
        }

        val showToast: suspend (String) -> Unit = { msg ->
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context, msg, Toast.LENGTH_SHORT
                ).show()
            }
        }

        val changelog = changelogResult.getOrElse { "" }

        // changelog is not empty, show it and wait for confirm
        val confirmResult = confirmDialog.awaitConfirm(
            changelogText,
            content = changelog,
            markdown = true,
            confirm = updateText,
        )

        if (confirmResult != ConfirmResult.Confirmed) {
            return
        }

        showToast(startDownloadingText.format(module.name))

        val downloading = downloadingText.format(module.name)
        withContext(Dispatchers.IO) {
            download(
                downloadUrl, fileName, onDownloaded = onInstallModule, onDownloading = {
                    launch(Dispatchers.Main) {
                        snackBarHost.showSnackbar(
                            message = downloading, duration = SnackbarDuration.Short
                        )
                    }
                })
        }
    }

    suspend fun onModuleUndoUninstall(module: ModuleInfo) {
        val success = loadingDialog.withLoading {
            withContext(Dispatchers.IO) {
                undoUninstallModule(module.id)
            }
        }

        if (success) {
            viewModel.fetchModuleList()
        }
        val message = if (success) {
            successUndoUninstall.format(module.name)
        } else {
            failedUndoUninstall.format(module.name)
        }
        snackBarHost.showSnackbar(
            message = message, duration = SnackbarDuration.Short
        )
    }


    suspend fun onModuleUninstall(module: ModuleInfo) {
        val formatter = if (module.metamodule) metaModuleUninstallConfirm else moduleUninstallConfirm
        val confirmResult = confirmDialog.awaitConfirm(
            moduleStr, content = formatter.format(module.name), confirm = uninstall, dismiss = cancel
        )
        if (confirmResult != ConfirmResult.Confirmed) {
            return
        }

        val success = loadingDialog.withLoading {
            withContext(Dispatchers.IO) {
                uninstallModule(module.id)
            }
        }

        if (success) {
            viewModel.fetchModuleList()
        }
        val message = if (success) {
            successUninstall.format(module.name)
        } else {
            failedUninstall.format(module.name)
        }
        val actionLabel = if (success) {
            reboot
        } else {
            null
        }
        val result = snackBarHost.showSnackbar(
            message = message, actionLabel = actionLabel, duration = SnackbarDuration.Short
        )
        if (result == SnackbarResult.ActionPerformed) {
            reboot()
        }
    }

    val state = rememberPullToRefreshState()
    PullToRefreshBox(
        modifier = boxModifier, state = state, onRefresh = {
            viewModel.fetchModuleList()
        }, indicator = {
            PullToRefreshDefaults.LoadingIndicator(
                state = state, isRefreshing = viewModel.isRefreshing, modifier = Modifier.align(Alignment.TopCenter)
            )
        }, isRefreshing = viewModel.isRefreshing
    ) {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = remember {
                PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp + 56.dp + 16.dp + 48.dp + 6.dp /* Scaffold Fab Spacing + Fab container height + SnackBar height */
                )
            },
        ) {
            var displayModuleList = viewModel.moduleList
            if (viewModel.searchStatus.value.resultStatus == SearchStatus.ResultStatus.SHOW) {
                displayModuleList = viewModel.searchResults.value
            }
            when {
                viewModel.moduleList.isEmpty() -> {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center
                        ) {
                            if (viewModel.searchStatus.value.resultStatus == SearchStatus.ResultStatus.EMPTY) {
                                Text(
                                    "no modules found", textAlign = TextAlign.Center
                                )
                            } else {
                                Text(
                                    stringResource(R.string.module_empty), textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                else -> {
                    items(displayModuleList) { module ->
                        val scope = rememberCoroutineScope()
                        val updatedModule by produceState(ModuleUpdateInfo.Empty) {
                            scope.launch(Dispatchers.IO) {
                                value = viewModel.checkUpdate(module)
                            }
                        }

                        ModuleItem(module = module, hasUpdate = updatedModule.downloadUrl.isNotEmpty(), onEnableChanged = {
                            scope.launch {
                                val success = loadingDialog.withLoading {
                                    withContext(Dispatchers.IO) {
                                        toggleModule(module.id, !module.enabled)
                                    }
                                }
                                if (success) {
                                    viewModel.fetchModuleList()

                                    val result = snackBarHost.showSnackbar(
                                        message = rebootToApply, actionLabel = reboot, duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        reboot()
                                    }
                                } else {
                                    val message = if (module.enabled) failedDisable else failedEnable
                                    snackBarHost.showSnackbar(message.format(module.name))
                                }
                            }
                        }, onModuleAction = {
                            navigator.navigateTo(Route.ExecuteModuleAction(module.id))
                            viewModel.markNeedRefresh()
                        }, onOpenWebUI = {
                            onClickModule(module.id, module.name, module.hasWebUi)
                        }, onUndoUninstall = {
                            scope.launch { onModuleUndoUninstall(module) }
                        }, onUninstall = {
                            scope.launch { onModuleUninstall(module) }
                        }) {
                            scope.launch {
                                onModuleUpdate(
                                    module, updatedModule.changelog, updatedModule.downloadUrl, "${module.name}-${updatedModule.version}.zip"
                                )
                            }
                        }

                        // fix last item shadow incomplete in LazyColumn
                        Spacer(Modifier.height(1.dp))
                    }
                }
            }
        }

        DownloadListener(context, onInstallModule)

    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ModuleItem(
    module: ModuleInfo,
    hasUpdate: Boolean,
    onEnableChanged: (Boolean) -> Unit,
    onModuleAction: () -> Unit,
    onOpenWebUI: () -> Unit,
    onUndoUninstall: () -> Unit,
    onUninstall: () -> Unit,
    onUpdate: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.let {
            if (module.hasWebUi) {
                it.clickable(
                    enabled = !module.remove && module.enabled, role = Role.Button, onClick = onOpenWebUI
                )
            } else {
                it
            }.fillMaxWidth()
        }, colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceBright)
    ) {
        val textDecoration = if (!module.remove) null else TextDecoration.LineThrough

        Column(
            modifier = Modifier.padding(22.dp, 18.dp, 22.dp, 18.dp)
        ) {
            // tiele and switch
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = module.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = MaterialTheme.typography.titleMedium.fontSize,
                            textDecoration = textDecoration,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (module.metamodule) {
                            StatusTag("META", 12.sp)
                        }
                    }

                    Text(
                        style = MaterialTheme.typography.bodySmall,
                        text = "${stringResource(id = R.string.module_version)}: ${module.version}",
                        textDecoration = textDecoration,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${stringResource(id = R.string.module_author)}: ${module.author}",
                        style = MaterialTheme.typography.bodySmall,
                        textDecoration = textDecoration,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Switch(
                    enabled = !module.update,
                    checked = module.enabled,
                    onCheckedChange = onEnableChanged,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            // module description
            Text(
                text = module.description,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = 5,
                textDecoration = textDecoration
            )

            Spacer(modifier = Modifier.height(8.dp))
            StatusTag(module.id)
            Spacer(modifier = Modifier.height(8.dp))
            // button row
            ModuleButtonRow(module, hasUpdate, onModuleAction, onOpenWebUI, onUpdate, onUndoUninstall, onUninstall)
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun ModuleButtonRow(
    module: ModuleInfo,
    hasUpdate: Boolean,
    onModuleAction: () -> Unit,
    onOpenWebUI: () -> Unit,
    onUpdate: () -> Unit,
    onUndoUninstall: () -> Unit,
    onUninstall: () -> Unit
) {
    val iconSize = ButtonDefaults.iconSizeFor(ButtonDefaults.MinHeight)

    val startButtons = remember(module) {
        val list = mutableListOf<ButtonSpec>()
        list.add(
            ButtonSpec(
                id = "action",
                text = { stringResource(R.string.action) },
                isVisible = module.hasActionScript,
                isEnabled = !module.remove && module.enabled,
                icon = {
                    Icon(
                        modifier = Modifier.size(iconSize),
                        imageVector = Icons.Outlined.PlayArrow,
                        contentDescription = stringResource(R.string.action)
                    )
                },
                onClick = onModuleAction,
                buttonGroup = ButtonGroup.START
            )
        )

        list.add(
            ButtonSpec(
                id = "webui",
                text = { stringResource(R.string.open) },
                isVisible = module.hasWebUi,
                isEnabled = !module.remove && module.enabled,
                icon = {
                    Icon(
                        modifier = Modifier.size(iconSize),
                        painter = painterResource(R.drawable.ic_wysiwyg_rounded),
                        contentDescription = stringResource(R.string.open)
                    )
                },
                onClick = onOpenWebUI,
                buttonGroup = ButtonGroup.START
            )
        )
        list
    }

    val endButtons = remember(module, hasUpdate) {
        val list = mutableListOf<ButtonSpec>()
        list.add(
            ButtonSpec(
                id = "update",
                text = { stringResource(R.string.module_update) },
                isVisible = hasUpdate,
                isEnabled = !module.remove,
                type = ButtonType.PRIMARY,
                icon = {
                    Icon(
                        modifier = Modifier.size(iconSize),
                        painter = painterResource(R.drawable.ic_download_2_rounded),
                        contentDescription = null
                    )
                },
                onClick = onUpdate,
                buttonGroup = ButtonGroup.END
            )
        )

        list.add(
            ButtonSpec(
                id = "uninstall",
                text = { stringResource(if (module.remove) R.string.undo else R.string.uninstall) },
                isVisible = true,
                isEnabled = true,
                icon = {
                    Icon(
                        modifier = Modifier.size(iconSize),
                        painter = painterResource(if (module.remove) R.drawable.ic_undo_rounded_filled else R.drawable.ic_delete_rounded_filled),
                        contentDescription = stringResource(if (module.remove) R.string.undo else R.string.uninstall)
                    )
                },
                onClick = { if (module.remove) onUndoUninstall() else onUninstall() },
                buttonGroup = ButtonGroup.END
            )
        )
        list
    }

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        val lastVisibleStartButtonId = remember(startButtons) {
            startButtons.lastOrNull { it.isVisible }?.id
        }
        val lastVisibleEndButtonId = remember(endButtons) {
            endButtons.lastOrNull { it.isVisible }?.id
        }
        EnumeratedPriorityButtonRow(
            centerSpacing = 150.dp,
            startButtons = startButtons,
            endButtons = endButtons,
            buttonFactory = { spec, isExpanded ->
                val isLastVisibleInGroup = when (spec.buttonGroup) {
                    ButtonGroup.START -> spec.id == lastVisibleStartButtonId
                    ButtonGroup.END -> spec.id == lastVisibleEndButtonId
                }

                val targetPadding = if (isLastVisibleInGroup) 0.dp else 8.dp

                val animatedEndPadding by animateDpAsState(
                    targetValue = targetPadding,
                    label = "SpacingAnimation"
                )

                ActionButton(
                    modifier = Modifier.padding(end = animatedEndPadding),
                    text = spec.text(),
                    icon = spec.icon,
                    onClick = spec.onClick,
                    visible = spec.isVisible,
                    enabled = spec.isEnabled,
                    isExpanded = isExpanded,
                    buttonType = spec.type,
                    buttonGroup = spec.buttonGroup
                )
            }
        )
    }
}


@Composable
private fun ShortByMenuButton(viewModel: ModuleViewModel, prefs: SharedPreferences) {
    val showDropdown = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    IconButton(
        onClick = { showDropdown.value = true },
    ) {
        Icon(
            imageVector = Icons.Filled.MoreVert, contentDescription = stringResource(id = R.string.settings)
        )
        DropdownMenu(expanded = showDropdown.value, onDismissRequest = {
            showDropdown.value = false
        }) {
            DropdownMenuItem(text = {
                Text(stringResource(R.string.module_sort_action_first))
            }, trailingIcon = {
                Checkbox(viewModel.sortActionFirst, null)
            }, onClick = {
                viewModel.sortActionFirst = !viewModel.sortActionFirst
                prefs.edit {
                    putBoolean(
                        "module_sort_action_first", viewModel.sortActionFirst
                    )
                }
                scope.launch {
                    viewModel.fetchModuleList()
                }
            })
            DropdownMenuItem(text = {
                Text(stringResource(R.string.module_sort_enabled_first))
            }, trailingIcon = {
                Checkbox(viewModel.sortEnabledFirst, null)
            }, onClick = {
                viewModel.sortEnabledFirst = !viewModel.sortEnabledFirst
                prefs.edit {
                    putBoolean(
                        "module_sort_enabled_first", viewModel.sortEnabledFirst
                    )
                }
                scope.launch {
                    viewModel.fetchModuleList()
                }
            })
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun InstallModuleFAB(visible: Boolean, viewModel: ModuleViewModel) {
    val navigator = LocalNavController.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val loadingDialog = rememberLoadingDialog()
    val confirmTitle = stringResource(R.string.module)
    val multiConfirmContent = stringResource(R.string.module_install_prompt_with_name)
    var zipUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val confirmDialog = rememberConfirmDialog(onConfirm = {
        navigator.navigateTo(Route.Flash(FlashIt.FlashModules(zipUris)))
        viewModel.markNeedRefresh()
    })
    val selectZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode != RESULT_OK) {
            return@rememberLauncherForActivityResult
        }
        val data = it.data ?: return@rememberLauncherForActivityResult
        val clipData = data.clipData

        val uris = mutableListOf<Uri>()
        if (clipData != null) {
            for (i in 0 until clipData.itemCount) {
                clipData.getItemAt(i)?.uri?.let { uri -> uris.add(uri) }
            }
        } else {
            data.data?.let { uri -> uris.add(uri) }
        }
        scope.launch {
            var confirmContent = ""
            if (uris.size == 1) {
                zipUris = uris
                confirmContent = loadingDialog.withLoading {
                    withContext(Dispatchers.IO) {
                        ModuleParser.getModuleInstallDesc(context, uris.first(), viewModel.moduleList)
                    }
                }
            } else if (uris.size > 1) {
                // multiple files selected
                viewModel.markNeedRefresh()
                val moduleNames = uris.mapIndexed { index, uri -> "\n${index + 1}. ${uri.getFileName(context)}" }.joinToString("")
                confirmContent = multiConfirmContent.format(moduleNames)
                zipUris = uris
            }
            confirmDialog.showConfirm(title = confirmTitle, content = confirmContent, markdown = true)
        }
    }

    ExtendedFloatingActionButton(
        modifier = Modifier.animateFloatingActionButton(visible = visible, alignment = Alignment.CenterEnd),
        onClick = {
            // Select the zip files to install
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            selectZipLauncher.launch(intent)
        },
        icon = { Icon(Icons.Filled.Add, stringResource(id = R.string.module_install)) },
        text = { Text(text = stringResource(id = R.string.module_install)) },
    )
}


@Preview
@Composable
fun ModuleItemPreview(
    enabled: Boolean = false,
    updating: Boolean = true,
    remove: Boolean = false,
    hasWebUi: Boolean = true,
    hasActionScript: Boolean = true,
    hasUpdate: Boolean = true,
    metamodule: Boolean = true,
) {
    val module = ModuleInfo(
        id = "moduleid",
        name = "name",
        version = "v114.514.191.9810",
        versionCode = 1,
        author = "author",
        description = "恭喜你啊，年轻人，被我恭喜到了。说实话，现在的年轻人啊，跟老一辈的人比起来，其实啊，真的很年轻，但是怎么说呢，老一辈吃过的盐可能比你吃过的饭还要咸。咱们再说，如果说上学或者上班不那么累，其实啊，也是很轻松的，所以啊，当你吃过晚饭以后，其实这个晚上啊，你就已经吃过了饭。中国不是有这么一句话吗？叫做有句古话说的好，情况就这么个情况，但具体什么情况呢？还要看情况\nCongratulations to you, young man, you've been congratulated by me. To be honest, young people nowadays, compared to the older generation, are indeed very young, but how should I put it? The salt the older generation has eaten might be saltier than the rice you've eaten. Let's also say, if going to school or work weren't so tiring, it would actually be quite relaxing. So, after you've had dinner, well, you've already eaten for the evening. Isn't there a saying in China? There's an old saying that goes: That's the situation, but what exactly is the situation? It still depends on the situation.",
        enabled = enabled,
        update = updating,
        remove = remove,
        updateJson = "",
        hasWebUi = hasWebUi,
        hasActionScript = hasActionScript,
        metamodule = metamodule
    )
    ModuleItem(module, hasUpdate, {}, {}, {}, {}, {}, {})
}
