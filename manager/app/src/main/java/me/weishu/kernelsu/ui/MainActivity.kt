package me.weishu.kernelsu.ui

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.FlashScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.utils.isRouteOnBackStackAsState
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import kotlinx.coroutines.flow.MutableStateFlow
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.rememberConfirmDialog
import me.weishu.kernelsu.ui.screen.BottomBarDestination
import me.weishu.kernelsu.ui.screen.FlashIt
import me.weishu.kernelsu.ui.theme.KernelSUTheme
import me.weishu.kernelsu.ui.util.LocalSnackbarHost
import me.weishu.kernelsu.ui.util.getFileName
import me.weishu.kernelsu.ui.util.install
import me.weishu.kernelsu.ui.util.rootAvailable

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
                val navController = rememberNavController()
                val navigator = navController.rememberDestinationsNavigator()

                // Handle ZIP file installation from external apps
                ZipFileIntentHandler(
                    intentState = intentState,
                    intent = intent,
                    isManager = isManager,
                    navigator = navigator
                )

                val snackBarHostState = remember { SnackbarHostState() }
                val bottomBarRoutes = remember {
                    BottomBarDestination.entries.map { it.direction.route }.toSet()
                }
                Scaffold(
                    bottomBar = { BottomBar(navController) },
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    CompositionLocalProvider(
                        LocalSnackbarHost provides snackBarHostState,
                    ) {
                        DestinationsNavHost(
                            modifier = Modifier.padding(innerPadding),
                            navGraph = NavGraphs.root,
                            navController = navController,
                            defaultTransitions = object : NavHostAnimatedDestinationStyle() {
                                override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
                                    // If the target is a detail page (not a bottom navigation page), slide in from the right
                                    if (targetState.destination.route !in bottomBarRoutes) {
                                        slideInHorizontally(initialOffsetX = { it })
                                    } else {
                                        // Otherwise (switching between bottom navigation pages), use fade in
                                        fadeIn(animationSpec = tween(340))
                                    }
                                }

                                override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
                                    // If navigating from the home page (bottom navigation page) to a detail page, slide out to the left
                                    if (initialState.destination.route in bottomBarRoutes && targetState.destination.route !in bottomBarRoutes) {
                                        slideOutHorizontally(targetOffsetX = { -it / 4 }) + fadeOut()
                                    } else {
                                        // Otherwise (switching between bottom navigation pages), use fade out
                                        fadeOut(animationSpec = tween(340))
                                    }
                                }

                                override val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
                                    // If returning to the home page (bottom navigation page), slide in from the left
                                    if (targetState.destination.route in bottomBarRoutes) {
                                        slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn()
                                    } else {
                                        // Otherwise (e.g., returning between multiple detail pages), use default fade in
                                        fadeIn(animationSpec = tween(340))
                                    }
                                }

                                override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
                                    // If returning from a detail page (not a bottom navigation page), scale down and fade out
                                    if (initialState.destination.route !in bottomBarRoutes) {
                                        scaleOut(targetScale = 0.9f) + fadeOut()
                                    } else {
                                        // Otherwise, use default fade out
                                        fadeOut(animationSpec = tween(340))
                                    }
                                }
                            }
                        )
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

@Composable
private fun BottomBar(navController: NavHostController) {
    val navigator = navController.rememberDestinationsNavigator()
    val isManager = Natives.isManager
    val fullFeatured = isManager && !Natives.requireNewKernel() && rootAvailable()
    NavigationBar(
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout).only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
        )
    ) {
        BottomBarDestination.entries.forEach { destination ->
            if (!fullFeatured && destination.rootRequired) return@forEach
            val isCurrentDestOnBackStack by navController.isRouteOnBackStackAsState(destination.direction)
            NavigationBarItem(
                selected = isCurrentDestOnBackStack,
                onClick = {
                    if (isCurrentDestOnBackStack) {
                        navigator.popBackStack(destination.direction, false)
                    }
                    navigator.navigate(destination.direction) {
                        popUpTo(NavGraphs.root) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    if (isCurrentDestOnBackStack) {
                        Icon(destination.iconSelected, stringResource(destination.label))
                    } else {
                        Icon(destination.iconNotSelected, stringResource(destination.label))
                    }
                },
                label = { Text(stringResource(destination.label)) },
                alwaysShowLabel = false
            )
        }
    }
}

/**
 * Handles ZIP file installation from external apps (e.g., file managers).
 * - In normal mode: Shows a confirmation dialog before installation
 * - In safe mode: Shows a Toast notification and prevents installation
 */
@Composable
private fun ZipFileIntentHandler(
    intentState: MutableStateFlow<Int>,
    intent: android.content.Intent?,
    isManager: Boolean,
    navigator: DestinationsNavigator
) {
    val context = LocalActivity.current ?: return
    var zipUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val isSafeMode = Natives.isSafeMode
    val clearZipUri = { zipUri = null }

    val installDialog = rememberConfirmDialog(
        onConfirm = {
            zipUri?.let { uri ->
                navigator.navigate(FlashScreenDestination(FlashIt.FlashModules(listOf(uri))))
            }
            clearZipUri()
        },
        onDismiss = clearZipUri
    )

    fun getDisplayName(uri: android.net.Uri): String {
        return uri.getFileName(context) ?: uri.lastPathSegment ?: "Unknown"
    }

    val intentStateValue by intentState.collectAsState()
    LaunchedEffect(intentStateValue) {
        val uri = intent?.data ?: return@LaunchedEffect

        if (!isManager || uri.scheme != "content" || intent.type != "application/zip") {
            return@LaunchedEffect
        }

        if (isSafeMode) {
            Toast.makeText(context,
                context.getString(R.string.safe_mode_module_disabled), Toast.LENGTH_SHORT)
                .show()
        } else {
            zipUri = uri
            installDialog.showConfirm(
                title = context.getString(R.string.module),
                content = context.getString(
                    R.string.module_install_prompt_with_name,
                    "\n${getDisplayName(uri)}"
                )
            )
        }
    }
}

