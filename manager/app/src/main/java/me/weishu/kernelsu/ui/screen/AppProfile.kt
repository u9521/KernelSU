package me.weishu.kernelsu.ui.screen

import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.data.model.TemplateInfo
import me.weishu.kernelsu.ui.component.AppIconImage
import me.weishu.kernelsu.ui.component.BreezeSnackBarHost
import me.weishu.kernelsu.ui.component.LocalSnackbarHost
import me.weishu.kernelsu.ui.component.OutlinedTextEdit
import me.weishu.kernelsu.ui.component.SegmentedListGroup
import me.weishu.kernelsu.ui.component.SplitScreenRatioButton
import me.weishu.kernelsu.ui.component.StatusTag
import me.weishu.kernelsu.ui.component.profile.NonRootProfileConfig
import me.weishu.kernelsu.ui.component.profile.RootProfileConfig
import me.weishu.kernelsu.ui.component.profile.RootTemplateSelector
import me.weishu.kernelsu.ui.component.slideHorizontal
import me.weishu.kernelsu.ui.component.switchHapticFeedBack
import me.weishu.kernelsu.ui.navigation3.LocalIsDetailPane
import me.weishu.kernelsu.ui.navigation3.LocalNavController
import me.weishu.kernelsu.ui.theme.defaultTopAppBarColors
import me.weishu.kernelsu.ui.util.forceStopApp
import me.weishu.kernelsu.ui.util.getSepolicy
import me.weishu.kernelsu.ui.util.launchApp
import me.weishu.kernelsu.ui.util.ownerNameForUid
import me.weishu.kernelsu.ui.util.restartApp
import me.weishu.kernelsu.ui.util.setSepolicy
import me.weishu.kernelsu.ui.viewmodel.SuperUserViewModel
import me.weishu.kernelsu.ui.viewmodel.TemplateViewModel

/**
 * @author weishu
 * @date 2023/5/16.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppProfileScreen(
    uid: Int,
    packageName: String,
) {
    val snackBarHost = LocalSnackbarHost.current
    val navigator = LocalNavController.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val superUserVM = viewModel<SuperUserViewModel>()
    val templateVM = viewModel<TemplateViewModel>()
    val appUiState by superUserVM.uiState.collectAsState()
    val templateUiState by templateVM.uiState.collectAsState()
    val groupAppState = remember(uid, packageName) {
        derivedStateOf { appUiState.groupedApps.find { it.uid == uid && it.primary.packageName == packageName } }
    }
    val groupApp = groupAppState.value
    if (groupApp == null) {
        LaunchedEffect(Unit) {
            navigator.popBackStack()
        }
        return
    }
    val sharedUserId = remember {
        groupApp.primary.packageInfo.sharedUserId ?: groupApp.apps.firstOrNull { it.packageInfo.sharedUserId != null }?.packageInfo?.sharedUserId ?: ""
    }
    val initialProfile = Natives.getAppProfile(packageName, uid)
    if (initialProfile.allowSu) {
        initialProfile.rules = getSepolicy(packageName)
    }
    var profile by rememberSaveable {
        mutableStateOf(initialProfile)
    }
    LaunchedEffect(Unit) {
        if (templateUiState.templateList.isEmpty()) {
            templateVM.fetchTemplates()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopBar(
                onBack = dropUnlessResumed { navigator.popBackStack() }, scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { BreezeSnackBarHost(hostState = snackBarHost) },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) { paddingValues ->
        val isUidGroup = groupApp.apps.size > 1
        val isRootGranted = profile.allowSu

        val onProfileChange = setProfile(groupApp.primary) { profile = it }
        var rootMode by rememberSaveable {
            mutableStateOf(
                if (profile.rootUseDefault) Mode.Default
                else if (profile.rootTemplate != null) Mode.Template
                else Mode.Custom
            )
        }

        var nonRootMode by rememberSaveable {
            mutableStateOf(
                if (profile.nonRootUseDefault) Mode.Default
                else Mode.Custom
            )
        }
        val currentMode = if (isRootGranted) rootMode else nonRootMode

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
        ) {
            AppInfoGroup(
                appIcon = {
                    AppIconImage(
                        modifier = Modifier
                            .padding(4.dp)
                            .width(48.dp)
                            .height(48.dp), packageInfo = groupApp.primary.packageInfo
                    )
                },
                appLabel = if (isUidGroup) ownerNameForUid(uid) else groupApp.primary.label,
                appUid = uid,
                appVersionName = groupApp.primary.packageInfo.versionName ?: "",
                appVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    groupApp.primary.packageInfo.longVersionCode
                } else {
                    @Suppress("DEPRECATION") groupApp.primary.packageInfo.versionCode.toLong()
                },
                packageName = if (isUidGroup) sharedUserId else packageName,
                affectedAppCount = groupApp.apps.size,
                isRootGranted = profile.allowSu,
                mode = currentMode.text
            ) {
                onProfileChange(profile.copy(allowSu = it))
            }
            ModeChipBar(currentMode, isRootGranted) { newMode ->
                if (isRootGranted) {
                    // template mode shouldn't change profile here!
                    val shouldClearTemplate = (newMode == Mode.Default || newMode == Mode.Custom)
                    onProfileChange(
                        profile.copy(
                            rootUseDefault = (newMode == Mode.Default), rootTemplate = if (shouldClearTemplate) null else profile.rootTemplate
                        )
                    )
                    rootMode = newMode
                } else {
                    onProfileChange(
                        profile.copy(
                            nonRootUseDefault = (newMode == Mode.Default)
                        )
                    )
                    nonRootMode = newMode
                }
            }
            Crossfade(targetState = isRootGranted) { isRoot ->
                if (isRoot) {
                    val template = templateUiState.templateList.find { it.id == profile.rootTemplate }
                    RootProfile(profile = profile, template, rootMode, onProfileChange = onProfileChange)
                } else {
                    NonRootProfileConfig(
                        enabled = nonRootMode == Mode.Custom, profile = profile, onProfileChange = onProfileChange
                    )
                }
            }
            AffectedAppColumn(groupApp.apps)
            Spacer(modifier = Modifier.height(6.dp + 48.dp + 6.dp /* SnackBar height */))
        }
    }
}

@Composable
private fun setProfile(
    appInfo: SuperUserViewModel.AppInfo, setUiProfile: (Natives.Profile) -> Unit
): (Natives.Profile) -> Unit {
    val failToUpdateAppProfile = stringResource(R.string.failed_to_update_app_profile).format(appInfo.label)
    val failToUpdateSepolicy = stringResource(R.string.failed_to_update_sepolicy).format(appInfo.label)
    val suNotAllowed = stringResource(R.string.su_not_allowed).format(appInfo.label)
    val scope = rememberCoroutineScope()
    val snackBarHost = LocalSnackbarHost.current
    return { newProfile ->
        scope.launch {
            if (newProfile.allowSu) {
                if (appInfo.uid < 2000 && appInfo.uid != 1000) {
                    snackBarHost.showSnackbar(suNotAllowed)
                    return@launch
                }
                if (!newProfile.rootUseDefault && newProfile.rules.isNotEmpty() && !setSepolicy(newProfile.name, newProfile.rules)) {
                    snackBarHost.showSnackbar(failToUpdateSepolicy)
                    return@launch
                }
            }
            if (!Natives.setAppProfile(newProfile)) {
                snackBarHost.showSnackbar(failToUpdateAppProfile.format(newProfile.uid))
            } else {
                setUiProfile(newProfile)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AppInfoGroup(
    appIcon: @Composable (() -> Unit),
    appLabel: String,
    appUid: Int,
    appVersionName: String,
    appVersionCode: Long,
    packageName: String,
    affectedAppCount: Int,
    isRootGranted: Boolean,
    mode: String,
    onAllowSuChange: (Boolean) -> Unit,
) {
    val resources = LocalResources.current
    val isUidGroup = affectedAppCount > 1
    val switchFeedback = switchHapticFeedBack()
    val userId = appUid / 100000
    SegmentedListGroup(modifier = Modifier.padding(all = 16.dp)) {
        menuItem(
            leadingContent = appIcon, content = { Text(appLabel) }, supportingContent = {
                Column {
                    if (packageName.isNotEmpty()) Text(text = packageName)
                    if (!isUidGroup) {
                        Text("$appVersionName ($appVersionCode)")
                    } else {
                        Text(
                            text = stringResource(R.string.group_contains_apps, affectedAppCount),
                        )
                    }
                }
            }, trailingContent = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    StatusTag("UID $appUid")
                    if (userId != 0) StatusTag("USER $userId")
                }
            }, menuContent = if (isUidGroup) null else appMenuContent(packageName)
        )
        switchItem(
            title = resources.getString(R.string.superuser),
            leadingContent = { Icon(painterResource(R.drawable.ic_security_rounded), stringResource(id = R.string.superuser)) },
            checked = isRootGranted,
            onCheckedChange = {
                switchFeedback(it)
                onAllowSuChange(it)
            })
        item(
            content = { Text(stringResource(R.string.profile)) },
            supportingContent = { Text(mode) },
            leadingContent = { Icon(Icons.Filled.AccountCircle, stringResource(R.string.profile)) },
        )
    }
}

private enum class Mode(@param:StringRes private val res: Int) {
    Default(R.string.profile_default), Template(R.string.profile_template), Custom(R.string.profile_custom);

    val text: String
        @Composable get() = stringResource(res)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TopBar(onBack: () -> Unit, scrollBehavior: TopAppBarScrollBehavior) {
    LargeFlexibleTopAppBar(
        title = {
            Text(stringResource(R.string.profile))
        }, navigationIcon = {
            if (LocalIsDetailPane.current) return@LargeFlexibleTopAppBar
            IconButton(
                onClick = onBack
            ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
        }, actions = { SplitScreenRatioButton() }, scrollBehavior = scrollBehavior, colors = defaultTopAppBarColors()
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ModeChipBar(mode: Mode, showTemple: Boolean, onModeChange: (Mode) -> Unit) {
    Crossfade(showTemple, animationSpec = MaterialTheme.motionScheme.slowEffectsSpec()) { showTemple ->
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilterChip(
                selected = mode == Mode.Default,
                label = { Text(stringResource(R.string.profile_default)) },
                onClick = { onModeChange(Mode.Default) },
            )
            if (showTemple) {
                FilterChip(
                    selected = mode == Mode.Template,
                    label = {
                        Text(
                            stringResource(R.string.profile_template)
                        )
                    },
                    onClick = { onModeChange(Mode.Template) },
                )
            }

            FilterChip(
                selected = mode == Mode.Custom,
                label = { Text(stringResource(R.string.profile_custom)) },
                onClick = { onModeChange(Mode.Custom) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RootProfile(profile: Natives.Profile, template: TemplateInfo?, mode: Mode, onProfileChange: (Natives.Profile) -> Unit) {
    val motionScheme = MaterialTheme.motionScheme
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Column {
        AnimatedContent(
            targetState = mode, label = "ModeSwitchAnimation", transitionSpec = {
                slideHorizontal(isRtl == targetState.ordinal > initialState.ordinal, animationSpec = motionScheme.defaultSpatialSpec())
            }) { targetMode ->
            when (targetMode) {
                Mode.Template -> {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        RootTemplateSelector(
                            profile = profile, onProfileChange = onProfileChange
                        )
                        AnimatedVisibility(profile.rootTemplate != null, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                            Column {
                                RootTempleInfo(template)
                                RootProfileConfig(
                                    profile = profile, readOnly = true, onProfileChange = { }
                                )
                            }
                        }
                    }
                }

                Mode.Custom -> {
                    RootProfileConfig(
                        modifier = Modifier.padding(horizontal = 16.dp), profile = profile, onProfileChange = onProfileChange
                    )
                }

                else -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Default mode, requires a placeholder, otherwise the animation will be strange
                    }
                }
            }
        }
    }
}

@Composable
private fun RootTempleInfo(template: TemplateInfo?) {
    AnimatedContent(
        targetState = template, transitionSpec = {
            if (targetState == null) {
                EnterTransition.None togetherWith fadeOut(animationSpec = snap(delayMillis = 250))
            } else {
                EnterTransition.None togetherWith ExitTransition.None
            }
        }, label = ""
    ) { localTemplate ->
        if (localTemplate == null) {
            return@AnimatedContent
        }
        Column {
            val editTextModifier = Modifier.fillMaxWidth()
            OutlinedTextEdit(
                modifier = editTextModifier,
                label = { Text(stringResource(id = R.string.app_profile_template_name)) },
                text = localTemplate.name,
                readOnly = true,
                onValueChange = {})
            OutlinedTextEdit(
                modifier = editTextModifier,
                label = { Text(stringResource(id = R.string.app_profile_template_description)) },
                text = localTemplate.description,
                singleLine = false,
                readOnly = true,
                onValueChange = {})
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AffectedAppColumn(affectedApps: List<SuperUserViewModel.AppInfo>) {
    if (affectedApps.size < 2) {
        return
    }
    SegmentedListGroup(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp), title = stringResource(R.string.app_profile_affects_following_apps)
    ) {
        affectedApps.forEach { app ->
            val appVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                app.packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION") app.packageInfo.versionCode.toLong()
            }
            menuItem(content = { Text(app.label) }, supportingContent = {
                Column {
                    Text(app.packageName)
                    Text("${app.packageInfo.versionName} (${appVersionCode})")
                }
            }, leadingContent = {
                AppIconImage(
                    modifier = Modifier
                        .padding(4.dp)
                        .width(48.dp)
                        .height(48.dp), packageInfo = app.packageInfo
                )
            }, menuContent = appMenuContent(app.packageName))
        }
    }
}

private fun appMenuContent(packageName: String): @Composable ColumnScope.(dismissMenu: () -> Unit) -> Unit = { dismissMenu ->
    DropdownMenuItem(
        text = { Text(stringResource(id = R.string.launch_app)) },
        onClick = {
            dismissMenu()
            launchApp(packageName)
        },
    )
    DropdownMenuItem(
        text = { Text(stringResource(id = R.string.force_stop_app)) },
        onClick = {
            dismissMenu()
            forceStopApp(packageName)
        },
    )
    DropdownMenuItem(
        text = { Text(stringResource(id = R.string.restart_app)) },
        onClick = {
            dismissMenu()
            restartApp(packageName)
        },
    )
}
