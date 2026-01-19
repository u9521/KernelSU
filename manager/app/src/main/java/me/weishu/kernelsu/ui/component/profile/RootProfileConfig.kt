package me.weishu.kernelsu.ui.component.profile

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import kotlinx.coroutines.launch
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.profile.Capabilities
import me.weishu.kernelsu.profile.Groups
import me.weishu.kernelsu.ui.component.OutlinedTextEdit
import me.weishu.kernelsu.ui.component.rememberCustomDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootProfileConfig(
    modifier: Modifier = Modifier, profile: Natives.Profile, readOnly: Boolean = false, onProfileChange: (Natives.Profile) -> Unit
) {

    val handleProfileChange: (Natives.Profile) -> Unit = { newProfile ->
        if (!readOnly) {
            onProfileChange(newProfile)
        }
    }
    Column(modifier = modifier) {
        val editTextModifier = Modifier.fillMaxWidth()
        // UID Input
        OutlinedTextEdit(
            modifier = editTextModifier,
            label = { Text("uid") },
            text = profile.uid.toString(),
            readOnly = readOnly,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            validator = { if (isTextValidUid(it)) null else "Invalid UID" }) {
            val uidVal = if (it.isEmpty()) 0 else it.toInt()
            handleProfileChange(profile.copy(uid = uidVal, rootUseDefault = false))
        }

        // GID Input
        OutlinedTextEdit(
            modifier = editTextModifier,
            label = { Text("gid") },
            text = profile.gid.toString(),
            readOnly = readOnly,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            validator = { if (isTextValidUid(it)) null else "Invalid GID" }) {
            val gidVal = if (it.isEmpty()) 0 else it.toInt()
            handleProfileChange(profile.copy(gid = gidVal, rootUseDefault = false))
        }

        val selectedGroups = profile.groups.ifEmpty { listOf(0) }.let { e ->
            e.mapNotNull { g ->
                Groups.entries.find { it.gid == g }
            }
        }

        GroupsPanel(selectedGroups, readOnly) {
            handleProfileChange(
                profile.copy(
                    groups = it.map { group -> group.gid }.ifEmpty { listOf(0) }, rootUseDefault = false
                )
            )
        }

        val selectedCaps = profile.capabilities.mapNotNull { e ->
            Capabilities.entries.find { it.cap == e }
        }

        CapsPanel(selectedCaps, readOnly) {
            handleProfileChange(
                profile.copy(
                    capabilities = it.map { cap -> cap.cap }, rootUseDefault = false
                )
            )
        }

        NamespaceDropdown(profile.namespace, readOnly = readOnly, onNamespaceChange = { handleProfileChange(profile.copy(namespace = it)) })

        SELinuxPanel(profile = profile, readOnly = readOnly, onSELinuxChange = { domain, rules ->
            handleProfileChange(
                profile.copy(
                    context = domain, rules = rules, rootUseDefault = false
                )
            )
        })
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GroupsPanel(selected: List<Groups>, readOnly: Boolean, closeSelection: (selection: Set<Groups>) -> Unit) {
    val scope = rememberCoroutineScope()
    val jumpIndex = remember { mutableIntStateOf(-1) }
    val allOptions = remember(selected) {
        Groups.entries.sortedWith(compareBy<Groups> { if (selected.contains(it)) 0 else 1 }.then(compareBy {
            when (it) {
                Groups.ROOT -> 0
                Groups.SYSTEM -> 1
                Groups.SHELL -> 2
                else -> Int.MAX_VALUE
            }
        }).thenBy { it.name }).map { group ->
            ListOption(
                titleText = group.display, subtitleText = group.desc, data = group
            )
        }
    }
    val initialSelectionOptions = remember(allOptions, selected) {
        allOptions.filter { it.data in selected }
    }
    val selectGroupsDialog = rememberCustomDialog { dismiss: () -> Unit ->
        val jumpTarget = remember(initialSelectionOptions) {
            val index = jumpIndex.intValue
            if (index < 0 || index >= initialSelectionOptions.size) null else initialSelectionOptions[jumpIndex.intValue]
        }
        MultiSelectSearchBottomSheet(
            title = stringResource(R.string.profile_groups),
            options = allOptions,
            maxSelection = 32, // Kernel only supports 32 groups at most
            readOnly = readOnly,
            initialSelection = initialSelectionOptions,
            scrollToItem = jumpTarget,
            onDismissRequest = { dismiss() },
            onConfirm = { resultOptions ->
                scope.launch {
                    val newSelection = resultOptions.mapNotNull { it.data as? Groups }.toHashSet()
                    closeSelection(newSelection)
                }
            })
    }
    MultiSelectionCard(title = stringResource(R.string.profile_groups), items = initialSelectionOptions.map { it.titleText }, onClick = { index ->
        jumpIndex.intValue = index ?: -1
        selectGroupsDialog.show()
    })
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CapsPanel(
    selected: Collection<Capabilities>, readOnly: Boolean, closeSelection: (selection: Set<Capabilities>) -> Unit
) {
    val scope = rememberCoroutineScope()
    val jumpIndex = remember { mutableIntStateOf(-1) }
    val allOptions = remember(selected) {
        Capabilities.entries.sortedWith(compareBy<Capabilities> { if (selected.contains(it)) 0 else 1 }.thenBy { it.name }).map { cap ->
            ListOption(
                titleText = cap.display, subtitleText = cap.desc, data = cap
            )
        }
    }
    val initialSelectionOptions = remember(allOptions, selected) {
        allOptions.filter { it.data in selected }
    }
    val jumpTarget = remember(allOptions, initialSelectionOptions) {
        val index = jumpIndex.intValue
        if (index < 0 || index >= allOptions.size) null else allOptions[jumpIndex.intValue]
    }
    val selectCapsBottomSheet = rememberCustomDialog { dismiss ->
        MultiSelectSearchBottomSheet(
            title = stringResource(R.string.profile_capabilities),
            options = allOptions,
            readOnly = readOnly,
            initialSelection = initialSelectionOptions,
            scrollToItem = jumpTarget,
            onDismissRequest = dismiss,
            onConfirm = { resultOptions ->
                scope.launch {
                    val newSelection = resultOptions.mapNotNull { it.data as? Capabilities }.toHashSet()
                    closeSelection(newSelection)
                }
            })
    }

    MultiSelectionCard(title = stringResource(R.string.profile_capabilities), items = initialSelectionOptions.map { it.titleText }, onClick = { index ->
        jumpIndex.intValue = index ?: -1
        selectCapsBottomSheet.show()
    })
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SELinuxPanel(
    profile: Natives.Profile, readOnly: Boolean = false, onSELinuxChange: (domain: String, rules: String) -> Unit
) {
    val scope = rememberCoroutineScope()
    
    val editSELinuxBottomSheet = rememberCustomDialog { dismiss ->
        SeLinuxEditBottomSheet(
            readOnly = readOnly,
            onDismissRequest = dismiss,
            onSELinuxChange = { domain, rules ->
                scope.launch {
                    onSELinuxChange(domain, rules)
                }
            },
            seLinuxContext = profile.context,
            seLinuxRules = profile.rules
        )
    }


    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                editSELinuxBottomSheet.show()
            },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        label = { Text(text = stringResource(R.string.profile_selinux_context)) },
        value = profile.context,
        supportingText = { /*placeholder keep same height*/ },
        onValueChange = { })
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MultiSelectionCard(
    modifier: Modifier = Modifier, title: String, items: Collection<String>, onClick: (Int?) -> Unit
) {
    val motionScheme = MaterialTheme.motionScheme
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(motionScheme.defaultSpatialSpec())
            .padding(top = 9.dp, bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = { onClick(null) })
                .padding(16.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            if (items.isEmpty()) {
                return@Column
            }
            Spacer(Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                    items.forEachIndexed { index, item ->
                        AssistChip(onClick = { onClick(index) }, label = { Text(item) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NamespaceDropdown(
    currentNamespace: Int, readOnly: Boolean, onNamespaceChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val setExpanded: (Boolean) -> Unit = { expanded = it }
    val nameSpaceItems = mapOf(
        Natives.Profile.Namespace.INHERITED.ordinal to R.string.profile_namespace_inherited,
        Natives.Profile.Namespace.GLOBAL.ordinal to R.string.profile_namespace_global,
        Natives.Profile.Namespace.INDIVIDUAL.ordinal to R.string.profile_namespace_individual
    )

    val namespaceText = stringResource(nameSpaceItems[currentNamespace] ?: R.string.profile_namespace_inherited)


    ExposedDropdownMenuBox(
        expanded = expanded, onExpandedChange = { if (!readOnly) setExpanded(!expanded) }) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            readOnly = true,
            label = { Text(stringResource(R.string.profile_namespace)) },
            value = namespaceText,
            onValueChange = {},
            supportingText = { /*placeholder keep same height*/ },
            trailingIcon = {
                if (!readOnly) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        if (!readOnly) {
            ExposedDropdownMenu(
                expanded = expanded, onDismissRequest = { setExpanded(false) }) {
                nameSpaceItems.forEach { (ns, resId) ->
                    DropdownMenuItem(text = { Text(stringResource(resId)) }, onClick = {
                        onNamespaceChange(ns)
                        setExpanded(false)
                    })
                }
            }
        }
    }
}


@Preview
@Composable
private fun RootProfileConfigPreview() {
    var profile by remember { mutableStateOf(Natives.Profile("")) }
    val setProfile: (Natives.Profile) -> Unit = { profile = it }
    RootProfileConfig(profile = profile) {
        setProfile(it)
    }
}

private fun isTextValidUid(text: String): Boolean {
    return try {
        text.isNotEmpty() && text.isDigitsOnly() && text.toInt() >= 0
    } catch (_: NumberFormatException) {
        false
    }
}
