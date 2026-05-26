package me.weishu.kernelsu.ui.screen.flash

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import me.weishu.kernelsu.ui.component.breeze.ShellLogScaffold
import me.weishu.kernelsu.ui.component.breeze.keyDownFeedBack
import me.weishu.kernelsu.ui.util.fABBottomPadding

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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
                text = { Text(stringResource(R.string.reboot)) },
                modifier = Modifier
                    .padding(bottom = fABBottomPadding())
                    .animateFloatingActionButton(state.showRebootAction, alignment = Alignment.CenterEnd),
            )
        },
    )
}
