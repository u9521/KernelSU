package me.weishu.kernelsu.breezeui.component.text

import androidx.compose.animation.core.animate
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import me.weishu.kernelsu.R.string.save_log
import me.weishu.kernelsu.breezeui.component.bar.BreezeTopAppBar
import me.weishu.kernelsu.breezeui.component.button.BreezeBackButton
import me.weishu.kernelsu.breezeui.component.feedback.BreezeSnackBarHost
import me.weishu.kernelsu.breezeui.component.list.ScrollbarDefaults
import me.weishu.kernelsu.breezeui.component.list.VerticalScrollbar
import me.weishu.kernelsu.breezeui.component.list.rememberScrollbarAdapter
import me.weishu.kernelsu.breezeui.icons.MaterialSymbols
import me.weishu.kernelsu.breezeui.util.onlyHorizontal
import me.weishu.kernelsu.ui.component.KeyEventBlocker
import me.weishu.kernelsu.ui.component.material.ExpressiveScaffold

@Composable
fun ShellLogScaffold(
    title: @Composable () -> Unit,
    subtitle: (@Composable () -> Unit)? = null,
    snackBarHost: SnackbarHostState,
    text: String,
    onBack: () -> Unit,
    onSave: () -> Unit,
    floatingActionButton: @Composable () -> Unit = {},
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollState = rememberScrollState()
    val showScrollbar by remember {
        derivedStateOf { scrollBehavior.state.collapsedFraction > 0.99f }
    }
    val hazeState = rememberHazeState()

    ExpressiveScaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BreezeTopAppBar(
                title = title,
                subtitle = subtitle,
                navigationIcon = {
                    BreezeBackButton(
                        onClick = onBack,
                        collapseFraction = scrollBehavior.state.collapsedFraction,
                    )
                },
                actions = {
                    IconButton(onClick = onSave) {
                        Icon(
                            imageVector = MaterialSymbols.Filled.Rounded.Save,
                            contentDescription = stringResource(id = save_log),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                hazeState = hazeState
            )
        },
        snackbarHost = { BreezeSnackBarHost(modifier = Modifier.safeDrawingPadding(), hostState = snackBarHost) },
        floatingActionButton = floatingActionButton,
    ) { innerPadding ->
        KeyEventBlocker {
            it.key == Key.VolumeDown || it.key == Key.VolumeUp
        }
        Box(
            modifier = Modifier
                .hazeSource(hazeState)
                .padding(innerPadding.onlyHorizontal()),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
            ) {
                Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))
                ShellLogText(
                    modifier = Modifier.fillMaxWidth(),
                    text = text,
                    scrollState = scrollState,
                    scrollBehavior = scrollBehavior,
                )
            }

            VerticalScrollbar(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(top = innerPadding.calculateTopPadding())
                    .navigationBarsPadding(),
                adapter = rememberScrollbarAdapter(scrollState),
                durationMillis = 1500L,
                visible = showScrollbar,
                style = ScrollbarDefaults.style.copy(
                    color = MaterialTheme.colorScheme.primary,
                    railColor = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.5f),
                ),
            )
        }
    }
}

@Composable
fun ShellLogText(
    modifier: Modifier = Modifier,
    text: String,
    scrollState: ScrollState,
    scrollBehavior: TopAppBarScrollBehavior,
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
                        animationSpec = animationSpec,
                    ) { value, _ ->
                        scrollBehavior.state.heightOffset = value
                    }
                }
            }
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Text(
        modifier = modifier.padding(8.dp),
        text = text,
        fontSize = MaterialTheme.typography.bodySmall.fontSize,
        fontFamily = FontFamily.Monospace,
        lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
    )
}

