package me.weishu.kernelsu.ui.screen

import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.AppIconImage
import me.weishu.kernelsu.ui.component.BrMenuBox
import me.weishu.kernelsu.ui.component.OutlinedTextEdit
import me.weishu.kernelsu.ui.component.SwitchItem
import me.weishu.kernelsu.ui.component.profile.AppProfileConfig
import me.weishu.kernelsu.ui.component.profile.RootProfileConfig
import me.weishu.kernelsu.ui.component.profile.RootTemplateSelector
import me.weishu.kernelsu.ui.navigation.slideInFromLeft
import me.weishu.kernelsu.ui.navigation.slideInFromRight
import me.weishu.kernelsu.ui.util.LocalNavController
import me.weishu.kernelsu.ui.util.LocalSnackbarHost
import me.weishu.kernelsu.ui.util.forceStopApp
import me.weishu.kernelsu.ui.util.getSepolicy
import me.weishu.kernelsu.ui.util.launchApp
import me.weishu.kernelsu.ui.util.ownerNameForUid
import me.weishu.kernelsu.ui.util.pickPrimary
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
    appInfo: SuperUserViewModel.AppInfo,
) {
    val snackBarHost = LocalSnackbarHost.current
    val navigator = LocalNavController.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val scope = rememberCoroutineScope()
    val failToUpdateAppProfile = stringResource(R.string.failed_to_update_app_profile).format(appInfo.label)
    val failToUpdateSepolicy = stringResource(R.string.failed_to_update_sepolicy).format(appInfo.label)
    val suNotAllowed = stringResource(R.string.su_not_allowed).format(appInfo.label)

    val packageName = appInfo.packageName
    val sameUidApps = remember(appInfo.uid) {
        SuperUserViewModel.apps.filter { it.uid == appInfo.uid }
    }
    val isUidGroup = sameUidApps.size > 1
    val primaryForIcon = remember(appInfo.uid, sameUidApps) {
        runCatching { pickPrimary(sameUidApps) }.getOrNull() ?: appInfo
    }
    val sharedUserId = remember(appInfo.uid, sameUidApps, primaryForIcon) {
        primaryForIcon.packageInfo.sharedUserId ?: sameUidApps.firstOrNull { it.packageInfo.sharedUserId != null }?.packageInfo?.sharedUserId ?: ""
    }
    val initialProfile = Natives.getAppProfile(packageName, appInfo.uid)
    if (initialProfile.allowSu) {
        initialProfile.rules = getSepolicy(packageName)
    }
    var profile by rememberSaveable {
        mutableStateOf(initialProfile)
    }
    val setProfile: (Natives.Profile) -> Unit = { profile = it }
    LaunchedEffect(Unit) {
        if (TemplateViewModel().templateList.isEmpty()) {
            TemplateViewModel().fetchTemplates()
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                onBack = dropUnlessResumed { navigator.popBackStack() }, scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHost) },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { paddingValues ->
        AppProfileInner(
            modifier = Modifier
                .padding(paddingValues)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState()),
            packageName = if (isUidGroup) "" else appInfo.packageName,
            appLabel = if (isUidGroup) ownerNameForUid(appInfo.uid) else appInfo.label,
            appIcon = {
                val iconApp = if (isUidGroup) primaryForIcon else appInfo
                AppIconImage(
                    iconApp.packageInfo, modifier = Modifier
                        .padding(4.dp)
                        .width(48.dp)
                        .height(48.dp)
                )
            },
            appUid = appInfo.uid,
            sharedUserId = if (isUidGroup) sharedUserId else "",
            appVersionName = if (isUidGroup) "" else (appInfo.packageInfo.versionName ?: ""),
            appVersionCode = if (isUidGroup) 0L else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                appInfo.packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION") appInfo.packageInfo.versionCode.toLong()
            },
            profile = profile,
            affectedApps = sameUidApps,
        ) {
            scope.launch {
                if (it.allowSu) {
                    if (appInfo.uid < 2000 && appInfo.uid != 1000) {
                        snackBarHost.showSnackbar(suNotAllowed)
                        return@launch
                    }
                    if (!it.rootUseDefault && it.rules.isNotEmpty() && !setSepolicy(profile.name, it.rules)) {
                        snackBarHost.showSnackbar(failToUpdateSepolicy)
                        return@launch
                    }
                }
                if (!Natives.setAppProfile(it)) {
                    snackBarHost.showSnackbar(failToUpdateAppProfile.format(appInfo.uid))
                } else {
                    setProfile(it)
                }
            }
        }
    }
}

@Composable
private fun AppProfileInner(
    modifier: Modifier = Modifier,
    packageName: String,
    appLabel: String,
    appIcon: @Composable () -> Unit,
    appUid: Int,
    sharedUserId: String = "",
    appVersionName: String,
    appVersionCode: Long,
    profile: Natives.Profile,
    affectedApps: List<SuperUserViewModel.AppInfo> = emptyList(),
    onProfileChange: (Natives.Profile) -> Unit,
) {
    val isRootGranted = profile.allowSu
    val isUidGroup = affectedApps.size > 1
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


    Column(modifier = modifier) {
        AppMenuBox(packageName, isUidGroup) {
            ListItem(
                headlineContent = { Text(appLabel) },
                supportingContent = {
                    Column {
                        if (!isUidGroup) {
                            Text(text = packageName)
                            Text("$appVersionName ($appVersionCode)")
                        } else {
                            if (sharedUserId.isNotEmpty()) {
                                Text(
                                    text = sharedUserId,
                                )
                            }
                            Text(
                                text = stringResource(R.string.group_contains_apps, affectedApps.size),
                            )
                        }
                    }
                },
                trailingContent = {
                    StatusTag("UID $appUid")
                },
                leadingContent = appIcon,
            )
        }

        SwitchItem(
            icon = Icons.Filled.Security,
            title = stringResource(id = R.string.superuser),
            checked = isRootGranted,
            onCheckedChange = { onProfileChange(profile.copy(allowSu = it)) },
        )

        ProfileBox(currentMode, isRootGranted) { newMode ->
            if (isRootGranted) {
                // template mode shouldn't change profile here!
                val shouldClearTemplate = (newMode == Mode.Default || newMode == Mode.Custom)
                onProfileChange(
                    profile.copy(
                        rootUseDefault = (newMode == Mode.Default),
                        rootTemplate = if (shouldClearTemplate) null else profile.rootTemplate
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

        Crossfade(targetState = isRootGranted, label = "") { isRoot ->
            if (isRoot) {
                RootProfile(profile = profile, rootMode, onProfileChange = onProfileChange)
            } else {
                AppProfileConfig(
                    enabled = nonRootMode == Mode.Custom, profile = profile, onProfileChange = onProfileChange
                )
            }
        }
        affectedAppColumn(affectedApps)
        Spacer(modifier = Modifier.height(6.dp + 48.dp + 6.dp /* SnackBar height */))
    }
}

private enum class Mode(@param:StringRes private val res: Int) {
    Default(R.string.profile_default), Template(R.string.profile_template), Custom(R.string.profile_custom);

    val text: String
        @Composable get() = stringResource(res)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(onBack: () -> Unit, scrollBehavior: TopAppBarScrollBehavior? = null) {
    TopAppBar(
        title = {
            Text(stringResource(R.string.profile))
        }, navigationIcon = {
            IconButton(
                onClick = onBack
            ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
        }, windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal), scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProfileBox(mode: Mode, hasTemplate: Boolean, onModeChange: (Mode) -> Unit) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.profile)) },
        supportingContent = { Text(mode.text) },
        leadingContent = { Icon(Icons.Filled.AccountCircle, null) },
    )
    HorizontalDivider(thickness = Dp.Hairline)
    ListItem(headlineContent = {
        Row(
            modifier = Modifier
                .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilterChip(
                selected = mode == Mode.Default,
                label = { Text(stringResource(R.string.profile_default)) },
                onClick = { onModeChange(Mode.Default) },
            )
            val motionScheme = MaterialTheme.motionScheme
            AnimatedVisibility(
                visible = hasTemplate,
                enter = expandHorizontally(
                    expandFrom = Alignment.CenterHorizontally,
                    animationSpec = motionScheme.fastEffectsSpec()
                ) + fadeIn(),
                exit = shrinkHorizontally(
                    shrinkTowards = Alignment.CenterHorizontally,
                    animationSpec = motionScheme.fastEffectsSpec()
                ) + fadeOut()
            ) {
                FilterChip(
                    selected = mode == Mode.Template,
                    label = {
                        Text(
                            stringResource(R.string.profile_template),
                            maxLines = 1, // 确保文字只有一行，避免高度变化
                            softWrap = false
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
    })
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RootProfile(profile: Natives.Profile, mode: Mode, onProfileChange: (Natives.Profile) -> Unit) {
    Column {
        val motionScheme = MaterialTheme.motionScheme
        AnimatedContent(
            targetState = mode, label = "ModeSwitchAnimation", transitionSpec = {
                if (targetState.ordinal > initialState.ordinal) {
                    slideInFromRight(animationSpec = motionScheme.defaultSpatialSpec())
                } else {
                    slideInFromLeft(animationSpec = motionScheme.defaultSpatialSpec())
                }
            }) { targetMode ->
            when (targetMode) {
                Mode.Template -> {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        RootTemplateSelector(
                            profile = profile, onProfileChange = onProfileChange
                        )
                        AnimatedVisibility(profile.rootTemplate != null, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                            Column {
                                RootTempleInfo(profile.rootTemplate)
                                RootProfileConfig(
                                    profile = profile, readOnly = true, onProfileChange = onProfileChange
                                )
                            }
                        }
                    }
                }

                Mode.Custom -> {
                    RootProfileConfig(
                        modifier = Modifier.padding(horizontal = 16.dp),profile = profile, onProfileChange = onProfileChange
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
private fun RootTempleInfo(templateId: String?) {
    val template = viewModel<TemplateViewModel>().templateList.find { it.id == templateId }
    AnimatedContent(
        targetState = template,
        transitionSpec = {
            if (targetState == null) {
                EnterTransition.None togetherWith fadeOut(animationSpec = snap(delayMillis = 250))
            } else {
                EnterTransition.None togetherWith ExitTransition.None
            }
        },
        label = ""
    ) { localTemplate ->
        if (localTemplate == null) {
            return@AnimatedContent
        }
        Column {
            val editTextModifier = Modifier
                .fillMaxWidth()
            OutlinedTextEdit(
                modifier = editTextModifier,
                label = { Text(stringResource(id = R.string.app_profile_template_name)) },
                text = localTemplate.name,
                readOnly = true,
                onValueChange = {}
            )
            OutlinedTextEdit(
                modifier = editTextModifier,
                label = { Text(stringResource(id = R.string.app_profile_template_description)) },
                text = localTemplate.description,
                singleLine = false,
                readOnly = true,
                onValueChange = {}
            )
        }
    }
}


@Composable
private fun AppProfile(profile: Natives.Profile, onProfileChange: (Natives.Profile) -> Unit) {


}

@Composable
private fun AppMenuBox(packageName: String, isUidGroup: Boolean, content: @Composable () -> Unit) {
    if (isUidGroup) {
        content()
        return
    }
    BrMenuBox(
        content = content, menuContent = { dismissMenu ->
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
        }, description = null
    )
}

@Composable
private fun affectedAppColumn(affectedApps: List<SuperUserViewModel.AppInfo> = emptyList()) {
    val isUidGroup = affectedApps.size > 1
    if (!isUidGroup) {
        return
    }
    Column {
        Text(
            color = colorScheme.primary, modifier = Modifier.padding(16.dp), text = stringResource(R.string.app_profile_affects_following_apps)
        )
        affectedApps.forEach { app ->
            val appVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                app.packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION") app.packageInfo.versionCode.toLong()
            }
            AppMenuBox(app.packageName, false) {
                ListItem(
                    headlineContent = { Text(app.label) },
                    supportingContent = {
                        Column {
                            Text(app.packageName)
                            Text("${app.packageInfo.versionName} (${appVersionCode})")
                        }
                    },
                    leadingContent = {
                        AppIconImage(
                            app.packageInfo, modifier = Modifier
                                .padding(4.dp)
                                .width(48.dp)
                                .height(48.dp)
                        )
                    },
                )
            }
        }
    }
}

@Preview
@Composable
private fun AppProfilePreview() {
    var profile by remember { mutableStateOf(Natives.Profile("")) }
    val setProfile: (Natives.Profile) -> Unit = { profile = it }
    AppProfileInner(
        packageName = "icu.nullptr.test",
        appLabel = "Test",
        appIcon = { Icon(Icons.Filled.Android, null) },
        appUid = 1234,
        appVersionName = "test",
        appVersionCode = 1234,
        profile = profile,
    ) {
        setProfile(it)
    }
}