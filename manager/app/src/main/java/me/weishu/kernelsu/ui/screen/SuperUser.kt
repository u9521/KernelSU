package me.weishu.kernelsu.ui.screen

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarScrollBehavior
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.AppIconImage
import me.weishu.kernelsu.ui.component.SearchBar
import me.weishu.kernelsu.ui.component.SearchStatus
import me.weishu.kernelsu.ui.component.StatusTag
import me.weishu.kernelsu.ui.component.popUps.PopupFeedBack
import me.weishu.kernelsu.ui.component.scrollbar.ScrollbarDefaults
import me.weishu.kernelsu.ui.component.scrollbar.VerticalScrollbar
import me.weishu.kernelsu.ui.component.scrollbar.rememberScrollbarAdapter
import me.weishu.kernelsu.ui.navigation3.LocalHasDetailPane
import me.weishu.kernelsu.ui.navigation3.LocalNavController
import me.weishu.kernelsu.ui.navigation3.Route
import me.weishu.kernelsu.ui.theme.defaultTopAppBarColors
import me.weishu.kernelsu.ui.util.isRailNavbar
import me.weishu.kernelsu.ui.util.ownerNameForUid
import me.weishu.kernelsu.ui.viewmodel.GroupedApps
import me.weishu.kernelsu.ui.viewmodel.SuperUserViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SuperUserScreen() {
    val viewModel = viewModel<SuperUserViewModel>()
    val searchBarSB = SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()
    val topAppBarSB = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val uiState by viewModel.uiState.collectAsState()
    val searchStatus = uiState.searchStatus
    val context = LocalContext.current
    var isInitialized by rememberSaveable { mutableStateOf(false) }
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    LaunchedEffect(key1 = Unit) {
        when {
            !isInitialized || uiState.appList.isEmpty() -> {
                viewModel.setShowSystemApps(prefs.getBoolean("show_system_apps", false))
                viewModel.setShowOnlyPrimaryUserApps(prefs.getBoolean("show_only_primary_user_apps", false))
                viewModel.loadAppList()
            }

            viewModel.isNeedRefresh -> {
                viewModel.loadAppList(resort = false)
            }
        }
    }

    Scaffold(
        topBar = {
            SuperUserTopBar(viewModel, prefs, topAppBarSB)
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        Box(
            Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            GroupedAppList(
                modifier = Modifier.fillMaxSize(),
                lazyListModifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(topAppBarSB.nestedScrollConnection)
                    .nestedScroll(searchBarSB.nestedScrollConnection),
                viewModel,
                false
            )
            SuperUserSearchBar(searchStatus, viewModel, searchBarSB)
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
private fun SuperUserSearchBar(
    searchStatus: SearchStatus,
    viewModel: SuperUserViewModel,
    searchBarSB: SearchBarScrollBehavior,
) {
    val scope = rememberCoroutineScope()
    SearchBar(searchStatus = searchStatus, onSearchStatusChange = {
        viewModel.updateSearchStatus(it)
        scope.launch {
            viewModel.updateSearchText(it.searchText)
        }
    }, scrollBehavior = searchBarSB, emptyContent = {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("no Apps found")
        }
    }, content = {
        GroupedAppList(
            modifier = Modifier.fillMaxSize(),
            lazyListModifier = Modifier.fillMaxSize(),
            viewModel = viewModel,
            inSearchBar = true,
            closeSearchBar = { viewModel.updateSearchStatus(searchStatus.copy(current = SearchStatus.Status.COLLAPSED)) })
    })
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
private fun SuperUserTopBar(
    viewModel: SuperUserViewModel, prefs: SharedPreferences, scrollBehavior: TopAppBarScrollBehavior
) {
    val navigator = LocalNavController.current
    val onBack = if (LocalHasDetailPane.current) {
        dropUnlessResumed { navigator.popBackStack() }
    } else null
    LargeFlexibleTopAppBar(
        colors = defaultTopAppBarColors(), title = {
            Text(stringResource(R.string.superuser))
        }, actions = { FilterMenu(viewModel, prefs) }, navigationIcon = {
            AnimatedVisibility(
                visible = onBack != null, enter = fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()) + expandHorizontally(
                    expandFrom = Alignment.Start, animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()
                ), exit = fadeOut(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()) + shrinkHorizontally(
                    shrinkTowards = Alignment.Start, animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()
                )
            ) {
                IconButton(
                    onClick = onBack ?: {},
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            }
        }, scrollBehavior = scrollBehavior
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
private fun GroupedAppList(
    modifier: Modifier, lazyListModifier: Modifier, viewModel: SuperUserViewModel, inSearchBar: Boolean, closeSearchBar: () -> Unit = {}
) {
    val navigator = LocalNavController.current
    var expandedUids by rememberSaveable { mutableStateOf(setOf<Int>()) }
    val state = rememberPullToRefreshState()
    val uiState by viewModel.uiState.collectAsState()
    val groups = if (inSearchBar) {
        val searchSet = uiState.searchResults.toSet()
        uiState.groupedApps.filter { it.apps.intersect(searchSet).isNotEmpty() }
    } else {
        uiState.groupedApps
    }
    if (inSearchBar) {
        expandedUids = groups.map { it.uid }.toSet()
    }
    val topPadding = (if (inSearchBar) 16.dp else uiState.searchStatus.offsetY).coerceAtLeast(16.dp)
    PullToRefreshBox(modifier = modifier, state = state, onRefresh = { viewModel.loadAppList(true) }, isRefreshing = uiState.isRefreshing, indicator = {
        PullToRefreshDefaults.LoadingIndicator(
            state = state, isRefreshing = uiState.isRefreshing, modifier = Modifier
                .align(
                    Alignment.TopCenter
                )
                .padding(top = topPadding)
        )
    }) {
        val bottomPadding = if (isRailNavbar()) WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding() else 0.dp
        val listState = rememberLazyListState()
        LazyColumn(
            state = listState,
            modifier = lazyListModifier,
            contentPadding = PaddingValues(top = topPadding, bottom = 16.dp + bottomPadding, start = 16.dp, end = 16.dp)
        ) {
            if (groups.isEmpty()) return@LazyColumn
            itemsIndexed(groups, key = { _, group -> group.uid }) { index, group ->
                val count = groups.size
                val isExpanded = expandedUids.contains(group.uid) && group.isGroup()
                val isPrevExpanded = if (index > 0) expandedUids.contains(groups[index - 1].uid) && groups[index - 1].isGroup() else true
                val isNextExpanded = if (index < count - 1) expandedUids.contains(groups[index + 1].uid) && groups[index + 1].isGroup() else true

                val targetTopRadius = if (isExpanded || isPrevExpanded) 16.dp else 4.dp
                val targetBottomRadius = if (isExpanded) 4.dp else if (isNextExpanded) 16.dp else 4.dp
                val targetTopPadding = if (index == 0) 0.dp else if (isExpanded || isPrevExpanded) 16.dp else ListItemDefaults.SegmentedGap

                val animatedTopRadius by animateDpAsState(
                    targetValue = targetTopRadius, animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(), label = "TopCorner"
                )
                val animatedBottomRadius by animateDpAsState(
                    targetValue = targetBottomRadius, animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(), label = "BottomCorner"
                )
                val animatedTopPadding by animateDpAsState(
                    targetValue = targetTopPadding, animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(), label = "TopPadding"
                )

                val itemShape = RoundedCornerShape(
                    topStart = animatedTopRadius, topEnd = animatedTopRadius, bottomStart = animatedBottomRadius, bottomEnd = animatedBottomRadius
                )

                GroupItem(
                    group = group, shape = itemShape, onToggleExpand = if (group.apps.size > 1) {
                        {
                            expandedUids = if (isExpanded) expandedUids - group.uid else expandedUids + group.uid
                        }
                    } else null, onClickPrimary = {
                        navigator.navigateTo(Route.AppProfile(uid = group.uid, packageName = group.primary.packageName))
                        viewModel.markNeedRefresh()
                        closeSearchBar()
                    }, modifier = Modifier.padding(top = animatedTopPadding.coerceAtLeast(0.dp)), expanded = isExpanded
                )
            }
        }
        VerticalScrollbar(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(),
            adapter = rememberScrollbarAdapter(listState),
            durationMillis = 1500L,
            style = ScrollbarDefaults.style.copy(
                color = MaterialTheme.colorScheme.primary, railColor = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.5f)
            )
        )
    }
}

private fun GroupedApps.isGroup(): Boolean {
    return this.apps.size > 1
}

@Composable
private fun FilterMenu(viewModel: SuperUserViewModel, prefs: SharedPreferences) {
    val showDropdown = remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val isMultiUser = remember(uiState.userIds) {
        uiState.userIds.size > 1
    }
    IconButton(
        onClick = { showDropdown.value = true },
    ) {
        Icon(
            imageVector = Icons.Filled.MoreVert, contentDescription = stringResource(id = R.string.settings)
        )
        DropdownMenu(expanded = showDropdown.value, onDismissRequest = { showDropdown.value = false }) {
            PopupFeedBack()
            val showSysOnclick = {
                viewModel.setShowSystemApps(!uiState.showSystemApps)
                prefs.edit {
                    putBoolean("show_system_apps", !uiState.showSystemApps)
                }
                showDropdown.value = false
            }
            val showPrimaryUserOnclick = {
                viewModel.setShowOnlyPrimaryUserApps(!uiState.showOnlyPrimaryUserApps)
                prefs.edit {
                    putBoolean("show_only_primary_user_apps", uiState.showOnlyPrimaryUserApps)
                }
                showDropdown.value = false
            }
            DropdownMenuItem(
                trailingIcon = {
                    Checkbox(uiState.showSystemApps, null)
                }, text = {
                    Text(
                        stringResource(R.string.show_system_apps)
                    )
                }, onClick = showSysOnclick
            )
            if (isMultiUser) {
                DropdownMenuItem(
                    trailingIcon = {
                        Checkbox(uiState.showOnlyPrimaryUserApps, null)
                    }, text = {
                        Text(
                            stringResource(R.string.show_only_primary_user_apps)
                        )
                    }, onClick = showPrimaryUserOnclick
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun AppIconItem(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    supportingContent: @Composable () -> Unit,
    shapes: ListItemShapes,
    packageInfo: PackageInfo,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    SegmentedListItem(
        modifier = modifier,
        onClick = onClick ?: {},
        onLongClick = onLongClick,
        verticalAlignment = Alignment.CenterVertically,
        colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
        shapes = shapes,
        leadingContent = {
            AppIconImage(
                packageInfo = packageInfo, modifier = Modifier
                    .padding(4.dp)
                    .size(48.dp)
            )
        },
        content = title,
        supportingContent = supportingContent
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun GroupItem(
    group: GroupedApps,
    shape: Shape,
    expanded: Boolean,
    onToggleExpand: (() -> Unit)?,
    onClickPrimary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isGroup = group.apps.size > 1
    val title = if (isGroup) {
        "${ownerNameForUid(group.uid)} (${group.uid})"
    } else {
        group.primary.label
    }
    val summaryText = if (isGroup) {
        stringResource(R.string.group_contains_apps, group.apps.size)
    } else {
        group.primary.packageName
    }

    Column(modifier = modifier) {
        AppIconItem(
            onClick = onClickPrimary,
            onLongClick = onToggleExpand,
            shapes = ListItemDefaults.shapes(shape = shape, pressedShape = shape),
            packageInfo = group.primary.packageInfo,
            title = { Text(title) },
            supportingContent = {
                Column {
                    Text(summaryText)
                    GroupTags(group)
                }
            },
        )
        AnimatedVisibility(
            visible = expanded && group.apps.size > 1,
            enter = expandVertically(MaterialTheme.motionScheme.defaultEffectsSpec()) + fadeIn(MaterialTheme.motionScheme.slowSpatialSpec()),
            exit = shrinkVertically(MaterialTheme.motionScheme.defaultEffectsSpec()) + fadeOut(MaterialTheme.motionScheme.fastEffectsSpec())
        ) {
            Column(
                modifier = Modifier.padding(top = ListItemDefaults.SegmentedGap), verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
            ) {
                group.apps.forEachIndexed { appIndex, app ->
                    val shapes = ListItemDefaults.segmentedShapes(appIndex + 1, group.apps.size + 1).let { it.copy(pressedShape = it.shape) }
                    AppIconItem(title = { Text(app.label) }, supportingContent = { Text(app.packageName) }, shapes = shapes, packageInfo = app.packageInfo)
                }
            }
        }
    }
}

@Composable
private fun GroupTags(group: GroupedApps) {
    val userId = group.uid / 100000
    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (userId != 0) {
            StatusTag(label = "USER $userId", contentColor = MaterialTheme.colorScheme.onPrimary, backgroundColor = MaterialTheme.colorScheme.primary)
        }
        if (group.anyAllowSu) {
            StatusTag("ROOT", contentColor = MaterialTheme.colorScheme.onTertiaryContainer, backgroundColor = MaterialTheme.colorScheme.tertiaryContainer)
        } else if (Natives.uidShouldUmount(group.uid)) {
            StatusTag("UMOUNT", contentColor = MaterialTheme.colorScheme.onSecondaryContainer, backgroundColor = MaterialTheme.colorScheme.secondaryContainer)
        }
        if (group.anyCustom) {
            StatusTag("CUSTOM", contentColor = MaterialTheme.colorScheme.onPrimaryContainer, backgroundColor = MaterialTheme.colorScheme.primaryContainer)
        }
    }
}
