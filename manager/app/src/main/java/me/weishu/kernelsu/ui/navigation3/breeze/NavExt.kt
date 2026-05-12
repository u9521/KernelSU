package me.weishu.kernelsu.ui.navigation3.breeze

import me.weishu.kernelsu.ui.navigation3.Navigator
import me.weishu.kernelsu.ui.navigation3.Route

fun Navigator.isTopRoute(): Boolean {
    return when (this.current()) {
        is Route.Main -> true
        is Route.Home -> true
        is Route.SuperUser -> true
        is Route.Module -> true
        is Route.Settings -> true
        else -> false
    }
}