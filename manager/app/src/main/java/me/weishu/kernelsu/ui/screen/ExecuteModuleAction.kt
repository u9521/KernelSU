package me.weishu.kernelsu.ui.screen

import android.widget.Toast
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.ShellLogScaffold
import me.weishu.kernelsu.ui.component.processShellOutput
import me.weishu.kernelsu.ui.util.LocalNavController
import me.weishu.kernelsu.ui.util.runModuleAction
import me.weishu.kernelsu.ui.viewmodel.ModuleViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExecuteModuleActionScreen(moduleId: String) {
    val navigator = LocalNavController.current
    val context = LocalContext.current
    var shellOutputText by rememberSaveable { mutableStateOf("") }
    val logContentSB = rememberSaveable { StringBuilder() }
    var showFloatAction by rememberSaveable { mutableStateOf(false) }

    val noModule = stringResource(R.string.no_such_module)
    val moduleUnavailable = stringResource(R.string.module_unavailable)

    LaunchedEffect(Unit) {
        if (shellOutputText.isNotEmpty()) {
            return@LaunchedEffect
        }
        val viewModel = ModuleViewModel()
        if (viewModel.moduleList.isEmpty()) {
            viewModel.loadModuleList()
        }
        val moduleInfo = viewModel.moduleList.find { info -> info.id == moduleId }
        if (moduleInfo == null) {
            Toast.makeText(context, noModule.format(moduleId), Toast.LENGTH_SHORT).show()
            navigator.popBackStack()
            return@LaunchedEffect
        }
        if (!moduleInfo.hasActionScript) {
            navigator.popBackStack()
            return@LaunchedEffect
        }
        if (!moduleInfo.enabled || moduleInfo.update || moduleInfo.remove) {
            Toast.makeText(context, moduleUnavailable.format(moduleInfo.name), Toast.LENGTH_SHORT).show()
            navigator.popBackStack()
            return@LaunchedEffect
        }
        withContext(Dispatchers.IO) {
            runModuleAction(
                moduleId = moduleId,
                onStdout = { output ->
                    val result = processShellOutput(output, shellOutputText, logContentSB)
                    shellOutputText = result.first
                },
                onStderr = { output ->
                    logContentSB.append(output).append("\n")
                }
            )
        }
        showFloatAction = true
    }

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
    val onBack = dropUnlessResumed { navigator.popBackStack() }
    ExtendedFloatingActionButton(
        modifier = Modifier
            .padding(bottom = 6.dp + 48.dp + 6.dp /* SnackBar height */)
            .animateFloatingActionButton(
                visible = showFloatAction,
                alignment = Alignment.CenterEnd,
            ),
        onClick = onBack,
        icon = { Icon(Icons.Filled.Close, stringResource(R.string.close)) },
        text = { Text(text = stringResource(R.string.close)) },
    )
}