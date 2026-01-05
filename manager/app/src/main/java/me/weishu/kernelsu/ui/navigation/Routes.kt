package me.weishu.kernelsu.ui.navigation

import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.screen.FlashIt
import me.weishu.kernelsu.ui.viewmodel.SuperUserViewModel
import me.weishu.kernelsu.ui.viewmodel.TemplateViewModel

enum class TopLevelRoute(
    @param:StringRes val labelId: Int, val selectedIcon: ImageVector, val defaultIcon: ImageVector, val rootRequired: Boolean, val navKey: NavKey
) {
    Home(
        labelId = R.string.home, selectedIcon = Icons.Filled.Home, defaultIcon = Icons.Outlined.Home, rootRequired = false, navKey = HomeNavKey
    ),

    SuperUser(
        labelId = R.string.superuser, selectedIcon = Icons.Filled.Security, defaultIcon = Icons.Outlined.Shield, rootRequired = true, navKey = SuperUserNavKey
    ),

    Module(
        labelId = R.string.module, selectedIcon = Icons.Filled.Extension, defaultIcon = Icons.Outlined.Extension, rootRequired = true, navKey = ModuleNavKey
    ),

    Setting(
        labelId = R.string.settings, selectedIcon = Icons.Filled.Settings, defaultIcon = Icons.Outlined.Settings, rootRequired = false, navKey = SettingNavKey
    );
}

@Parcelize
@Serializable
data object HomeNavKey : NavKey, Parcelable

@Parcelize
@Serializable
data object SuperUserNavKey : NavKey, Parcelable

@Parcelize
@Serializable
data object ModuleNavKey : NavKey, Parcelable

@Parcelize
@Serializable
data object SettingNavKey : NavKey, Parcelable


@Parcelize
@Serializable
data class AppProfileScreenNavKey(
    @Serializable(with = AppInfoSerializer::class) val appInfo: SuperUserViewModel.AppInfo
) : NavKey, Parcelable

@Parcelize
@Serializable
data class ExecuteModuleActionNavKey(
    val moduleId: String
) : NavKey, Parcelable

@Parcelize
@Serializable
data class FlashScreenNavKey(
    @Serializable(with = FlashItSerializer::class) val flashIt: FlashIt
) : NavKey, Parcelable

@Parcelize
@Serializable
data object InstallScreenNavKey : NavKey, Parcelable

@Parcelize
@Serializable
data object AppProfileTemplateNavKey : NavKey, Parcelable

@Parcelize
@Serializable
data class TemplateEditorNavKey(
    @Serializable(with = TemplateInfoSerializer::class) val initialTemplate: TemplateViewModel.TemplateInfo, val readOnly: Boolean = true
) : NavKey, Parcelable