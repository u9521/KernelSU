package me.weishu.kernelsu.ui.component

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.Role


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SwitchItem(
    painterIcon: Painter? = null,
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
            ), headlineContent = {
            Text(title)
        }, leadingContent = painterIcon?.let {
            { Icon(painterIcon, title) }
        }, trailingContent = {
            Crossfade(enabled) {
                Switch(
                    checked = checked, enabled = it, onCheckedChange = onCheckedChange, interactionSource = interactionSource
                )
            }
        }, supportingContent = {
            if (summary != null) {
                Text(summary)
            }
        })
}

@Composable
fun SwitchItem(
    icon: ImageVector? = null,
    title: String,
    summary: String? = null,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    SwitchItem(
        painterIcon = icon?.let { rememberVectorPainter(it) },
        title = title,
        summary = summary,
        checked = checked,
        enabled = enabled,
        onCheckedChange = onCheckedChange
    )
}
