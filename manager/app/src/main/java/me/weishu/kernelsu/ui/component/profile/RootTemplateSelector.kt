package me.weishu.kernelsu.ui.component.profile

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.navigation3.Route
import me.weishu.kernelsu.ui.util.LocalNavController
import me.weishu.kernelsu.ui.util.listAppProfileTemplates
import me.weishu.kernelsu.ui.viewmodel.getTemplateInfoById

/**
 * @author weishu
 * @date 2023/10/21.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootTemplateSelector(
    profile: Natives.Profile, onProfileChange: (Natives.Profile) -> Unit
) {
    val navigator = LocalNavController.current
    var expanded by remember { mutableStateOf(false) }
    var template by rememberSaveable {
        mutableStateOf(profile.rootTemplate ?: "")
    }
    val setExpand: (Boolean) -> Unit = { expanded = it }
    val setTemplate: (String) -> Unit = { template = it }
    val profileTemplates = listAppProfileTemplates()
    val noTemplates = profileTemplates.isEmpty()
    val unselected = if (noTemplates) stringResource(R.string.profile_template_none) else stringResource(R.string.profile_template_unselected)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { setExpand(it) },
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            readOnly = true,
            label = { Text(stringResource(R.string.profile_template)) },
            value = template.ifEmpty { unselected },
            onValueChange = {},
            supportingText = { /*placeholder keep same height*/ },
            trailingIcon = {
                if (noTemplates) {
                    IconButton(
                        onClick = { navigator.navigateTo(Route.AppProfileTemplate) }
                    ) {
                        Icon(Icons.Filled.Create, null)
                    }
                } else if (expanded) Icon(Icons.Filled.ArrowDropUp, null)
                else Icon(Icons.Filled.ArrowDropDown, null)
            },
        )
        if (noTemplates) {
            return@ExposedDropdownMenuBox
        }
        ExposedDropdownMenu(
            expanded = expanded, onDismissRequest = { setExpand(false) }) {
            profileTemplates.forEach { tid ->
                val templateInfo = getTemplateInfoById(tid) ?: return@forEach
                DropdownMenuItem(text = { Text(tid) }, onClick = {
                    setTemplate(tid)
                    onProfileChange(
                        profile.copy(
                            rootTemplate = tid,
                            rootUseDefault = false,
                            uid = templateInfo.uid,
                            gid = templateInfo.gid,
                            groups = templateInfo.groups,
                            capabilities = templateInfo.capabilities,
                            context = templateInfo.context,
                            namespace = templateInfo.namespace,
                            rules = templateInfo.rules.joinToString("\n")
                        )
                    )
                    setExpand(false)
                })
            }
        }
    }
}