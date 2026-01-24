package me.weishu.kernelsu.ui.util

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class IntentEvent(
    val id: Long,
    val intent: Intent
)

interface IntentEventSource {
    val intentFlow: SharedFlow<IntentEvent>
    fun processOnCreate(savedInstanceState: Bundle?, intent: Intent?)
    fun processOnNewIntent(intent: Intent)
}

class IntentHelperImpl : IntentEventSource {
    private val _intentFlow = MutableSharedFlow<IntentEvent>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val intentFlow: SharedFlow<IntentEvent> = _intentFlow.asSharedFlow()

    override fun processOnCreate(savedInstanceState: Bundle?, intent: Intent?) {
        if (savedInstanceState == null && intent != null) {
            emit(intent)
        }
    }

    override fun processOnNewIntent(intent: Intent) {
        emit(intent)
    }

    private fun emit(intent: Intent) {
        val event = IntentEvent(System.nanoTime(), intent)
        _intentFlow.tryEmit(event)
    }
}

@Composable
fun HandleIntentEffect(onIntent: suspend (Intent) -> Unit) {
    val activity = LocalActivity.current
    val eventSource = activity as? IntentEventSource ?: return

    var lastHandledId by rememberSaveable { mutableLongStateOf(-1L) }
    val setLastHandledId: (Long) -> Unit = { lastHandledId = it }

    LaunchedEffect(eventSource) {
        eventSource.intentFlow.collect { event ->
            if (event.id != lastHandledId) {
                setLastHandledId(event.id)
                onIntent(event.intent)
            }
        }
    }
}