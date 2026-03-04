package me.weishu.kernelsu.ui.screen.flash

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.ShellLogScaffold
import me.weishu.kernelsu.ui.util.reboot

/**
 * @author weishu
 * @date 2023/1/1.
 */


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FlashScreen(flashIt: FlashIt) {

    var flashOutputText by rememberSaveable { mutableStateOf("") }
    val logContentSB = rememberSaveable { StringBuilder() }
    var showFloatAction by rememberSaveable { mutableStateOf(false) }
    var flashing by rememberSaveable {
        mutableStateOf(FlashingStatus.FLASHING)
    }

    FlashEffect(
        flashIt = flashIt,
        text = flashOutputText,
        logContent = logContentSB,
        onTextUpdate = { flashOutputText = it },
        onShowRebootChange = { showFloatAction = it },
        onFlashingStatusChange = { flashing = it }
    )

    ShellLogScaffold(
        title = {
            Text(
                stringResource(
                    when (flashing) {
                        FlashingStatus.FLASHING -> R.string.flashing
                        FlashingStatus.SUCCESS -> R.string.flash_success
                        FlashingStatus.FAILED -> R.string.flash_failed
                    }
                )
            )
        },
        text = flashOutputText,
        logContent = logContentSB.toString(),
        logFileNamePrefix = "KernelSU_install_log",
        floatingActionButton = {
            RebootFAB(showFloatAction)
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun RebootFAB(showFloatAction: Boolean) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    ExtendedFloatingActionButton(
        modifier = Modifier
            .padding(bottom = 80.dp /* NavBar height */)
            .animateFloatingActionButton(
                visible = showFloatAction,
                alignment = Alignment.CenterEnd,
            ),
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
            scope.launch { withContext(Dispatchers.IO) { reboot() } }
        },
        icon = { Icon(Icons.Filled.Refresh, stringResource(id = R.string.reboot)) },
        text = { Text(text = stringResource(id = R.string.reboot)) },
    )
}
