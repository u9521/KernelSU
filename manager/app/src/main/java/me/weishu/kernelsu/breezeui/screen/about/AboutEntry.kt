package me.weishu.kernelsu.breezeui.screen.about

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.dropUnlessResumed
import me.weishu.kernelsu.BuildConfig
import me.weishu.kernelsu.R
import me.weishu.kernelsu.breezeui.nav.LocalBreezeNavigator
import me.weishu.kernelsu.ui.screen.about.AboutScreenActions
import me.weishu.kernelsu.ui.screen.about.AboutUiState
import me.weishu.kernelsu.ui.screen.about.extractLinks

/**
 * Breeze's about page: owns the state/action wiring so the upstream about screen does
 * not have to know about Breeze.
 */
@Composable
fun AboutEntry() {
    val navigator = LocalBreezeNavigator.current
    val uriHandler = LocalUriHandler.current
    val htmlString = stringResource(
        id = R.string.about_source_code,
        "<b><a href=\"https://github.com/tiann/KernelSU\">GitHub</a></b>",
        "<b><a href=\"https://t.me/KernelSU\">Telegram</a></b>"
    )
    val state = AboutUiState(
        title = stringResource(R.string.about),
        appName = stringResource(R.string.app_name),
        versionName = BuildConfig.VERSION_NAME,
        links = extractLinks(htmlString),
    )
    val actions = AboutScreenActions(
        onBack = dropUnlessResumed { navigator.pop() },
        onOpenLink = uriHandler::openUri,
    )

    AboutScreenBreeze(state, actions)
}
