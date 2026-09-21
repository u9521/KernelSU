package me.weishu.kernelsu.breezeui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Breeze's back stack and result bus.
 *
 * A local copy of the upstream navigator (see `ui.navigation3.Navigator`) so that
 * Breeze navigation is unaffected by upstream rewrites, including the pending
 * miuix-navigation migration. The `push` behaviour — replacing the top entry when
 * it has the same type — is what Breeze's list/detail panes rely on.
 */
@Suppress("unused")
class BreezeNavigator(
    initialKey: NavKey
) {
    val backStack: SnapshotStateList<NavKey> = mutableStateListOf(initialKey)

    private val resultBus = mutableMapOf<String, MutableSharedFlow<Any>>()

    /**
     * Navigate to [key].
     *
     * A page route ([BreezeRoute.isMainPage]) is not stacked on top of anything: it *is* the page
     * on screen, so it owns the whole back stack - which is emptied and rebuilt around it, dropping
     * the details of the page that was left. Going back then returns to the first page rather than
     * to the page the tab was tapped from, and the pager follows the stack: the page slides in from
     * where it is now instead of appearing out of a transition.
     *
     * Every other key is pushed on top of the page it was opened from, replacing a top entry of the
     * same type - which is what Breeze's list/detail panes rely on.
     */
    fun push(key: NavKey) {
        if (key.isMainPage()) {
            if (backStack.size == 1 && current() == key) return
            backStack.clear()
            backStack.add(key)
            return
        }
        if (current() == key) return
        current()?.let {
            if (it.javaClass == key.javaClass) {
                backStack[backStack.lastIndex] = key
                return
            }
        }
        backStack.add(key)
    }

    fun replace(key: NavKey) {
        if (backStack.isNotEmpty()) {
            backStack[backStack.lastIndex] = key
        } else {
            backStack.add(key)
        }
    }

    fun replaceAll(keys: List<NavKey>) {
        if (keys.isEmpty()) {
            return
        }
        if (backStack.isNotEmpty()) {
            backStack.clear()
            backStack.addAll(keys)
        }
    }

    /**
     * Pop the top key if present. The bottom entry is the page on screen (a page route) and is never
     * popped: an empty back stack makes androidx.navigation3's `NavDisplay` fail its
     * `require(backStack.isNotEmpty())` check. Matches upstream's `ui.navigation3.Navigator` after
     * `manager: fix a crash issue`.
     */
    fun pop() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }

    /**
     * Pop until predicate matches the top key, keeping the root entry.
     */
    fun popUntil(predicate: (NavKey) -> Boolean) {
        while (backStack.size > 1 && !predicate(backStack.last())) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    fun navigateForResult(route: BreezeRoute, requestKey: String) {
        ensureChannel(requestKey)
        push(route)
    }

    fun <T : Any> setResult(requestKey: String, value: T) {
        ensureChannel(requestKey).tryEmit(value)
        pop()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> observeResult(requestKey: String): SharedFlow<T> {
        return ensureChannel(requestKey) as SharedFlow<T>
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun clearResult(requestKey: String) {
        ensureChannel(requestKey).resetReplayCache()
    }

    fun current(): NavKey? {
        return backStack.lastOrNull()
    }

    fun backStackSize(): Int {
        return backStack.size
    }

    private fun ensureChannel(key: String): MutableSharedFlow<Any> {
        return resultBus.getOrPut(key) { MutableSharedFlow(replay = 1, extraBufferCapacity = 0) }
    }

    companion object {
        val Saver: Saver<BreezeNavigator, Any> = listSaver(save = { navigator ->
            navigator.backStack.toList()
        }, restore = { savedList ->
            val initialKey = savedList.firstOrNull() ?: BreezeRoute.TabRoute.Home
            val navigator = BreezeNavigator(initialKey)
            navigator.backStack.clear()
            navigator.backStack.addAll(savedList)
            navigator
        })
    }
}

@Composable
fun rememberBreezeNavigator(startRoute: NavKey): BreezeNavigator {
    return rememberSaveable(startRoute, saver = BreezeNavigator.Saver) {
        BreezeNavigator(startRoute)
    }
}

val LocalBreezeNavigator = staticCompositionLocalOf<BreezeNavigator> {
    error("LocalBreezeNavigator not provided")
}
