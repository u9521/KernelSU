package me.weishu.kernelsu.ui.component.breeze

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.navigation3.breeze.LocalContentRatio
import me.weishu.kernelsu.ui.navigation3.breeze.LocalIsDetailPane

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
                painter = painterResource(R.drawable.ic_splitscreen_right_rounded),
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
                    value = currentRatioState.floatValue,
                    onValueChange = { newValue ->
                        currentRatioState.floatValue = newValue
                    },
                    valueRange = minRatio..maxRatio,
                    steps = 0,
                    modifier = Modifier
                        .width(200.dp)
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                )
            }
        }
    }
}
