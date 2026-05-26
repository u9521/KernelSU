package me.weishu.kernelsu.ui.screen.template

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import me.weishu.kernelsu.R
import me.weishu.kernelsu.data.model.TemplateInfo
import me.weishu.kernelsu.ui.component.breeze.BreezeSnackBarHost
import me.weishu.kernelsu.ui.component.breeze.PopupFeedBack
import me.weishu.kernelsu.ui.component.breeze.ScrollbarDefaults
import me.weishu.kernelsu.ui.component.breeze.VerticalScrollbar
import me.weishu.kernelsu.ui.component.breeze.keyDownFeedBack
import me.weishu.kernelsu.ui.component.breeze.rememberScrollbarAdapter
import me.weishu.kernelsu.ui.component.statustag.StatusTag
import me.weishu.kernelsu.ui.theme.expressiveTopBarColors
import me.weishu.kernelsu.ui.util.fABBottomPadding
import me.weishu.kernelsu.ui.util.onlyHorizontal
import me.weishu.kernelsu.ui.util.topBarHazeEffect

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppProfileTemplateScreenBreeze(
    state: TemplateUiState,
    actions: TemplateActions,
    snackBarHost: SnackbarHostState,
) {
    val hazeState = rememberHazeState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val pullToRefreshState = rememberPullToRefreshState()
    val listState = rememberLazyListState()
    val showListScrollbar by remember {
        derivedStateOf { scrollBehavior.state.collapsedFraction > 0.99f }
    }

    val scaleFraction = {
        if (state.isRefreshing) 1f
        else LinearOutSlowInEasing.transform(pullToRefreshState.distanceFraction).coerceIn(0f, 1f)
    }

    Scaffold(
        modifier = Modifier.pullToRefresh(
            state = pullToRefreshState,
            isRefreshing = state.isRefreshing,
            onRefresh = { actions.onRefresh(true) },
        ),
        snackbarHost = { BreezeSnackBarHost(hostState = snackBarHost) },
        topBar = {
            TopBar(
                onBack = actions.onBack,
                onRefresh = { actions.onRefresh(true) },
                onImport = actions.onImport,
                onExport = actions.onExport,
                hazeState = hazeState,
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            NewTemplateFab(onClick = actions.onCreateTemplate)
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
    ) { innerPadding ->
        val templates = state.templateList

        if (templates.isEmpty() && !state.isRefreshing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                if (state.offline) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.network_offline),
                            color = MaterialTheme.colorScheme.outline,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { actions.onRefresh(false) }) {
                            Text(stringResource(R.string.network_retry))
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No templates found",
                            color = MaterialTheme.colorScheme.outline,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { actions.onRefresh(true) }) {
                            Text(stringResource(R.string.network_retry))
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .padding(innerPadding.onlyHorizontal())
                    .hazeSource(hazeState),
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp + innerPadding.calculateTopPadding(),
                        bottom = 16.dp + 56.dp + 12.dp + 48.dp + 12.dp + fABBottomPadding(),
                    ),
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                ) {
                    itemsIndexed(
                        items = templates,
                        key = { _, template -> template.id },
                    ) { index, template ->
                        TemplateItem(
                            modifier = Modifier.animateItem(),
                            template = template,
                            index = index,
                            count = templates.size,
                            onClick = { actions.onOpenTemplate(template) },
                        )
                    }
                }

                VerticalScrollbar(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .padding(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding()),
                    adapter = rememberScrollbarAdapter(listState),
                    durationMillis = 1500L,
                    visible = showListScrollbar,
                    style = ScrollbarDefaults.style.copy(
                        color = MaterialTheme.colorScheme.primary,
                        railColor = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.5f),
                    ),
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .graphicsLayer {
                            scaleX = scaleFraction()
                            scaleY = scaleFraction()
                        },
                ) {
                    PullToRefreshDefaults.LoadingIndicator(
                        modifier = Modifier.padding(top = innerPadding.calculateTopPadding() + 16.dp),
                        state = pullToRefreshState,
                        isRefreshing = state.isRefreshing,
                    )
                }
            }
        }
    }
}

@Composable
private fun NewTemplateFab(
    onClick: () -> Unit,
) {
    val keyFeedback = keyDownFeedBack()
    ExtendedFloatingActionButton(
        modifier = Modifier.padding(bottom = fABBottomPadding()),
        onClick = {
            keyFeedback()
            onClick()
        },
        icon = { Icon(Icons.Filled.Add, null) },
        text = { Text(stringResource(id = R.string.app_profile_template_create)) },
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TemplateItem(
    modifier: Modifier,
    template: TemplateInfo,
    index: Int,
    count: Int,
    onClick: () -> Unit,
) {
    SegmentedListItem(
        modifier = modifier,
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index, count).let { it.copy(pressedShape = it.shape) },
        colors = ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.surfaceBright,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceBright,
            supportingContentColor = MaterialTheme.colorScheme.outline,
        ),
        content = {
            Text(
                text = template.name,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
        supportingContent = {
            Column {
                Text(
                    text = "${template.id}${if (template.author.isEmpty()) "" else "@${template.author}"}",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = template.description,
                    color = MaterialTheme.colorScheme.outline,
                )
                FlowRow {
                    StatusTag(
                        label = "UID: ${template.uid}",
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        backgroundColor = MaterialTheme.colorScheme.primary,
                    )
                    StatusTag(
                        label = "GID: ${template.gid}",
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    )
                    StatusTag(
                        label = template.context,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                    )
                    if (template.local) {
                        StatusTag(
                            label = "local",
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            backgroundColor = MaterialTheme.colorScheme.primary,
                        )
                    } else {
                        StatusTag(
                            label = "remote",
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            backgroundColor = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TopBar(
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    hazeState: HazeState,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    LargeFlexibleTopAppBar(
        modifier = Modifier.topBarHazeEffect(hazeState, scrollBehavior),
        title = { Text(stringResource(R.string.settings_profile_template)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        },
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(
                    painter = painterResource(R.drawable.ic_sync_rounded_filled),
                    contentDescription = null,
                )
            }
            ImportExportMenuButton(
                onImport = onImport,
                onExport = onExport,
            )
        },
        colors = expressiveTopBarColors(),
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun ImportExportMenuButton(
    onImport: () -> Unit,
    onExport: () -> Unit,
) {
    val showDropdown = remember { mutableStateOf(false) }

    IconButton(onClick = { showDropdown.value = true }) {
        Icon(
            painter = painterResource(R.drawable.ic_swap_vert_rounded),
            contentDescription = stringResource(id = R.string.app_profile_import_export),
        )

        DropdownMenu(
            expanded = showDropdown.value,
            onDismissRequest = { showDropdown.value = false },
        ) {
            PopupFeedBack()
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.app_profile_import_from_clipboard)) },
                onClick = {
                    onImport()
                    showDropdown.value = false
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.app_profile_export_to_clipboard)) },
                onClick = {
                    onExport()
                    showDropdown.value = false
                },
            )
        }
    }
}
