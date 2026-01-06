package me.weishu.kernelsu.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.NavDisplay
import me.weishu.kernelsu.ui.util.LocalNavController
import me.weishu.kernelsu.ui.util.isRailNavbar


@Composable
fun MainNavDisplay(modifier: Modifier = Modifier) {
    val isRail = isRailNavbar()
    val navigator = LocalNavController.current
    val navbarSwitch = navigator.getResult<Boolean>(NAVBAR_SWITCH) ?: false
    val reverseAnim = navigator.getResult<Boolean>(REVERSE_ANIM) ?: false
    NavDisplay(
        modifier = modifier,
        entries = navigator.genEntries(mainEntryProvider), onBack = { navigator.popBackStack() }, transitionSpec = {
            // Slide in from right when navigating forward
            if (navbarSwitch) {
                if (isRail) {
                    if (reverseAnim) slidePopDown() else slidePushUp()
                } else {
                    if (reverseAnim) slideInFromLeft() else slideInFromRight()
                }
            } else {
                slideInFromRight()
            }
        },
        popTransitionSpec = {
            // Slide in from left when navigating back
            if (navbarSwitch) {
                if (isRail) {
                    if (!reverseAnim) slidePopDown() else slidePushUp()
                } else {
                    if (!reverseAnim) slideInFromLeft() else slideInFromRight()
                }
            } else {
                slideInFromLeft()
            }
        },
        predictivePopTransitionSpec = {
            // Slide in from left when navigating back
            if (navbarSwitch) {
                if (isRail) {
                    if (!reverseAnim) slidePopDown() else slidePushUp()
                } else {
                    if (!reverseAnim) slideInFromLeft() else slideInFromRight()
                }
            } else {
                slideInFromLeft()
            }
        })
}
