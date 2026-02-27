package me.weishu.kernelsu.ui.component.popUps

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.rememberConfirmDialog
import me.weishu.kernelsu.ui.component.rememberCustomDialog
import me.weishu.kernelsu.ui.navigation3.LocalNavController
import me.weishu.kernelsu.ui.navigation3.Route
import me.weishu.kernelsu.ui.screen.FlashIt


enum class UninstallType(@get:StringRes val title: Int, @get:StringRes val message: Int, @get:DrawableRes val icon: Int) {
    TEMPORARY(
        R.string.settings_uninstall_temporary, R.string.settings_uninstall_temporary_message, R.drawable.ic_delete_rounded_filled
    ),
    PERMANENT(
        R.string.settings_uninstall_permanent, R.string.settings_uninstall_permanent_message, R.drawable.ic_delete_forever_rounded_filled
    ),
    RESTORE_STOCK_IMAGE(
        R.string.settings_restore_stock_image, R.string.settings_restore_stock_image_message, R.drawable.ic_undo_rounded_filled
    ),
}

@Composable
fun uninstallDialog(): me.weishu.kernelsu.ui.component.DialogHandle {
    val navigator = LocalNavController.current
    val context = LocalContext.current

    var pendingUninstallType by remember { mutableStateOf<UninstallType?>(null) }

    val performUninstall = { type: UninstallType ->
        when (type) {
            UninstallType.TEMPORARY -> {
                Toast.makeText(context, "TODO", Toast.LENGTH_SHORT).show()
            }

            UninstallType.PERMANENT -> {
                navigator.navigateTo(Route.Flash(FlashIt.FlashUninstall))
            }

            UninstallType.RESTORE_STOCK_IMAGE -> {
                navigator.navigateTo(Route.Flash(FlashIt.FlashRestore))
            }
        }
    }

    val selectionDialog = rememberCustomDialog { dismiss ->
        UninstallSelectionDialog(
            onDismissRequest = dismiss, onOptionSelected = {
                if (it == null) return@UninstallSelectionDialog
                pendingUninstallType = it
            })
    }

    val confirmDialog = rememberConfirmDialog(onConfirm = {
        pendingUninstallType?.let { type ->
            selectionDialog.hide()
            performUninstall(type)
            pendingUninstallType = null
        }
    }, onDismiss = {
        pendingUninstallType = null
    })
    if (pendingUninstallType != null) {
        val type = pendingUninstallType!!
        val title = stringResource(type.title)
        val message = stringResource(type.message)

        LaunchedEffect(type) {
            confirmDialog.showConfirm(title = title, content = message)
        }
    }
    return selectionDialog
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun UninstallOptionItem(
    title: String, summary: String?, selected: Boolean = false, icon: Painter, index: Int, counts: Int, onClick: () -> Unit
) {
    SegmentedListItem(
        selected = selected,
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index, counts),
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceBright),
        content = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = icon, contentDescription = null, modifier = Modifier
                        .padding(end = 16.dp)
                        .size(24.dp), tint = MaterialTheme.colorScheme.primary
                )
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface, maxLines = 1
                    )

                    summary?.let {
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        })
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UninstallSelectionDialog(
    onDismissRequest: () -> Unit, onOptionSelected: (UninstallType?) -> Unit
) {
    var selectedOption by remember { mutableStateOf<UninstallType?>(null) }
    val haptic = LocalHapticFeedback.current
    PopupFeedBack()
    AlertDialog(containerColor = MaterialTheme.colorScheme.surfaceContainer, onDismissRequest = {}, icon = {
        Icon(painterResource(R.drawable.ic_delete_rounded_filled), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    }, title = {
        Text(text = stringResource(R.string.settings_uninstall))
    }, text = {
        Column(
            modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
            val uninstallTypes = UninstallType.entries.filter { it != UninstallType.TEMPORARY }
            uninstallTypes.forEachIndexed { index, type ->
                UninstallOptionItem(
                    title = stringResource(type.title),
                    summary = stringResource(type.message),
                    selected = selectedOption == type,
                    icon = painterResource(type.icon),
                    index = index,
                    counts = uninstallTypes.size,
                    onClick = {
                        selectedOption = type
                    })
            }
        }
    }, confirmButton = {
        Button(enabled = selectedOption != null, onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
            onOptionSelected(selectedOption)
        }, shapes = ButtonDefaults.shapes()) {
            Text(stringResource(android.R.string.ok))
        }
    }, dismissButton = {
        OutlinedButton(shapes = ButtonDefaults.shapes(), onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
            onDismissRequest()
        }) {
            Text(stringResource(android.R.string.cancel))
        }
    })
}


@Composable
@Preview
private fun UninstallDialogPrev() {
    UninstallSelectionDialog({}) { }
}