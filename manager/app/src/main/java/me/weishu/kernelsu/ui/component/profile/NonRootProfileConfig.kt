package me.weishu.kernelsu.ui.component.profile

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.SegmentedListGroup

@Composable
fun NonRootProfileConfig(
    enabled: Boolean,
    profile: Natives.Profile,
    onProfileChange: (Natives.Profile) -> Unit,
) {
    SegmentedListGroup(modifier = Modifier.padding(16.dp)) {
        switchItem(
            title = { stringResource(R.string.profile_umount_modules) }, summary = { stringResource(R.string.profile_umount_modules_summary) },
            checked = if (enabled) {
                profile.umountModules
            } else {
                Natives.isDefaultUmountModules()
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
