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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallExtendedFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import me.weishu.kernelsu.R
import me.weishu.kernelsu.getKernelVersion
import me.weishu.kernelsu.ui.component.BrDropdownMenuItem
import me.weishu.kernelsu.ui.component.rememberConfirmDialog
import me.weishu.kernelsu.ui.component.rememberSelectKmiDialog
import me.weishu.kernelsu.ui.navigation3.Route
import me.weishu.kernelsu.ui.util.LkmSelection
import me.weishu.kernelsu.ui.util.LocalNavController
import me.weishu.kernelsu.ui.util.getAvailablePartitions
import me.weishu.kernelsu.ui.util.getCurrentKmi
import me.weishu.kernelsu.ui.util.getDefaultPartition
import me.weishu.kernelsu.ui.util.getSlotSuffix
import me.weishu.kernelsu.ui.util.isAbDevice
import me.weishu.kernelsu.ui.util.rootAvailable

/**
 * @author weishu
 * @date 2024/3/12.
 */


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
        override val label: Int
            get() = R.string.direct_install
    }

    data object DirectInstallToInactiveSlot : InstallMethod() {
        override val label: Int
            get() = R.string.install_inactive_slot
    }

    abstract val label: Int

    @IgnoredOnParcel
    open val summary: String? = null
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InstallScreen() {
    val context = LocalContext.current
    val navigator = LocalNavController.current
    val hapticFeedback = LocalHapticFeedback.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    var installMethod by rememberSaveable { mutableStateOf<InstallMethod?>(null) }
    var lkmSelection by rememberSaveable { mutableStateOf<LkmSelection>(LkmSelection.KmiNone) }
    var partitionSelectionIndex by rememberSaveable { mutableIntStateOf(0) }
    var partitionSelected by rememberSaveable { mutableStateOf(false) }

    val envData = produceState(initialValue = EnvData()) {
        value = EnvData(
            rootAvailable = rootAvailable(),
            isAbDevice = isAbDevice(),
            defaultPartition = getDefaultPartition(),
            partitions = getAvailablePartitions(),
            currentKmi = getCurrentKmi()
        )
    }.value

    val partitionRecommend = stringResource(R.string.select_file_tip, envData.defaultPartition)

    val onFileSelectLaunch = { uri: Uri ->
        installMethod = InstallMethod.SelectFile(uri, summary = partitionRecommend)
    }

    val onLkmSelectLaunch = { uri: Uri ->
        if (isKoFile(context, uri)) {
            lkmSelection = LkmSelection.LkmUri(uri)
        } else {
            lkmSelection = LkmSelection.KmiNone
            Toast.makeText(context, R.string.install_only_support_ko_file, Toast.LENGTH_SHORT).show()
        }
    }

    val selectImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == Activity.RESULT_OK) res.data?.data?.let(onFileSelectLaunch)
    }
    val selectLkmLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == Activity.RESULT_OK) res.data?.data?.let(onLkmSelectLaunch)
    }

    val onInstallClick = {
        installMethod?.let { method ->
            val isOta = method is InstallMethod.DirectInstallToInactiveSlot
            val partitionSelection = envData.partitions.getOrNull(partitionSelectionIndex)
            val flashIt = FlashIt.FlashBoot(
                boot = if (method is InstallMethod.SelectFile) method.uri else null, lkm = lkmSelection, ota = isOta, partition = partitionSelection
            )
            navigator.navigateTo(Route.Flash(flashIt))
            // reset after navigation
            installMethod = null
            lkmSelection = LkmSelection.KmiNone
            partitionSelected = false
        }
    }

    LaunchedEffect(envData.defaultPartition, envData.partitions) {
        if (!partitionSelected && envData.partitions.isNotEmpty()) {
            val idx = envData.partitions.indexOf(envData.defaultPartition)
            if (idx >= 0) partitionSelectionIndex = idx
        }
    }

    val selectKmiDialog = rememberSelectKmiDialog(envData.currentKmi) { kmi ->
        hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
        lkmSelection = LkmSelection.KmiString(kmi)
        onInstallClick()
    }

    val checkAndInstall: () -> Unit = {
        val isKmiUnselected = lkmSelection == LkmSelection.KmiNone
        val isKmiUnknown = envData.currentKmi.isBlank()
        val isSelectFileMode = installMethod is InstallMethod.SelectFile
        // When the KMI version is unknown or when manually selecting an image file for patching, display the KMI selection dialog
        if (isKmiUnselected && (isKmiUnknown || isSelectFileMode)) {
            selectKmiDialog.show()
        } else {
            onInstallClick()
        }
    }

    Scaffold(topBar = {
        TopBar(
            onBack = dropUnlessResumed { navigator.popBackStack() }, scrollBehavior = scrollBehavior
        )
    }, contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal), floatingActionButton = {
        SmallExtendedFloatingActionButton(
            content = {
                Text(stringResource(id = R.string.install_next))
                Spacer(Modifier.width(2.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, stringResource(id = R.string.install_next))
            },
            modifier = Modifier
                .padding(bottom = 6.dp + 48.dp + 6.dp /* SnackBar height */)
                .animateFloatingActionButton(
                    visible = installMethod != null,
                    alignment = Alignment.CenterEnd,
                ),
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                checkAndInstall()
            },
        )
    }


    ) { innerPadding ->
        val selectFileTip = stringResource(R.string.select_file_tip, envData.defaultPartition)
        val kernelVersion = getKernelVersion()
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
        InstallInner(
            modifier = Modifier.padding(innerPadding),
            scrollBehavior = scrollBehavior,
            availableMethods = availableMethods,
            currentMethod = installMethod,
            lkmSelection = lkmSelection,
            partitions = envData.partitions,
            defaultPartition = envData.defaultPartition,
            partitionIndex = partitionSelectionIndex,
            onMethodChange = { installMethod = it },
            onPartitionChange = { index ->
                hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                partitionSelected = true
                partitionSelectionIndex = index
            },
            onLaunchImagePicker = {
                selectImageLauncher.launch(Intent(Intent.ACTION_GET_CONTENT).apply { type = "application/octet-stream" })
            },
            onLaunchLkmPicker = {
                selectLkmLauncher.launch(Intent(Intent.ACTION_GET_CONTENT).apply { type = "application/octet-stream" })
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun InstallInner(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior,
    availableMethods: List<InstallMethod>,
    currentMethod: InstallMethod?,
    lkmSelection: LkmSelection,
    partitions: List<String>,
    defaultPartition: String,
    partitionIndex: Int,
    onMethodChange: (InstallMethod) -> Unit,
    onPartitionChange: (Int) -> Unit,
    onLaunchImagePicker: () -> Unit,
    onLaunchLkmPicker: () -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val motionScheme = MaterialTheme.motionScheme
    Column(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .verticalScroll(rememberScrollState())
    ) {
        InstallMethodSelector(
            options = availableMethods, selectedOption = currentMethod, onOptionSelected = { method ->
                if (method is InstallMethod.SelectFile) {
                    onLaunchImagePicker()
                } else {
                    onMethodChange(method)
                }
            })

        val showPartitionSelector = currentMethod is InstallMethod.DirectInstall || currentMethod is InstallMethod.DirectInstallToInactiveSlot

        AnimatedVisibility(
            visible = showPartitionSelector,
            enter = expandVertically(animationSpec = motionScheme.fastSpatialSpec()),
            exit = shrinkVertically(animationSpec = motionScheme.fastSpatialSpec())
        ) {
            val isOta = currentMethod is InstallMethod.DirectInstallToInactiveSlot
            val suffix by produceState("", isOta) { value = getSlotSuffix(isOta) }

            PartitionSelectorCard(
                partitions = partitions, defaultPartition = defaultPartition, selectedIndex = partitionIndex, suffix = suffix, onSelect = onPartitionChange
            )
        }

        LkmSelectorCard(
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                onLaunchLkmPicker()
            })

        (lkmSelection as? LkmSelection.LkmUri)?.let {
            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp), text = stringResource(
                    id = R.string.selected_lkm, it.uri.lastPathSegment ?: "(file)"
                )
            )
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InstallMethodSelector(
    options: List<InstallMethod>, selectedOption: InstallMethod?, onOptionSelected: (InstallMethod) -> Unit
) {
    var pendingOption by remember { mutableStateOf<InstallMethod?>(null) }
    val confirmDialog = rememberConfirmDialog(onConfirm = { pendingOption?.let { onOptionSelected(it) } }, onDismiss = null)
    val motionScheme = MaterialTheme.motionScheme
    val otaTitle = stringResource(id = android.R.string.dialog_alert_title)
    val otDialogContent = stringResource(id = R.string.install_inactive_slot_warning)
    val handleSelection = { option: InstallMethod ->
        if (option is InstallMethod.DirectInstallToInactiveSlot) {
            pendingOption = option

            confirmDialog.showConfirm(
                title = otaTitle, content = otDialogContent
            )
        } else {
            onOptionSelected(option)
        }
    }
    Column(modifier = Modifier.animateContentSize(motionScheme.fastSpatialSpec())) {
        options.forEach { option ->
            val isSelected = option.javaClass == selectedOption?.javaClass
            InstallMethodItem(
                title = stringResource(option.label), summary = option.summary, isSelected = isSelected, onClick = { handleSelection(option) })
        }
    }
}

@Composable
fun InstallMethodItem(title: String, summary: String?, isSelected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = isSelected, onValueChange = {
                    onClick()
                }, role = Role.RadioButton, indication = LocalIndication.current, interactionSource = interactionSource
            )
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        RadioButton(
            selected = isSelected, onClick = null
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Text(
                text = title,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
                fontStyle = MaterialTheme.typography.titleMedium.fontStyle
            )
            summary?.let {
                Text(
                    text = it,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                    fontStyle = MaterialTheme.typography.bodySmall.fontStyle
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
    }
}

@Composable
fun PartitionSelectorCard(
    partitions: List<String>, defaultPartition: String, selectedIndex: Int, suffix: String, onSelect: (Int) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, start = 16.dp, end = 16.dp),
    ) {
        val displayNames = remember(partitions, defaultPartition) {
            partitions.map { if (it == defaultPartition) "$it (default)" else it }
        }

        BrDropdownMenuItem(
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            icon = { Icon(painterResource(R.drawable.ic_hard_drive_rounded_24dp), null) },
            title = "${stringResource(R.string.install_select_partition)} ($suffix)",
            selected = displayNames.getOrNull(selectedIndex)
        ) { dismiss ->
            displayNames.forEachIndexed { index, name ->
                DropdownMenuItem(text = { Text(name) }, onClick = { onSelect(index); dismiss() })
            }
        }
    }
}

@Composable
fun LkmSelectorCard(onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, start = 16.dp, end = 16.dp), onClick = onClick
    ) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            leadingContent = { Icon(painterResource(R.drawable.ic_drive_file_move_rounded_24dp), null) },
            headlineContent = { Text(stringResource(id = R.string.install_upload_lkm_file)) })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onBack: () -> Unit = {}, scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        title = { Text(stringResource(R.string.install)) }, navigationIcon = {
        IconButton(
            onClick = onBack
        ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
    }, windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal), scrollBehavior = scrollBehavior
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


@Composable
@Preview
fun SelectInstallPreview() {
    InstallScreen()
}