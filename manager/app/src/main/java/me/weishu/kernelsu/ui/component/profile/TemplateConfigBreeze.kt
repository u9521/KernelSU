package me.weishu.kernelsu.ui.component.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.data.model.TemplateInfo
import me.weishu.kernelsu.ui.component.breeze.OutlinedTextEdit
import me.weishu.kernelsu.ui.component.breeze.PopupFeedBack
import me.weishu.kernelsu.ui.navigation3.LocalNavigator
import me.weishu.kernelsu.ui.navigation3.Route
import me.weishu.kernelsu.ui.util.listAppProfileTemplates
import me.weishu.kernelsu.ui.viewmodel.getTemplateInfoById

@Composable
fun TemplateConfigBreeze(
    profile: Natives.Profile,
    onProfileChange: (Natives.Profile) -> Unit
) {
    val template = remember(profile.rootTemplate) {
        profile.rootTemplate?.let(::getTemplateInfoById)
    }
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        RootTemplateSelectorBreeze(
            profile = profile,
            onProfileChange = onProfileChange,
        )
        AnimatedVisibility(
            visible = profile.rootTemplate != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                RootTemplateInfoBreeze(template)
                RootProfileConfigBreeze(
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    profile = profile,
                    onProfileChange = {},
                )
            }
        }
    }
}

@Composable
private fun RootTemplateInfoBreeze(template: TemplateInfo?) {
    AnimatedContent(
        targetState = template,
        transitionSpec = {
            if (targetState == null) {
                EnterTransition.None togetherWith fadeOut(animationSpec = snap(delayMillis = 250))
            } else {
                EnterTransition.None togetherWith ExitTransition.None
            }
        },
        label = "RootTemplateInfoBreeze",
    ) { currentTemplate ->
        if (currentTemplate == null) {
            return@AnimatedContent
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextEdit(
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.app_profile_template_name)) },
                text = currentTemplate.name,
                readOnly = true,
                onValueChange = {},
            )
            OutlinedTextEdit(
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.app_profile_template_description)) },
                text = currentTemplate.description,
                singleLine = false,
                readOnly = true,
                onValueChange = {},
            )
        }
    }
}

@Composable
fun RootTemplateSelectorBreeze(
    profile: Natives.Profile, onProfileChange: (Natives.Profile) -> Unit
) {
    val navigator = LocalNavigator.current
    var expanded by remember { mutableStateOf(false) }
    var template by rememberSaveable {
        mutableStateOf(profile.rootTemplate ?: "")
    }
    val setExpand: (Boolean) -> Unit = { expanded = it }
    val setTemplate: (String) -> Unit = { template = it }
    val profileTemplates = listAppProfileTemplates()
    val noTemplates = profileTemplates.isEmpty()
    val unselected = if (noTemplates) stringResource(R.string.profile_template_none) else stringResource(R.string.profile_template_unselected)
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "SelectTemplateRotation"
    )
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
                        onClick = { navigator.push(Route.AppProfileTemplate) }
                    ) {
                        Icon(Icons.Filled.Create, null)
                    }
                } else Icon(
                    modifier = Modifier.rotate(rotationState),
                    painter = painterResource(R.drawable.ic_keyboard_arrow_down_rounded),
                    contentDescription = null
                )
            },
        )
        if (noTemplates) {
            return@ExposedDropdownMenuBox
        }
        ExposedDropdownMenu(
            expanded = expanded, onDismissRequest = { setExpand(false) }) {
            PopupFeedBack()
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