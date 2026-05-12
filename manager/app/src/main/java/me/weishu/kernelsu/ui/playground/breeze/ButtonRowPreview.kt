package me.weishu.kernelsu.ui.playground.breeze

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.ui.UiMode
import me.weishu.kernelsu.ui.component.breeze.ActionButton
import me.weishu.kernelsu.ui.component.breeze.ButtonPosition
import me.weishu.kernelsu.ui.component.breeze.ButtonSpec
import me.weishu.kernelsu.ui.component.breeze.ButtonType
import me.weishu.kernelsu.ui.component.breeze.EnumeratedPriorityButtonRow
import me.weishu.kernelsu.ui.theme.KernelSUTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ButtonRowPreview() {
    val context = LocalContext.current
    val sc = { Toast.makeText(context, "single click", Toast.LENGTH_SHORT).show() }
    val lc = { Toast.makeText(context, "long click", Toast.LENGTH_SHORT).show() }
    val startButtons = listOf(
        ButtonSpec(
            id = "edit",
            text = "Edit",
            icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
            onClick = { sc() },
            onLongClick = { lc() },
            type = ButtonType.TONAL,
            buttonPosition = ButtonPosition.START
        ), ButtonSpec(
            id = "add",
            text = "Add",
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            onClick = { sc() },
            onLongClick = { lc() },
            type = ButtonType.PRIMARY,
            buttonPosition = ButtonPosition.START
        )
    )
    val endButtons = listOf(
        ButtonSpec(
            id = "share",
            text = "Share",
            icon = { Icon(Icons.Filled.Share, contentDescription = null) },
            onClick = { sc() },
            onLongClick = { lc() },
            type = ButtonType.TONAL,
            buttonPosition = ButtonPosition.END
        ), ButtonSpec(
            id = "delete",
            text = "Delete",
            icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            onClick = { sc() },
            onLongClick = { lc() },
            type = ButtonType.TONAL,
            buttonPosition = ButtonPosition.END
        )
    )

    var cardWidth by remember { mutableFloatStateOf(360f) }
    val windowInfo = LocalWindowInfo.current
    val screenWidth = windowInfo.containerDpSize.width

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "ButtonRow — EnumeratedPriorityButtonRow", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            "Drag the slider to shrink the card width and observe how start buttons expand before end buttons.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            "Card width: ${cardWidth.toInt()} dp", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 4.dp)
        )

        Slider(
            value = cardWidth, onValueChange = { cardWidth = it }, valueRange = 180f..screenWidth.value, modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier.widthIn(max = cardWidth.dp)
        ) {
            EnumeratedPriorityButtonRow(
                centerSpacing = 50.dp, startButtons = startButtons, endButtons = endButtons, buttonFactory = { spec, isExpanded ->
                    ActionButton(
                        text = if (isExpanded) spec.text else null,
                        icon = spec.icon,
                        enabled = spec.isEnabled,
                        visible = spec.isVisible,
                        isExpanded = isExpanded,
                        buttonType = spec.type,
                        buttonPosition = spec.buttonPosition,
                        onClick = spec.onClick,
                        onLongClick = spec.onLongClick
                    )
                })
        }
        Button(
            shapes = ButtonDefaults.shapes(),
            onClick = { },
        ) {
            Text("material default")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewButtonRow() {
    KernelSUTheme(uiMode = UiMode.Breeze) {
        ButtonRowPreview()
    }
}
