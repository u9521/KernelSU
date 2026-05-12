package me.weishu.kernelsu.ui.component.breeze

import android.annotation.SuppressLint
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.data.model.Module
import me.weishu.kernelsu.data.repository.ModuleRepositoryImpl
import me.weishu.kernelsu.ui.navigation3.LocalNavigator
import me.weishu.kernelsu.ui.navigation3.Route
import me.weishu.kernelsu.ui.screen.flash.FlashIt

/**
 * Handles ZIP file installation from external apps (e.g., file managers).
 * - In normal mode: Shows a confirmation dialog before installation
 * - In safe mode: Shows a Toast notification and prevents installation
 */
@SuppressLint("StringFormatInvalid", "LocalContextGetResourceValueCall")
@Composable
fun BreezeZipFileIntentHandler(
    intentState: MutableStateFlow<Int>,
    isManager: Boolean,
) {
    val activity = LocalActivity.current ?: return
    val context = LocalContext.current
    var zipUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val isSafeMode = Natives.isSafeMode
    val clearZipUri = { zipUri = null }
    var installedModules by remember { mutableStateOf(listOf<Module>()) }
    val navigator = LocalNavigator.current
    val moduleRepo = ModuleRepositoryImpl()
    InstallModuleDialog(
        zipUri?.let { listOf(zipUri!!) } ?: emptyList(),
        installedModules,
        onConfirmInstall = {
            navigator.push(Route.Flash(FlashIt.FlashModules(it)))
            clearZipUri()
        }, onDismiss = clearZipUri
    )

    val intentStateValue by intentState.collectAsStateWithLifecycle()
    LaunchedEffect(intentStateValue) {
        val currentIntent = activity.intent
        val uri = currentIntent?.data ?: return@LaunchedEffect

        if (!isManager || uri.scheme != "content" || currentIntent.type != "application/zip") {
            return@LaunchedEffect
        }

        activity.intent.data = null
        activity.intent.type = null

        if (isSafeMode) {
            Toast.makeText(context, context.getString(R.string.safe_mode_module_disabled), Toast.LENGTH_SHORT).show()
        } else {
            installedModules = moduleRepo.getModules().getOrDefault(emptyList())
            zipUri = uri
        }
    }
}
