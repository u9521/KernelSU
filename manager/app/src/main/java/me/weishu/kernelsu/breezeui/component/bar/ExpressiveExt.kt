package me.weishu.kernelsu.breezeui.component.bar

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import me.weishu.kernelsu.ui.theme.LocalEnableBlur

/** Breeze top bar colours: transparent while blur is on, tonal otherwise. */
@Composable
internal fun expressiveTopBarColors() = if (LocalEnableBlur.current) TopAppBarDefaults.topAppBarColors(
    containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent
) else TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.surfaceContainer, scrolledContainerColor = MaterialTheme.colorScheme.surfaceBright
)

/** Keeps a Breeze top bar pinned: Breeze scrolls its own content instead of the app bar. */
@Composable
internal fun TopAppBarScrollBehavior.disableDrag(): TopAppBarScrollBehavior {
    return remember(this) {
        object : TopAppBarScrollBehavior {
            override val state = this@disableDrag.state
            override val isPinned = true
            override val snapAnimationSpec = this@disableDrag.snapAnimationSpec
            override val flingAnimationSpec = this@disableDrag.flingAnimationSpec
            override val nestedScrollConnection = this@disableDrag.nestedScrollConnection
        }
    }
}
