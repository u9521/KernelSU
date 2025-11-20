package me.weishu.kernelsu.ui.component

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.NativeKeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.R
import kotlin.math.min


@Composable
fun SwitchItem(
    icon: ImageVector? = null,
    title: String,
    summary: String? = null,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    ListItem(
        modifier = Modifier
            .toggleable(
                value = checked,
                interactionSource = interactionSource,
                role = Role.Switch,
                enabled = enabled,
                indication = LocalIndication.current,
                onValueChange = onCheckedChange
            ),
        headlineContent = {
            Text(title)
        },
        leadingContent = icon?.let {
            { Icon(icon, title) }
        },
        trailingContent = {
            Switch(
                checked = checked,
                enabled = enabled,
                onCheckedChange = onCheckedChange,
                interactionSource = interactionSource
            )
        },
        supportingContent = {
            if (summary != null) {
                Text(summary)
            }
        }
    )
}

@Composable
fun RadioItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(title)
        },
        leadingContent = {
            RadioButton(selected = selected, onClick = onClick)
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FeatureItem(
    icon: ImageVector? = null, title: String,enabled: Boolean = true, summary: String? = null, index: Int, onCheckedChange: (Int) -> Unit
) {
    val modeItems = listOf(
        stringResource(id = R.string.settings_mode_default),
        stringResource(id = R.string.settings_mode_temp_enable),
        stringResource(id = R.string.settings_mode_always_enable),
    )
    var selectedIndex by remember { mutableIntStateOf(index) }
    Column {
        ListItem(headlineContent = {
            Text(title)
        }, leadingContent = {
            icon?.let {
                Icon(icon, title)
            }
        }, supportingContent = {
            if (summary != null) {
                Text(summary)
            }
        })
        FlowRow(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            modeItems.forEachIndexed { index, label ->
                ToggleButton(
                    checked = selectedIndex == index,
                    enabled = enabled,
                    onCheckedChange = {
                        selectedIndex = index
                        onCheckedChange(index)
                    },
                    shapes = when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        modeItems.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .semantics { role = Role.RadioButton },
                ) {
                    Text(label)
                }
            }
        }
    }
}

@Composable
fun BrMenuBox(
    description: String?, menuContent: @Composable ColumnScope.(dismissMenu: () -> Unit) -> Unit, content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var touchPoint: Offset by remember { mutableStateOf(Offset.Zero) }
    val interactionSource = remember { MutableInteractionSource() }
    val dismissMenu = { expanded = false }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .indication(interactionSource, LocalIndication.current)
            .focusable(interactionSource = interactionSource)
            .semantics {
                role = Role.Button
                onClick(label = description) {
                    touchPoint = Offset.Zero
                    expanded = true
                    true
                }
            }
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp && (event.key == Key.Enter || event.key == Key.NumPadEnter || event.nativeKeyEvent.keyCode == NativeKeyEvent.KEYCODE_DPAD_CENTER)) {
                    touchPoint = Offset.Zero
                    expanded = true
                    return@onKeyEvent true
                }
                false
            }
            .pointerInput(Unit) {
                detectTapGestures(onPress = { offset ->
                    val press = PressInteraction.Press(offset)
                    interactionSource.emit(press)
                    val isReleased = tryAwaitRelease()
                    if (isReleased) {
                        interactionSource.emit(PressInteraction.Release(press))
                    } else {
                        interactionSource.emit(PressInteraction.Cancel(press))
                    }
                }, onTap = { offset ->
                    touchPoint = offset
                    expanded = true
                })
            }) {
        content()
        Box(
            modifier = Modifier
                .offset { IntOffset(touchPoint.x.toInt(), touchPoint.y.toInt()) }
        ) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = dismissMenu,
                offset = DpOffset.Zero,
            ) {
                menuContent(dismissMenu)
            }
        }
    }
}

@Composable
fun BrDropdownMenuItem(
    icon: ImageVector? = null,
    title: String,
    summary: String? = null,
    selected: String? = null,
    menuContent: @Composable ColumnScope.(dismissMenu: () -> Unit) -> Unit
) {
    BrMenuBox(title, menuContent = menuContent, content = {
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = {
                if (summary != null) {
                    Text(summary)
                }
            },
            leadingContent = {
                icon?.let {
                    Icon(icon, title)
                }
            },
            trailingContent = {
                selected?.let {
                    Text(selected)
                }
            }
        )
    })
}