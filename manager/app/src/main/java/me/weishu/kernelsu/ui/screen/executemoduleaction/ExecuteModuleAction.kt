package me.weishu.kernelsu.ui.screen.executemoduleaction

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.ShellLogScaffold
import me.weishu.kernelsu.ui.navigation3.LocalNavController

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExecuteModuleActionScreen(moduleId: String) {
    val navigator = LocalNavController.current
    var shellOutputText by rememberSaveable { mutableStateOf("") }
    val logContentSB = rememberSaveable { StringBuilder() }
    var showFloatAction by rememberSaveable { mutableStateOf(false) }

    ExecuteModuleActionEffect(
        moduleId = moduleId,
        text = shellOutputText,
        logContent = logContentSB,
        onTextUpdate = { shellOutputText = it },
        onExit = { navigator.popBackStack() },
        onFinish = { showFloatAction = true }
    )

    ShellLogScaffold(
        title = {
            Text(stringResource(R.string.action))
        },
        text = shellOutputText,
        logContent = logContentSB.toString(),
        logFileNamePrefix = "KernelSU_module_action_log",
        floatingActionButton = {
            CloseFAB(showFloatAction)
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun CloseFAB(showFloatAction: Boolean) {
    val navigator = LocalNavController.current
    val haptic = LocalHapticFeedback.current
    val onBack = dropUnlessResumed {
        haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
        navigator.popBackStack()
    }
    ExtendedFloatingActionButton(
        modifier = Modifier
            .padding(bottom = 80.dp /* NavBar height */)
            .animateFloatingActionButton(
                visible = showFloatAction,
                alignment = Alignment.CenterEnd,
            ),
        onClick = onBack,
        icon = { Icon(Icons.Filled.Close, stringResource(R.string.close)) },
        text = { Text(text = stringResource(R.string.close)) },
    )
}