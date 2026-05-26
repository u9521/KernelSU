package me.weishu.kernelsu.ui.screen.install

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.breeze.BreezeSnackBarHost
import me.weishu.kernelsu.ui.component.breeze.SegmentedListGroup
import me.weishu.kernelsu.ui.component.breeze.SegmentedListScope
import me.weishu.kernelsu.ui.component.breeze.keyDownFeedBack
import me.weishu.kernelsu.ui.component.dialog.rememberConfirmDialog
import me.weishu.kernelsu.ui.theme.expressiveTopBarColors
import me.weishu.kernelsu.ui.util.LkmSelection
import me.weishu.kernelsu.ui.util.fABBottomPadding
import me.weishu.kernelsu.ui.util.onlyHorizontal
import me.weishu.kernelsu.ui.util.topBarHazeEffect

/**
 * @author weishu
 * @date 2024/3/12.
 */
@Composable
internal fun InstallScreenBreeze(
    uiState: InstallUiState,
    actions: InstallScreenActions,
    snackbarHostState: SnackbarHostState
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val hazeState = rememberHazeState()
    val keyDownFeedBack = keyDownFeedBack()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = {
            BreezeSnackBarHost(snackbarHostState, modifier = Modifier.let {
                if (uiState.installMethod == null) it.safeDrawingPadding() else it
            })
        },
        topBar = {
            TopBar(
                onBack = actions.onBack,
                scrollBehavior = scrollBehavior,
                hazeState = hazeState,
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                content = {
                    Text(stringResource(id = R.string.install_next))
                    Spacer(Modifier.width(2.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, stringResource(id = R.string.install_next))
                },
                modifier = Modifier
                    .animateFloatingActionButton(
                        visible = uiState.installMethod != null,
                        alignment = Alignment.CenterEnd,
                    )
                    .padding(bottom = fABBottomPadding()),
                onClick = {
                    keyDownFeedBack()
                    actions.onNext()
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
                .padding(innerPadding.onlyHorizontal())
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(innerPadding.calculateTopPadding() + 16.dp))
            InstallMethodSelector(
                state = uiState,
                onSelected = actions.onSelectMethod,
                onSelectBootImage = actions.onSelectBootImage,
            )
            Spacer(Modifier.height(16.dp))
            InstallConfigGroup(
                state = uiState,
                onSelectPartition = actions.onSelectPartition,
                onUploadLkm = actions.onUploadLkm,
                onClearLkm = actions.onClearLkm,
            )
            Spacer(Modifier.height(16.dp))
            AdvancedOptionsGroup(
                state = uiState,
                onToggleExpanded = actions.onAdvancedOptionsClicked,
                onSelectAllowShell = actions.onSelectAllowShell,
                onSelectEnableAdb = actions.onSelectEnableAdb,
            )
            Spacer(Modifier.height(fABBottomPadding() + 56.dp + 16.dp * 2 + 6.dp * 2 + 48.dp))
        }
    }
}

@Composable
private fun InstallMethodSelector(
    state: InstallUiState,
    onSelected: (InstallMethod) -> Unit,
    onSelectBootImage: () -> Unit,
) {
    val confirmDialog = rememberConfirmDialog(
        onConfirm = { onSelected(InstallMethod.DirectInstallToInactiveSlot) },
        onDismiss = null,
    )
    val dialogTitle = stringResource(android.R.string.dialog_alert_title)
    val dialogContent = stringResource(R.string.install_inactive_slot_warning)

    val onClick = { option: InstallMethod ->
        when (option) {
            is InstallMethod.SelectFile -> onSelectBootImage()
            is InstallMethod.DirectInstall -> onSelected(option)
            is InstallMethod.DirectInstallToInactiveSlot -> confirmDialog.showConfirm(dialogTitle, dialogContent)
        }
    }

    key(state.installMethodOptions.size) {
        SegmentedListGroup {
            state.installMethodOptions.forEach { option ->
                val isSelected = option.javaClass == state.installMethod?.javaClass
                item(
                    key = option.label,
                    onClick = { onClick(option) },
                    leadingContent = { RadioButton(selected = isSelected, onClick = null) },
                    supportingContent = option.summary?.let { summary -> { Text(summary) } },
                ) {
                    Text(stringResource(option.label))
                }
            }
        }
    }
}

@Composable
private fun InstallConfigGroup(
    state: InstallUiState,
    onSelectPartition: (Int) -> Unit,
    onUploadLkm: () -> Unit,
    onClearLkm: () -> Unit,
) {
    val selectedLkmName = remember(state.lkmSelection) {
        (state.lkmSelection as? LkmSelection.LkmUri)?.let { it.uri.lastPathSegment ?: "(file)" }
    }
    val selectedPartition = state.displayPartitions.getOrNull(state.partitionSelectionIndex).orEmpty()
    val slotSuffix = state.slotSuffix.ifBlank { null }

    SegmentedListGroup {
        partitionSelector(
            visible = state.canSelectPartition,
            partitions = state.displayPartitions,
            partition = selectedPartition,
            partitionSuffix = slotSuffix,
            onPartitionChange = onSelectPartition,
        )
        lkmSelector(
            selectedLkmName = selectedLkmName,
            onLaunchLkmPicker = onUploadLkm,
            onClearLkm = onClearLkm,
        )
    }
}

@Composable
private fun AdvancedOptionsGroup(
    state: InstallUiState,
    onToggleExpanded: () -> Unit,
    onSelectAllowShell: (Boolean) -> Unit,
    onSelectEnableAdb: (Boolean) -> Unit,
) {
    val resources = LocalResources.current
    val rotationState by animateFloatAsState(
        targetValue = if (state.advancedOptionsShown) 180f else 0f,
        label = "InstallAdvancedOptionsRotation"
    )

    SegmentedListGroup {
        item(
            onClick = onToggleExpanded,
            trailingContent = {
                Icon(
                    painterResource(R.drawable.ic_keyboard_arrow_down_rounded),
                    contentDescription = stringResource(R.string.expand),
                    modifier = Modifier.graphicsLayer { rotationZ = rotationState }
                )
            },
        ) {
            Text(stringResource(R.string.advanced_options))
        }
        checkboxItem(
            visible = state.advancedOptionsShown,
            title = resources.getString(R.string.allow_shell),
            summary = resources.getString(R.string.allow_shell_summary),
            checked = { state.allowShell },
            onCheckedChange = onSelectAllowShell,
        )
        checkboxItem(
            visible = state.advancedOptionsShown,
            title = resources.getString(R.string.enable_adb),
            summary = resources.getString(R.string.enable_adb_summary),
            checked = { state.enableAdb },
            onCheckedChange = onSelectEnableAdb,
        )
    }
}

private fun SegmentedListScope.lkmSelector(
    selectedLkmName: String?,
    onLaunchLkmPicker: () -> Unit,
    onClearLkm: () -> Unit,
) {
    item(
        onClick = {
            onLaunchLkmPicker()
        },
        leadingContent = { Icon(painterResource(R.drawable.ic_drive_file_move_rounded), null) },
        supportingContent = selectedLkmName?.let { name ->
            {
                Text(
                    text = stringResource(id = R.string.selected_lkm, name),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        trailingContent = if (selectedLkmName != null) {
            {
                IconButton(onClick = onClearLkm) {
                    Icon(Icons.Default.Clear, null)
                }
            }
        } else {
            null
        },
    ) {
        Text(stringResource(id = R.string.install_upload_lkm_file))
    }
}

private fun SegmentedListScope.partitionSelector(
    visible: Boolean,
    partitions: List<String>,
    partition: String,
    partitionSuffix: String?,
    onPartitionChange: (Int) -> Unit,
) {
    val suffix = if (partitionSuffix != null) " ($partitionSuffix)" else ""
    menuItem(
        visible = visible,
        content = { Text("${stringResource(R.string.install_select_partition)}$suffix") },
        leadingContent = { Icon(painterResource(R.drawable.ic_hard_drive_rounded), null) },
        selected = { partition },
        menuContent = { dismiss ->
            partitions.forEachIndexed { index, name ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onPartitionChange(index)
                        dismiss()
                    }
                )
            }
        },
    )
}

@Composable
private fun TopBar(
    onBack: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior,
    hazeState: dev.chrisbanes.haze.HazeState,
) {
    LargeFlexibleTopAppBar(
        modifier = Modifier.topBarHazeEffect(hazeState, scrollBehavior),
        title = { Text(stringResource(R.string.install)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        },
        colors = expressiveTopBarColors(),
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        scrollBehavior = scrollBehavior,
    )
}
