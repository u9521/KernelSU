package me.weishu.kernelsu.breezeui.component.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.parcelize.Parcelize
import me.weishu.kernelsu.ui.component.dialog.ConfirmCallback
import me.weishu.kernelsu.ui.component.dialog.ConfirmDialogHandle
import me.weishu.kernelsu.ui.component.dialog.ConfirmDialogVisuals
import me.weishu.kernelsu.ui.component.dialog.ConfirmResult
import me.weishu.kernelsu.ui.component.dialog.DialogHandle
import me.weishu.kernelsu.ui.component.dialog.LoadingDialogHandle
import me.weishu.kernelsu.ui.component.dialog.NullableCallback
import me.weishu.kernelsu.ui.component.dialog.rememberConfirmCallback
import kotlin.coroutines.resume

/**
 * Breeze's own dialog handles.
 *
 * The upstream component exposes the handles but keeps their implementations private,
 * so Breeze would silently fall back to the Material dialogs. These are the same
 * public contracts (`LoadingDialogHandle` / `ConfirmDialogHandle`) driving the
 * Breeze renderers from `DialogBreeze.kt` instead.
 */

private abstract class BreezeDialogHandle(
    protected val visible: MutableState<Boolean>,
    private val scope: CoroutineScope,
) : DialogHandle {
    override val isShown: Boolean get() = visible.value

    override fun show() {
        scope.launch { visible.value = true }
    }

    override fun hide() {
        scope.launch { visible.value = false }
    }

    override fun toString(): String = dialogType
}

private class BreezeLoadingDialogHandle(
    visible: MutableState<Boolean>,
    private val scope: CoroutineScope,
) : LoadingDialogHandle, BreezeDialogHandle(visible, scope) {
    override suspend fun <R> withLoading(block: suspend () -> R): R {
        return scope.async {
            try {
                visible.value = true
                block()
            } finally {
                visible.value = false
            }
        }.await()
    }

    override fun showLoading() = show()

    override val dialogType: String get() = "LoadingDialog"
}

@Parcelize
private data class BreezeConfirmDialogVisuals(
    override val title: String,
    override val content: String?,
    override val isMarkdown: Boolean,
    override val isHtml: Boolean,
    override val confirm: String?,
    override val dismiss: String?,
) : ConfirmDialogVisuals {
    companion object {
        val Empty: ConfirmDialogVisuals = BreezeConfirmDialogVisuals("", "", isMarkdown = false, isHtml = false, confirm = null, dismiss = null)
    }
}

private class BreezeConfirmDialogHandle(
    visible: MutableState<Boolean>,
    private val scope: CoroutineScope,
    private val callback: ConfirmCallback,
    initialVisuals: ConfirmDialogVisuals,
    resultChannel: ReceiveChannel<ConfirmResult>,
) : ConfirmDialogHandle, BreezeDialogHandle(visible, scope) {

    private val visualsState = mutableStateOf(initialVisuals)
    override val visuals: ConfirmDialogVisuals get() = visualsState.value

    private var awaitContinuation: CancellableContinuation<ConfirmResult>? = null

    init {
        scope.launch {
            resultChannel.consumeAsFlow()
                .onEach { result ->
                    awaitContinuation?.let {
                        awaitContinuation = null
                        if (it.isActive) it.resume(result)
                    }
                    hide()
                    when (result) {
                        ConfirmResult.Confirmed -> callback.onConfirm?.invoke()
                        ConfirmResult.Canceled -> callback.onDismiss?.invoke()
                    }
                }
                .collect {}
        }
    }

    private fun updateVisuals(visuals: ConfirmDialogVisuals) {
        visualsState.value = visuals
    }

    override fun showConfirm(
        title: String,
        content: String?,
        markdown: Boolean,
        html: Boolean,
        confirm: String?,
        dismiss: String?,
    ) {
        scope.launch {
            updateVisuals(BreezeConfirmDialogVisuals(title, content, markdown, html, confirm, dismiss))
            show()
        }
    }

    override suspend fun awaitConfirm(
        title: String,
        content: String?,
        markdown: Boolean,
        html: Boolean,
        confirm: String?,
        dismiss: String?,
    ): ConfirmResult {
        scope.launch {
            updateVisuals(BreezeConfirmDialogVisuals(title, content, markdown, html, confirm, dismiss))
            show()
        }
        return suspendCancellableCoroutine { awaitContinuation = it }
    }

    override val dialogType: String get() = "ConfirmDialog"
}

@Composable
fun rememberBreezeLoadingDialog(): LoadingDialogHandle {
    val visible = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LoadingDialogBreeze(visible)
    return remember { BreezeLoadingDialogHandle(visible, scope) }
}

@Composable
fun rememberBreezeConfirmDialog(
    onConfirm: NullableCallback = null,
    onDismiss: NullableCallback = null,
): ConfirmDialogHandle {
    return rememberBreezeConfirmDialog(rememberConfirmCallback(onConfirm, onDismiss))
}

@Composable
fun rememberBreezeConfirmDialog(callback: ConfirmCallback): ConfirmDialogHandle {
    val visible = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val resultChannel = remember { Channel<ConfirmResult>() }
    val handle = remember {
        BreezeConfirmDialogHandle(visible, scope, callback, BreezeConfirmDialogVisuals.Empty, resultChannel)
    }

    ConfirmDialogBreeze(
        visuals = handle.visuals,
        confirm = { scope.launch { resultChannel.send(ConfirmResult.Confirmed) } },
        dismiss = { scope.launch { resultChannel.send(ConfirmResult.Canceled) } },
        showDialog = visible,
    )

    return handle
}
