package me.weishu.kernelsu.ui.component.uninstalldialog

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.breeze.PopupFeedBack
import me.weishu.kernelsu.ui.component.breeze.keyDownFeedBack
import me.weishu.kernelsu.ui.component.dialog.rememberConfirmDialog
import me.weishu.kernelsu.ui.navigation3.LocalNavigator
import me.weishu.kernelsu.ui.navigation3.Navigator
import me.weishu.kernelsu.ui.navigation3.Route
import me.weishu.kernelsu.ui.navigation3.Route.Flash
import me.weishu.kernelsu.ui.screen.flash.FlashIt
import me.weishu.kernelsu.ui.screen.flash.UninstallType
import me.weishu.kernelsu.ui.util.windowBlurBehind

@Composable
fun UninstallDialogBreeze(
    show: Boolean,
    onDismissRequest: () -> Unit
) {
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    var pendingUninstallType by rememberSaveable { mutableStateOf<UninstallType?>(null) }

    val performUninstall = { type: UninstallType ->
        when (type) {
            UninstallType.TEMPORARY -> {
                Toast.makeText(context, "TODO", Toast.LENGTH_SHORT).show()
            }

            UninstallType.PERMANENT -> {
                navigator.push(Flash(FlashIt.FlashUninstall))
            }

            UninstallType.RESTORE_STOCK_IMAGE -> {
                navigator.push(Flash(FlashIt.FlashRestore))
            }

            UninstallType.NONE -> {}
        }
    }

    val confirmDialog = rememberConfirmDialog(onConfirm = {
        pendingUninstallType?.let { type ->
            performUninstall(type)
            pendingUninstallType = null
            onDismissRequest()
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


    if (show) {
        UninstallSelectionDialogBreeze(onDismissRequest = onDismissRequest, onOptionSelected = { type ->
            if (type == null) return@UninstallSelectionDialogBreeze
            pendingUninstallType = type
        })
    }
}

@Composable
private fun UninstallOptionItemBreeze(
    title: String, summary: String?, selected: Boolean = false, icon: Painter, index: Int, counts: Int, onClick: () -> Unit
) {
    SegmentedListItem(
        selected = selected,
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index, counts),
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceBright),
        verticalAlignment = Alignment.CenterVertically,
        leadingContent = {
            Icon(
                painter = icon, contentDescription = null, modifier = Modifier
                    .padding(end = 16.dp)
                    .size(24.dp), tint = MaterialTheme.colorScheme.primary
            )
        },
        supportingContent = summary?.let {
            {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        content = {
            Text(
                text = title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface, maxLines = 1
            )
        }
    )
}

@Composable
private fun UninstallSelectionDialogBreeze(
    onDismissRequest: () -> Unit, onOptionSelected: (UninstallType?) -> Unit
) {
    val keyFeedback = keyDownFeedBack()
    var selectedOption by rememberSaveable { mutableStateOf<UninstallType?>(null) }
    PopupFeedBack()
    AlertDialog(modifier = Modifier.windowBlurBehind(), containerColor = MaterialTheme.colorScheme.surfaceContainer, onDismissRequest = {}, icon = {
        Icon(painterResource(R.drawable.ic_delete_rounded_filled), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    }, title = {
        Text(text = stringResource(R.string.settings_uninstall))
    }, text = {
        Column(
            modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
            val uninstallTypes = UninstallType.entries.filter { it != UninstallType.TEMPORARY && it != UninstallType.NONE }
            uninstallTypes.forEachIndexed { index, type ->
                UninstallOptionItemBreeze(
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
            keyFeedback()
            onOptionSelected(selectedOption)
        }, shapes = ButtonDefaults.shapes()) {
            Text(stringResource(android.R.string.ok))
        }
    }, dismissButton = {
        OutlinedButton(shapes = ButtonDefaults.shapes(), onClick = {
            keyFeedback()
            onDismissRequest()
        }) {
            Text(stringResource(android.R.string.cancel))
        }
    })
}

@Composable
@Preview
private fun UninstallDialogPrevBreeze() {
    val show = remember { mutableStateOf(true) }
    val navigator = Navigator(Route.Home)
    CompositionLocalProvider(LocalNavigator provides navigator) {
        UninstallDialogBreeze(show.value) { show.value = false }
    }
}
