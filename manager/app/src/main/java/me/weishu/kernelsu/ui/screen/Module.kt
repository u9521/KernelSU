package me.weishu.kernelsu.ui.screen

import android.annotation.SuppressLint
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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
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
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.BreezeSnackBarHost
import me.weishu.kernelsu.ui.component.SearchAppBar
import me.weishu.kernelsu.ui.component.SearchStatus
import me.weishu.kernelsu.ui.component.StatusTag
import me.weishu.kernelsu.ui.component.module.ActionButton
import me.weishu.kernelsu.ui.component.module.ButtonPosition
import me.weishu.kernelsu.ui.component.module.ButtonSpec
import me.weishu.kernelsu.ui.component.module.ButtonType
import me.weishu.kernelsu.ui.component.module.EnumeratedPriorityButtonRow
import me.weishu.kernelsu.ui.component.module.InstallModuleDialog
import me.weishu.kernelsu.ui.component.popUps.RebootListPopup
import me.weishu.kernelsu.ui.component.rememberConfirmDialog
import me.weishu.kernelsu.ui.component.rememberLoadingDialog
import me.weishu.kernelsu.ui.component.scrollbar.ScrollbarDefaults
import me.weishu.kernelsu.ui.component.scrollbar.VerticalScrollbar
import me.weishu.kernelsu.ui.component.scrollbar.rememberScrollbarAdapter
import me.weishu.kernelsu.ui.navigation3.Route
import me.weishu.kernelsu.ui.util.LocalNavController
import me.weishu.kernelsu.ui.util.LocalSnackbarHost
import me.weishu.kernelsu.ui.util.download
import me.weishu.kernelsu.ui.util.hasMagisk
import me.weishu.kernelsu.ui.util.reboot
import me.weishu.kernelsu.ui.viewmodel.ModuleViewModel
import me.weishu.kernelsu.ui.viewmodel.ModuleViewModel.ModuleInfo
import me.weishu.kernelsu.ui.viewmodel.ModuleViewModel.ModuleIntent
import me.weishu.kernelsu.ui.viewmodel.ModuleViewModel.ModuleUiEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleScreen() {
    val viewModel = viewModel<ModuleViewModel>()
    val context = LocalContext.current
    val snackBarHost = LocalSnackbarHost.current
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val searchStatus by viewModel.searchStatus

    LaunchedEffect(Unit) {
        when {
            viewModel.moduleList.isEmpty() -> {
                viewModel.checkModuleUpdate = prefs.getBoolean("module_check_update", true)
                viewModel.sortEnabledFirst = prefs.getBoolean("module_sort_enabled_first", false)
                viewModel.sortActionFirst = prefs.getBoolean("module_sort_action_first", false)
                viewModel.onIntent(ModuleIntent.Refresh(checkUpdate = true))
            }

            viewModel.isNeedRefresh -> {
                viewModel.onIntent(ModuleIntent.Refresh())
            }
        }
    }

    LaunchedEffect(searchStatus.searchText, viewModel.moduleList) {
        viewModel.onIntent(ModuleIntent.Search(searchStatus.searchText))
    }

    val isSafeMode = Natives.isSafeMode
    val magiskInstalled by produceState(initialValue = false) {
        value = withContext(Dispatchers.IO) { hasMagisk() }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

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

        ModuleList(
            viewModel = viewModel,
            lazyColumnModifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            boxModifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ModuleList(
    viewModel: ModuleViewModel,
    @SuppressLint("ModifierParameter") lazyColumnModifier: Modifier = Modifier,
    boxModifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val navigator = LocalNavController.current
    val snackBarHost = LocalSnackbarHost.current

    val loadingDialog = rememberLoadingDialog()
    val confirmDialog =
        rememberConfirmDialog(onConfirm = { viewModel.onIntent(ModuleIntent.ConfirmAction) }, onDismiss = { viewModel.onIntent(ModuleIntent.DismissAction) })
    val installModuleUris = remember { mutableStateOf<List<Uri>>(emptyList()) }

    val webUILauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { viewModel.onIntent(ModuleIntent.Refresh()) }

    LaunchedEffect(viewModel.actionLoading) {
        if (viewModel.actionLoading) {
            loadingDialog.showLoading()
        } else {
            loadingDialog.hide()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ModuleUiEffect.ShowSnackbar -> {
                    val message = resources.getString(effect.messageRes, *effect.formatArgs.toTypedArray())
                    val actionLabel = effect.actionLabelRes?.let { resources.getString(it) }
                    val result = snackBarHost.showSnackbar(
                        message = message,
                        actionLabel = actionLabel,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed && effect.withReboot) {
                        reboot()
                    }
                }

                is ModuleUiEffect.ShowToast -> {
                    Toast.makeText(context, resources.getString(effect.messageRes), Toast.LENGTH_SHORT).show()
                }

                is ModuleUiEffect.ShowConfirmDialog -> {
                    confirmDialog.showConfirm(
                        title = resources.getString(effect.titleRes),
                        content = effect.content,
                        markdown = effect.markdown,
                        confirm = effect.confirmTextRes?.let { resources.getString(it) },
                        dismiss = effect.dismissTextRes?.let { resources.getString(it) }
                    )
                }

                is ModuleUiEffect.StartDownload -> {
                    val downloadingMsg = resources.getString(R.string.module_start_downloading, effect.moduleName)
                    withContext(Dispatchers.IO) {
                        download(
                            url = effect.url,
                            fileName = effect.fileName,
                            onDownloaded = { uri -> installModuleUris.value = listOf(uri) },
                            onDownloading = {
                                launch(Dispatchers.Main) {
                                    snackBarHost.showSnackbar(message = downloadingMsg, duration = SnackbarDuration.Short)
                                }
                            }
                        )
                    }
                }

                is ModuleUiEffect.RunModuleAction -> {
                    navigator.navigateTo(Route.ExecuteModuleAction(effect.moduleId))
                }

                is ModuleUiEffect.LaunchIntent -> {
                    webUILauncher.launch(effect.intent)
                }
            }
        }
    }

    InstallModuleDialog(installModuleUris.value, viewModel) {
        installModuleUris.value = emptyList()
    }

    val state = rememberPullToRefreshState()
    PullToRefreshBox(
        modifier = boxModifier,
        state = state,
        onRefresh = { viewModel.onIntent(ModuleIntent.Refresh(checkUpdate = true)) },
        indicator = {
            PullToRefreshDefaults.LoadingIndicator(
                state = state, isRefreshing = viewModel.isRefreshing, modifier = Modifier.align(Alignment.TopCenter)
            )
        }, isRefreshing = viewModel.isRefreshing
    ) {
        if (viewModel.moduleList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                if (viewModel.isRefreshing) return@Box
                val text = if (viewModel.searchStatus.value.resultStatus == SearchStatus.ResultStatus.EMPTY) {
                    "no modules found"
                } else {
                    stringResource(R.string.module_empty)
                }
                Text(text, textAlign = TextAlign.Center)
            }
            return@PullToRefreshBox
        }
        val lazyListState = rememberLazyListState()

        LazyColumn(
            modifier = lazyColumnModifier,
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = remember {
                PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp + 56.dp + 16.dp + 48.dp + 6.dp
                )
            },
        ) {
            var displayModuleList = viewModel.moduleList
            if (viewModel.searchStatus.value.resultStatus == SearchStatus.ResultStatus.SHOW) {
                displayModuleList = viewModel.searchResults.value
            }

            items(displayModuleList, key = { it.id }) { module ->
                ModuleItem(
                    module = module,
                    hasUpdate = viewModel.updateInfo[module.id]?.downloadUrl?.isNotEmpty() ?: false,
                    onEnableChanged = { viewModel.onIntent(ModuleIntent.Toggle(module)) },
                    onModuleAction = { viewModel.onIntent(ModuleIntent.OpenAction(module)) },
                    onOpenWebUI = { viewModel.onIntent(ModuleIntent.OpenWebUI(module)) },
                    onUndoUninstall = { viewModel.onIntent(ModuleIntent.RequestUndoUninstall(module)) },
                    onUninstall = { viewModel.onIntent(ModuleIntent.RequestUninstall(module)) },
                    onUpdate = { viewModel.onIntent(ModuleIntent.RequestUpdate(module, viewModel.updateInfo[module.id])) }
                )
            }

        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd), adapter = rememberScrollbarAdapter(lazyListState),
            durationMillis = 1500L,
            style = ScrollbarDefaults.style.copy(
                color = MaterialTheme.colorScheme.primary, railColor = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.5f)
            )
        )
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
            // title and switch
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
    val resources = LocalResources.current
    val iconSize = ButtonDefaults.iconSizeFor(ButtonDefaults.MinHeight)
    val moduleStable = !(module.remove || module.update)

    val startButtons = remember(module) {
        val list = mutableListOf<ButtonSpec>()
        list.add(
            ButtonSpec(
                id = "action",
                text = resources.getString(R.string.action),
                isVisible = module.hasActionScript,
                isEnabled = moduleStable && module.enabled,
                icon = {
                    Icon(
                        modifier = Modifier.size(iconSize),
                        imageVector = Icons.Outlined.PlayArrow,
                        contentDescription = stringResource(R.string.action)
                    )
                },
                onClick = onModuleAction,
                buttonPosition = ButtonPosition.START
            )
        )

        list.add(
            ButtonSpec(
                id = "webui",
                text = resources.getString(R.string.open),
                isVisible = module.hasWebUi,
                isEnabled = moduleStable && module.enabled,
                icon = {
                    Icon(
                        modifier = Modifier.size(iconSize),
                        painter = painterResource(R.drawable.ic_wysiwyg_rounded),
                        contentDescription = stringResource(R.string.open)
                    )
                },
                onClick = onOpenWebUI,
                buttonPosition = ButtonPosition.START
            )
        )
        list
    }

    val endButtons = remember(module, hasUpdate) {
        val list = mutableListOf<ButtonSpec>()
        list.add(
            ButtonSpec(
                id = "update",
                text = resources.getString(R.string.module_update),
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
                buttonPosition = ButtonPosition.END
            )
        )

        list.add(
            ButtonSpec(
                id = "uninstall",
                text = resources.getString(if (module.remove) R.string.undo else R.string.uninstall),
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
                buttonPosition = ButtonPosition.END
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
                val isLastVisibleInGroup = when (spec.buttonPosition) {
                    ButtonPosition.START -> spec.id == lastVisibleStartButtonId
                    ButtonPosition.END -> spec.id == lastVisibleEndButtonId
                }

                val targetPadding = if (isLastVisibleInGroup) 0.dp else 8.dp

                val animatedEndPadding by animateDpAsState(
                    targetValue = targetPadding,
                    label = "SpacingAnimation"
                )

                ActionButton(
                    modifier = Modifier.padding(end = animatedEndPadding),
                    text = spec.text,
                    icon = spec.icon,
                    onClick = spec.onClick,
                    visible = spec.isVisible,
                    enabled = spec.isEnabled,
                    isExpanded = isExpanded,
                    buttonType = spec.type,
                    buttonPosition = spec.buttonPosition
                )
            }
        )
    }
}


@Composable
private fun ShortByMenuButton(viewModel: ModuleViewModel, prefs: SharedPreferences) {
    val showDropdown = remember { mutableStateOf(false) }

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
                viewModel.onIntent(ModuleIntent.Refresh())
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
                viewModel.onIntent(ModuleIntent.Refresh())
            })
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun InstallModuleFAB(visible: Boolean, viewModel: ModuleViewModel) {
    val zipUris = remember { mutableStateOf<List<Uri>>(emptyList()) }

    InstallModuleDialog(zipUris.value, viewModel) {
        zipUris.value = emptyList()
    }
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
        zipUris.value = uris
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