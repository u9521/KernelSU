package me.weishu.kernelsu.breezeui.nav

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.core.net.toUri
import kotlinx.coroutines.channels.ReceiveChannel
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.breezeui.component.dialog.InstallModuleDialog
import me.weishu.kernelsu.data.repository.ModuleRepositoryImpl
import me.weishu.kernelsu.data.repository.SettingsRepositoryImpl
import me.weishu.kernelsu.ui.screen.flash.FlashIt
import me.weishu.kernelsu.ui.util.DownloadService
import me.weishu.kernelsu.ui.util.getFileName
import me.weishu.kernelsu.ui.webui.WebUIActivity

private const val SCHEME_KSU = "ksu"
private const val HOST_ACTION = "action"
private const val HOST_WEBUI = "webui"
private const val PARAM_ID = "id"
private const val PARAM_TOKEN = "token"

/**
 * Breeze's intent routing.
 *
 * Mirrors the upstream dispatcher's logic (upstream keeps its resolver private, so it
 * cannot be called from here) but pushes into Breeze's own navigation graph. The
 * intent contract itself — `ksu://` deep links, zip handling, the download service
 * token — is upstream's and unchanged.
 */
private sealed interface BreezePendingAction {
    data class InstallModule(
        val uri: Uri,
        val displayName: String,
        val requiresConfirmation: Boolean,
    ) : BreezePendingAction {
        companion object {
            val Saver = listSaver<InstallModule?, Any>(
                save = { action ->
                    if (action == null) {
                        emptyList()
                    } else {
                        listOf(action.uri, action.displayName, action.requiresConfirmation)
                    }
                },
                restore = { list ->
                    if (list.isEmpty()) {
                        null
                    } else {
                        InstallModule(
                            uri = list[0] as Uri,
                            displayName = list[1] as String,
                            requiresConfirmation = list[2] as Boolean
                        )
                    }
                }
            )
        }
    }

    data class ExecuteAction(val moduleId: String) : BreezePendingAction

    data class OpenWebUI(val moduleId: String) : BreezePendingAction
}

private sealed interface KsuDeepLink {
    data class Action(val moduleId: String) : KsuDeepLink
    data class WebUi(val moduleId: String) : KsuDeepLink
}

private fun buildInternalWebUiUri(moduleId: String): Uri {
    return Uri.Builder()
        .scheme(SCHEME_KSU)
        .authority(HOST_WEBUI)
        .appendQueryParameter(PARAM_ID, moduleId)
        .build()
}

private fun getDisplayName(uri: Uri): String {
    return uri.getFileName(context = me.weishu.kernelsu.ksuApp) ?: uri.lastPathSegment ?: "Unknown"
}

private fun parseValidatedDeepLink(uri: Uri?): KsuDeepLink? {
    if (uri?.scheme != SCHEME_KSU) return null

    val moduleId = uri.getQueryParameter(PARAM_ID)?.takeIf { it.isNotBlank() } ?: return null
    val token = uri.getQueryParameter(PARAM_TOKEN)?.takeIf { it.isNotBlank() } ?: return null
    if (token != SettingsRepositoryImpl().intentToken) return null

    return when (uri.host) {
        HOST_ACTION -> KsuDeepLink.Action(moduleId)
        HOST_WEBUI -> KsuDeepLink.WebUi(moduleId)
        else -> null
    }
}

private fun resolveIntent(intent: Intent): BreezePendingAction? {
    // DownloadService notification: install module
    if (intent.action == DownloadService.ACTION_INSTALL_MODULE) {
        val token = intent.getStringExtra(DownloadService.EXTRA_TOKEN)?.takeIf { it.isNotBlank() } ?: return null
        if (token != SettingsRepositoryImpl().intentToken) return null
        val uriString = intent.getStringExtra(DownloadService.EXTRA_MODULE_URI) ?: return null
        val uri = uriString.toUri()
        return BreezePendingAction.InstallModule(
            uri = uri,
            displayName = getDisplayName(uri),
            requiresConfirmation = true,
        )
    }

    // File manager: open ZIP
    val viewUri = intent.data
    if (viewUri != null && viewUri.scheme == "content" && intent.type == "application/zip") {
        return BreezePendingAction.InstallModule(
            uri = viewUri,
            displayName = getDisplayName(viewUri),
            requiresConfirmation = true,
        )
    }

    return when (val deepLink = parseValidatedDeepLink(intent.data)) {
        is KsuDeepLink.Action -> BreezePendingAction.ExecuteAction(deepLink.moduleId)
        is KsuDeepLink.WebUi -> BreezePendingAction.OpenWebUI(deepLink.moduleId)
        null -> null
    }
}

@SuppressLint("StringFormatInvalid")
@Composable
fun BreezeIntentRouter(intentChannel: ReceiveChannel<Intent>) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val navigator = LocalBreezeNavigator.current
    val isSafeMode = Natives.isSafeMode
    val isManager = Natives.isManager
    var pendingZipInstall by rememberSaveable(stateSaver = BreezePendingAction.InstallModule.Saver) { mutableStateOf(null) }

    InstallModuleDialog(
        uris = pendingZipInstall?.let { listOf(it.uri) } ?: emptyList(),
        getInstalledModules = { ModuleRepositoryImpl().getModules().getOrDefault(defaultValue = emptyList()) },
        onConfirmInstall = {
            pendingZipInstall?.let { action ->
                navigator.push(BreezeRoute.Flash(FlashIt.FlashModules(listOf(action.uri))))
            }
            pendingZipInstall = null
        },
        onDismiss = { pendingZipInstall = null }
    )

    LaunchedEffect(intentChannel) {
        for (intent in intentChannel) {
            if (!isManager) continue
            when (val action = resolveIntent(intent)) {
                is BreezePendingAction.InstallModule -> {
                    if (isSafeMode) {
                        Toast.makeText(
                            context,
                            resources.getString(R.string.safe_mode_module_disabled),
                            Toast.LENGTH_SHORT
                        ).show()
                        continue
                    }
                    if (action.requiresConfirmation) {
                        pendingZipInstall = action
                    } else {
                        navigator.push(BreezeRoute.Flash(FlashIt.FlashModules(listOf(action.uri))))
                    }
                }

                is BreezePendingAction.ExecuteAction -> {
                    navigator.push(BreezeRoute.ExecuteModuleAction(action.moduleId, fromShortcut = true))
                }

                is BreezePendingAction.OpenWebUI -> {
                    val webIntent = Intent(context, WebUIActivity::class.java)
                        .setData(buildInternalWebUiUri(action.moduleId))
                    context.startActivity(webIntent)
                }

                null -> Unit
            }
        }
    }
}
