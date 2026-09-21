package me.weishu.kernelsu.breezeui

import androidx.compose.runtime.Composable
import me.weishu.kernelsu.ui.theme.MaterialKernelSUTheme
import me.weishu.kernelsu.ui.theme.ThemeController

/**
 * Theme wrapper for Breeze `@Preview`s. Breeze is a Material3 theme, so previews use
 * the Material theme directly instead of the upstream `KernelSUTheme` dispatcher.
 */
@Composable
fun BreezePreviewTheme(content: @Composable () -> Unit) {
    MaterialKernelSUTheme(appSettings = ThemeController.getAppSettings(), content = content)
}
