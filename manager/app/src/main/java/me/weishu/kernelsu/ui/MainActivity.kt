package me.weishu.kernelsu.ui

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.NavBarItems
import me.weishu.kernelsu.ui.component.rememberConfirmDialog
import me.weishu.kernelsu.ui.component.rememberLoadingDialog
import me.weishu.kernelsu.ui.navigation.FlashScreenNavKey
import me.weishu.kernelsu.ui.navigation.MainNavDisplay
import me.weishu.kernelsu.ui.navigation.TopLevelRoute
import me.weishu.kernelsu.ui.navigation.rememberNavController
import me.weishu.kernelsu.ui.screen.FlashIt
import me.weishu.kernelsu.ui.theme.KernelSUTheme
import me.weishu.kernelsu.ui.util.LocalNavController
import me.weishu.kernelsu.ui.util.LocalSnackbarHost
import me.weishu.kernelsu.ui.util.ModuleParser
import me.weishu.kernelsu.ui.util.getFileName
import me.weishu.kernelsu.ui.util.install
import me.weishu.kernelsu.ui.util.navigationSuiteType
import me.weishu.kernelsu.ui.viewmodel.ModuleViewModel

class MainActivity : ComponentActivity() {

    private val intentState = MutableStateFlow(0)

    override fun onCreate(savedInstanceState: Bundle?) {

        // Enable edge to edge
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        super.onCreate(savedInstanceState)

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
                    ZipFileIntentHandler(
                        intentState = intentState, intent = intent, isManager = isManager
                    )
                    NavigationSuiteScaffold(
                        navigationItems = { NavBarItems() }, state = state, navigationSuiteType = navigationSuiteType(currentWindowAdaptiveInfo())
                    ) {
                        MainNavDisplay()
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Increment intentState to trigger LaunchedEffect re-execution
        intentState.value += 1
    }
}

/**
 * Handles ZIP file installation from external apps (e.g., file managers).
 * - In normal mode: Shows a confirmation dialog before installation
 * - In safe mode: Shows a Toast notification and prevents installation
 */
@Composable
private fun ZipFileIntentHandler(
    intentState: MutableStateFlow<Int>, intent: android.content.Intent?, isManager: Boolean
) {
    val context = LocalActivity.current ?: return
    val navigator = LocalNavController.current
    var zipUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val isSafeMode = Natives.isSafeMode
    val clearZipUri = { zipUri = null }

    val installDialog = rememberConfirmDialog(
        onConfirm = {
            zipUri?.let { uri ->
                navigator.navigateTo(FlashScreenNavKey(FlashIt.FlashModules(listOf(uri))))
            }
            clearZipUri()
        }, onDismiss = clearZipUri
    )

    fun getDisplayName(uri: android.net.Uri): String {
        return uri.getFileName(context) ?: uri.lastPathSegment ?: "Unknown"
    }

    val intentStateValue by intentState.collectAsState()
    val loadingDialog = rememberLoadingDialog()
    val viewModel = viewModel<ModuleViewModel>()

    LaunchedEffect(intentStateValue) {
        val uri = intent?.data ?: return@LaunchedEffect

        if (!isManager || uri.scheme != "content" || intent.type != "application/zip") {
            return@LaunchedEffect
        }

        if (isSafeMode) {
            Toast.makeText(
                context, context.getString(R.string.safe_mode_module_disabled), Toast.LENGTH_SHORT
            ).show()
        } else {
            zipUri = uri
            viewModel.fetchModuleList()
            val moduleInstallDesc = loadingDialog.withLoading {
                withContext(Dispatchers.IO) {
                    zipUri?.let { uri ->
                        ModuleParser.getModuleInstallDesc(
                            context, uri, viewModel.moduleList
                        )
                    }
                }
            }
            installDialog.showConfirm(
                title = context.getString(R.string.module), content = moduleInstallDesc!!, markdown = true
            )
        }
    }
}

