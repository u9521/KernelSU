package me.weishu.kernelsu.breezeui.webui

import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import me.weishu.kernelsu.breezeui.util.BlurController
import me.weishu.kernelsu.breezeui.util.LocalBlurController
import me.weishu.kernelsu.breezeui.util.blurOverlay
import me.weishu.kernelsu.data.repository.SettingsRepositoryImpl
import me.weishu.kernelsu.ui.LocalUiMode
import me.weishu.kernelsu.ui.UiMode
import me.weishu.kernelsu.ui.theme.LocalEnableBlur
import me.weishu.kernelsu.ui.theme.MaterialKernelSUTheme
import me.weishu.kernelsu.ui.theme.ThemeController
import me.weishu.kernelsu.ui.webui.Insets
import me.weishu.kernelsu.ui.webui.WebUIEvent
import me.weishu.kernelsu.ui.webui.WebUIState
import me.weishu.kernelsu.ui.webui.prepareWebView
import me.weishu.kernelsu.ui.webui.rememberFileLauncher

/**
 * Breeze's WebUI activity content.
 *
 * `WebUIActivity` is a second, independent window with its own theme; upstream keeps
 * its WebView host private, so Breeze renders its own copy of that host here and
 * only reuses the theme-agnostic `WebUIState`/`prepareWebView` plumbing.
 */
@Composable
fun BreezeWebUIRoot(
    activity: ComponentActivity,
    onFinish: () -> Unit,
) {
    val blurController = remember { BlurController() }
    val enableBlur = remember { SettingsRepositoryImpl().enableBlur }

    CompositionLocalProvider(
        LocalUiMode provides UiMode.Material,
        LocalEnableBlur provides enableBlur,
        LocalBlurController provides blurController,
    ) {
        MaterialKernelSUTheme(appSettings = ThemeController.getAppSettings()) {
            BreezeWebUIContent(activity = activity, onFinish = onFinish)
        }
    }
}

@Composable
private fun BreezeWebUIContent(activity: ComponentActivity, onFinish: () -> Unit) {
    val moduleId = remember { activity.intent.data?.getQueryParameter("id") }
    val webUIState = remember { WebUIState() }

    LaunchedEffect(moduleId) {
        if (moduleId == null) {
            onFinish()
            return@LaunchedEffect
        }
        prepareWebView(activity, moduleId, webUIState)
    }

    DisposableEffect(Unit) {
        onDispose { webUIState.dispose(activity) }
    }

    when (val event = webUIState.uiEvent) {
        is WebUIEvent.Error -> {
            LaunchedEffect(event) {
                Toast.makeText(activity, event.message, Toast.LENGTH_SHORT).show()
                onFinish()
            }
        }

        is WebUIEvent.Close -> {
            LaunchedEffect(event) { onFinish() }
        }

        else -> {}
    }
    val isLoading = webUIState.uiEvent is WebUIEvent.Loading

    Crossfade(modifier = Modifier.blurOverlay(), targetState = isLoading, animationSpec = tween(300)) { loading ->
        if (loading) {
            BreezeWebUILoading()
        } else {
            BreezeWebUIHost(webUIState = webUIState)
        }
    }
}

@Composable
private fun BreezeWebUILoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.LoadingIndicator()
    }
}

@Composable
private fun BreezeWebUIHost(webUIState: WebUIState) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val drawingInsets = WindowInsets.safeDrawing
    val systemBarsInsets = WindowInsets.systemBars
    val imeInsets = WindowInsets.ime
    val innerPadding = if (webUIState.isInsetsEnabled) imeInsets.asPaddingValues() else drawingInsets.asPaddingValues()
    val fileLauncher = rememberFileLauncher(webUIState)

    LaunchedEffect(density, layoutDirection, systemBarsInsets, webUIState.isInsetsEnabled) {
        if (!webUIState.isInsetsEnabled) {
            return@LaunchedEffect
        }
        snapshotFlow {
            val top = (systemBarsInsets.getTop(density) / density.density).toInt()
            val bottom = (systemBarsInsets.getBottom(density) / density.density).toInt()
            val left = (systemBarsInsets.getLeft(density, layoutDirection) / density.density).toInt()
            val right = (systemBarsInsets.getRight(density, layoutDirection) / density.density).toInt()
            Insets(top, bottom, left, right)
        }.collect { newInsets ->
            if (webUIState.currentInsets != newInsets) {
                webUIState.currentInsets = newInsets
                webUIState.webView?.evaluateJavascript(newInsets.js, null)
            }
        }
    }

    BackHandler(enabled = webUIState.webCanGoBack) {
        webUIState.webView?.goBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        webUIState.webView?.let { webView ->
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { _ ->
                    webView.apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        if (!webUIState.isUrlLoaded) {
                            val homePage = "https://mui.kernelsu.org/index.html"
                            if (width > 0 && height > 0) {
                                loadUrl(homePage)
                                webUIState.isUrlLoaded = true
                            } else {
                                val listener = object : View.OnLayoutChangeListener {
                                    override fun onLayoutChange(
                                        v: View, left: Int, top: Int, right: Int, bottom: Int,
                                        oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
                                    ) {
                                        if (v.width > 0 && v.height > 0) {
                                            (v as WebView).loadUrl(homePage)
                                            webUIState.isUrlLoaded = true
                                            v.removeOnLayoutChangeListener(this)
                                        }
                                    }
                                }
                                addOnLayoutChangeListener(listener)
                            }
                        }
                    }
                },
                update = { view ->
                    view.requestLayout()
                }
            )
        }
    }

    HandleWebUIEventBreeze(webUIState, fileLauncher)

    BreezeHandleWebViewLifecycle(webUIState)
    BreezeHandleConfigurationChanges(webUIState)
}

@Composable
private fun BreezeHandleWebViewLifecycle(webUIState: WebUIState) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, webUIState) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> webUIState.webView?.onResume()
                Lifecycle.Event.ON_PAUSE -> webUIState.webView?.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@Composable
private fun BreezeHandleConfigurationChanges(webUIState: WebUIState) {
    val configuration = LocalConfiguration.current
    LaunchedEffect(configuration.fontScale, webUIState.webView) {
        webUIState.webView?.settings?.textZoom = (configuration.fontScale * 100).toInt()
    }
}
