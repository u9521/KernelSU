package me.weishu.kernelsu.ui.screen.superuser

import android.content.pm.ApplicationInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import me.weishu.kernelsu.R
import me.weishu.kernelsu.data.model.AppInfo
import me.weishu.kernelsu.ui.component.AppIconImage
import me.weishu.kernelsu.ui.component.breeze.BreezeBackButton
import me.weishu.kernelsu.ui.component.breeze.PopupFeedBack
import me.weishu.kernelsu.ui.component.breeze.ScrollbarDefaults
import me.weishu.kernelsu.ui.component.breeze.VerticalScrollbar
import me.weishu.kernelsu.ui.component.breeze.keyDownFeedBack
import me.weishu.kernelsu.ui.component.breeze.rememberScrollbarAdapter
import me.weishu.kernelsu.ui.component.material.ExpressiveScaffold
import me.weishu.kernelsu.ui.component.material.SearchAppBarBreeze
import me.weishu.kernelsu.ui.component.material.disableDrag
import me.weishu.kernelsu.ui.component.statustag.StatusTag
import me.weishu.kernelsu.ui.navigation3.LocalNavigator
import me.weishu.kernelsu.ui.navigation3.breeze.LocalHasDetailPane
import me.weishu.kernelsu.ui.navigation3.breeze.isRailNavbar
import me.weishu.kernelsu.ui.util.onlyHorizontal
import me.weishu.kernelsu.ui.util.ownerNameForUid
import me.weishu.kernelsu.ui.util.topBarHazeEffect

@Composable
fun SuperUserPagerBreeze(
    uiState: SuperUserUiState,
    actions: SuperUserActions,
    bottomInnerPadding: Dp,
) {
    val scope = rememberCoroutineScope()
    val navigator = LocalNavigator.current
    val onBack = if (LocalHasDetailPane.current) {
        dropUnlessResumed { navigator.pop() }
    } else {
        null
    }
    val hazeState = rememberHazeState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior().disableDrag()
    val searchBarScrollBehavior = SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()
    val listState = rememberLazyListState()
    val searchListState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()
    val snackbarHostState = remember { SnackbarHostState() }
    val expandedUids = remember { mutableStateOf(setOf<Int>()) }
    val showListScrollbar by remember {
        derivedStateOf { scrollBehavior.state.collapsedFraction > 0.99f }
    }

    val scaleFraction = {
        if (uiState.isRefreshing) 1f
        else LinearOutSlowInEasing.transform(pullToRefreshState.distanceFraction).coerceIn(0f, 1f)
    }

    var localSearchText by remember { mutableStateOf(uiState.searchStatus.searchText) }
    LaunchedEffect(uiState.searchStatus.searchText) {
        localSearchText = uiState.searchStatus.searchText
    }

    val haptic = LocalHapticFeedback.current

    ExpressiveScaffold(
        modifier = Modifier
            .pullToRefresh(
                state = pullToRefreshState,
                isRefreshing = uiState.isRefreshing,
                onRefresh = {
                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    actions.onRefresh()
                },
            ),
        topBar = {
            SearchAppBarBreeze(
                modifier = Modifier.topBarHazeEffect(hazeState, scrollBehavior),
                title = { Text(stringResource(R.string.superuser)) },
                searchText = localSearchText,
                navigationIcon = {
                    Row {
                        BackIconButton(onBack, scrollBehavior)
                        IconButton(onClick = actions.onOpenSulog) {
                            Icon(
                                painter = painterResource(R.drawable.ic_article_rounded_filled),
                                contentDescription = stringResource(R.string.settings_sulog),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                onSearchTextChange = {
                    localSearchText = it
                    actions.onSearchTextChange(it)
                    scope.launch { listState.scrollToItem(0) }
                },
                onClearClick = {
                    localSearchText = ""
                    actions.onClearSearch()
                },
                actions = {
                    SortMenu(uiState, actions)
                    FilterMenu(uiState, actions)
                },
                scrollBehavior = scrollBehavior,
                searchBarScrollBehavior = searchBarScrollBehavior,
                snackbarHostState = snackbarHostState,
                searchContent = { bottomPadding, closeSearch ->
                    LaunchedEffect(localSearchText) {
                        searchListState.scrollToItem(0)
                    }
                    GroupedAppList(
                        groups = uiState.searchResults,
                        listState = searchListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 16.dp + bottomPadding
                        ),
                        expandGroups = true,
                        onGroupClick = { group ->
                            closeSearch()
                            actions.onOpenProfile(group)
                        },
                        onAppClick = { group, _ ->
                            closeSearch()
                            actions.onOpenProfile(group)
                        },
                    )
                    VerticalScrollbar(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .padding(bottom = bottomPadding),
                        adapter = rememberScrollbarAdapter(searchListState),
                        durationMillis = 1500L,
                        visible = showListScrollbar,
                        style = ScrollbarDefaults.style.copy(
                            color = MaterialTheme.colorScheme.primary,
                            railColor = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.5f),
                        )
                    )
                }
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding.onlyHorizontal())
                .hazeSource(hazeState)
        ) {
            GroupedAppList(
                groups = uiState.groupedApps,
                listState = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .nestedScroll(searchBarScrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp + innerPadding.calculateTopPadding(),
                    bottom = 16.dp + bottomInnerPadding
                ),
                expandedUids = expandedUids.value,
                onToggleExpand = { uid ->
                    expandedUids.value = if (expandedUids.value.contains(uid)) {
                        expandedUids.value - uid
                    } else {
                        expandedUids.value + uid
                    }
                },
                onGroupClick = actions.onOpenProfile,
                onAppClick = { group, _ -> actions.onOpenProfile(group) },
            )
            val scrollBottomPadding = if (isRailNavbar()) 0.dp else bottomInnerPadding
            VerticalScrollbar(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .navigationBarsPadding()
                    .padding(top = innerPadding.calculateTopPadding(), bottom = scrollBottomPadding),
                adapter = rememberScrollbarAdapter(listState),
                durationMillis = 1500L,
                visible = showListScrollbar,
                style = ScrollbarDefaults.style.copy(
                    color = MaterialTheme.colorScheme.primary,
                    railColor = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.5f)
                )
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
                    isRefreshing = uiState.isRefreshing
                )
            }
        }
    }
}

@Composable
private fun GroupedAppList(
    groups: List<GroupedApps>,
    listState: LazyListState,
    modifier: Modifier,
    contentPadding: PaddingValues,
    expandedUids: Set<Int> = emptySet(),
    expandGroups: Boolean = false,
    onToggleExpand: ((Int) -> Unit)? = null,
    onGroupClick: (GroupedApps) -> Unit,
    onAppClick: (GroupedApps, AppInfo) -> Unit,
) {
    // lock auto scroll
    remember(groups) {
        val currentIndex = Snapshot.withoutReadObservation { listState.firstVisibleItemIndex }
        val currentOffset = Snapshot.withoutReadObservation { listState.firstVisibleItemScrollOffset }
        listState.requestScrollToItem(currentIndex, currentOffset)
        null
    }
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
        itemsIndexed(
            items = groups,
            key = { _, group -> group.uid },
        ) { index, group ->
            val isExpanded = (expandGroups || expandedUids.contains(group.uid)) && group.apps.size > 1
            val previousExpanded = if (index > 0) {
                val previous = groups[index - 1]
                (expandGroups || expandedUids.contains(previous.uid)) && previous.apps.size > 1
            } else {
                true
            }
            val nextExpanded = if (index < groups.lastIndex) {
                val next = groups[index + 1]
                (expandGroups || expandedUids.contains(next.uid)) && next.apps.size > 1
            } else {
                true
            }

            val targetTopRadius = if (isExpanded || previousExpanded) 16.dp else 4.dp
            val targetBottomRadius = if (isExpanded) 4.dp else if (nextExpanded) 16.dp else 4.dp
            val targetTopPadding = if (index == 0) 0.dp else if (isExpanded || previousExpanded) {
                16.dp
            } else {
                ListItemDefaults.SegmentedGap
            }

            val animatedTopRadius by animateDpAsState(
                targetValue = targetTopRadius,
                animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
                label = "TopCorner",
            )
            val animatedBottomRadius by animateDpAsState(
                targetValue = targetBottomRadius,
                animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
                label = "BottomCorner",
            )
            val animatedTopPadding by animateDpAsState(
                targetValue = targetTopPadding,
                animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
                label = "TopPadding",
            )

            val itemShape = RoundedCornerShape(
                topStart = animatedTopRadius,
                topEnd = animatedTopRadius,
                bottomStart = animatedBottomRadius,
                bottomEnd = animatedBottomRadius,
            )

            Column(
                modifier = Modifier
                    .padding(top = animatedTopPadding.coerceAtLeast(0.dp))
                    .animateItem()
            ) {
                GroupItem(
                    group = group,
                    shape = itemShape,
                    onToggleExpand = if (group.apps.size > 1 && !expandGroups) {
                        { onToggleExpand?.invoke(group.uid) }
                    } else {
                        null
                    },
                ) { onGroupClick(group) }
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(MaterialTheme.motionScheme.defaultEffectsSpec()) +
                            fadeIn(MaterialTheme.motionScheme.slowSpatialSpec()),
                    exit = shrinkVertically(MaterialTheme.motionScheme.defaultEffectsSpec()) +
                            fadeOut(MaterialTheme.motionScheme.fastEffectsSpec())
                ) {
                    Column(
                        modifier = Modifier.padding(top = ListItemDefaults.SegmentedGap),
                        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                    ) {
                        group.apps.forEachIndexed { appIndex, app ->
                            val shapes = ListItemDefaults.segmentedShapes(
                                index = appIndex + 1,
                                count = group.apps.size + 1,
                            )
                            SimpleAppItem(
                                app = app,
                                matched = group.matchedPackageNames.contains(app.packageName),
                                shape = shapes.shape,
                            ) {
                                onAppClick(group, app)
                            }
                        }
                    }
                }
            }
        }
        item("bottomPadding") {
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun AppIconItem(
    title: @Composable () -> Unit,
    supportingContent: @Composable () -> Unit,
    shapes: ListItemShapes,
    packageInfo: android.content.pm.PackageInfo,
    modifier: Modifier = Modifier,
    iconSize: Dp = 48.dp,
    colors: androidx.compose.material3.ListItemColors = ListItemDefaults.segmentedColors(
        containerColor = MaterialTheme.colorScheme.surfaceBright,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceBright,
        supportingContentColor = MaterialTheme.colorScheme.outline,
    ),
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    SegmentedListItem(
        modifier = modifier,
        onClick = onClick ?: {},
        onLongClick = onLongClick,
        verticalAlignment = Alignment.CenterVertically,
        colors = colors,
        shapes = shapes,
        leadingContent = {
            AppIconImage(
                packageInfo = packageInfo,
                label = packageInfo.packageName,
                modifier = Modifier
                    .padding(4.dp)
                    .size(iconSize)
            )
        },
        content = title,
        supportingContent = supportingContent,
    )
}

@Composable
private fun SimpleAppItem(
    app: AppInfo,
    matched: Boolean = false,
    shape: Shape = RoundedCornerShape(0.dp),
    onNavigate: () -> Unit,
) {
    val colors = if (matched) {
        ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            supportingContentColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f),
        )
    } else {
        ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.surfaceBright,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceBright,
            supportingContentColor = MaterialTheme.colorScheme.outline,
        )
    }

    AppIconItem(
        title = { Text(app.label, overflow = TextOverflow.Ellipsis, maxLines = 1) },
        supportingContent = { Text(app.packageName, overflow = TextOverflow.Ellipsis, maxLines = 1) },
        shapes = ListItemDefaults.shapes(shape = shape, pressedShape = shape),
        packageInfo = app.packageInfo,
        iconSize = 40.dp,
        colors = colors,
        onClick = onNavigate,
    )
}

@Composable
private fun GroupItem(
    group: GroupedApps,
    shape: Shape,
    onToggleExpand: (() -> Unit)?,
    onClickPrimary: () -> Unit,
) {
    val isGroup = group.apps.size > 1
    val title = if (isGroup) {
        "${group.ownerName ?: ownerNameForUid(group.uid)} (${group.uid})"
    } else {
        group.primary.label
    }
    val summaryText = if (isGroup) {
        stringResource(R.string.group_contains_apps, group.apps.size)
    } else {
        group.primary.packageName
    }
    SegmentedListItem(
        onClick = onClickPrimary,
        onLongClick = onToggleExpand,
        verticalAlignment = Alignment.CenterVertically,
        colors = ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.surfaceBright,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceBright,
            supportingContentColor = MaterialTheme.colorScheme.outline,
        ),
        shapes = ListItemDefaults.shapes(shape = shape, pressedShape = shape),
        content = {
            Text(
                text = title,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        },
        supportingContent = {
            Column {
                Text(
                    text = summaryText,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                FlowRow {
                    val userId = group.uid / 100000
                    val packageInfo = group.primary.packageInfo
                    val applicationFlags = packageInfo.applicationInfo?.flags ?: 0

                    if (userId != 0) {
                        StatusTag(
                            label = "USER $userId",
                            modifier = Modifier.padding(top = 4.dp),
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            backgroundColor = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (group.anyAllowSu) {
                        StatusTag(
                            label = "ROOT",
                            modifier = Modifier.padding(top = 4.dp),
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    } else if (group.shouldUmount) {
                        StatusTag(
                            label = "UMOUNT",
                            modifier = Modifier.padding(top = 4.dp),
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            backgroundColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    }
                    if (group.anyCustom) {
                        StatusTag(
                            label = "CUSTOM",
                            modifier = Modifier.padding(top = 4.dp),
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    }
                    if (applicationFlags.and(ApplicationInfo.FLAG_SYSTEM) != 0
                        || applicationFlags.and(ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                    ) {
                        StatusTag(
                            label = "SYSTEM",
                            modifier = Modifier.padding(top = 4.dp),
                            contentColor = MaterialTheme.colorScheme.onTertiary,
                            backgroundColor = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    if (!packageInfo.sharedUserId.isNullOrEmpty()) {
                        StatusTag(
                            label = "SHARED UID",
                            modifier = Modifier.padding(top = 4.dp),
                            contentColor = MaterialTheme.colorScheme.onTertiary,
                            backgroundColor = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        },
        leadingContent = {
            AppIconImage(
                packageInfo = group.primary.packageInfo,
                label = group.primary.label,
                modifier = Modifier.size(48.dp)
            )
        },
    )
}

@Composable
private fun BackIconButton(onBack: (() -> Unit)?, scrollBehavior: TopAppBarScrollBehavior) {
    AnimatedVisibility(
        visible = onBack != null,
        enter = fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()) +
                expandHorizontally(
                    expandFrom = Alignment.Start,
                    animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                ),
        exit = fadeOut(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()) +
                shrinkHorizontally(
                    shrinkTowards = Alignment.Start,
                    animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                ),
    ) {
        BreezeBackButton(
            onClick = onBack ?: {},
            collapseFraction = scrollBehavior.state.collapsedFraction,
        )
    }
}


@Composable
private fun FilterMenu(
    uiState: SuperUserUiState,
    actions: SuperUserActions,
) {
    var showDropdown by remember { mutableStateOf(false) }

    IconButton(onClick = { showDropdown = true }) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = stringResource(id = R.string.settings)
        )

        DropdownMenuPopup(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false }
        ) {
            DropdownMenuGroup(shapes = MenuDefaults.groupShapes()) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.show_system_apps)) },
                    checked = uiState.showSystemApps,
                    checkedLeadingIcon = {
                        Icon(
                            Icons.Filled.Check,
                            modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                            contentDescription = null,
                        )
                    },
                    onCheckedChange = {
                        actions.onToggleShowSystemApps()
                    },
                    shapes = MenuDefaults.itemShape(index = 0, count = 2)
                )
                if (uiState.userIds.size > 1) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.show_only_primary_user_apps)) },
                        checked = uiState.showOnlyPrimaryUserApps,
                        checkedLeadingIcon = {
                            Icon(
                                Icons.Filled.Check,
                                modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                                contentDescription = null,
                            )
                        },
                        onCheckedChange = {
                            actions.onToggleShowOnlyPrimaryUserApps()
                        },
                        shapes = MenuDefaults.itemShape(index = 1, count = 2)
                    )
                }
            }
        }
    }
}

@Composable
private fun SortMenu(
    uiState: SuperUserUiState,
    actions: SuperUserActions,
) {
    var showSortMenu by remember { mutableStateOf(false) }
    val keydownFB = keyDownFeedBack()
    val isReverse = uiState.sortOption % 2 != 0
    val currentSortType = uiState.sortOption / 2
    val sortResIds = listOf(
        R.string.sort_by_name,
        R.string.sort_by_package_name,
        R.string.sort_by_install_time,
        R.string.sort_by_update_time,
    )
    IconButton(onClick = { showSortMenu = true }) {
        Icon(
            painter = painterResource(R.drawable.ic_sort_rounded),
            contentDescription = stringResource(R.string.menu_sort)
        )

        DropdownMenuPopup(
            expanded = showSortMenu,
            onDismissRequest = { showSortMenu = false }
        ) {
            PopupFeedBack()
            DropdownMenuGroup(shapes = MenuDefaults.groupShape(index = 0, count = 2)) {
                sortResIds.forEachIndexed { index, resId ->
                    DropdownMenuItem(
                        text = { Text(stringResource(resId)) },
                        selected = currentSortType == index,
                        selectedLeadingIcon = {
                            Icon(
                                Icons.Filled.Check,
                                modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            keydownFB()
                            val newOption = index * 2 + (if (isReverse) 1 else 0)
                            actions.onUpdateSortOption(newOption)
                        },
                        shapes = MenuDefaults.itemShape(
                            index = index,
                            count = sortResIds.size + 1
                        )
                    )
                }
            }
            Spacer(Modifier.height(MenuDefaults.GroupSpacing))
            DropdownMenuGroup(shapes = MenuDefaults.groupShape(index = 1, count = 2)) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.sort_reverse)) },
                    selected = isReverse,
                    selectedLeadingIcon = {
                        Icon(
                            Icons.Filled.Check,
                            modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        keydownFB()
                        val newOption = currentSortType * 2 + (if (!isReverse) 1 else 0)
                        actions.onUpdateSortOption(newOption)
                    },
                    shapes = MenuDefaults.itemShape(
                        index = 1,
                        count = 2
                    ),
                )
            }
        }
    }
}
