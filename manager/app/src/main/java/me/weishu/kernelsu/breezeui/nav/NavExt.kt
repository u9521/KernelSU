package me.weishu.kernelsu.breezeui.nav

/** True while the top of the back stack is the pager host, i.e. the bottom bar / rail is visible. */
fun BreezeNavigator.isTopRoute(): Boolean {
    return when (this.current()) {
        is BreezeRoute.Main -> true
        is BreezeRoute.Home -> true
        is BreezeRoute.SuperUser -> true
        is BreezeRoute.Module -> true
        is BreezeRoute.Settings -> true
        else -> false
    }
}
