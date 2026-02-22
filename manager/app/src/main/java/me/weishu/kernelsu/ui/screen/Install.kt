package me.weishu.kernelsu.ui.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallExtendedFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import me.weishu.kernelsu.R
import me.weishu.kernelsu.getKernelVersion
import me.weishu.kernelsu.ui.component.SegmentedListGroup
import me.weishu.kernelsu.ui.component.SegmentedListScope
import me.weishu.kernelsu.ui.component.popUps.rememberSelectKmiDialog
import me.weishu.kernelsu.ui.component.rememberConfirmDialog
import me.weishu.kernelsu.ui.navigation3.Route
import me.weishu.kernelsu.ui.theme.defaultTopAppBarColors
import me.weishu.kernelsu.ui.util.LkmSelection
import me.weishu.kernelsu.ui.util.LocalNavController
import me.weishu.kernelsu.ui.util.getAvailablePartitions
import me.weishu.kernelsu.ui.util.getCurrentKmi
import me.weishu.kernelsu.ui.util.getDefaultPartition
import me.weishu.kernelsu.ui.util.getSlotSuffix
import me.weishu.kernelsu.ui.util.isAbDevice
import me.weishu.kernelsu.ui.util.rootAvailable

data class EnvData(
    val rootAvailable: Boolean = false,
    val isAbDevice: Boolean = false,
    val defaultPartition: String = "boot",
    val partitions: List<String> = emptyList(),
    val currentKmi: String = ""
)

@Parcelize
sealed class InstallMethod : Parcelable {
    data class SelectFile(
        val uri: Uri? = null, @get:StringRes override val label: Int = R.string.select_file, override val summary: String?
    ) : InstallMethod()

    data object DirectInstall : InstallMethod() {
        override val label: Int get() = R.string.direct_install
    }

    data object DirectInstallToInactiveSlot : InstallMethod() {
        override val label: Int get() = R.string.install_inactive_slot
    }

    abstract val label: Int

    @IgnoredOnParcel
    open val summary: String? = null
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InstallScreen() {
    val context = LocalContext.current
    val navigator = LocalNavController.current
    val hapticFeedback = LocalHapticFeedback.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    var installMethod by rememberSaveable { mutableStateOf<InstallMethod?>(null) }
    var lkmSelection by rememberSaveable { mutableStateOf<LkmSelection>(LkmSelection.KmiNone) }
    val kernelVersion = getKernelVersion()

    val selectImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            res.data?.data?.let { uri ->
                installMethod = InstallMethod.SelectFile(uri, summary = null)
            }
        }
    }

    val selectLkmLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            res.data?.data?.let { uri ->
                if (isKoFile(context, uri)) {
                    lkmSelection = LkmSelection.LkmUri(uri)
                } else {
                    lkmSelection = LkmSelection.KmiNone
                    Toast.makeText(context, R.string.install_only_support_ko_file, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val envData = produceState(initialValue = EnvData()) {
        value = EnvData(
            rootAvailable = rootAvailable(),
            isAbDevice = isAbDevice(),
            defaultPartition = getDefaultPartition(),
            partitions = getAvailablePartitions(),
            currentKmi = getCurrentKmi()
        )
    }.value

    val selectFileTip = stringResource(R.string.select_file_tip, envData.defaultPartition)
    var selectedPartition by rememberSaveable(envData.defaultPartition) { mutableStateOf(envData.defaultPartition) }
    val availableMethods = remember(envData.rootAvailable, envData.isAbDevice) {
        buildList {
            add(InstallMethod.SelectFile(summary = selectFileTip))
            if (envData.rootAvailable && kernelVersion.isGKI()) {
                add(InstallMethod.DirectInstall)
                if (envData.isAbDevice && kernelVersion.isGKI()) {
                    add(InstallMethod.DirectInstallToInactiveSlot)
                }
            }
        }
    }

    val performInstall = {
        installMethod?.let { method ->
            val isOta = method is InstallMethod.DirectInstallToInactiveSlot
            val partitionSelection = selectedPartition
            val flashIt = FlashIt.FlashBoot(
                boot = if (method is InstallMethod.SelectFile) method.uri else null, lkm = lkmSelection, ota = isOta, partition = partitionSelection
            )
            navigator.navigateTo(Route.Flash(flashIt))
            // Reset state
            installMethod = null
            lkmSelection = LkmSelection.KmiNone
        }
    }

    val selectKmiDialog = rememberSelectKmiDialog(envData.currentKmi) { kmi ->
        hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
        lkmSelection = LkmSelection.KmiString(kmi)
        performInstall()
    }

    val checkAndInstall: () -> Unit = {
        val isKmiUnselected = lkmSelection == LkmSelection.KmiNone
        val isKmiUnknown = envData.currentKmi.isBlank()
        val isSelectFileMode = installMethod is InstallMethod.SelectFile

        if (isKmiUnselected && (isKmiUnknown || isSelectFileMode)) {
            selectKmiDialog.show()
        } else {
            performInstall()
        }
    }

    var pendingMethod by rememberSaveable { mutableStateOf<InstallMethod?>(null) }
    val warningDialog = rememberConfirmDialog(
        onConfirm = { pendingMethod?.let { installMethod = it } }, onDismiss = null
    )
    val warningTitle = stringResource(id = android.R.string.dialog_alert_title)
    val warningContent = stringResource(id = R.string.install_inactive_slot_warning)

    val handleMethodSelection = { method: InstallMethod ->
        when (method) {
            is InstallMethod.SelectFile -> {
                selectImageLauncher.launch(Intent(Intent.ACTION_GET_CONTENT).apply { type = "application/octet-stream" })
            }

            is InstallMethod.DirectInstallToInactiveSlot -> {
                pendingMethod = method
                warningDialog.showConfirm(title = warningTitle, content = warningContent)
            }

            else -> {
                installMethod = method
            }
        }
    }

    Scaffold(topBar = {
        TopBar(onBack = dropUnlessResumed { navigator.popBackStack() }, scrollBehavior = scrollBehavior)
    }, containerColor = MaterialTheme.colorScheme.surfaceContainer, floatingActionButton = {
        SmallExtendedFloatingActionButton(
            content = {
                Text(stringResource(id = R.string.install_next))
                Spacer(Modifier.width(2.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, stringResource(id = R.string.install_next))
            },
            modifier = Modifier
                .padding(bottom = 80.dp) // Adjusted padding
                .animateFloatingActionButton(
                    visible = installMethod != null,
                    alignment = Alignment.CenterEnd,
                ),
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                checkAndInstall()
            },
        )
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(all = 16.dp)
                .navigationBarsPadding()
        ) {

            InstallMethodSelector(
                options = availableMethods, selectedOption = installMethod, onOptionClick = handleMethodSelection
            )

            Spacer(Modifier.height(16.dp))

            InstallConfigGroup(
                currentMethod = installMethod,
                lkmSelection = lkmSelection,
                partitions = envData.partitions,
                defaultPartition = envData.defaultPartition,
                onPartitionChange = { newPartition ->
                    selectedPartition = newPartition
                },
                onLaunchLkmPicker = {
                    selectLkmLauncher.launch(Intent(Intent.ACTION_GET_CONTENT).apply { type = "application/octet-stream" })
                },
                onClearLkm = { lkmSelection = LkmSelection.KmiNone },
                selectedPartition = selectedPartition
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun InstallMethodSelector(
    options: List<InstallMethod>, selectedOption: InstallMethod?, onOptionClick: (InstallMethod) -> Unit
) {
    SegmentedListGroup {
        options.forEach { option ->
            val isSelected = option.javaClass == selectedOption?.javaClass
            item(key = option.label, onClick = { onOptionClick(option) }, leadingContent = {
                RadioButton(selected = isSelected, onClick = null)
            }, supportingContent = option.summary?.let { summary -> { Text(summary) } }, content = { Text(stringResource(option.label)) })
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun InstallConfigGroup(
    currentMethod: InstallMethod?,
    lkmSelection: LkmSelection,
    partitions: List<String>,
    defaultPartition: String,
    selectedPartition: String,
    onPartitionChange: (String) -> Unit,
    onLaunchLkmPicker: () -> Unit,
    onClearLkm: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current

    val showPartitionSelector = currentMethod is InstallMethod.DirectInstall || currentMethod is InstallMethod.DirectInstallToInactiveSlot
    val isOta = currentMethod is InstallMethod.DirectInstallToInactiveSlot
    val slotSuffix by produceState<String?>(null, isOta) { value = getSlotSuffix(isOta).let { it.ifBlank { null } } }

    val displayPartitions = remember(partitions, defaultPartition) {
        partitions.map { if (it == defaultPartition) "$it (default)" else it }
    }

    val currentDisplaySelection = remember(selectedPartition, defaultPartition) {
        if (selectedPartition == defaultPartition && selectedPartition.isNotEmpty()) {
            "$selectedPartition (default)"
        } else {
            selectedPartition
        }
    }

    val selectedLkmName = remember(lkmSelection) {
        (lkmSelection as? LkmSelection.LkmUri)?.let { it.uri.lastPathSegment ?: "(file)" }
    }

    SegmentedListGroup {
        partitionSelector(
            visible = showPartitionSelector,
            partitions = displayPartitions,
            partition = currentDisplaySelection,
            partitionSuffix = slotSuffix,
        ) { displayString ->
            hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
            val index = displayPartitions.indexOf(displayString)
            if (index != -1 && index < partitions.size) {
                onPartitionChange(partitions[index])
            }
        }

        lkmSelector(selectedLkmName, onLaunchLkmPicker = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
            onLaunchLkmPicker()
        }) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
            onClearLkm()
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun SegmentedListScope.lkmSelector(
    selectedLkmName: String?, onLaunchLkmPicker: () -> Unit, onClearLkm: () -> Unit
) {
    item(
        onClick = {
            onLaunchLkmPicker()
        }, leadingContent = { Icon(painterResource(R.drawable.ic_drive_file_move_rounded), null) }, supportingContent = selectedLkmName?.let { name ->
            {
                Text(
                    text = stringResource(id = R.string.selected_lkm, name), color = MaterialTheme.colorScheme.primary
                )
            }
        }, trailingContent = if (selectedLkmName != null) {
            {
                IconButton(onClick = onClearLkm) {
                    Icon(Icons.Default.Clear, null)
                }
            }
        } else null, content = { Text(stringResource(id = R.string.install_upload_lkm_file)) })
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun SegmentedListScope.partitionSelector(
    visible: Boolean, partitions: List<String>, partition: String, partitionSuffix: String?, onPartitionChange: (String) -> Unit
) {
    val suffix = if (partitionSuffix != null) " ($partitionSuffix)" else ""
    menuItem(
        visible = visible,
        leadingContent = { Icon(painterResource(R.drawable.ic_hard_drive_rounded_24dp), null) },
        selected = partition,
        menuContent = { dismiss ->
            partitions.forEachIndexed { index, name ->
                DropdownMenuItem(text = { Text(name) }, onClick = {
                    if (index in partitions.indices) {
                        onPartitionChange(partitions[index])
                    }
                    dismiss()
                })
            }
        },
        content = { Text("${stringResource(R.string.install_select_partition)}$suffix") })
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TopBar(
    onBack: () -> Unit = {}, scrollBehavior: TopAppBarScrollBehavior
) {
    LargeFlexibleTopAppBar(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        title = { Text(stringResource(R.string.install)) },
        colors = defaultTopAppBarColors(),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        },
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        scrollBehavior = scrollBehavior
    )
}

private fun isKoFile(context: Context, uri: Uri): Boolean {
    val seg = uri.lastPathSegment ?: ""
    if (seg.endsWith(".ko", ignoreCase = true)) return true

    return try {
        context.contentResolver.query(
            uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
        )?.use { cursor ->
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx != -1 && cursor.moveToFirst()) {
                val name = cursor.getString(idx)
                name?.endsWith(".ko", ignoreCase = true) == true
            } else {
                false
            }
        } ?: false
    } catch (_: Throwable) {
        false
    }
}
