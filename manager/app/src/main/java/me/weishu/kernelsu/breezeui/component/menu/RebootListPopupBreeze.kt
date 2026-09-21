package me.weishu.kernelsu.breezeui.component.menu

import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import me.weishu.kernelsu.R
import me.weishu.kernelsu.breezeui.icons.MaterialSymbols
import me.weishu.kernelsu.ui.component.KsuIsValid
import me.weishu.kernelsu.ui.component.rebootlistpopup.RebootDropdownItems
import me.weishu.kernelsu.ui.component.rebootlistpopup.rememberRebootAction

/**
 * Reboot action for top bars. The option list and the jailbreak confirmation are
 * shared with the other themes; only the popup chrome is Breeze's own.
 */
@Composable
fun RebootListPopupBreeze() {
    KsuIsValid {
        var expanded by remember { mutableStateOf(false) }
        val onReboot = rememberRebootAction()

        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = MaterialSymbols.Rounded.RestartAlt,
                contentDescription = stringResource(id = R.string.reboot)
            )
        }

        DropdownMenuPopup(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuGroup(shapes = MenuDefaults.groupShapes()) {
                RebootDropdownItems { reason ->
                    expanded = false
                    onReboot(reason)
                }
            }
        }
    }
}
