package me.weishu.kernelsu.ui.navigation3

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow


class NavController(val startKey: NavKey) {

    private var topLevelState = mutableStateOf(startKey)

    private val backStacks = TopLevelRoute.entries.map { it.navKey }.associateWith { route ->
        NavBackStack(route)
    }

    private val resultBus = mutableMapOf<String, MutableSharedFlow<Any>>()

    var currentTopLevel: NavKey
        get() = topLevelState.value
        set(value) {
            topLevelState.value = value
        }

    fun getTopLevel(key: NavKey?): TopLevelRoute? {
        return if (key == null) null else TopLevelRoute.entries.find { it.navKey == key }
    }

    fun isTopLevel(): Boolean {
        return backStacks[currentTopLevel]!!.size == 1
    }

    fun navigateTo(key: NavKey) {
        if (current() == key) return
        if (getTopLevel(key) != null) {
            currentTopLevel = key
            return
        }
        // eg. foldable devices appProfile to appProfile
        current()?.let {
            if (it.javaClass == key.javaClass) {
                backStacks[currentTopLevel]?.set(backStacks[currentTopLevel]!!.lastIndex, key)
                return
            }
        }
        backStacks[currentTopLevel]?.add(key)
    }

    fun popBackStack() {
        val currentStack = backStacks[currentTopLevel]!!
        if (currentStack.size > 1) {
            currentStack.removeLastOrNull()
        } else if (currentTopLevel != startKey) {
            currentTopLevel = startKey
        } else {
            currentStack.removeLastOrNull()
        }
    }

    fun current(): NavKey? {
        return backStacks[currentTopLevel]!!.lastOrNull()
    }

    /**
     * Set a result for the given request
     */
    fun <T : Any> setResult(requestKey: String, value: T) {
        ensureChannel(requestKey).tryEmit(value)
    }

    /**
     * Observe results for a given request key as a SharedFlow.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> observeResult(requestKey: String): SharedFlow<T> {
        return ensureChannel(requestKey) as SharedFlow<T>
    }

    /**
     * Clear the last emitted result for the request key.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun clearResult(requestKey: String) {
        ensureChannel(requestKey).resetReplayCache()
    }

    private fun ensureChannel(key: String): MutableSharedFlow<Any> {
        return resultBus.getOrPut(key) { MutableSharedFlow(replay = 1, extraBufferCapacity = 0) }
    }

    @Composable
    fun genEntries(provider: (NavKey) -> NavEntry<NavKey>): SnapshotStateList<NavEntry<NavKey>> {
        val stackEntries = backStacks.mapValues { (_, stack) ->
            rememberDecoratedNavEntries(
                backStack = stack, entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                ), entryProvider = provider
            )
        }
        return if (currentTopLevel == startKey) {
            stackEntries[startKey] ?: emptyList()
        } else {
            // Only the homepage tab and the current page tab
            (stackEntries[startKey] ?: emptyList()) + (stackEntries[currentTopLevel]
                ?: emptyList())
        }.toMutableStateList()
    }

    companion object {
        fun Saver(startKey: NavKey): Saver<NavController, Any> =
            listSaver(save = { controller ->
                val currentLevel = controller.currentTopLevel
                val savedStacks = controller.backStacks.mapValues { (_, stack) ->
                    ArrayList(stack)
                }
                listOf(currentLevel, savedStacks)
            }, restore = { savedList ->
                val controller = NavController(startKey)
                // Tab
                val currentLevel = savedList[0] as NavKey
                controller.currentTopLevel = currentLevel

                // Stack
                @Suppress("UNCHECKED_CAST") val savedStacks =
                    savedList[1] as Map<NavKey, List<NavKey>>

                savedStacks.forEach { (route, keys) ->
                    val targetStack = controller.backStacks[route]
                    if (targetStack is NavBackStack<NavKey>) {
                        targetStack.clear()
                        targetStack.addAll(keys)
                    }
                }
                controller
            })
    }
}

@Composable
fun rememberNavController(startKey: NavKey): NavController {
    return rememberSaveable(startKey, saver = NavController.Saver(startKey)) {
        NavController(startKey)
    }
}
