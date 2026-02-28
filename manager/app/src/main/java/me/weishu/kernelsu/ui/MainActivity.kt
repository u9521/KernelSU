package me.weishu.kernelsu.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.LocalSnackbarHost
import me.weishu.kernelsu.ui.component.NavBarItems
import me.weishu.kernelsu.ui.component.module.InstallModuleDialog
import me.weishu.kernelsu.ui.navigation3.LocalNavController
import me.weishu.kernelsu.ui.navigation3.MainNavDisplay
import me.weishu.kernelsu.ui.navigation3.Route
import me.weishu.kernelsu.ui.navigation3.TopLevelRoute
import me.weishu.kernelsu.ui.navigation3.rememberNavController
import me.weishu.kernelsu.ui.theme.KernelSUTheme
import me.weishu.kernelsu.ui.util.HandleIntentEffect
import me.weishu.kernelsu.ui.util.IntentEventSource
import me.weishu.kernelsu.ui.util.IntentHelperImpl
import me.weishu.kernelsu.ui.util.install
import me.weishu.kernelsu.ui.util.navigationSuiteType
import me.weishu.kernelsu.ui.viewmodel.ModuleViewModel
import me.weishu.kernelsu.ui.webui.WebUIActivity

class MainActivity : ComponentActivity(), IntentEventSource by IntentHelperImpl() {
    override fun onCreate(savedInstanceState: Bundle?) {

        // Enable edge to edge
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        super.onCreate(savedInstanceState)
        processOnCreate(savedInstanceState, intent)
        val isManager = Natives.isManager
        if (isManager && !Natives.requireNewKernel()) install()

        setContent {
            KernelSUTheme {
                val navigator = rememberNavController(TopLevelRoute.Home.navKey)
                val snackBarHostState = remember { SnackbarHostState() }
                val state = rememberNavigationSuiteScaffoldState()
                val showNavTab by remember {
                    derivedStateOf { navigator.isTopLevel() }
                }
                LaunchedEffect(showNavTab) {
                    if (showNavTab) {
                        state.show()
                    } else {
                        state.hide()
                    }
                }
                CompositionLocalProvider(
                    LocalSnackbarHost provides snackBarHostState, LocalNavController provides navigator
                ) {
                    // Handle ZIP file installation from external apps
                    if (isManager) ZipFileIntentHandler()
                    if (isManager) URLSchemeIntentHandler()
                    NavigationSuiteScaffold(
                        navigationItems = { NavBarItems() },
                        state = state,
                        navigationSuiteType = navigationSuiteType(currentWindowAdaptiveInfo()),
                        navigationSuiteColors = NavigationSuiteDefaults.colors(
                            shortNavigationBarContainerColor = MaterialTheme.colorScheme.surfaceBright,
                            wideNavigationRailColors = WideNavigationRailDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                            navigationBarContainerColor = MaterialTheme.colorScheme.surfaceBright,
                            navigationRailContainerColor = MaterialTheme.colorScheme.surfaceBright,
                            navigationDrawerContainerColor = MaterialTheme.colorScheme.surfaceBright,
                        ),
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        MainNavDisplay()
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        processOnNewIntent(intent)
    }
}

/**
 * Handles ZIP file installation from external apps (e.g., file managers).
 * - In normal mode: Shows a confirmation dialog before installation
 * - In safe mode: Shows a Toast notification and prevents installation
 */
@Composable
private fun ZipFileIntentHandler() {
    val context = LocalContext.current
    val urisToInstall = remember { mutableStateOf<List<Uri>>(emptyList()) }
    val isSafeMode = Natives.isSafeMode

    val inSafeMode = stringResource(R.string.safe_mode_module_disabled)
    val viewModel = viewModel<ModuleViewModel>()
    InstallModuleDialog(urisToInstall.value, viewModel) {
        urisToInstall.value = emptyList()
    }


    HandleIntentEffect { intent ->
        val uri = intent.data ?: return@HandleIntentEffect

        if (uri.scheme != "content" || intent.type != "application/zip") {
            return@HandleIntentEffect
        }

        if (isSafeMode) {
            Toast.makeText(context, inSafeMode, Toast.LENGTH_SHORT).show()
            return@HandleIntentEffect
        }
        urisToInstall.value = listOf(uri)
    }
}

@Composable
private fun URLSchemeIntentHandler() {
    val context = LocalContext.current
    val navigator = LocalNavController.current
    HandleIntentEffect { intent ->
        val uri = intent.data ?: return@HandleIntentEffect
        if (uri.scheme != "ksu") return@HandleIntentEffect
        when (uri.host) {
            "action" -> {
                val moduleId = uri.getQueryParameter("id") ?: return@HandleIntentEffect
                navigator.navigateTo(Route.ExecuteModuleAction(moduleId))
            }

            "webui" -> {
                val moduleId = uri.getQueryParameter("id") ?: return@HandleIntentEffect
                val webIntent = Intent(context, WebUIActivity::class.java).setData("kernelsu://webui/$moduleId".toUri())
                    .putExtra("id", moduleId)
                    .putExtra("from_webui_shortcut", true)
                    .addFlags(
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    )
                context.startActivity(webIntent)
            }

            else -> return@HandleIntentEffect
        }
    }
}
