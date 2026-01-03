package me.weishu.kernelsu.ui.screen

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.AppProfileScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.SearchAppBar
import me.weishu.kernelsu.ui.component.SearchStatus
import me.weishu.kernelsu.ui.util.UidGroupUtils
import me.weishu.kernelsu.ui.util.UidGroupUtils.ownerNameForUid
import me.weishu.kernelsu.ui.viewmodel.SuperUserViewModel

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun SuperUserScreen(navigator: DestinationsNavigator) {
    val viewModel = viewModel<SuperUserViewModel>()
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val listState = rememberLazyListState()
    val searchStatus by viewModel.searchStatus
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    LaunchedEffect(key1 = navigator) {
        viewModel.updateSearchText("")
        if (viewModel.appList.value.isEmpty() || viewModel.searchResults.value.isEmpty()) {
            viewModel.showSystemApps = prefs.getBoolean("show_system_apps", false)
            viewModel.fetchAppList()
        }
    }

    LaunchedEffect(searchStatus.searchText) {
        viewModel.updateSearchText(searchStatus.searchText)
    }

    Scaffold(
        topBar = {
            SearchAppBar(
                title = { Text(stringResource(R.string.superuser)) }, searchStatus = searchStatus, dropdownContent = {
                    var showDropdown by remember { mutableStateOf(false) }

                    IconButton(
                        onClick = { showDropdown = true },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert, contentDescription = stringResource(id = R.string.settings)
                        )

                        DropdownMenu(expanded = showDropdown, onDismissRequest = {
                            showDropdown = false
                        }) {
                            DropdownMenuItem(text = {
                                Text(stringResource(R.string.refresh))
                            }, onClick = {
                                scope.launch {
                                    viewModel.fetchAppList()
                                }
                                showDropdown = false
                            })
                            DropdownMenuItem(text = {
                                Text(
                                    if (viewModel.showSystemApps) {
                                        stringResource(R.string.hide_system_apps)
                                    } else {
                                        stringResource(R.string.show_system_apps)
                                    }
                                )
                            }, onClick = {
                                viewModel.showSystemApps = !viewModel.showSystemApps
                                prefs.edit {
                                    putBoolean("show_system_apps", viewModel.showSystemApps)
                                }
                                scope.launch {
                                    viewModel.fetchAppList()
                                }
                                showDropdown = false
                            })
                        }
                    }
                }, scrollBehavior = scrollBehavior
            )
        }, contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        var displayAppList = viewModel.appList.value
        if (viewModel.searchStatus.value.resultStatus == SearchStatus.ResultStatus.SHOW) {
            displayAppList = viewModel.searchResults.value
        }
        if (viewModel.searchStatus.value.resultStatus == SearchStatus.ResultStatus.EMPTY) {
            displayAppList = viewModel.searchResults.value
        }
        val allGroups = remember(viewModel.appList.value) { buildGroups(viewModel.appList.value) }
        val matchedByUid = remember(viewModel.searchResults.value) {
            viewModel.searchResults.value.groupBy { it.uid }
        }
        val searchGroups = remember(allGroups, matchedByUid) {
            allGroups.filter { matchedByUid.containsKey(it.uid) }
        }
        val expandedSearchUids = remember { mutableStateOf(setOf<Int>()) }
        LaunchedEffect(matchedByUid) {
            expandedSearchUids.value = searchGroups.filter { it.apps.size > 1 }.map { it.uid }.toSet()
        }

        val groups = remember(viewModel.appList.value) { buildGroups(viewModel.appList.value) }
        val expandedUids = remember { mutableStateOf(setOf<Int>()) }
        PullToRefreshBox(
            modifier = Modifier.padding(innerPadding), onRefresh = {
                scope.launch { viewModel.fetchAppList() }
            }, isRefreshing = viewModel.isRefreshing
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
                        navigator.navigate(AppProfileScreenDestination(group.primary))
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
    app: SuperUserViewModel.AppInfo,
) {
    ListItem(
        modifier = Modifier.clickable(onClick = { }),
        headlineContent = { Text(app.label) },
        supportingContent = {
            Column {
                Text(app.packageName)
            }
        },
        leadingContent = {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(app.packageInfo).crossfade(true).build(),
                contentDescription = app.label,
                modifier = Modifier
                    .padding(4.dp)
                    .width(48.dp)
                    .height(48.dp)
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
        val primary = UidGroupUtils.pickPrimary(sorted)
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
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(group.primary.packageInfo).crossfade(true).build(),
                contentDescription = if (group.apps.size > 1) "${ownerNameForUid(group.uid)} (${group.uid})" else group.primary.label,
                modifier = Modifier
                    .padding(4.dp)
                    .width(48.dp)
                    .height(48.dp)
            )
        },
        supportingContent = {
            Column {
                Text(summaryText)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    tags.forEach { meta ->
                        StatusTag(
                            label = meta.label, backgroundColor = meta.bg, contentColor = meta.fg
                        )
                    }
                }
            }
        })
}

@Composable
fun StatusTag(
    label: String, backgroundColor: Color, contentColor: Color
) {
    Box(
        modifier = Modifier.background(
            color = backgroundColor, shape = RoundedCornerShape(6.dp)
        )
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 5.dp),
            text = label,
            color = contentColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Immutable
private data class StatusMeta(
    val label: String, val bg: Color, val fg: Color
)
