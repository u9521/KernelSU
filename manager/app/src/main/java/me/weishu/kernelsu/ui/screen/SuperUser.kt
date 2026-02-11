package me.weishu.kernelsu.ui.screen

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberTopAppBarState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ksuApp
import me.weishu.kernelsu.ui.component.AppIconImage
import me.weishu.kernelsu.ui.component.SearchAppBar
import me.weishu.kernelsu.ui.component.SearchStatus
import me.weishu.kernelsu.ui.navigation3.LocalHasDetailPane
import me.weishu.kernelsu.ui.navigation3.Route
import me.weishu.kernelsu.ui.util.LocalNavController
import me.weishu.kernelsu.ui.util.ownerNameForUid
import me.weishu.kernelsu.ui.util.pickPrimary
import me.weishu.kernelsu.ui.viewmodel.SuperUserViewModel

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SuperUserScreen() {
    val navigator = LocalNavController.current
    val viewModel = viewModel<SuperUserViewModel>()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val listState = rememberLazyListState()
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
            var showDropdown by remember { mutableStateOf(false) }
            val dissMissMenu = { showDropdown = false }
            val onBack = if (LocalHasDetailPane.current) {
                { navigator.popBackStack() }
            } else null
            SearchAppBar(
                title = { Text(stringResource(R.string.superuser)) }, searchStatus = searchStatus, dropdownContent = {
                    IconButton(
                        onClick = { showDropdown = true },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert, contentDescription = stringResource(id = R.string.settings)
                        )
                        DropdownMenu(expanded = showDropdown, onDismissRequest = dissMissMenu) {
                            val interactionSource = remember { MutableInteractionSource() }
                            val showSysOnclick = {
                                viewModel.showSystemApps = !viewModel.showSystemApps
                                prefs.edit {
                                    putBoolean("show_system_apps", viewModel.showSystemApps)
                                }
                                viewModel.loadAppList()
                                dissMissMenu()
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
                }, scrollBehavior = scrollBehavior, onBackClick = onBack
            )
        }, contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        val displayAppList = when (viewModel.searchStatus.value.resultStatus) {
            SearchStatus.ResultStatus.SHOW, SearchStatus.ResultStatus.EMPTY -> viewModel.searchResults.value.filter { it.packageName != ksuApp.packageName }
            else -> viewModel.appList.value.filter { it.packageName != ksuApp.packageName }
        }
        val groups = remember(displayAppList) {
            buildGroups(displayAppList)
        }
        val expandedUids = rememberSaveable { mutableStateOf(setOf<Int>()) }

        LaunchedEffect(viewModel.searchStatus.value.resultStatus, viewModel.searchResults.value) {
            when (viewModel.searchStatus.value.resultStatus) {
                SearchStatus.ResultStatus.SHOW -> {
                    // 搜索状态下，默认展开有多个应用的搜索结果组
                    val searchResultsByUid = viewModel.searchResults.value.groupBy { it.uid }
                    expandedUids.value = groups.filter { group ->
                        // 只展开有多个应用且出现在搜索结果中的组
                        val appsInGroup = searchResultsByUid[group.uid] ?: emptyList()
                        appsInGroup.size > 1
                    }.map { it.uid }.toSet()
                }

                SearchStatus.ResultStatus.EMPTY, SearchStatus.ResultStatus.DEFAULT -> expandedUids.value = emptySet()

                SearchStatus.ResultStatus.LOAD -> {}
            }
        }
        val state = rememberPullToRefreshState()
        PullToRefreshBox(
            modifier = Modifier.padding(innerPadding),
            state = state,
            onRefresh = { viewModel.loadAppList(true) },
            isRefreshing = viewModel.isRefreshing,
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(state = state, isRefreshing = viewModel.isRefreshing, modifier = Modifier.align(Alignment.TopCenter))
            }
        ) {
            LazyColumn(
                state = listState, modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                items(groups, key = { it.uid }) { group ->
                    val expanded = expandedUids.value.contains(group.uid)
                    GroupItem(
                        group = group, onToggleExpand = {
                            if (group.apps.size > 1) {
                                expandedUids.value = if (expanded) expandedUids.value - group.uid else expandedUids.value + group.uid
                            }
                        }) {
                        navigator.navigateTo(Route.AppProfile(group.primary.packageName))
                        viewModel.markNeedRefresh()
                    }
                    AnimatedVisibility(
                        visible = expanded && group.apps.size > 1, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()
                    ) {
                        Column {
                            group.apps.forEach { app ->
                                SimpleAppItem(app)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SimpleAppItem(
    app: SuperUserViewModel.AppInfo
) {
    ListItem(
        headlineContent = { Text(app.label) },
        supportingContent = {
            Column {
                Text(app.packageName)
            }
        },
        leadingContent = {
            AppIconImage(
                app.packageInfo, modifier = Modifier
                    .padding(4.dp)
                    .width(32.dp)
                    .height(32.dp)
            )
        },
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

@Composable
private fun GroupItem(
    group: GroupedApps,
    onToggleExpand: () -> Unit,
    onClickPrimary: () -> Unit,
) {
    val userId = group.uid / 100000
    val colorScheme = colorScheme
    val tags = remember(group.uid, group.anyAllowSu, group.anyCustom, colorScheme) {
        buildList {
            if (userId != 0) {
                add(StatusMeta("UID$userId", colorScheme.primary, colorScheme.onPrimary))
            }
            if (group.anyAllowSu) {
                add(StatusMeta("ROOT", colorScheme.tertiaryContainer, colorScheme.onTertiaryContainer))
            } else if (Natives.uidShouldUmount(group.uid)) {
                add(StatusMeta("UMOUNT", colorScheme.secondaryContainer, colorScheme.onSecondaryContainer))
            }
            if (group.anyCustom) {
                add(StatusMeta("CUSTOM", colorScheme.primaryContainer, colorScheme.onPrimaryContainer))
            }
        }
    }
    val summaryText = if (group.apps.size > 1) {
        stringResource(R.string.group_contains_apps, group.apps.size)
    } else {
        group.primary.packageName
    }
    ListItem(
        modifier = Modifier.combinedClickable(onClick = onClickPrimary, onLongClick = onToggleExpand),
        headlineContent = { Text(if (group.apps.size > 1) "${ownerNameForUid(group.uid)} (${group.uid})" else group.primary.label) },
        leadingContent = {
            AppIconImage(
                group.primary.packageInfo,
                modifier = Modifier
                    .padding(4.dp)
                    .width(48.dp)
                    .height(48.dp),
                contentDescription = if (group.apps.size > 1) "${ownerNameForUid(group.uid)} (${group.uid})" else group.primary.label
            )
        },
        supportingContent = {
            Column {
                Text(summaryText)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    tags.forEach { meta ->
                        StatusTag(
                            label = meta.label, contentColor = meta.fg, backgroundColor = meta.bg
                        )
                    }
                }
            }
        })
}

@Composable
fun StatusTag(
    label: String, textSize: TextUnit = 10.sp, contentColor: Color = colorScheme.onPrimary, backgroundColor: Color = colorScheme.primary
) {
    Box(
        modifier = Modifier.background(
            color = backgroundColor, shape = RoundedCornerShape(6.dp)
        ), propagateMinConstraints = true
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            text = label,
            color = contentColor,
            fontSize = textSize,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
            lineHeight = textSize,
        )
    }
}

@Immutable
private data class StatusMeta(
    val label: String, val bg: Color, val fg: Color
)
