package me.weishu.kernelsu.ui.navigation3

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
import me.weishu.kernelsu.ui.util.FlashItSerializer
import me.weishu.kernelsu.ui.util.TemplateInfoSerializer
import me.weishu.kernelsu.ui.viewmodel.TemplateViewModel

enum class TopLevelRoute(
    @param:StringRes val labelId: Int, val selectedIcon: ImageVector, val defaultIcon: ImageVector, val rootRequired: Boolean, val navKey: NavKey
) {
    Home(
        labelId = R.string.home, selectedIcon = Icons.Filled.Home, defaultIcon = Icons.Outlined.Home, rootRequired = false, navKey = Route.Home
    ),

    SuperUser(
        labelId = R.string.superuser, selectedIcon = Icons.Filled.Security, defaultIcon = Icons.Outlined.Shield, rootRequired = true, navKey = Route.SuperUser
    ),

    Module(
        labelId = R.string.module, selectedIcon = Icons.Filled.Extension, defaultIcon = Icons.Outlined.Extension, rootRequired = true, navKey = Route.Module
    ),

    Setting(
        labelId = R.string.settings, selectedIcon = Icons.Filled.Settings, defaultIcon = Icons.Outlined.Settings, rootRequired = false, navKey = Route.Settings
    );
}

/**
 * Type-safe navigation keys for Navigation3.
 * Each destination is a NavKey (data object/data class) and can be saved/restored in the back stack.
 */
sealed interface Route : NavKey, Parcelable {

    @Parcelize
    @Serializable
    data object Home : NavKey, Parcelable

    @Parcelize
    @Serializable
    data object SuperUser : NavKey, Parcelable

    @Parcelize
    @Serializable
    data object Module : NavKey, Parcelable

    @Parcelize
    @Serializable
    data object Settings : NavKey, Parcelable

    @Parcelize
    @Serializable
    data class AppProfile(val packageName: String) : NavKey, Parcelable

    @Parcelize
    @Serializable
    data class ExecuteModuleAction(
        val moduleId: String
    ) : NavKey, Parcelable

    @Parcelize
    @Serializable
    data class Flash(
        @Serializable(with = FlashItSerializer::class) val flashIt: FlashIt
    ) : NavKey, Parcelable

    @Parcelize
    @Serializable
    data object Install : NavKey, Parcelable

    @Parcelize
    @Serializable
    data object AppProfileTemplate : NavKey, Parcelable

    @Parcelize
    @Serializable
    data class TemplateEditor(
        @Serializable(with = TemplateInfoSerializer::class) val initialTemplate: TemplateViewModel.TemplateInfo, val readOnly: Boolean = true
    ) : NavKey, Parcelable
}
