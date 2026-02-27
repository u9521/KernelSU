package me.weishu.kernelsu.ui.screen

import android.content.ClipData
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.BreezeSnackBarHost
import me.weishu.kernelsu.ui.component.StatusTag
import me.weishu.kernelsu.ui.component.scrollbar.ScrollbarDefaults
import me.weishu.kernelsu.ui.component.scrollbar.VerticalScrollbar
import me.weishu.kernelsu.ui.component.scrollbar.rememberScrollbarAdapter
import me.weishu.kernelsu.ui.navigation3.Route
import me.weishu.kernelsu.ui.theme.defaultTopAppBarColors
import me.weishu.kernelsu.ui.util.LocalNavController
import me.weishu.kernelsu.ui.util.LocalSnackbarHost
import me.weishu.kernelsu.ui.util.isNetworkAvailable
import me.weishu.kernelsu.ui.viewmodel.TemplateViewModel

/**
 * @author weishu
 * @date 2023/10/20.
 */

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppProfileTemplateScreen() {
    val navigator = LocalNavController.current
    val viewModel = viewModel<TemplateViewModel>()
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackBarHostState = LocalSnackbarHost.current

    LaunchedEffect(Unit) {
        if (viewModel.templateList.isEmpty()) {
            viewModel.fetchTemplates()
        }
    }
    // handle result from TemplateEditorScreen, refresh if needed
    LaunchedEffect(Unit) {
        navigator.observeResult<Boolean>(NeedRefreshTemplate).collect { success ->
            if (success) {
                navigator.clearResult(NeedRefreshTemplate)
                scope.launch { viewModel.fetchTemplates() }
            }
        }
    }

    Scaffold(
        topBar = { TopBar(scrollBehavior = scrollBehavior) },
        snackbarHost = { BreezeSnackBarHost(snackBarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = dropUnlessResumed {
                    navigator.navigateTo(Route.TemplateEditor(TemplateViewModel.TemplateInfo(), false))
                },
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text(stringResource(id = R.string.app_profile_template_create)) },
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) { innerPadding ->
        val state = rememberPullToRefreshState()
        val context = LocalContext.current
        val offline = !isNetworkAvailable(context)
        PullToRefreshBox(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            isRefreshing = viewModel.isRefreshing,
            state = state,
            onRefresh = {
                scope.launch { viewModel.fetchTemplates() }
            }, indicator = {
                PullToRefreshDefaults.LoadingIndicator(state = state, isRefreshing = viewModel.isRefreshing, modifier = Modifier.align(Alignment.TopCenter))
            }) {
            if (viewModel.templateList.isEmpty()) {
                if (!viewModel.isRefreshing) {
                    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                        val promptText = if (offline) {
                            stringResource(R.string.network_offline)
                        } else {
                            "No templates found"
                        }
                        Text(
                            text = promptText, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                scope.launch { viewModel.fetchTemplates(true) }
                            },
                            shapes = ButtonDefaults.shapes(),
                        ) {
                            Text(stringResource(R.string.network_retry))
                        }
                    }
                }
            } else {
                val bottomPadding = WindowInsets.navigationBars.union(WindowInsets.ime).asPaddingValues().calculateBottomPadding()
                val lazyListState = rememberLazyListState()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection), contentPadding = remember {
                        PaddingValues(
                            start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp + 56.dp + 16.dp + bottomPadding /* Scaffold Fab Spacing + Fab
                        container height */
                        )
                    }, state = lazyListState
                ) {
                    items(viewModel.templateList, key = { it.id }) { template ->
                        TemplateItem(template, viewModel.templateList.indexOf(template), viewModel.templateList.size)
                    }
                }
                VerticalScrollbar(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(lazyListState),
                    durationMillis = 1500L,
                    style = ScrollbarDefaults.style.copy(
                        color = MaterialTheme.colorScheme.primary, railColor = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TemplateItem(
    template: TemplateViewModel.TemplateInfo, index: Int, count: Int
) {
    val navigator = LocalNavController.current
    SegmentedListItem(
        onClick = { navigator.navigateTo(Route.TemplateEditor(template, !template.local)) },
        shapes = ListItemDefaults.segmentedShapes(index, count).let { it.copy(pressedShape = it.shape) },
        colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
        content = { Text(template.name) },
        supportingContent = {
            Column {
                Text(
                    text = "${template.id}${if (template.author.isEmpty()) "" else "@${template.author}"}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                )
                Text(template.description)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    StatusTag(label = "UID: ${template.uid}")
                    StatusTag(
                        label = "GID: ${template.gid}",
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                    StatusTag(
                        label = template.context,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                    if (template.local) {
                        StatusTag(label = "local")
                    } else {
                        StatusTag(label = "remote")
                    }
                }
            }
        })
    if (index != count - 1) Spacer(Modifier.height(ListItemDefaults.SegmentedGap))
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior
) {
    val scope = rememberCoroutineScope()
    val navigator = LocalNavController.current
    val viewModel = viewModel<TemplateViewModel>()
    val resources = LocalResources.current
    val clipboard = LocalClipboard.current
    val snackBarHost = LocalSnackbarHost.current

    LargeFlexibleTopAppBar(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection), colors = defaultTopAppBarColors(), title = {
            Text(stringResource(R.string.settings_profile_template))
        }, navigationIcon = {
            IconButton(
                onClick = dropUnlessResumed { navigator.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        }, actions = {
            IconButton(onClick = {
                scope.launch { viewModel.fetchTemplates(true) }
            }) {
                Icon(painter = painterResource(R.drawable.ic_sync_rounded_filled), contentDescription = null)
            }
            ImportExportMenuButton(onImport = {
                scope.launch {
                    clipboard.getClipEntry()?.clipData?.getItemAt(0)?.text?.toString()?.let {
                        if (it.isEmpty()) {
                            snackBarHost.showSnackbar(resources.getString(R.string.app_profile_template_import_empty))
                            return@let
                        }
                        viewModel.importTemplates(it, onSuccess = {
                            snackBarHost.showSnackbar(resources.getString(R.string.app_profile_template_import_success))
                            viewModel.fetchTemplates(false)
                        }, onFailure = { e -> snackBarHost.showSnackbar(e) })
                    }
                }
            }, onExport = {
                scope.launch {
                    viewModel.exportTemplates(onTemplateEmpty = {
                        snackBarHost.showSnackbar(resources.getString(R.string.app_profile_template_export_empty))
                    }, {
                        clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("template", it)))
                    })
                }
            })
        }, scrollBehavior = scrollBehavior
    )
}

@Composable
private fun ImportExportMenuButton(onImport: () -> Unit, onExport: () -> Unit) {
    val showDropdown = remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    IconButton(onClick = {
        showDropdown.value = true
        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
    }) {
        Icon(
            painter = painterResource(R.drawable.ic_swap_vert_rounded), contentDescription = stringResource(id = R.string.app_profile_import_export)
        )

        DropdownMenu(expanded = showDropdown.value, onDismissRequest = {
            showDropdown.value = false
        }) {
            DropdownMenuItem(text = {
                Text(stringResource(id = R.string.app_profile_import_from_clipboard))
            }, onClick = {
                onImport()
                showDropdown.value = false
            })
            DropdownMenuItem(text = {
                Text(stringResource(id = R.string.app_profile_export_to_clipboard))
            }, onClick = {
                onExport()
                showDropdown.value = false
            })
        }
    }
}