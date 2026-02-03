package me.weishu.kernelsu.ui.navigation3

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.screen.FlashIt
import me.weishu.kernelsu.ui.util.FlashItSerializer
import me.weishu.kernelsu.ui.util.TemplateInfoSerializer
import me.weishu.kernelsu.ui.viewmodel.TemplateViewModel

enum class TopLevelRoute(
    @get:StringRes val labelId: Int, @get:DrawableRes val selectedIcon: Int, @get:DrawableRes val defaultIcon: Int, val
    rootRequired: Boolean, val navKey: NavKey
) {
    Home(
        labelId = R.string.home,
        selectedIcon = R.drawable.ic_cottage_rounded_filled,
        defaultIcon = R.drawable.ic_cottage_rounded,
        rootRequired = false,
        navKey = Route.Home
    ),

    SuperUser(
        labelId = R.string.superuser,
        selectedIcon = R.drawable.ic_security_rounded,
        defaultIcon = R.drawable.ic_shield_rounded,
        rootRequired = true,
        navKey = Route
            .SuperUser
    ),

    Module(
        labelId = R.string.module,
        selectedIcon = R.drawable.ic_extension_rounded_filled,
        defaultIcon = R.drawable.ic_extension_rounded,
        rootRequired = true,
        navKey =
            Route.Module
    ),

    Setting(
        labelId = R.string.settings,
        selectedIcon = R.drawable.ic_settings_rounded_filled,
        defaultIcon = R.drawable.ic_settings_rounded,
        rootRequired =
            false,
        navKey = Route.Settings
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
