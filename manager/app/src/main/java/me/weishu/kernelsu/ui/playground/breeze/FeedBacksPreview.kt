package me.weishu.kernelsu.ui.playground.breeze

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.ui.component.breeze.keyDownFeedBack
import me.weishu.kernelsu.ui.component.breeze.longPressFeedBack
import me.weishu.kernelsu.ui.component.breeze.switchHapticFeedBack

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FeedBacksPreview() {
    val onKeyDown = keyDownFeedBack()
    val onLongPress = longPressFeedBack()
    val onSwitch = switchHapticFeedBack()

    var switchChecked by remember { mutableStateOf(false) }
    var keyDownClicked by remember { mutableStateOf(false) }
    var longPressClicked by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "FeedBacks — Haptic Feedback",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // keyDown
        Button(onClick = {
            onKeyDown()
            keyDownClicked = true
        }) {
            Text("keyDownFeedBack (VirtualKey)")
        }
        if (keyDownClicked) {
            Text("VirtualKey triggered.", style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(8.dp))

        // longPress
        Button(onClick = {
            onLongPress()
            longPressClicked = true
        }) {
            Text("longPressFeedBack (LongPress)")
        }
        if (longPressClicked) {
            Text("LongPress triggered.", style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(8.dp))

        // switch
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = switchChecked,
                onCheckedChange = {
                    switchChecked = it
                    onSwitch(it)
                }
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "switchHapticFeedBack (ToggleOn/Off)",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Switch is ${if (switchChecked) "ON" else "OFF"}.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewFeedBacks() {
    FeedBacksPreview()
}
