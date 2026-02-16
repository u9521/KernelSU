package me.weishu.kernelsu.ui.component.popUps

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.NativeKeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset

@Composable
fun BrMenuBox(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    menuContent: @Composable ColumnScope.(dismissMenu: () -> Unit) -> Unit,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var touchPoint: Offset by remember { mutableStateOf(Offset.Zero) }
    val interactionSource = remember { MutableInteractionSource() }
    val dismissMenu = { expanded = false }
    val hapticFeedback = LocalHapticFeedback.current
    LaunchedEffect(expanded) {
        if (expanded) hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
    }
    Box(
        modifier = modifier
            .indication(interactionSource, LocalIndication.current)
            .focusable(enabled = enabled, interactionSource = interactionSource)
            .semantics {
                role = Role.Button
                if (!enabled) {
                    disabled()
                }
                onClick {
                    touchPoint = Offset.Zero
                    expanded = true
                    true
                }
            }
            .onKeyEvent { event ->
                if (!enabled) return@onKeyEvent false
                if (event.type == KeyEventType.KeyUp && (event.key == Key.Enter || event.key == Key.NumPadEnter || event.nativeKeyEvent.keyCode == NativeKeyEvent.KEYCODE_DPAD_CENTER)) {
                    touchPoint = Offset.Zero
                    expanded = true
                    return@onKeyEvent true
                }
                false
            }) {
        content()
        // consume click events
        if (enabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = { offset ->
                                val press = PressInteraction.Press(offset)
                                interactionSource.emit(press)
                                val isReleased = tryAwaitRelease()
                                if (isReleased) {
                                    interactionSource.emit(PressInteraction.Release(press))
                                } else {
                                    interactionSource.emit(PressInteraction.Cancel(press))
                                }
                            },
                            onTap = { offset ->
                                touchPoint = offset
                                expanded = true
                            }
                        )
                    }
            )
        }
        Box(
            modifier = Modifier.offset { IntOffset(touchPoint.x.toInt(), touchPoint.y.toInt()) }) {
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

