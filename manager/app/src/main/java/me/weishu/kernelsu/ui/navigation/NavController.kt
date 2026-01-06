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

class NavController(val startRoute: TopLevelRoute) {

    private var topLevelState = mutableStateOf(startRoute)

    private val backStacks = TopLevelRoute.entries.associateWith { route ->
        val stack: NavBackStack<NavKey> = NavBackStack(route)
        stack
    }

    @PublishedApi
    internal val results = mutableStateMapOf<String, Any?>()

    companion object {
        fun Saver(startRoute: TopLevelRoute): Saver<NavController, Any> =
            listSaver(save = { controller ->
                val currentLevel = controller.currentTopLevel
                val savedStacks = controller.backStacks.mapValues { (_, stack) ->
                    ArrayList(stack)
                }
                val savedResults = HashMap(controller.results)
                listOf(currentLevel, savedStacks, savedResults)
            }, restore = { savedList ->
                val controller = NavController(startRoute)
                // Tab
                val currentLevel = savedList[0] as TopLevelRoute
                controller.currentTopLevel = currentLevel

                // Stack
                @Suppress("UNCHECKED_CAST") val savedStacks =
                    savedList[1] as Map<TopLevelRoute, List<NavKey>>

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


    var currentTopLevel: TopLevelRoute
        get() = topLevelState.value
        set(value) {
            topLevelState.value = value
        }


    fun isTopLevel(): Boolean {
        return backStacks[currentTopLevel]!!.size == 1
    }

    fun navigateTo(route: NavKey) {
        if (route is TopLevelRoute) {
            setResult(NAVBAR_SWITCH, true, goback = false)
            // we don't need to reverse Home
            val revAnim = route != startRoute && route.ordinal < currentTopLevel.ordinal
            setResult(REVERSE_ANIM, revAnim, goback = false)
            currentTopLevel = route
        } else {
            popResult<Boolean>(NAVBAR_SWITCH)
            popResult<Boolean>(REVERSE_ANIM)
            backStacks[currentTopLevel]?.add(route)
        }
    }

    fun popBackStack() {
        val currentStack = backStacks[currentTopLevel]!!
        if (currentStack.size > 1) {
            currentStack.removeLastOrNull()
        } else if (currentTopLevel != startRoute) {
            currentTopLevel = startRoute
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
        return if (currentTopLevel == startRoute) {
            stackEntries[startRoute] ?: emptyList()
        } else {
            // Only the homepage tab and the current page tab
            (stackEntries[startRoute] ?: emptyList()) + (stackEntries[currentTopLevel]
                ?: emptyList())
        }.toMutableStateList()
    }
}

@Composable
fun rememberNavController(startRoute: TopLevelRoute): NavController {
    return rememberSaveable(startRoute, saver = NavController.Saver(startRoute)) {
        NavController(startRoute)
    }
}
