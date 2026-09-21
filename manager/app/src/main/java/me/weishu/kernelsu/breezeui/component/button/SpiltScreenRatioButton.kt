package me.weishu.kernelsu.breezeui.component.button

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.breezeui.component.feedback.PopupFeedBack
import me.weishu.kernelsu.breezeui.icons.MaterialSymbols
import me.weishu.kernelsu.breezeui.nav.LocalContentRatio
import me.weishu.kernelsu.breezeui.nav.LocalIsDetailPane

@Composable
fun SplitScreenRatioButton() {

    val currentRatioState = LocalContentRatio.current ?: return
    val inDetailPane = LocalIsDetailPane.current
    if (!inDetailPane) return
    val windowInfo = LocalWindowInfo.current
    val screenWidthDp = windowInfo.containerDpSize.width
    val safeLimitDp = 250f.dp

    val minRatio = if (screenWidthDp.value > 0) {
        (safeLimitDp / screenWidthDp).coerceIn(0.1f, 0.5f)
    } else {
        0.1f
    }
    val maxRatio = 1f - minRatio

    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {

        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = MaterialSymbols.Rounded.SplitscreenRight,
                contentDescription = "Adjust Split Ratio"
            )
        }

        DropdownMenuPopup(
            expanded = expanded,
            modifier = Modifier.padding(end = 16.dp),
            onDismissRequest = { expanded = false }
        ) {
            PopupFeedBack()
            DropdownMenuGroup(shapes = MenuDefaults.groupShapes()) {
                Slider(
                    state = rememberSliderState(value = currentRatioState.floatValue, steps = 0, trackRange = minRatio..maxRatio),
                    modifier = Modifier
                        .width(200.dp)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    enabled = true,
                    onValueChange = { newValue ->
                        currentRatioState.floatValue = newValue
                    },
                )
            }
        }
    }
}
