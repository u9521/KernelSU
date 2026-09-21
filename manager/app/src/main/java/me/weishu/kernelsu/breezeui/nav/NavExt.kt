package me.weishu.kernelsu.breezeui.nav

import androidx.navigation3.runtime.NavKey

/**
 * The content key shared by every entry that renders the main pager.
 *
 * Entries sharing a content key share their saveable state and their view models, and navigation3
 * identifies a scene by its class and this key - so all four page routes are one scene, not four.
 * `BreezeRoot` renders that scene's content through a movable content, which is what keeps the host
 * (its pager and its four pages) alive across a page change, and `breezeui.animation.NavTransition`
 * makes the page change itself a no-op instead of sliding one page over the other.
 */
const val BreezeMainHostContentKey: String = "Breeze.MainHost"


/**
 * The pager page this route shows, or `null` for anything that is not a page route.
 *
 * A page route is the page on screen, which is why it owns the whole back stack (see
 * [BreezeNavigator.push]); anything else is a screen pushed on top of the page it was opened from.
 */
fun BreezeRoute.mainPageIndex(): Int? = BreezeRoute.TabRoute.entries.indexOf(this).takeIf { it >= 0 }

/** The route that shows [index] as the page. */
fun mainPageRoute(index: Int): BreezeRoute {
    return BreezeRoute.TabRoute.entries[index.coerceIn(0, BreezeRoute.TabRoute.entries.lastIndex)]
}

/**
 * The binding key that pairs a page's list pane with the details opened from it.
 *
 * Only the rail layout pairs panes: `BreezeListDetailScene` shows a detail beside the entry
 * underneath it when the two name the same binding key, and a page route is the list pane of every
 * detail opened from it. A route that is not a page has no list pane, so asking for its key is a
 * bug.
 */
fun BreezeRoute.TabRoute.mainPageBindingKey(): String = when (this) {
    BreezeRoute.TabRoute.Home -> "home"
    BreezeRoute.TabRoute.SuperUser -> "superuser"
    BreezeRoute.TabRoute.Module -> "module"
    BreezeRoute.TabRoute.Settings -> "settings"
}

/** True if this key is one of the pages, i.e. a route that owns the whole back stack. */
internal fun NavKey.isMainPage(): Boolean = this as? BreezeRoute.TabRoute != null

/** The page the top of the back stack is bound to, or `null` if it is not a page route. */
fun BreezeNavigator.currentPageIndex(): Int? = (current() as? BreezeRoute.TabRoute)?.mainPageIndex()

/**
 * True while the top of the back stack is the pager host, i.e. the bottom bar / rail is visible.
 *
 * A page route is the host itself; a detail pushed on top of it is not.
 */
fun BreezeNavigator.isTopRoute(): Boolean {
    return currentPageIndex() != null
}
