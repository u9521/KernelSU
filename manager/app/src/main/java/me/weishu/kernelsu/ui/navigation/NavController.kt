package me.weishu.kernelsu.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
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

internal const val REVERSE_ANIM = "reverse_anim"
internal const val NAVBAR_SWITCH = "navbar_switch"

class NavController(val startKey: NavKey) {

    private var topLevelState = mutableStateOf(startKey)

    private val backStacks = TopLevelRoute.entries.map { it.navKey }.associateWith { route ->
        NavBackStack(route)
    }

    @PublishedApi
    internal val results = mutableStateMapOf<String, Any?>()

    var currentTopLevel: NavKey
        get() = topLevelState.value
        set(value) {
            topLevelState.value = value
        }


    companion object {
        fun Saver(startKey: NavKey): Saver<NavController, Any> =
            listSaver(save = { controller ->
                val currentLevel = controller.currentTopLevel
                val savedStacks = controller.backStacks.mapValues { (_, stack) ->
                    ArrayList(stack)
                }
                val savedResults = HashMap(controller.results)
                listOf(currentLevel, savedStacks, savedResults)
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

                // 3. Results
                @Suppress("UNCHECKED_CAST") val savedResults = savedList[2] as Map<String, Any?>
                controller.results.putAll(savedResults)
                controller
            })
    }

    fun getTopLevel(key: NavKey): TopLevelRoute? {
        return TopLevelRoute.entries.find { it.navKey == key }
    }

    fun isTopLevel(): Boolean {
        return backStacks[currentTopLevel]!!.size == 1
    }

    fun navigateTo(key: NavKey) {
        if (current() == key) return
        if (getTopLevel(key) != null) {
            setResult(NAVBAR_SWITCH, true, goback = false)
            // we don't need to reverse Home
            val revAnim = key != startKey && getTopLevel(key)!!.ordinal < getTopLevel(currentTopLevel)!!.ordinal
            setResult(REVERSE_ANIM, revAnim, goback = false)
            currentTopLevel = key
            return
        }
        popResult<Boolean>(NAVBAR_SWITCH)
        popResult<Boolean>(REVERSE_ANIM)
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
        }
    }

    fun current(): NavKey? {
        return backStacks[currentTopLevel]!!.lastOrNull()
    }


    // Ensure that the data stored is serializable, otherwise the app will crash.
    fun setResult(key: String, result: Any?, goback: Boolean = true) {
        results[key] = result
        if (goback) {
            popBackStack()
        }
    }

    inline fun <reified T> popResult(key: String): T? {
        if (results.containsKey(key)) {
            val value = results[key]
            results.remove(key)
            return value as? T
        }
        return null
    }

    inline fun <reified T> getResult(key: String): T? {
        if (results.containsKey(key)) {
            val value = results[key]
            return value as? T
        }
        return null
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
}

@Composable
fun rememberNavController(startKey: NavKey): NavController {
    return rememberSaveable(startKey, saver = NavController.Saver(startKey)) {
        NavController(startKey)
    }
}
