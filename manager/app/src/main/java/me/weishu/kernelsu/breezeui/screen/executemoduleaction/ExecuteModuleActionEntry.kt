package me.weishu.kernelsu.breezeui.screen.executemoduleaction

import androidx.activity.compose.LocalActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.dropUnlessResumed
import kotlinx.coroutines.launch
import me.weishu.kernelsu.breezeui.nav.LocalBreezeNavigator
import me.weishu.kernelsu.ui.screen.executemoduleaction.ExecuteModuleActionEffect
import me.weishu.kernelsu.ui.screen.executemoduleaction.ExecuteModuleActionScreenActions
import me.weishu.kernelsu.ui.screen.executemoduleaction.ExecuteModuleActionUiState
import me.weishu.kernelsu.ui.screen.executemoduleaction.saveLog

/**
 * Breeze's module action page: owns the state/action wiring so the upstream module
 * action screen does not have to know about Breeze.
 */
@Composable
fun ExecuteModuleActionEntry(moduleId: String, fromShortcut: Boolean) {
    val navigator = LocalBreezeNavigator.current
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()
    var text by rememberSaveable { mutableStateOf("") }
    val logContent = remember { StringBuilder() }
    var isComplete by rememberSaveable { mutableStateOf(false) }
    val snackbarHost = remember { SnackbarHostState() }
    val exitExecute = {
        if (fromShortcut && activity != null) {
            activity.finishAndRemoveTask()
        } else {
            navigator.pop()
        }
    }

    fun showMessage(message: String) {
        scope.launch {
            snackbarHost.showSnackbar(message)
        }
    }

    // Always auto disable from shortcuts
    LaunchedEffect(isComplete) {
        if (isComplete) {
            if (fromShortcut) {
                exitExecute()
            }
        }
    }

    ExecuteModuleActionEffect(
        moduleId = moduleId,
        text = text,
        logContent = logContent,
        fromShortcut = fromShortcut,
        onTextUpdate = { text = it },
        onComplete = { isComplete = true },
        onExit = exitExecute
    )

    val state = ExecuteModuleActionUiState(
        text = text,
        isComplete = isComplete,
    )
    val actions = ExecuteModuleActionScreenActions(
        onBack = dropUnlessResumed { navigator.pop() },
        onSaveLog = saveLog(logContent, scope) { showMessage(it) },
        onClose = exitExecute,
    )

    ExecuteModuleActionScreenBreeze(state, actions, snackbarHost)
}
