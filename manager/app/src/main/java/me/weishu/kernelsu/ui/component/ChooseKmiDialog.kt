package me.weishu.kernelsu.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.R
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

    AlertDialog(onDismissRequest = onDismiss, modifier = Modifier.heightIn(max = 500.dp), title = {
        Column {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Text(text = stringResource(R.string.current_kmi, currentKmi.let { it.ifBlank { "Unknown" } }), style = MaterialTheme.typography.bodyMedium)
        }
    }, text = {
        Column(
            modifier = Modifier
                .selectableGroup()
                .verticalScroll(rememberScrollState())
        ) {
            options.forEach { kmi ->
                val isSelected = (kmi == selectedOption)

                LaunchedEffect(isSelected) {
                    if (isSelected) enableConfirm()
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .heightIn(min = 56.dp)
                        .selectable(
                            selected = isSelected, onClick = { selectedOption = kmi }, role = Role.RadioButton
                        )
                        .padding(horizontal = 12.dp,vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected, onClick = null
                    )

                    Column(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .weight(1f)
                    ) {
                        Text(
                            text = kmi,
                            style = MaterialTheme.typography.titleMedium,
                        )

                        if (kmi == currentKmi) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.current_device_kmi),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }, confirmButton = {
        Button(
            enabled = canConfirm, onClick = {
                onConfirm(selectedOption)
            }, shapes = ButtonDefaults.shapes()
        ) {
            Text(stringResource(android.R.string.ok))
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text(stringResource(android.R.string.cancel))
        }
    })
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberSelectKmiDialog(defaultKmi: String, onSelected: (String) -> Unit): DialogHandle {
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