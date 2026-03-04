package me.weishu.kernelsu.ui.component.module

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.rememberConfirmDialog
import me.weishu.kernelsu.ui.component.rememberLoadingDialog
import me.weishu.kernelsu.ui.navigation3.LocalNavController
import me.weishu.kernelsu.ui.navigation3.Route
import me.weishu.kernelsu.ui.screen.flash.FlashIt
import me.weishu.kernelsu.ui.util.ModuleParser
import me.weishu.kernelsu.ui.util.getFileName
import me.weishu.kernelsu.ui.viewmodel.ModuleViewModel

@Composable
fun InstallModuleDialog(
    uris: List<Uri>,
    viewModel: ModuleViewModel,
    onDismiss: () -> Unit
) {
    // Do nothing if the URI list is empty.
    if (uris.isEmpty()) return
    val uiState by viewModel.uiState.collectAsState()

    val navigator = LocalNavController.current
    val context = LocalContext.current
    val loadingDialog = rememberLoadingDialog()
    val confirmTitle = stringResource(R.string.module)
    val multiConfirmContent = stringResource(R.string.module_install_prompt_with_name)

    // Setup the confirmation dialog with actions for confirmation and dismissal.
    val confirmDialog = rememberConfirmDialog(
        onConfirm = {
            navigator.navigateTo(Route.Flash(FlashIt.FlashModules(uris)))
            viewModel.markNeedRefresh()
            onDismiss()
        },
        onDismiss = onDismiss
    )

    LaunchedEffect(uris) {
        val confirmContent = if (uris.size == 1) {
            // Single file: parse its metadata for a detailed description.
            loadingDialog.withLoading {
                if (uiState.moduleList.isEmpty()) {
                    viewModel.loadModuleList()
                }
                withContext(Dispatchers.IO) {
                    ModuleParser.getModuleInstallDesc(context, uris.first(), uiState.moduleList)
                }
            }
        } else {
            // Multiple files: list their names.
            // Note: Original logic had markNeedRefresh here. Keeping for consistency.
            viewModel.markNeedRefresh()
            val moduleNames = uris.mapIndexed { index, uri ->
                "\n${index + 1}. ${uri.getFileName(context)}"
            }.joinToString("")
            multiConfirmContent.format(moduleNames)
        }

        confirmDialog.showConfirm(title = confirmTitle, content = confirmContent, markdown = true)
    }
}
