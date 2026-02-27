package me.weishu.kernelsu.ui.screen

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ksuApp
import me.weishu.kernelsu.ui.component.AppIconImage
import me.weishu.kernelsu.ui.component.SearchAppBar
import me.weishu.kernelsu.ui.component.SearchStatus
import me.weishu.kernelsu.ui.component.StatusTag
import me.weishu.kernelsu.ui.component.popUps.PopupFeedBack
import me.weishu.kernelsu.ui.component.scrollbar.ScrollbarDefaults
import me.weishu.kernelsu.ui.component.scrollbar.VerticalScrollbar
import me.weishu.kernelsu.ui.component.scrollbar.rememberScrollbarAdapter
import me.weishu.kernelsu.ui.navigation3.LocalHasDetailPane
import me.weishu.kernelsu.ui.navigation3.LocalNavController
import me.weishu.kernelsu.ui.navigation3.Route
import me.weishu.kernelsu.ui.util.isRailNavbar
import me.weishu.kernelsu.ui.util.ownerNameForUid
import me.weishu.kernelsu.ui.util.pickPrimary
import me.weishu.kernelsu.ui.viewmodel.SuperUserViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SuperUserScreen() {
    val navigator = LocalNavController.current
    val viewModel = viewModel<SuperUserViewModel>()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val searchStatus by viewModel.searchStatus
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    LaunchedEffect(key1 = Unit) {
        when {
            viewModel.appList.value.isEmpty() -> {
                viewModel.showSystemApps = prefs.getBoolean("show_system_apps", false)
                viewModel.loadAppList()
            }

            viewModel.isNeedRefresh -> {
                viewModel.loadAppList()
            }
        }
    }

    LaunchedEffect(searchStatus.searchText) {
        viewModel.updateSearchText(searchStatus.searchText)
    }

    Scaffold(
        topBar = {
            val onBack = if (LocalHasDetailPane.current) {
                dropUnlessResumed { navigator.popBackStack() }
            } else null
            SearchAppBar(
                title = { Text(stringResource(R.string.superuser)) }, searchStatus = searchStatus, dropdownContent = {
                    FilterMenu(viewModel, prefs)
                }, scrollBehavior = scrollBehavior, onBackClick = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        val displayAppList = when (viewModel.searchStatus.value.resultStatus) {
            SearchStatus.ResultStatus.SHOW, SearchStatus.ResultStatus.EMPTY -> viewModel.searchResults.value.filter { it.packageName != ksuApp.packageName }
            else -> viewModel.appList.value.filter { it.packageName != ksuApp.packageName }
        }
        val groups = remember(displayAppList) {
            buildGroups(displayAppList)
        }
        var expandedUids by rememberSaveable { mutableStateOf(setOf<Int>()) }

        LaunchedEffect(viewModel.searchStatus.value.resultStatus, viewModel.searchResults.value) {
            when (viewModel.searchStatus.value.resultStatus) {
                SearchStatus.ResultStatus.SHOW -> {
                    val searchResultsByUid = viewModel.searchResults.value.groupBy { it.uid }
                    expandedUids = groups.filter { group ->
                        val appsInGroup = searchResultsByUid[group.uid] ?: emptyList()
                        appsInGroup.size > 1
                    }.map { it.uid }.toSet()
                }

                SearchStatus.ResultStatus.EMPTY, SearchStatus.ResultStatus.DEFAULT -> expandedUids = emptySet()

                SearchStatus.ResultStatus.LOAD -> {}
            }
        }
        val state = rememberPullToRefreshState()
        PullToRefreshBox(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize(),
            state = state,
            onRefresh = { viewModel.loadAppList(true) },
            isRefreshing = viewModel.isRefreshing,
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(state = state, isRefreshing = viewModel.isRefreshing, modifier = Modifier.align(Alignment.TopCenter))
            }) {
            val bottomPadding = if (isRailNavbar()) WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding() else 0.dp
            val listState = rememberLazyListState()
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp + bottomPadding, start = 16.dp, end = 16.dp)
            ) {
                itemsIndexed(groups, key = { _, group -> group.uid }) { index, group ->
                    val count = groups.size
                    val isExpanded = expandedUids.contains(group.uid)
                    val isPrevExpanded = if (index > 0) expandedUids.contains(groups[index - 1].uid) else true
                    val isNextExpanded = if (index < count - 1) expandedUids.contains(groups[index + 1].uid) else true

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
                            navigator.navigateTo(Route.AppProfile(group.primary.packageName))
                            viewModel.markNeedRefresh()
                        }, modifier = Modifier.padding(top = animatedTopPadding), expanded = isExpanded
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
}

@Composable
private fun FilterMenu(viewModel: SuperUserViewModel, prefs: SharedPreferences) {
    val showDropdown = remember { mutableStateOf(false) }
    IconButton(
        onClick = { showDropdown.value = true },
    ) {
        Icon(
            imageVector = Icons.Filled.MoreVert, contentDescription = stringResource(id = R.string.settings)
        )
        DropdownMenu(expanded = showDropdown.value, onDismissRequest = { showDropdown.value = false }) {
            PopupFeedBack()
            val interactionSource = remember { MutableInteractionSource() }
            val showSysOnclick = {
                viewModel.showSystemApps = !viewModel.showSystemApps
                prefs.edit {
                    putBoolean("show_system_apps", viewModel.showSystemApps)
                }
                viewModel.loadAppList()
                showDropdown.value = false
            }
            DropdownMenuItem(
                interactionSource = interactionSource, trailingIcon = {
                    Checkbox(viewModel.showSystemApps, { showSysOnclick() }, interactionSource = interactionSource)
                }, text = {
                    Text(
                        stringResource(R.string.show_system_apps)
                    )
                }, onClick = showSysOnclick
            )
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

@Immutable
private data class GroupedApps(
    val uid: Int,
    val apps: List<SuperUserViewModel.AppInfo>,
    val primary: SuperUserViewModel.AppInfo,
    val anyAllowSu: Boolean,
    val anyCustom: Boolean,
)

private fun buildGroups(apps: List<SuperUserViewModel.AppInfo>): List<GroupedApps> {
    val comparator = compareBy<SuperUserViewModel.AppInfo> {
        when {
            it.allowSu -> 0
            it.hasCustomProfile -> 1
            else -> 2
        }
    }.thenBy { it.label.lowercase() }
    val groups = apps.groupBy { it.uid }.map { (uid, list) ->
        val sorted = list.sortedWith(comparator)
        val primary = pickPrimary(sorted)
        GroupedApps(
            uid = uid,
            apps = sorted,
            primary = primary,
            anyAllowSu = sorted.any { it.allowSu },
            anyCustom = sorted.any { it.hasCustomProfile },
        )
    }
    return groups.sortedWith(Comparator { a, b ->
        fun rank(g: GroupedApps): Int = when {
            g.anyAllowSu -> 0
            g.anyCustom -> 1
            g.apps.size > 1 -> 2
            Natives.uidShouldUmount(g.uid) -> 4
            else -> 3
        }

        val ra = rank(a)
        val rb = rank(b)
        if (ra != rb) return@Comparator ra - rb
        return@Comparator when (ra) {
            2 -> a.uid.compareTo(b.uid)
            else -> a.primary.label.lowercase().compareTo(b.primary.label.lowercase())
        }
    })
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
            StatusTag(label = "UID$userId", contentColor = MaterialTheme.colorScheme.onPrimary, backgroundColor = MaterialTheme.colorScheme.primary)
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
