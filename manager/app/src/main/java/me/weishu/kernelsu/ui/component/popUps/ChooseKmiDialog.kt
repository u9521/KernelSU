package me.weishu.kernelsu.ui.component.popUps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.rememberCustomDialog
import me.weishu.kernelsu.ui.util.getSupportedKmis


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun KmiSelectDialog(
    title: String, currentKmi: String, options: List<String>, onConfirm: (String) -> Unit, onDismiss: () -> Unit
) {
    var selectedOption by remember {
        mutableStateOf(currentKmi)
    }
    var canConfirm by remember { mutableStateOf(false) }
    val enableConfirm = { canConfirm = true }
    val haptic = LocalHapticFeedback.current
    PopupFeedBack()

    AlertDialog(onDismissRequest = onDismiss, modifier = Modifier.heightIn(max = 500.dp), containerColor = MaterialTheme.colorScheme.surfaceContainer, title = {
        Column {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Text(text = stringResource(R.string.current_kmi, currentKmi.let { it.ifBlank { "Unknown" } }), style = MaterialTheme.typography.bodyMedium)
        }
    }, text = {
        Column(
            modifier = Modifier
                .selectableGroup()
                .verticalScroll(rememberScrollState()),
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
                        enableConfirm()
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberSelectKmiDialog(defaultKmi: String, onSelected: (String) -> Unit): me.weishu.kernelsu.ui.component.DialogHandle {
    return rememberCustomDialog { dismiss ->
        val supportedKmi by produceState(initialValue = emptyList()) {
            value = getSupportedKmis()
        }

        KmiSelectDialog(
            title = stringResource(R.string.select_kmi), options = supportedKmi, currentKmi = defaultKmi, onConfirm = { selected ->
                onSelected(selected)
                dismiss()
            }, onDismiss = dismiss
        )
    }
}


@Composable
@Preview
private fun KmiSelectDialogPreview() {
    KmiSelectDialog(
        title = "kmiSelection",
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