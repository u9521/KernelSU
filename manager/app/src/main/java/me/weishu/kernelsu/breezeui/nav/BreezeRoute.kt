package me.weishu.kernelsu.breezeui.nav

import android.os.Parcelable
import androidx.navigation3.runtime.NavKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import me.weishu.kernelsu.ui.screen.flash.FlashIt
import me.weishu.kernelsu.ui.util.FlashItSerializer
import me.weishu.kernelsu.ui.util.TemplateInfoSerializer
import me.weishu.kernelsu.ui.viewmodel.TemplateViewModel

/**
 * Breeze's own navigation keys.
 *
 * Deliberately independent from `ui.navigation3.Route`: Breeze owns its back stack
 * and must keep working when upstream reshapes its navigation graph. Only the
 * argument payloads (`FlashIt`, `RepoModuleArg`, `TemplateInfo`) are shared.
 */
sealed interface BreezeRoute : NavKey, Parcelable {
    @Parcelize
    @Serializable
    data object Main : BreezeRoute

    @Parcelize
    @Serializable
    data object Home : BreezeRoute

    @Parcelize
    @Serializable
    data object SuperUser : BreezeRoute

    @Parcelize
    @Serializable
    data object Module : BreezeRoute

    @Parcelize
    @Serializable
    data object Settings : BreezeRoute

    @Parcelize
    @Serializable
    data object About : BreezeRoute

    @Parcelize
    @Serializable
    data object Sulog : BreezeRoute

    @Parcelize
    @Serializable
    data object ColorPalette : BreezeRoute

    @Parcelize
    @Serializable
    data object AppProfileTemplate : BreezeRoute

    @Parcelize
    @Serializable
    data class TemplateEditor(
        @Serializable(with = TemplateInfoSerializer::class) val template: TemplateViewModel.TemplateInfo,
        val readOnly: Boolean
    ) : BreezeRoute

    @Parcelize
    @Serializable
    data class AppProfile(val uid: Int) : BreezeRoute

    @Parcelize
    @Serializable
    data object Install : BreezeRoute

    @Parcelize
    @Serializable
    data class Flash(@Serializable(with = FlashItSerializer::class) val flashIt: FlashIt) : BreezeRoute

    @Parcelize
    @Serializable
    data class ExecuteModuleAction(val moduleId: String, val fromShortcut: Boolean = false) : BreezeRoute
}
