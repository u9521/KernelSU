package me.weishu.kernelsu.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import me.weishu.kernelsu.KernelVersion
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.WarningLevel
import me.weishu.kernelsu.ui.component.dialog.rememberConfirmDialog
import me.weishu.kernelsu.ui.component.material.ExpressiveScaffold
import me.weishu.kernelsu.ui.component.breeze.SegmentedListGroup
import me.weishu.kernelsu.ui.component.breeze.SegmentedListScope
import me.weishu.kernelsu.ui.component.material.disableDrag
import me.weishu.kernelsu.ui.component.material.expressiveTopBarColors
import me.weishu.kernelsu.ui.component.rebootlistpopup.RebootListPopup
import me.weishu.kernelsu.ui.component.statustag.StatusTag
import me.weishu.kernelsu.ui.util.module.LatestVersionInfo
import me.weishu.kernelsu.ui.util.onlyHorizontal
import me.weishu.kernelsu.ui.util.topBarHazeEffect

@Composable
fun HomePagerBreeze(
    state: HomeUiState,
    actions: HomeActions,
    bottomInnerPadding: Dp,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior().disableDrag()
    val hazeState = remember { HazeState() }
    ExpressiveScaffold(topBar = { TopBar(scrollBehavior = scrollBehavior, hazeState) }) { innerPadding ->
        Column(
            modifier = Modifier
                .hazeSource(hazeState)
                .padding(innerPadding.onlyHorizontal())
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))
            if (state.showManagerPrBuildWarning) {
                WarningCard(stringResource(id = R.string.home_pr_build_warning), level = WarningLevel.Notice)
            } else if (state.showKernelPrBuildWarning) {
                WarningCard(stringResource(id = R.string.home_pr_kernel_warning), level = WarningLevel.Notice)
            }
            if (state.showVersionMismatchWarning) {
                WarningCard(
                    stringResource(
                        id = R.string.home_version_mismatch,
                        state.currentManagerVersionCode,
                        state.ksuVersion ?: 0
                    )
                )
            }
            if (state.showGkiWarning) {
                WarningCard(stringResource(id = R.string.home_gki_warning), level = WarningLevel.Notice)
            }
            if (state.showUAPIMisMatchWarning) {
                WarningCard(
                    stringResource(
                        id = R.string.uapi_mismatch,
                        state.managerUAPIVersion,
                        state.kernelUAPIVersion ?: 0,
                    )
                )
            }
            if (state.showRequireKernelWarning) {
                WarningCard(
                    stringResource(
                        id = R.string.require_kernel_version,
                        state.ksuVersion ?: 0,
                        Natives.MINIMAL_SUPPORTED_KERNEL
                    )
                )
            }
            if (state.showRootWarning) {
                WarningCard(stringResource(id = R.string.grant_root_failed))
            }
            if (state.checkUpdateEnabled) {
                UpdateCard(state = state, actions = actions)
            }
            StatusCard(
                state = state,
                actions = actions,
            )
            InfoCard(systemInfo = state.systemInfo)
            SupportLinks(onOpenUrl = actions.onOpenUrl)
            Spacer(
                Modifier
                    .navigationBarsPadding()
                    .height(bottomInnerPadding)
            )
        }
    }
}

@Composable
private fun UpdateCard(
    state: HomeUiState,
    actions: HomeActions,
) {
    val newVersion = state.latestVersionInfo
    val title = stringResource(id = R.string.module_changelog)
    val updateText = stringResource(id = R.string.module_update)

    AnimatedVisibility(
        visible = state.hasUpdate,
        enter = fadeIn() + expandVertically(),
        exit = shrinkVertically() + fadeOut()
    ) {
        val updateDialog = rememberConfirmDialog(onConfirm = { actions.onOpenUrl(newVersion.downloadUrl) })
        WarningCard(
            message = stringResource(id = R.string.new_version_available, newVersion.versionCode),
            level = WarningLevel.Notice
        ) {
            if (newVersion.changelog.isEmpty()) {
                actions.onOpenUrl(newVersion.downloadUrl)
            } else {
                updateDialog.showConfirm(
                    title = title,
                    content = newVersion.changelog,
                    markdown = true,
                    confirm = updateText
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior? = null,
    hazeState: HazeState
) {
    LargeFlexibleTopAppBar(
        modifier = Modifier.topBarHazeEffect(hazeState, scrollBehavior),
        title = { Text(stringResource(R.string.app_name)) },
        actions = { RebootListPopup() },
        colors = expressiveTopBarColors(),
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun StatusCard(
    state: HomeUiState,
    actions: HomeActions,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TonalCard(
            containerColor = if (state.ksuVersion != null) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        actions.onInstallClick()
                    }
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when {
                    state.ksuVersion != null -> {
                        val workingMode = when (state.lkmMode) {
                            null -> "Unknown"
                            true -> "LKM"
                            else -> "Built-In"
                        }

                        Icon(painterResource(R.drawable.ic_check_circle_rounded), stringResource(R.string.home_working))
                        Column(Modifier.padding(start = 20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(id = R.string.home_working),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(Modifier.width(4.dp))
                                if (workingMode.isNotEmpty()) {
                                    StatusTag(
                                        label = workingMode,
                                        contentColor = MaterialTheme.colorScheme.onPrimary,
                                        backgroundColor = MaterialTheme.colorScheme.primary
                                    )
                                }
                                if (state.isSafeMode) {
                                    StatusTag(
                                        label = stringResource(id = R.string.safe_mode),
                                        contentColor = MaterialTheme.colorScheme.onError,
                                        backgroundColor = MaterialTheme.colorScheme.error
                                    )
                                }
                                if (state.isLateLoadMode) {
                                    StatusTag(
                                        label = stringResource(id = R.string.jailbreak_mode),
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                        backgroundColor = MaterialTheme.colorScheme.errorContainer
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.home_working_version, "${state.ksuVersion}-${state.kernelUAPIVersion}"),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    state.kernelVersion.isGKI() -> {
                        Icon(painterResource(R.drawable.ic_warning_rounded), stringResource(R.string.home_not_installed))
                        Column(
                            modifier = Modifier
                                .padding(start = 20.dp)
                                .weight(1f)
                        ) {
                            Text(
                                text = stringResource(R.string.home_not_installed),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.home_click_to_install),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if (state.isSELinuxPermissive) {
                            Button(
                                onClick = actions.onJailbreakClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                )
                            ) {
                                Text(stringResource(R.string.home_jailbreak))
                            }
                        }
                    }

                    else -> {
                        Icon(painterResource(R.drawable.ic_block_rounded), stringResource(R.string.home_unsupported))
                        Column(Modifier.padding(start = 20.dp)) {
                            Text(
                                text = stringResource(R.string.home_unsupported),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.home_unsupported_reason),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WarningCard(
    message: String,
    level: WarningLevel = WarningLevel.Error,
    onClick: (() -> Unit)? = null
) {
    val containerColor = when (level) {
        WarningLevel.Error -> MaterialTheme.colorScheme.errorContainer
        WarningLevel.Notice -> MaterialTheme.colorScheme.tertiaryContainer
    }
    TonalCard(containerColor = containerColor) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(onClick?.let { Modifier.clickable { it() } } ?: Modifier)
                .padding(24.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.contentColorFor(containerColor)
            )
        }
    }
}

@Composable
fun TonalCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceBright,
    shape: Shape = MaterialTheme.shapes.large,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = shape
    ) {
        content()
    }
}

@Composable
private fun SupportLinks(
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val learnMoreUrl = stringResource(R.string.home_learn_kernelsu_url)
    val supportTitle = stringResource(R.string.home_support_title)
    val supportContent = stringResource(R.string.home_support_content)
    val learnTitle = stringResource(R.string.home_learn_kernelsu)
    val learnContent = stringResource(R.string.home_click_to_learn_kernelsu)

    SegmentedListGroup(modifier = modifier.fillMaxWidth()) {
        item(
            onClick = { onOpenUrl("https://patreon.com/weishu") },
            content = { Text(supportTitle) },
            supportingContent = { Text(supportContent) },
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.ic_volunteer_activism_rounded),
                    contentDescription = supportTitle,
                )
            },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
        )
        item(
            onClick = { onOpenUrl(learnMoreUrl) },
            content = { Text(learnTitle) },
            supportingContent = { Text(learnContent) },
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.ic_menu_book_rounded),
                    contentDescription = learnTitle,
                )
            },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
        )
    }
}

@Composable
private fun InfoCard(
    systemInfo: SystemInfo,
    modifier: Modifier = Modifier,
) {
    fun SegmentedListScope.infoCardItem(
        iconRes: Int,
        label: String,
        content: String,
    ) {
        item(
            content = { Text(text = label, style = MaterialTheme.typography.bodyLarge) },
            leadingContent = {
                Icon(
                    imageVector = ImageVector.vectorResource(iconRes),
                    contentDescription = label,
                )
            },
            supportingContent = {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
    }

    val managerVersionLabel = stringResource(R.string.home_manager_version)
    val kernelLabel = stringResource(R.string.home_kernel)
    val deviceModelLabel = stringResource(R.string.home_device_model)
    val fingerprintLabel = stringResource(R.string.home_fingerprint)
    val selinuxLabel = stringResource(R.string.home_selinux_status)
    val seccompLabel = stringResource(R.string.home_seccomp_status)

    val selinuxDisplay = when (systemInfo.selinuxStatus) {
        "Enforcing" -> stringResource(R.string.selinux_status_enforcing)
        "Permissive" -> stringResource(R.string.selinux_status_permissive)
        "Disabled" -> stringResource(R.string.selinux_status_disabled)
        else -> stringResource(R.string.selinux_status_unknown)
    }
    val seccompDisplay = when (systemInfo.seccompStatus) {
        -1 -> stringResource(R.string.seccomp_status_not_supported)
        0 -> stringResource(R.string.seccomp_status_disabled)
        1 -> stringResource(R.string.seccomp_status_strict)
        2 -> stringResource(R.string.seccomp_status_filter)
        else -> stringResource(R.string.seccomp_status_unknown)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        SegmentedListGroup(modifier = Modifier.fillMaxWidth()) {
            infoCardItem(
                iconRes = R.drawable.ic_tag_rounded,
                label = managerVersionLabel,
                content = systemInfo.managerVersion,
            )
            infoCardItem(
                iconRes = R.drawable.ic_developer_board_rounded,
                label = kernelLabel,
                content = systemInfo.kernelVersion,
            )
            infoCardItem(
                iconRes = R.drawable.ic_smartphone_rounded,
                label = deviceModelLabel,
                content = systemInfo.deviceModel,
            )
            infoCardItem(
                iconRes = R.drawable.ic_fingerprint_rounded,
                label = fingerprintLabel,
                content = systemInfo.fingerprint,
            )
        }
        SegmentedListGroup(modifier = Modifier.fillMaxWidth()) {
            infoCardItem(
                iconRes = R.drawable.ic_security_rounded,
                label = selinuxLabel,
                content = selinuxDisplay,
            )
            infoCardItem(
                iconRes = R.drawable.ic_filter_list_rounded,
                label = seccompLabel,
                content = seccompDisplay,
            )
        }
    }
}

@Preview(name = "Activated")
@Composable
private fun StatusCardActivatedPreview() {
    StatusCard(
        state = previewHomeScreenState(ksuVersion = 12345, lkmMode = true),
        actions = HomeActions({}, {}, {})
    )
}

@Preview(name = "Not Activated")
@Composable
private fun StatusCardNotActivatedPreview() {
    StatusCard(state = previewHomeScreenState(ksuVersion = null, lkmMode = null), actions = HomeActions({}, {}, {}))
}

@Preview(name = "Permissive")
@Composable
private fun StatusCardPermissivePreview() {
    StatusCard(
        state = previewHomeScreenState(ksuVersion = null, lkmMode = null, selinuxStatus = "Permissive"),
        actions = HomeActions({}, {}, {})
    )
}

@Preview(name = "Jailbreak")
@Composable
private fun StatusCardJailbreakPreview() {
    StatusCard(
        state = previewHomeScreenState(ksuVersion = 12345, lkmMode = true, isLateLoadMode = true),
        actions = HomeActions({}, {}, {})
    )
}

private val previewSystemInfo = SystemInfo(
    kernelVersion = "6.1.0-android14-0-g1234567",
    managerVersion = "1.0.0 (10000)",
    fingerprint = "google/raven/raven:14/AP1A.240305.019:user/release-keys",
    selinuxStatus = "Enforcing",
    seccompStatus = 2,
    deviceModel = "linQing 114514"
)

private val previewUriHandler = object : UriHandler {
    override fun openUri(uri: String) {}
}

@Composable
private fun HomeScreenPreviewContent(
    ksuVersion: Int?,
    lkmMode: Boolean?,
    isSafeMode: Boolean = false,
    isLateLoadMode: Boolean = false,
    selinuxStatus: String = "Enforcing",
) {
    CompositionLocalProvider(LocalUriHandler provides previewUriHandler) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val actions = HomeActions({}, {}, {})
            StatusCard(
                state = previewHomeScreenState(
                    ksuVersion = ksuVersion,
                    lkmMode = lkmMode,
                    isSafeMode = isSafeMode,
                    isLateLoadMode = isLateLoadMode,
                    selinuxStatus = selinuxStatus,
                ),
                actions = actions
            )
            InfoCard(previewSystemInfo.copy(selinuxStatus = selinuxStatus))
            SupportLinks(onOpenUrl = {})
        }
    }
}

@Preview(name = "Home Activated", showBackground = true)
@Composable
private fun HomeScreenActivatedPreview() {
    HomeScreenPreviewContent(ksuVersion = 12345, lkmMode = true)
}

@Preview(name = "Home Not Activated", showBackground = true)
@Composable
private fun HomeScreenNotActivatedPreview() {
    HomeScreenPreviewContent(ksuVersion = null, lkmMode = null)
}

@Preview(name = "Home Permissive", showBackground = true)
@Composable
private fun HomeScreenPermissivePreview() {
    HomeScreenPreviewContent(ksuVersion = null, lkmMode = null, selinuxStatus = "Permissive")
}

@Preview(name = "Home Jailbreak", showBackground = true)
@Composable
private fun HomeScreenJailbreakPreview() {
    HomeScreenPreviewContent(ksuVersion = 12345, lkmMode = true, isLateLoadMode = true)
}

private fun previewHomeScreenState(
    ksuVersion: Int?,
    lkmMode: Boolean?,
    isSafeMode: Boolean = false,
    isLateLoadMode: Boolean = false,
    selinuxStatus: String = "Enforcing",
) = HomeUiState(
    kernelVersion = KernelVersion(6, 1, 0),
    ksuVersion = ksuVersion,
    lkmMode = lkmMode,
    isManager = true,
    isManagerPrBuild = false,
    isKernelPrBuild = false,
    requiresNewKernel = false,
    isRootAvailable = ksuVersion != null,
    isSafeMode = isSafeMode,
    isLateLoadMode = isLateLoadMode,
    checkUpdateEnabled = false,
    latestVersionInfo = LatestVersionInfo(),
    currentManagerVersionCode = 10000,
    systemInfo = previewSystemInfo.copy(selinuxStatus = selinuxStatus),
    managerUAPIVersion = 114,
    kernelUAPIVersion = 514,
    uapiMismatch = true,
)
