package me.weishu.kernelsu.ui.component

import android.os.Environment
import androidx.compose.animation.core.animate
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.scrollbar.ScrollbarDefaults
import me.weishu.kernelsu.ui.component.scrollbar.VerticalScrollbar
import me.weishu.kernelsu.ui.component.scrollbar.rememberScrollbarAdapter
import me.weishu.kernelsu.ui.navigation3.LocalNavController
import me.weishu.kernelsu.ui.theme.defaultTopAppBarColors
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShellLogScaffold(
    title: @Composable () -> Unit,
    text: String,
    logContent: String,
    logFileNamePrefix: String,
    floatingActionButton: @Composable () -> Unit = {}
) {
    val navigator = LocalNavController.current
    val snackBarHost = LocalSnackbarHost.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
        topBar = {
            ShellTopAppBar(
                title = title,
                onBack = dropUnlessResumed {
                    navigator.popBackStack()
                },
                onSave = {
                    scope.launch {
                        saveLogToDownloads(
                            logContent = logContent,
                            fileNamePrefix = logFileNamePrefix,
                            snackBarHost = snackBarHost
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { BreezeSnackBarHost(hostState = snackBarHost) },
        floatingActionButton = floatingActionButton,
    ) { innerPadding ->
        KeyEventBlocker {
            it.key == Key.VolumeDown || it.key == Key.VolumeUp
        }
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .navigationBarsPadding(),
            ) {
                ShellLogText(
                    modifier = Modifier.fillMaxSize(),
                    text = text,
                    scrollState = scrollState,
                    scrollBehavior = scrollBehavior
                )
            }

            VerticalScrollbar(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight(), adapter = rememberScrollbarAdapter(scrollState),
                durationMillis = 1500L,
                style = ScrollbarDefaults.style.copy(
                    color = MaterialTheme.colorScheme.primary, railColor = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.5f)
                )
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShellLogText(
    modifier: Modifier = Modifier, text: String, scrollState: ScrollState, scrollBehavior: TopAppBarScrollBehavior
) {
    val animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    LaunchedEffect(text) {
        if (scrollState.maxValue > 0) {
            launch {
                val currentOffset = scrollBehavior.state.heightOffset
                val targetOffset = scrollBehavior.state.heightOffsetLimit
                if (currentOffset > targetOffset + 0.5f) {
                    animate(
                        initialValue = currentOffset,
                        targetValue = targetOffset,
                        animationSpec = animationSpec
                    ) { value, _ ->
                        scrollBehavior.state.heightOffset = value
                    }
                }
            }
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Text(
        modifier = modifier
            .padding(8.dp),
        text = text,
        fontSize = MaterialTheme.typography.bodySmall.fontSize,
        fontFamily = FontFamily.Monospace,
        lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShellTopAppBar(
    title: @Composable () -> Unit, onBack: () -> Unit, onSave: () -> Unit, scrollBehavior: TopAppBarScrollBehavior
) {
    LargeFlexibleTopAppBar(
        title = title, navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        }, actions = {
            IconButton(onClick = onSave) {
                Icon(
                    painter = painterResource(R.drawable.ic_save_rounded_filled), contentDescription = stringResource(id = R.string.save_log)
                )
            }
        },
        colors = defaultTopAppBarColors(),
        scrollBehavior = scrollBehavior
    )
}

fun processShellOutput(
    newOutput: String, currentText: String, logBuilder: StringBuilder
): Pair<String, StringBuilder> {
    var updatedText = currentText
    val tempText = "$newOutput\n"

    if (tempText.startsWith("[H[J")) { // clear command
        updatedText = tempText.substring(6)
    } else {
        updatedText += tempText
    }

    logBuilder.append(newOutput).append("\n")
    return updatedText to logBuilder
}

suspend fun saveLogToDownloads(
    logContent: String, fileNamePrefix: String, snackBarHost: SnackbarHostState
) {
    withContext(Dispatchers.IO) {
        val format = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault())
        val date = format.format(Date())
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "${fileNamePrefix}_${date}.log"
        )
        file.writeText(logContent)
        snackBarHost.showSnackbar("Log saved to ${file.absolutePath}")
    }
}