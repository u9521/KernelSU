package me.weishu.kernelsu.ui.component.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.SwitchItem

@Composable
fun AppProfileConfig(
    enabled: Boolean,
    profile: Natives.Profile,
    onProfileChange: (Natives.Profile) -> Unit,
) {
    SwitchItem(
        icon = null,
        title = stringResource(R.string.profile_umount_modules),
        summary = stringResource(R.string.profile_umount_modules_summary),
        checked = if (enabled) {
            profile.umountModules
        } else {
            Natives.isDefaultUmountModules()
        },
        enabled = enabled
    ) {
        onProfileChange(
            profile.copy(
                umountModules = it,
                nonRootUseDefault = false
            )
        )
    }
}
