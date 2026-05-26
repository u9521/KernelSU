package me.weishu.kernelsu.ui.screen.executemoduleaction

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
fun ExecuteModuleActionScreenBreeze(
    state: ExecuteModuleActionUiState,
    actions: ExecuteModuleActionScreenActions,
    snackbarHostState: SnackbarHostState
) {
    val keyFeedback = keyDownFeedBack()
    BackHandler(enabled = !state.isComplete) { }
    ShellLogScaffold(
        title = { Text(stringResource(R.string.action)) },
        text = state.text,
        onBack = actions.onBack,
        onSave = actions.onSaveLog,
        snackBarHost = snackbarHostState,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    keyFeedback()
                    actions.onClose()
                },
                icon = { Icon(Icons.Filled.Close, null) },
                text = { Text(stringResource(R.string.close)) },
                modifier = Modifier
                    .padding(bottom = fABBottomPadding())
                    .animateFloatingActionButton(state.isComplete, alignment = Alignment.CenterEnd),
            )
        },
    )
}
