package me.weishu.kernelsu.ui.component.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.breeze.SegmentedListGroup

@Composable
fun AppProfileConfigBreeze(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    profile: Natives.Profile,
    onProfileChange: (Natives.Profile) -> Unit,
) {
    val resources = LocalResources.current
    SegmentedListGroup(modifier = modifier) {
        switchItem(
            title = resources.getString(R.string.profile_umount_modules), summary = resources.getString(R.string.profile_umount_modules_summary),
            checked = {
                if (enabled) {
                    profile.umountModules
                } else {
                    Natives.isDefaultUmountModules()
                }
            },
            enabled = enabled,
        ) {
            onProfileChange(
                profile.copy(
                    umountModules = it,
                    nonRootUseDefault = false
                )
            )
        }
    }
}
