package me.weishu.kernelsu.ui.screen.appprofile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.data.model.AppInfo
import me.weishu.kernelsu.ui.animation.breeze.slideHorizontal
import me.weishu.kernelsu.ui.component.AppIconImage
import me.weishu.kernelsu.ui.component.breeze.BreezeSnackBarHost
import me.weishu.kernelsu.ui.component.breeze.SegmentedListGroup
import me.weishu.kernelsu.ui.component.breeze.SplitScreenRatioButton
import me.weishu.kernelsu.ui.component.profile.AppProfileConfigBreeze
import me.weishu.kernelsu.ui.component.profile.RootProfileConfigBreeze
import me.weishu.kernelsu.ui.component.profile.TemplateConfigBreeze
import me.weishu.kernelsu.ui.component.statustag.StatusTag
import me.weishu.kernelsu.ui.navigation3.breeze.LocalIsDetailPane
import me.weishu.kernelsu.ui.theme.expressiveTopBarColors
import me.weishu.kernelsu.ui.util.onlyHorizontal
import me.weishu.kernelsu.ui.util.ownerNameForUid
import me.weishu.kernelsu.ui.util.topBarHazeEffect

/**
 * @author weishu
 * @date 2023/5/16.
 */
@Composable
fun AppProfileScreenBreeze(
    state: AppProfileUiState,
    actions: AppProfileActions,
    snackBarHost: SnackbarHostState
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val profile = state.profile
    val isRootGranted = profile.allowSu
    val hazeState = rememberHazeState()

    val initialRootMode = when {
        profile.rootUseDefault -> Mode.Default
        profile.rootTemplate != null -> Mode.Template
        else -> Mode.Custom
    }
    var rootMode by rememberSaveable(profile) {
        mutableStateOf(initialRootMode)
    }
    var nonRootMode by rememberSaveable(profile) {
        mutableStateOf(if (profile.nonRootUseDefault) Mode.Default else Mode.Custom)
    }
    val currentMode = if (isRootGranted) rootMode else nonRootMode

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopBar(
                modifier = Modifier.topBarHazeEffect(hazeState, scrollBehavior),
                onBack = actions.onBack,
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = {
            BreezeSnackBarHost(
                hostState = snackBarHost, modifier = Modifier.padding(
                    bottom = ScaffoldDefaults.contentWindowInsets
                        .asPaddingValues().calculateBottomPadding()
                )
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { paddingValues ->
        val innerTopPadding = paddingValues.calculateTopPadding()
        Column(
            modifier = Modifier
                .hazeSource(hazeState)
                .padding(paddingValues.onlyHorizontal())
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
        ) {
            Spacer(modifier = Modifier.height(innerTopPadding))
            AppInfoGroup(
                appIcon = {
                    AppIconImage(
                        modifier = Modifier
                            .padding(4.dp)
                            .width(48.dp)
                            .height(48.dp),
                        packageInfo = state.appGroup.primary.packageInfo,
                        label = state.appGroup.primary.label,
                    )
                },
                appLabel = if (state.isUidGroup) ownerNameForUid(state.uid) else state.appGroup.primary.label,
                appUid = state.uid,
                appVersionName = state.appGroup.primary.packageInfo.versionName ?: "",
                appVersionCode = state.appGroup.primary.packageInfo.longVersionCode,
                packageName = if (state.isUidGroup) state.sharedUserId else state.packageName,
                affectedAppCount = state.appGroup.apps.size,
                isRootGranted = isRootGranted,
                mode = currentMode.text,
                menuContent = if (state.isUidGroup) {
                    null
                } else {
                    appMenuContent(
                        packageName = state.packageName,
                        userId = state.uid / 100000,
                        actions = actions,
                    )
                },
                onAllowSuChange = {
                    actions.onProfileChange(profile.copy(allowSu = it))
                }
            )

            ModeChipBar(mode = currentMode, showTemplate = isRootGranted) { newMode ->
                if (isRootGranted) {
                    val shouldClearTemplate = newMode == Mode.Default || newMode == Mode.Custom
                    actions.onProfileChange(
                        profile.copy(
                            rootUseDefault = newMode == Mode.Default,
                            rootTemplate = if (shouldClearTemplate) null else profile.rootTemplate,
                        )
                    )
                    rootMode = newMode
                } else {
                    actions.onProfileChange(
                        profile.copy(nonRootUseDefault = newMode == Mode.Default)
                    )
                    nonRootMode = newMode
                }
            }

            Crossfade(targetState = isRootGranted, label = "AppProfileMode") { hasRoot ->
                if (hasRoot) {
                    RootProfile(
                        profile = profile,
                        mode = rootMode,
                        onProfileChange = actions.onProfileChange,
                    )
                } else {
                    AppProfileConfigBreeze(
                        modifier = Modifier.padding(all = 16.dp),
                        enabled = nonRootMode == Mode.Custom,
                        profile = profile,
                        onProfileChange = actions.onProfileChange,
                    )
                }
            }

            AffectedAppColumn(
                affectedApps = state.appGroup.apps,
                actions = actions,
            )
            Spacer(modifier = Modifier.height(6.dp + 48.dp + 6.dp))
        }
    }
}

@Composable
private fun AppInfoGroup(
    appIcon: @Composable () -> Unit,
    appLabel: String,
    appUid: Int,
    appVersionName: String,
    appVersionCode: Long,
    packageName: String,
    affectedAppCount: Int,
    isRootGranted: Boolean,
    mode: String,
    menuContent: (@Composable ColumnScope.(dismissMenu: () -> Unit) -> Unit)?,
    onAllowSuChange: (Boolean) -> Unit,
) {
    val isUidGroup = affectedAppCount > 1
    val userId = appUid / 100000
    val superuserLabel = stringResource(R.string.superuser)

    SegmentedListGroup(modifier = Modifier.padding(16.dp)) {
        menuItem(
            content = { Text(appLabel) },
            leadingContent = appIcon,
            supportingContent = {
                Column {
                    if (packageName.isNotEmpty()) {
                        Text(packageName)
                    }
                    if (!isUidGroup) {
                        Text("$appVersionName ($appVersionCode)")
                    } else {
                        Text(stringResource(R.string.group_contains_apps, affectedAppCount))
                    }
                }
            },
            trailingContent = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    StatusTag(
                        label = "UID $appUid",
                        backgroundColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary,
                    )
                    if (userId != 0) {
                        StatusTag(
                            label = "USER $userId",
                            backgroundColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary,
                        )
                    }
                }
            },
            menuContent = menuContent,
        )
        switchItem(
            title = superuserLabel,
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.ic_security_rounded),
                    contentDescription = superuserLabel,
                )
            },
            checked = { isRootGranted },
            onCheckedChange = onAllowSuChange,
        )
        item(
            content = { Text(stringResource(R.string.profile)) },
            supportingContent = { Text(mode) },
            leadingContent = {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = stringResource(R.string.profile),
                )
            },
        )
    }
}

@Composable
private fun TopBar(
    modifier: Modifier,
    scrollBehavior: TopAppBarScrollBehavior,
    onBack: () -> Unit,
) {
    LargeFlexibleTopAppBar(
        modifier = modifier,
        title = { Text(stringResource(R.string.profile)) },
        navigationIcon = {
            if (LocalIsDetailPane.current) return@LargeFlexibleTopAppBar
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        },
        actions = { SplitScreenRatioButton() },
        colors = expressiveTopBarColors(),
        windowInsets = TopAppBarDefaults.windowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun ModeChipBar(
    mode: Mode,
    showTemplate: Boolean,
    onModeChange: (Mode) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        item(key = Mode.Default) {
            FilterChip(
                modifier = Modifier.animateItem(),
                selected = mode == Mode.Default,
                label = { Text(stringResource(R.string.profile_default)) },
                onClick = { onModeChange(Mode.Default) },
            )
        }
        if (showTemplate) {
            item(key = Mode.Template) {
                FilterChip(
                    modifier = Modifier.animateItem(),
                    selected = mode == Mode.Template,
                    label = { Text(stringResource(R.string.profile_template)) },
                    onClick = { onModeChange(Mode.Template) },
                )
            }
        }
        item(key = Mode.Custom) {
            FilterChip(
                modifier = Modifier.animateItem(),
                selected = mode == Mode.Custom,
                label = { Text(stringResource(R.string.profile_custom)) },
                onClick = { onModeChange(Mode.Custom) },
            )
        }
    }
}

@Composable
private fun RootProfile(
    profile: Natives.Profile,
    mode: Mode,
    onProfileChange: (Natives.Profile) -> Unit,
) {
    val motionScheme = MaterialTheme.motionScheme
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    AnimatedContent(
        targetState = mode,
        transitionSpec = {
            slideHorizontal(
                isLtr = isRtl == (targetState.ordinal > initialState.ordinal),
                animationSpec = motionScheme.defaultSpatialSpec(),
            )
        },
        label = "RootProfileMode",
    ) { targetMode ->
        when (targetMode) {
            Mode.Template -> {
                TemplateConfigBreeze(profile, onProfileChange)
            }

            Mode.Custom -> {
                RootProfileConfigBreeze(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    readOnly = false,
                    profile = profile,
                    onProfileChange = onProfileChange,
                )
            }

            Mode.Default -> {
                Box(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}


@Composable
private fun AffectedAppColumn(
    affectedApps: List<AppInfo>,
    actions: AppProfileActions,
) {
    if (affectedApps.size < 2) {
        return
    }

    SegmentedListGroup(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        title = stringResource(R.string.app_profile_affects_following_apps),
    ) {
        affectedApps.forEach { app ->
            val versionCode = app.packageInfo.longVersionCode

            menuItem(
                content = { Text(app.label) },
                supportingContent = {
                    Column {
                        Text(app.packageName)
                        Text("${app.packageInfo.versionName} ($versionCode)")
                    }
                },
                leadingContent = {
                    AppIconImage(
                        modifier = Modifier
                            .padding(4.dp)
                            .width(48.dp)
                            .height(48.dp),
                        packageInfo = app.packageInfo,
                        label = app.label,
                    )
                },
                menuContent = appMenuContent(
                    packageName = app.packageName,
                    userId = app.uid / 100000,
                    actions = actions,
                ),
            )
        }
    }
}

private fun appMenuContent(
    packageName: String,
    userId: Int,
    actions: AppProfileActions,
): @Composable ColumnScope.(dismissMenu: () -> Unit) -> Unit = { dismissMenu ->
    DropdownMenuItem(
        text = { Text(stringResource(R.string.launch_app)) },
        onClick = {
            dismissMenu()
            actions.onLaunchApp(packageName, userId)
        },
    )
    DropdownMenuItem(
        text = { Text(stringResource(R.string.force_stop_app)) },
        onClick = {
            dismissMenu()
            actions.onForceStopApp(packageName, userId)
        },
    )
    DropdownMenuItem(
        text = { Text(stringResource(R.string.restart_app)) },
        onClick = {
            dismissMenu()
            actions.onRestartApp(packageName, userId)
        },
    )
}
