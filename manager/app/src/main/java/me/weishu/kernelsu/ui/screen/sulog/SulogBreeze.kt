package me.weishu.kernelsu.ui.screen.sulog

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.breeze.BrMenuBox
import me.weishu.kernelsu.ui.component.breeze.BreezeBackButton
import me.weishu.kernelsu.ui.component.breeze.PopupFeedBack
import me.weishu.kernelsu.ui.component.breeze.ScrollbarDefaults
import me.weishu.kernelsu.ui.component.breeze.VerticalScrollbar
import me.weishu.kernelsu.ui.component.breeze.rememberScrollbarAdapter
import me.weishu.kernelsu.ui.component.material.ExpressiveScaffold
import me.weishu.kernelsu.ui.component.material.SearchAppBarBreeze
import me.weishu.kernelsu.ui.component.material.TonalCard
import me.weishu.kernelsu.ui.component.material.disableDrag
import me.weishu.kernelsu.ui.component.statustag.StatusTag
import me.weishu.kernelsu.ui.util.SulogEntry
import me.weishu.kernelsu.ui.util.SulogEventFilter
import me.weishu.kernelsu.ui.util.onlyHorizontal
import me.weishu.kernelsu.ui.util.topBarHazeEffect
import me.weishu.kernelsu.ui.util.windowBlurBehind


@Composable
fun SulogScreenBreeze(
    state: SulogScreenState,
    actions: SulogActions,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior().disableDrag()
    val searchBarScrollBehavior = SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()
    val pullToRefreshState = rememberPullToRefreshState()
    val listState = rememberLazyListState()
    val searchListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var selectedEntry by remember { mutableStateOf<SulogEntry?>(null) }
    var localSearchText by remember { mutableStateOf(state.searchText) }
    val hazeState = rememberHazeState()
    val showListScrollbar by remember {
        derivedStateOf { scrollBehavior.state.collapsedFraction > 0.99f }
    }

    LaunchedEffect(state.searchText) {
        localSearchText = state.searchText
    }

    val scaleFraction = {
        if (state.isLoading || state.isRefreshing) 1f
        else LinearOutSlowInEasing.transform(pullToRefreshState.distanceFraction).coerceIn(0f, 1f)
    }

    if (selectedEntry != null) {
        SulogDetailDialog(
            entry = selectedEntry!!,
            onDismiss = { selectedEntry = null },
        )
    }

    ExpressiveScaffold(
        modifier = Modifier.pullToRefresh(
            state = pullToRefreshState,
            isRefreshing = state.isLoading || state.isRefreshing,
            onRefresh = actions.onRefresh,
        ),
        topBar = {
            SearchAppBarBreeze(
                modifier = Modifier.topBarHazeEffect(hazeState, scrollBehavior),
                title = { Text(stringResource(R.string.settings_sulog)) },
                searchText = localSearchText,
                onSearchTextChange = {
                    localSearchText = it
                    actions.onSearchTextChange(it)
                    scope.launch { searchListState.scrollToItem(0) }
                },
                onClearClick = {
                    localSearchText = ""
                    actions.onSearchTextChange("")
                },
                navigationIcon = {
                    BreezeBackButton(
                        onClick = actions.onBack,
                        collapseFraction = scrollBehavior.state.collapsedFraction,
                    )
                },
                actions = {
                    IconButton(onClick = actions.onCleanFile) {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete_sweep_rounded),
                            contentDescription = stringResource(R.string.sulog_clean_title),
                        )
                    }
                    FilterMenu(state, actions)
                },
                scrollBehavior = scrollBehavior,
                searchBarScrollBehavior = searchBarScrollBehavior,
                snackbarHostState = remember { SnackbarHostState() },
                searchContent = { bottomPadding, _ ->
                    LazyColumn(
                        state = searchListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 16.dp + bottomPadding,
                        ),
                    ) {
                        suLogEntriesSection(
                            entries = state.visibleEntries,
                            errorMessage = state.errorMessage,
                            onEntryClick = { selectedEntry = it },
                        )
                    }
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
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding.onlyHorizontal())
                .hazeSource(hazeState)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(searchBarScrollBehavior.nestedScrollConnection)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp).plus(PaddingValues(top = innerPadding.calculateTopPadding())),
            ) {
                item {
                    SulogStatusSection(state, actions)
                }

                item {
                    LogTimeCard(state, actions)
                }

                suLogEntriesSection(
                    entries = state.visibleEntries,
                    errorMessage = state.errorMessage,
                    onEntryClick = { selectedEntry = it },
                )

                item(key = "BottomPadding") {
                    Spacer(
                        Modifier.navigationBarsPadding()
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = innerPadding.calculateTopPadding() + 8.dp)
                    .graphicsLayer {
                        scaleX = scaleFraction()
                        scaleY = scaleFraction()
                    },
                contentAlignment = Alignment.TopCenter,
            ) {
                PullToRefreshDefaults.LoadingIndicator(
                    state = pullToRefreshState,
                    isRefreshing = state.isLoading || state.isRefreshing,
                )
            }
            VerticalScrollbar(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .navigationBarsPadding()
                    .padding(top = innerPadding.calculateTopPadding()),
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
}

@Composable
private fun FilterMenu(
    state: SulogScreenState,
    actions: SulogActions,
) {
    var showFilterMenu by remember { mutableStateOf(false) }
    IconButton(onClick = { showFilterMenu = true }) {
        Icon(
            painter = painterResource(R.drawable.ic_filter_list_rounded),
            contentDescription = stringResource(R.string.sulog_filter_title),
        )
    }
    DropdownMenuPopup(
        expanded = showFilterMenu,
        onDismissRequest = { showFilterMenu = false },
    ) {
        PopupFeedBack()
        DropdownMenuGroup(shapes = MenuDefaults.groupShapes()) {
            SulogEventFilter.entries.forEachIndexed { index, filter ->
                DropdownMenuItem(
                    text = { Text(sulogFilterLabel(filter)) }, checked = filter in state.selectedFilters, checkedLeadingIcon = {
                        Icon(
                            Icons.Filled.Check,
                            modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                            contentDescription = null,
                        )
                    }, onCheckedChange = {
                        actions.onToggleFilter(filter)
                    }, shapes = MenuDefaults.itemShape(index = index, count = SulogEventFilter.entries.size)
                )
            }
        }
    }
}

private fun LazyListScope.suLogEntriesSection(
    entries: List<SulogEntry>,
    errorMessage: String?,
    onEntryClick: (SulogEntry) -> Unit,
) {
    when {
        errorMessage != null -> item {
            SulogMessageCard(
                modifier = Modifier.fillParentMaxSize(),
                title = stringResource(R.string.sulog_failed_to_load),
                summary = errorMessage,
            )
        }

        else -> {
            itemsIndexed(entries, key = { _, entry -> entry.key }) { index, entry ->
                SulogItem(Modifier.animateItem(), index, entries.size, onEntryClick, entry)
            }
        }
    }
}

@Composable
private fun SulogItem(
    modifier: Modifier = Modifier, index: Int, count: Int, onEntryClick: (SulogEntry) -> Unit, entry: SulogEntry
) {
    SegmentedListItem(
        modifier = modifier,
        shapes = ListItemDefaults.segmentedShapes(index, count).let { it.copy(pressedShape = it.shape) },
        colors = ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.surfaceBright,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceBright,
            supportingContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        onClick = { onEntryClick(entry) },
        content = { Text(sulogEntryTitle(entry)) },
        verticalAlignment = Alignment.CenterVertically,
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                sulogEntryDescription(entry)?.let {
                    Text(
                        it, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis
                    )
                }
                entry.timestampText?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelMediumEmphasized,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    val colors = listOf(
                        MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary,
                        MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary,
                        MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.onTertiary,
                    )
                    sulogEntrySummaryTags(entry).forEachIndexed { index, tag ->
                        val (bg, fg) = colors.getOrElse(index) { colors.last() }
                        StatusTag(label = tag, backgroundColor = bg, contentColor = fg)
                    }
                }
            }
        },
        trailingContent = {
            sulogEntryStatus(entry)?.let { Text(it) }
        },
    )
    if (index < count) Spacer(Modifier.padding(bottom = 2.dp))
}

@Composable
private fun SulogStatusSection(
    state: SulogScreenState,
    actions: SulogActions,
) {
    when (state.sulogStatus) {
        "unsupported" -> {
            WarningCard(text = stringResource(R.string.sulog_unsupported_title))
        }

        "managed" -> {
            WarningCard(text = stringResource(R.string.feature_status_managed_summary))
        }

        "supported" if !state.isSulogEnabled -> {
            WarningCard(
                text = stringResource(R.string.sulog_disabled_title),
                action = {
                    Button(
                        shapes = ButtonDefaults.shapes(),
                        onClick = actions.onEnableSulog,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                    ) {
                        Text(stringResource(R.string.sulog_enable_action))
                    }
                },
            )
        }

        else -> Unit
    }
}


@Composable
private fun LogTimeCard(
    state: SulogScreenState,
    actions: SulogActions,
) {
    val fileSelector = buildSulogFileSelector(state.files, state.selectedFilePath)
    BrMenuBox(
        modifier = Modifier
            .padding(bottom = 16.dp, top = 8.dp)
            .clip(MaterialTheme.shapes.large),
        enabled = fileSelector.items.isNotEmpty(),
        menuContent = { dismiss ->
            DropdownMenuGroup(shapes = MenuDefaults.groupShapes()) {
                fileSelector.items.forEachIndexed { index, string ->
                    DropdownMenuItem(
                        text = { Text(string) }, selected = fileSelector.selectedIndex == index, selectedLeadingIcon = {
                            Icon(
                                Icons.Filled.Check,
                                modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                                contentDescription = null,
                            )
                        }, onClick = {
                            state.files.getOrNull(index)?.let { file ->
                                actions.onSelectFile(file.path)
                                dismiss()
                            }
                        }, shapes = MenuDefaults.itemShape(index, fileSelector.items.size)
                    )
                }
            }
        },
        content = {
            SegmentedListItem(
                onClick = {}, shapes = ListItemDefaults.shapes(shape = MaterialTheme.shapes.large), colors = ListItemDefaults.segmentedColors(
                    containerColor = MaterialTheme.colorScheme.surfaceBright,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceBright,
                    supportingContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ), trailingContent = {
                    Text(
                        text = fileSelector.items.getOrNull(fileSelector.selectedIndex) ?: "", color = MaterialTheme.colorScheme.primary
                    )
                }) { Text(stringResource(R.string.sulog_log_files)) }
        },
    )
}

@Composable
private fun SulogMessageCard(
    modifier: Modifier,
    title: String,
    summary: String? = null,
) {
    Box(
        modifier = modifier, contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (summary != null) {
                Text(
                    summary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun WarningCard(
    text: String,
    action: (@Composable () -> Unit)? = null,
) {
    TonalCard(
        modifier = Modifier.padding(bottom = 16.dp), containerColor = MaterialTheme.colorScheme.errorContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            action?.invoke()
        }
    }
}

@Composable
private fun SulogDetailDialog(
    entry: SulogEntry,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        modifier = Modifier.windowBlurBehind(),
        onDismissRequest = onDismiss,
        title = { Text(sulogEntryTitle(entry)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                SelectionContainer {
                    Text(
                        text = sulogEntryDetailText(entry),
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shapes = ButtonDefaults.shapes()) {
                Text(stringResource(android.R.string.ok))
            }
        },
    )
}
