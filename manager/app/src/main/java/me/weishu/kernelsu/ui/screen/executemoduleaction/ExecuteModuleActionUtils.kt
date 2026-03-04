package me.weishu.kernelsu.ui.screen.executemoduleaction

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.R
import me.weishu.kernelsu.data.repository.ModuleRepositoryImpl
import me.weishu.kernelsu.ui.util.runModuleAction

@Composable
fun ExecuteModuleActionEffect(
    moduleId: String,
    text: String,
    logContent: StringBuilder,
    onTextUpdate: (String) -> Unit,
    onExit: () -> Unit,
    onFinish: () -> Unit,
) {
    val context = LocalContext.current
    val noModule = stringResource(R.string.no_such_module)
    val moduleUnavailable = stringResource(R.string.module_unavailable)

    LaunchedEffect(Unit) {
        if (text.isNotEmpty()) {
            return@LaunchedEffect
        }
        val repo = ModuleRepositoryImpl()
        val modules = repo.getModules().getOrDefault(emptyList())
        val moduleInfo = modules.find { info -> info.id == moduleId }
        if (moduleInfo == null) {
            Toast.makeText(context, noModule.format(moduleId), Toast.LENGTH_SHORT).show()
            onExit()
            return@LaunchedEffect
        }
        if (!moduleInfo.hasActionScript) {
            onExit()
            return@LaunchedEffect
        }
        if (!moduleInfo.enabled || moduleInfo.update || moduleInfo.remove) {
            Toast.makeText(context, moduleUnavailable.format(moduleInfo.name), Toast.LENGTH_SHORT).show()
            onExit()
            return@LaunchedEffect
        }
        var actionResult: Boolean
        var currentText = text
        val mainHandler = Handler(Looper.getMainLooper())
        withContext(Dispatchers.IO) {
            runModuleAction(
                moduleId = moduleId,
                onStdout = {
                    val tempText = "$it\n"
                    if (tempText.startsWith("[H[J")) { // clear command
                        currentText = tempText.substring(6)
                    } else {
                        currentText += tempText
                    }
                    mainHandler.post {
                        onTextUpdate(currentText)
                    }
                    logContent.append(it).append("\n")
                },
                onStderr = {
                    logContent.append(it).append("\n")
                }
            ).let {
                actionResult = it
            }
        }
        if (actionResult) {
            onFinish()
        }
    }
}
