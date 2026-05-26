package me.weishu.kernelsu.ui.screen.module

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import me.weishu.kernelsu.R
import me.weishu.kernelsu.data.model.Module
import me.weishu.kernelsu.data.model.ModuleUpdateInfo
import me.weishu.kernelsu.ui.component.ObserveAsEvents
import me.weishu.kernelsu.ui.component.breeze.ActionButton
import me.weishu.kernelsu.ui.component.breeze.BreezeSnackBarHost
import me.weishu.kernelsu.ui.component.breeze.ButtonPosition
import me.weishu.kernelsu.ui.component.breeze.ButtonSpec
import me.weishu.kernelsu.ui.component.breeze.ButtonType
import me.weishu.kernelsu.ui.component.breeze.EnumeratedPriorityButtonRow
import me.weishu.kernelsu.ui.component.breeze.InstallModuleDialog
import me.weishu.kernelsu.ui.component.breeze.ScrollbarDefaults
import me.weishu.kernelsu.ui.component.breeze.VerticalScrollbar
import me.weishu.kernelsu.ui.component.breeze.rememberScrollbarAdapter
import me.weishu.kernelsu.ui.component.dialog.rememberConfirmDialog
import me.weishu.kernelsu.ui.component.dialog.rememberLoadingDialog
import me.weishu.kernelsu.ui.component.material.ExpressiveSwitch
import me.weishu.kernelsu.ui.component.material.SearchAppBarBreeze
import me.weishu.kernelsu.ui.component.rebootlistpopup.RebootListPopup
import me.weishu.kernelsu.ui.component.statustag.StatusTag
import me.weishu.kernelsu.ui.navigation3.breeze.isRailNavbar
import me.weishu.kernelsu.ui.screen.home.TonalCard
import me.weishu.kernelsu.ui.util.fABBottomPadding
import me.weishu.kernelsu.ui.util.onlyHorizontal
import me.weishu.kernelsu.ui.util.reboot
import me.weishu.kernelsu.ui.util.rememberBSState
import me.weishu.kernelsu.ui.util.topBarHazeEffect
import me.weishu.kernelsu.ui.util.windowBlurBehind

@Composable
fun ModulePagerBreeze(
    uiState: ModuleUiState,
    confirmDialogState: ModuleConfirmDialogState?,
    moduleEvent: Flow<ModuleEffect>,
    actions: ModuleActions,
    bottomInnerPadding: Dp,
) {
    val snackBarHost = remember { SnackbarHostState() }

    val context = LocalContext.current
    val resource = LocalResources.current
    val hazeState = remember { HazeState() }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val searchBarScrollBehavior = SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()

    val pullToRefreshState = rememberPullToRefreshState()

    val scaleFraction = {
        if (uiState.isRefreshing) 1f
        else LinearOutSlowInEasing.transform(pullToRefreshState.distanceFraction).coerceIn(0f, 1f)
    }

    val listState = rememberLazyListState()
    val searchListState = rememberLazyListState()
    val showListScrollbar by remember {
        derivedStateOf { scrollBehavior.state.collapsedFraction > 0.99f }
    }

    val shortcutState = rememberModuleShortcutState(context)
    val showShortcutDialog = remember { mutableStateOf(false) }
    val confirmDialog = rememberConfirmDialog(
        onConfirm = {
            when (val request = confirmDialogState?.request) {
                is ModuleConfirmRequest.Uninstall -> actions.onUninstallModule(request.module)
                is ModuleConfirmRequest.Update -> actions.onConfirmUpdate(request)
                null -> Unit
            }
        },
        onDismiss = actions.onDismissConfirmRequest,
    )

    fun openShortcutDialogForType(type: ShortcutType) {
        shortcutState.selectType(type)
        showShortcutDialog.value = true
    }

    val pickShortcutIconLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        shortcutState.updateIconUri(uri?.toString())
    }

    fun onModuleAddShortcut(module: Module, type: ShortcutType) {
        shortcutState.bindModule(module)
        openShortcutDialogForType(type)
    }

    LaunchedEffect(confirmDialogState) {
        confirmDialogState?.let {
            confirmDialog.showConfirm(
                title = it.title,
                content = it.content,
                markdown = it.markdown,
                html = it.html,
                confirm = it.confirm,
                dismiss = it.dismiss,
            )
        }
    }

    val scope = rememberCoroutineScope()
    val snackbarJob = remember { mutableStateOf<Job?>(null) }
    ObserveAsEvents(moduleEvent) { event ->
        when (event) {
            is ModuleEffect.Toast -> {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }

            is ModuleEffect.SnackBar -> {
                // Cancel the previous reboot snackbar so a new one replaces it instead of queueing
                snackbarJob.value?.cancel()
                snackbarJob.value = scope.launch {
                    val result = snackBarHost.showSnackbar(
                        message = event.message,
                        actionLabel = resource.getString(R.string.reboot),
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        reboot()
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .pullToRefresh(
                state = pullToRefreshState,
                isRefreshing = uiState.isRefreshing,
                onRefresh = { actions.onRefresh() },
            ),
        topBar = {
            SearchAppBarBreeze(
                modifier = Modifier.topBarHazeEffect(hazeState, scrollBehavior),
                title = { Text(stringResource(R.string.module)) },
                searchText = uiState.searchStatus.searchText,
                onSearchTextChange = actions.onSearchTextChange,
                onClearClick = actions.onClearSearch,
                actions = {
                    RebootListPopup()
                    SortMenu(uiState, actions)
                },
                scrollBehavior = scrollBehavior,
                searchBarScrollBehavior = searchBarScrollBehavior,
                snackbarHostState = snackBarHost,
                searchContent = { bottomPadding, closeSearch ->
                    LaunchedEffect(uiState.searchStatus.searchText) {
                        searchListState.scrollToItem(0)
                    }
                    ModuleList(
                        topInnerPadding = 0.dp,
                        bottomInnerPadding = bottomPadding,
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(searchBarScrollBehavior.nestedScrollConnection),
                        listState = searchListState,
                        displayModules = uiState.searchResults,
                        updateInfoMap = uiState.updateInfo,
                        actions = actions,
                        onClickModule = { module ->
                            if (module.hasWebUi) {
                                actions.onOpenWebUi(module)
                                closeSearch()
                            }
                        },
                        onModuleAddShortcut = { module, type -> onModuleAddShortcut(module, type) },
                        closeSearch = closeSearch,
                    )
                    VerticalScrollbar(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(searchListState),
                        durationMillis = 1500L,
                        visible = true,
                        style = ScrollbarDefaults.style.copy(
                            color = MaterialTheme.colorScheme.primary,
                            railColor = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.5f),
                        )
                    )
                },
            )
        },
        floatingActionButton = {
            ModuleInstallFAB(uiState, actions, bottomInnerPadding)
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        snackbarHost = {
            BreezeSnackBarHost(hostState = snackBarHost, Modifier.let {
                if (!uiState.installButtonVisible) it.padding(
                    bottom =
                        bottomInnerPadding
                ) else it
            })
        }
    ) { innerPadding ->
        if (uiState.magiskInstalled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.module_magisk_conflict),
                    textAlign = TextAlign.Center,
                )
            }
            return@Scaffold
        }
        Box(
            modifier = Modifier
                .hazeSource(hazeState)
                .padding(innerPadding.onlyHorizontal())
        ) {
            ModuleList(
                topInnerPadding = innerPadding.calculateTopPadding(),
                bottomInnerPadding = bottomInnerPadding + fABBottomPadding(hasNavBar = true),
                modifier = Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .nestedScroll(searchBarScrollBehavior.nestedScrollConnection),
                listState = listState,
                displayModules = uiState.moduleList,
                updateInfoMap = uiState.updateInfo,
                actions = actions,
                onClickModule = { module ->
                    if (module.hasWebUi) {
                        actions.onOpenWebUi(module)
                    }
                },
                onModuleAddShortcut = { module, type -> onModuleAddShortcut(module, type) },
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = innerPadding.calculateTopPadding())
                    .graphicsLayer {
                        scaleX = scaleFraction()
                        scaleY = scaleFraction()
                    },
                contentAlignment = Alignment.TopCenter
            ) {
                PullToRefreshDefaults.LoadingIndicator(
                    state = pullToRefreshState,
                    isRefreshing = uiState.isRefreshing,
                )
            }
            val scrollBottomPadding = if (isRailNavbar()) 0.dp else bottomInnerPadding
            VerticalScrollbar(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(top = innerPadding.calculateTopPadding(), bottom = scrollBottomPadding),
                adapter = rememberScrollbarAdapter(listState),
                durationMillis = 1500L,
                visible = showListScrollbar,
                style = ScrollbarDefaults.style.copy(
                    color = MaterialTheme.colorScheme.primary,
                    railColor = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.5f),
                )
            )
        }
    }

    ModuleShortcutSheet(
        show = showShortcutDialog.value,
        shortcutState = shortcutState,
        onDismiss = { showShortcutDialog.value = false },
        onPickShortcutIcon = { pickShortcutIconLauncher.launch("image/*") },
        onDeleteShortcut = {
            shortcutState.deleteShortcut(context)
            showShortcutDialog.value = false
        },
        onConfirmShortcut = {
            shortcutState.createShortcut(context)
            showShortcutDialog.value = false
        },
    )
}


@Composable
private fun ModuleList(
    topInnerPadding: Dp,
    bottomInnerPadding: Dp,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    displayModules: List<Module>,
    updateInfoMap: Map<String, ModuleUpdateInfo>,
    actions: ModuleActions,
    onClickModule: (Module) -> Unit,
    onModuleAddShortcut: (Module, ShortcutType) -> Unit,
    closeSearch: () -> Unit? = {},
) {
    val loadingDialog = rememberLoadingDialog()
    // lock auto scroll
    remember(displayModules) {
        val currentIndex = Snapshot.withoutReadObservation { listState.firstVisibleItemIndex }
        val currentOffset = Snapshot.withoutReadObservation { listState.firstVisibleItemScrollOffset }
        listState.requestScrollToItem(currentIndex, currentOffset)
        null
    }
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 8.dp + topInnerPadding,
            end = 16.dp,
            bottom = 16.dp + bottomInnerPadding + 56.dp + 16.dp + 48.dp + 12.dp
        ),
    ) {
        items(displayModules, key = { it.id }) { module ->
            val scope = rememberCoroutineScope()
            val moduleUpdateInfo = updateInfoMap[module.id] ?: ModuleUpdateInfo.Empty
            ModuleItem(
                modifier = Modifier.animateItem(),
                module = module,
                updateUrl = moduleUpdateInfo.downloadUrl,
                onUninstallClicked = {
                    if (module.remove) {
                        actions.onUndoUninstallModule(module)
                    } else {
                        actions.onRequestUninstallConfirmation(module)
                    }
                },
                onCheckChanged = {
                    actions.onToggleModule(module)
                },
                onUpdate = {
                    scope.launch {
                        loadingDialog.withLoading {
                            actions.onRequestUpdateConfirmation(module, moduleUpdateInfo)
                        }
                    }
                },
                onAddShortcut = { type -> onModuleAddShortcut(module, type) },
                onClick = { onClickModule(module) },
                onExecuteAction = { actions.onExecuteModuleAction(module) },
                closeSearch = { closeSearch() }
            )
        }
    }
}

@Composable
private fun ModuleShortcutSheet(
    show: Boolean,
    shortcutState: ModuleShortcutState,
    onDismiss: () -> Unit,
    onPickShortcutIcon: () -> Unit,
    onDeleteShortcut: () -> Unit,
    onConfirmShortcut: () -> Unit,
) {
    if (!show) return

    ModalBottomSheet(
        modifier = Modifier.windowBlurBehind(),
        onDismissRequest = onDismiss,
        sheetState = rememberBSState(skipPartiallyExpanded = true)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.module_shortcut_title),
                style = MaterialTheme.typography.titleLarge
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .size(100.dp)
                    .clip(RoundedCornerShape(25.dp))
            ) {
                val preview = shortcutState.previewIcon
                if (preview != null) {
                    Image(
                        bitmap = preview,
                        modifier = Modifier.size(100.dp),
                        contentDescription = null,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.White)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        contentScale = FixedScale(1.5f)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onPickShortcutIcon) {
                    Text(stringResource(id = R.string.module_shortcut_icon_pick))
                }
                AnimatedVisibility(
                    visible = shortcutState.iconUri != shortcutState.defaultShortcutIconUri,
                    enter = expandHorizontally() + slideInHorizontally(initialOffsetX = { it }),
                    exit = shrinkHorizontally() + slideOutHorizontally(targetOffsetX = { it }),
                ) {
                    IconButton(
                        onClick = shortcutState::resetIconToDefault,
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
            OutlinedTextField(
                value = shortcutState.name,
                onValueChange = shortcutState::updateName,
                label = { Text(stringResource(id = R.string.module_shortcut_name_label)) },
                modifier = Modifier.fillMaxWidth()
            )
            if (shortcutState.hasExistingShortcut) {
                TextButton(
                    onClick = onDeleteShortcut,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(id = R.string.module_shortcut_delete))
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(id = android.R.string.cancel))
                }
                Button(
                    onClick = onConfirmShortcut,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        if (shortcutState.hasExistingShortcut) {
                            stringResource(id = R.string.module_update)
                        } else {
                            stringResource(id = android.R.string.ok)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ModuleItem(
    modifier: Modifier = Modifier,
    module: Module,
    updateUrl: String,
    onUninstallClicked: () -> Unit,
    onCheckChanged: (Boolean) -> Unit,
    onUpdate: () -> Unit,
    onAddShortcut: (ShortcutType) -> Unit,
    onClick: () -> Unit,
    onExecuteAction: () -> Unit,
    closeSearch: () -> Unit
) {
    TonalCard(
        modifier = modifier.fillMaxWidth()
    ) {
        val textDecoration = if (!module.remove) null else TextDecoration.LineThrough
        val interactionSource = remember { MutableInteractionSource() }
        val indication = LocalIndication.current
        var expanded by rememberSaveable(module.id) { mutableStateOf(false) }
        var isOverflowing by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .run {
                    if (module.hasWebUi) {
                        toggleable(
                            value = module.enabled,
                            enabled = !module.remove && module.enabled,
                            interactionSource = interactionSource,
                            role = Role.Button,
                            indication = indication,
                            onValueChange = { onClick() }
                        )
                    } else {
                        this
                    }
                }
                .padding(22.dp, 18.dp, 22.dp, 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val moduleVersion = stringResource(id = R.string.module_version)
                val moduleAuthor = stringResource(id = R.string.module_author)

                Column(
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = module.name,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium,
                            lineHeight = MaterialTheme.typography.titleMedium.fontSize,
                            textDecoration = textDecoration,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (module.metamodule) {
                            StatusTag(
                                label = "META",
                                backgroundColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }

                    Text(
                        text = "$moduleVersion: ${module.version}",
                        style = MaterialTheme.typography.bodySmall,
                        textDecoration = textDecoration
                    )

                    Text(
                        text = "$moduleAuthor: ${module.author}",
                        style = MaterialTheme.typography.bodySmall,
                        textDecoration = textDecoration
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    ExpressiveSwitch(
                        enabled = !module.update,
                        checked = module.enabled,
                        onCheckedChange = onCheckChanged,
                        interactionSource = if (!module.hasWebUi) interactionSource else remember { MutableInteractionSource() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                modifier = Modifier
                    .animateContentSize(
                        animationSpec = tween(
                            durationMillis = 250,
                            easing = FastOutSlowInEasing
                        )
                    )
                    .then(
                        if (isOverflowing || expanded) {
                            Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { expanded = !expanded }
                        } else {
                            Modifier
                        }
                    ),
                text = module.description,
                style = MaterialTheme.typography.bodySmall,
                overflow = if (expanded) TextOverflow.Clip else TextOverflow.Ellipsis,
                maxLines = if (expanded) Int.MAX_VALUE else 4,
                textDecoration = textDecoration,
                onTextLayout = { textLayoutResult ->
                    isOverflowing = if (expanded) {
                        textLayoutResult.lineCount > 4
                    } else {
                        textLayoutResult.hasVisualOverflow
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            StatusTag(
                label = module.id,
                backgroundColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(4.dp))

            ModuleButtonRow(
                module = module,
                updateUrl = updateUrl,
                onExecuteAction = {
                    onExecuteAction()
                    closeSearch()
                },
                onOpenWebUi = {
                    onClick()
                    closeSearch()
                },
                onUpdate = onUpdate,
                onUninstallClicked = onUninstallClicked,
                onAddShortcut = onAddShortcut,
            )
        }
    }
}

@Composable
private fun ModuleButtonRow(
    module: Module,
    updateUrl: String,
    onExecuteAction: () -> Unit,
    onOpenWebUi: () -> Unit,
    onUpdate: () -> Unit,
    onUninstallClicked: () -> Unit,
    onAddShortcut: (ShortcutType) -> Unit,
) {
    val hasUpdate = updateUrl.isNotEmpty()
    val iconSize = ButtonDefaults.iconSizeFor(ButtonDefaults.MinHeight)
    val actionButtonsEnabled = !module.remove && module.enabled

    val actionText = stringResource(id = R.string.action)
    val openText = stringResource(id = R.string.open)
    val updateText = stringResource(id = R.string.module_update)
    val uninstallText = stringResource(id = R.string.uninstall)
    val undoText = stringResource(id = R.string.undo)

    val startButtons = remember(actionButtonsEnabled, actionText, openText) {
        buildList {
            add(
                ButtonSpec(
                    id = "action",
                    text = actionText,
                    isVisible = module.hasActionScript,
                    isEnabled = actionButtonsEnabled,
                    icon = {
                        Icon(
                            modifier = Modifier.size(iconSize),
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = null
                        )
                    },
                    onClick = onExecuteAction,
                    buttonPosition = ButtonPosition.START,
                )
            )
            add(
                ButtonSpec(
                    id = "webui",
                    text = openText,
                    isVisible = module.hasWebUi,
                    isEnabled = actionButtonsEnabled,
                    icon = {
                        Icon(
                            modifier = Modifier.size(iconSize),
                            painter = painterResource(R.drawable.ic_wysiwyg_rounded),
                            contentDescription = openText
                        )
                    },
                    onClick = onOpenWebUi,
                    buttonPosition = ButtonPosition.START,
                )
            )
        }
    }

    val endButtons = remember(hasUpdate, updateText, uninstallText, undoText, module.remove) {
        buildList {
            add(
                ButtonSpec(
                    id = "update",
                    text = updateText,
                    isVisible = hasUpdate,
                    isEnabled = !module.remove,
                    type = ButtonType.PRIMARY,
                    icon = {
                        Icon(
                            modifier = Modifier.size(iconSize),
                            painter = painterResource(R.drawable.ic_download_2_rounded),
                            contentDescription = updateText
                        )
                    },
                    onClick = onUpdate,
                    buttonPosition = ButtonPosition.END,
                )
            )
            add(
                ButtonSpec(
                    id = "uninstall",
                    text = if (module.remove) undoText else uninstallText,
                    isVisible = true,
                    isEnabled = true,
                    icon = {
                        Icon(
                            modifier = Modifier.size(iconSize),
                            painter = painterResource(
                                if (module.remove) R.drawable.ic_undo_rounded_filled else R.drawable.ic_delete_rounded_filled
                            ),
                            contentDescription = if (module.remove) undoText else uninstallText
                        )
                    },
                    onClick = onUninstallClicked,
                    buttonPosition = ButtonPosition.END,
                )
            )
        }
    }

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
    ) { spec, isExpanded ->
        val isLastVisibleInGroup = when (spec.buttonPosition) {
            ButtonPosition.START -> spec.id == lastVisibleStartButtonId
            ButtonPosition.END -> spec.id == lastVisibleEndButtonId
        }
        val animatedEndPadding by animateDpAsState(
            targetValue = if (isLastVisibleInGroup) 0.dp else 8.dp,
            label = "ModuleButtonSpacing"
        )
        val buttonModifier = Modifier.padding(end = animatedEndPadding)

        val onLongClick = when (spec.id) {
            "action" -> ({ onAddShortcut(ShortcutType.Action) })
            "webui" -> ({ onAddShortcut(ShortcutType.WebUI) })
            else -> null
        }
        ActionButton(
            modifier = buttonModifier,
            text = spec.text,
            icon = spec.icon,
            onClick = spec.onClick,
            onLongClick = onLongClick,
            visible = spec.isVisible,
            enabled = spec.isEnabled,
            isExpanded = isExpanded,
            buttonType = spec.type,
            buttonPosition = spec.buttonPosition,
        )
    }
}

@Composable
private fun SortMenu(
    uiState: ModuleUiState,
    actions: ModuleActions,
) {
    var showDropdown by remember { mutableStateOf(false) }
    IconButton(
        onClick = { showDropdown = true }
    ) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = stringResource(id = R.string.settings)
        )
        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.module_sort_action_first)) },
                trailingIcon = { Checkbox(uiState.sortActionFirst, null) },
                onClick = {
                    actions.onToggleSortActionFirst()
                    showDropdown = false
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.module_sort_enabled_first)) },
                trailingIcon = { Checkbox(uiState.sortEnabledFirst, null) },
                onClick = {
                    actions.onToggleSortEnabledFirst()
                    showDropdown = false
                }
            )
        }
    }
}

@Composable
private fun ModuleInstallFAB(
    uiState: ModuleUiState,
    actions: ModuleActions,
    bottomInnerPadding: Dp
) {
    val moduleInstall = stringResource(id = R.string.module_install)
    var zipUris by rememberSaveable { mutableStateOf<List<Uri>>(emptyList()) }
    InstallModuleDialog(
        zipUris, uiState.moduleList,
        onConfirmInstall = { actions.onOpenFlash(zipUris) },
        onDismiss = { zipUris = emptyList() }
    )

    val selectZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        val uris = mutableListOf<Uri>()
        if (activityResult.resultCode != RESULT_OK) {
            return@rememberLauncherForActivityResult
        }
        val data = activityResult.data ?: return@rememberLauncherForActivityResult
        val clipData = data.clipData

        if (clipData != null) {
            for (i in 0 until clipData.itemCount) {
                clipData.getItemAt(i)?.uri?.let { uris.add(it) }
            }
        } else {
            data.data?.let { uris.add(it) }
        }
        zipUris = uris
    }

    ExtendedFloatingActionButton(
        modifier = Modifier
            .padding(bottom = bottomInnerPadding + fABBottomPadding(hasNavBar = true))
            .animateFloatingActionButton(uiState.installButtonVisible, alignment = Alignment.CenterEnd),
        onClick = {
            // Select the zip files to install
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            selectZipLauncher.launch(intent)
        },
        icon = { Icon(Icons.Filled.Add, moduleInstall) },
        text = { Text(text = moduleInstall) },
    )
}
