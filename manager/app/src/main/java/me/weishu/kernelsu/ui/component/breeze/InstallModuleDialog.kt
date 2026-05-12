package me.weishu.kernelsu.ui.component.breeze

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.R
import me.weishu.kernelsu.data.model.Module
import me.weishu.kernelsu.ui.component.dialog.rememberConfirmDialog
import me.weishu.kernelsu.ui.component.dialog.rememberLoadingDialog
import me.weishu.kernelsu.ui.util.getFileName
import me.weishu.kernelsu.ui.util.module.ModuleParser

@Composable
fun InstallModuleDialog(
    uris: List<Uri>,
    installedModules: List<Module>,
    onConfirmInstall: (List<Uri>) -> Unit,
    onDismiss: () -> Unit
) {
    if (uris.isEmpty()) return

    val context = LocalContext.current
    val loadingDialog = rememberLoadingDialog()
    val confirmTitle = stringResource(R.string.module)
    val multiConfirmContent = stringResource(R.string.module_install_prompt_with_name)
    val confirmDialog = rememberConfirmDialog(
        onConfirm = {
            onConfirmInstall(uris)
            onDismiss()
        },
        onDismiss = onDismiss
    )

    LaunchedEffect(uris) {
        var isMarkdown = false
        val confirmContent = if (uris.size == 1) {
            isMarkdown = true
            loadingDialog.withLoading {
                withContext(Dispatchers.IO) {
                    ModuleParser.getModuleInstallDesc(context, uris.first(), installedModules)
                }
            }
        } else {
            val moduleNames = uris.mapIndexed { index, uri ->
                "\n${index + 1}. ${uri.getFileName(context)}"
            }.joinToString("")
            multiConfirmContent.format(moduleNames)
        }

        confirmDialog.showConfirm(title = confirmTitle, content = confirmContent, markdown = isMarkdown)
    }
}
