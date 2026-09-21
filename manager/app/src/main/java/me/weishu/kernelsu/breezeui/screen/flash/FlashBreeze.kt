package me.weishu.kernelsu.breezeui.screen.flash

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import me.weishu.kernelsu.R
import me.weishu.kernelsu.breezeui.component.feedback.keyDownFeedBack
import me.weishu.kernelsu.breezeui.component.text.ShellLogScaffold
import me.weishu.kernelsu.breezeui.util.fABBottomPadding
import me.weishu.kernelsu.ui.screen.flash.FlashScreenActions
import me.weishu.kernelsu.ui.screen.flash.FlashUiState
import me.weishu.kernelsu.ui.screen.flash.FlashingStatus
import me.weishu.kernelsu.ui.screen.flash.JailbreakFlashWarningDialog

@Composable
fun FlashScreenBreeze(
    state: FlashUiState,
    actions: FlashScreenActions,
    snackbarHostState: SnackbarHostState
) {
    if (state.showJailbreakWarning) {
        JailbreakFlashWarningDialog(
            onConfirm = actions.onConfirmJailbreakWarning,
            onDismiss = actions.onDismissJailbreakWarning,
        )
    }

    ShellLogScaffold(
        title = {
            Text(
                stringResource(
                    when (state.flashingStatus) {
                        FlashingStatus.FLASHING -> R.string.flashing
                        FlashingStatus.SUCCESS -> R.string.flash_success
                        FlashingStatus.FAILED -> R.string.flash_failed
                    },
                )
            )
        },
        text = state.text,
        onBack = actions.onBack,
        onSave = actions.onSaveLog,
        snackBarHost = snackbarHostState,
        floatingActionButton = {
            val keyFeedback = keyDownFeedBack()
            ExtendedFloatingActionButton(
                onClick = {
                    keyFeedback()
                    actions.onReboot()
                },
                icon = { Icon(Icons.Filled.Refresh, contentDescription = null) },
                text = { Text(stringResource(state.rebootLabelRes)) },
                modifier = Modifier
                    .padding(bottom = fABBottomPadding())
                    .animateFloatingActionButton(state.showRebootAction, alignment = Alignment.CenterEnd),
            )
        },
    )
}
