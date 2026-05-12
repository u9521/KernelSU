package me.weishu.kernelsu.ui.component.choosekmidialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.breeze.PopupFeedBack
import me.weishu.kernelsu.ui.util.getCurrentKmi
import me.weishu.kernelsu.ui.util.getSupportedKmis
import me.weishu.kernelsu.ui.util.windowBlurBehind

@Composable
fun ChooseKmiDialogBreeze(
    show: Boolean,
    onDismissRequest: () -> Unit,
    onSelected: (String?) -> Unit
) {
    if (!show) return

    val supportedKMIs by produceState(initialValue = emptyList()) {
        value = getSupportedKmis()
    }

    val currentKmi by produceState(initialValue = "") {
        value = getCurrentKmi()
    }

    KmiSelectDialog(currentKmi, supportedKMIs, onConfirm = onSelected, onDismiss = onDismissRequest)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun KmiSelectDialog(
    currentKmi: String, options: List<String>, onConfirm: (String) -> Unit, onDismiss: () -> Unit
) {
    var selectedOption by rememberSaveable { mutableStateOf(currentKmi) }
    var canConfirm by remember { mutableStateOf(false) }
    val enableConfirm = { canConfirm = true }
    val haptic = LocalHapticFeedback.current
    PopupFeedBack()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .heightIn(max = 500.dp)
            .windowBlurBehind(),
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        title = {
            Column {
                Text(text = stringResource(R.string.select_kmi), style = MaterialTheme.typography.titleLarge)
                Text(text = stringResource(R.string.current_kmi, currentKmi.let { it.ifBlank { "Unknown" } }), style = MaterialTheme.typography.bodyMedium)
            }
        }, text = {
            Column(
                modifier = Modifier
                    .selectableGroup()
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
            ) {
                options.forEachIndexed { index, kmi ->
                    val isSelected = (kmi == selectedOption)
                    LaunchedEffect(isSelected) {
                        if (isSelected) enableConfirm()
                    }
                    SegmentedListItem(
                        modifier = Modifier.heightIn(64.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        selected = isSelected, onClick = {
                            selectedOption = kmi
                        },
                        colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                        shapes = ListItemDefaults.segmentedShapes(index, options.size),
                        leadingContent = {
                            RadioButton(
                                selected = isSelected, onClick = null
                            )
                        }, content = {
                            Column {
                                Text(
                                    text = kmi,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                        },
                        supportingContent = if (kmi == currentKmi) {
                            {
                                Text(
                                    text = stringResource(R.string.current_device_kmi),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else null
                    )
                }
            }
        }, confirmButton = {
            Button(
                enabled = canConfirm, onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    onConfirm(selectedOption)
                }, shapes = ButtonDefaults.shapes()
            ) {
                Text(stringResource(android.R.string.ok))
            }
        }, dismissButton = {
            OutlinedButton(
                shapes = ButtonDefaults.shapes(),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    onDismiss()
                }) {
                Text(stringResource(android.R.string.cancel))
            }
        })
}

@Composable
@Preview
private fun KmiSelectDialogPreview() {
    KmiSelectDialog(
        currentKmi = "android14-6.1",
        options = listOf(
            "android12-5.10",
            "android13-5.10",
            "android13-5.15",
            "android14-5.15",
            "android14-6.1",
            "android15-6.6",
            "android16-6.12"
        ),
        onConfirm = {},
        onDismiss = {},
    )
}
