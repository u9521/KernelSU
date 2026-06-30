package me.weishu.kernelsu.ui.component.profile

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
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
import me.weishu.kernelsu.toRawFlags
import me.weishu.kernelsu.toRootProfileFlags
import me.weishu.kernelsu.ui.component.breeze.MultiSelectBottomSheet
import me.weishu.kernelsu.ui.component.breeze.OutlinedTextEdit
import me.weishu.kernelsu.ui.component.breeze.PopupFeedBack
import me.weishu.kernelsu.ui.component.breeze.SeLinuxEditBottomSheet

@Composable
fun RootProfileConfigBreeze(
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    profile: Natives.Profile,
    onProfileChange: (Natives.Profile) -> Unit
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

        GroupsPanelBreeze(selectedGroups, readOnly) {
            handleProfileChange(
                profile.copy(
                    groups = it.map { group -> group.gid }.ifEmpty { listOf(0) }, rootUseDefault = false
                )
            )
        }

        val selectedCaps = profile.capabilities.mapNotNull { e ->
            Capabilities.entries.find { it.cap == e }
        }

        CapsPanelBreeze(selectedCaps, readOnly) {
            handleProfileChange(
                profile.copy(
                    capabilities = it.map { cap -> cap.cap }, rootUseDefault = false
                )
            )
        }

        FlagPanelBreeze(
            readOnly = readOnly,
            selected = profile.flags.toRootProfileFlags(),
            closeSelection = {
                onProfileChange(
                    profile.copy(
                        flags = it.toRawFlags(),
                    )
                )
            }
        )

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

@Composable
private fun GroupsPanelBreeze(selected: List<Groups>, readOnly: Boolean, closeSelection: (selection: Set<Groups>) -> Unit) {
    val scope = rememberCoroutineScope()
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    val jumpIndex = remember { mutableIntStateOf(-1) }
    val allOptions = remember(selected) {
        Groups.entries.sortedWith(compareBy<Groups> { if (selected.contains(it)) 0 else 1 }.then(compareBy {
            when (it) {
                Groups.ROOT -> 0
                Groups.SYSTEM -> 1
                Groups.SHELL -> 2
                else -> Int.MAX_VALUE
            }
        }).thenBy { it.name })
    }
    if (showBottomSheet) {
        val jumpTarget = remember(selected) {
            val index = jumpIndex.intValue
            if (index < 0 || index >= selected.size) null else selected[jumpIndex.intValue]
        }
        MultiSelectBottomSheet(
            title = stringResource(R.string.profile_groups),
            options = allOptions,
            maxSelection = 32, // Kernel only supports 32 groups at most
            readOnly = readOnly,
            initialSelection = selected,
            scrollToItem = jumpTarget,
            onDismissRequest = { showBottomSheet = false },
            onConfirm = { groups ->
                scope.launch {
                    closeSelection(groups)
                }
            },
            optionTitle = { it.display },
            optionSubtitle = { it.desc }
        )
    }
    MultiSelectionCard(title = stringResource(R.string.profile_groups), items = selected.map { it.display }, onClick = { index ->
        jumpIndex.intValue = index ?: -1
        showBottomSheet = true
    })
    showBottomSheet //make lsp happy
}

@Composable
private fun CapsPanelBreeze(
    selected: List<Capabilities>, readOnly: Boolean, closeSelection: (selection: Set<Capabilities>) -> Unit
) {
    val scope = rememberCoroutineScope()
    val jumpIndex = remember { mutableIntStateOf(-1) }
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    val allOptions = remember(selected) {
        Capabilities.entries.sortedWith(compareBy<Capabilities> { if (selected.contains(it)) 0 else 1 }.thenBy { it.name })
    }
    if (showBottomSheet) {
        val jumpTarget = remember(allOptions, selected) {
            val index = jumpIndex.intValue
            if (index < 0 || index >= selected.size) null else selected[jumpIndex.intValue]
        }
        PopupFeedBack()
        MultiSelectBottomSheet(
            title = stringResource(R.string.profile_capabilities),
            options = allOptions,
            readOnly = readOnly,
            initialSelection = selected,
            scrollToItem = jumpTarget,
            onDismissRequest = { showBottomSheet = false },
            onConfirm = { caps ->
                scope.launch {
                    closeSelection(caps)
                }
            },
            optionTitle = { it.display },
            optionSubtitle = { it.desc }
        )
    }

    MultiSelectionCard(title = stringResource(R.string.profile_capabilities), items = selected.map { it.display }, onClick = { index ->
        jumpIndex.intValue = index ?: -1
        showBottomSheet = true
    })
    showBottomSheet //make lsp happy
}

@Composable
private fun FlagPanelBreeze(
    selected: List<Natives.Profile.RootProfileFlag>, readOnly: Boolean, closeSelection: (List<Natives.Profile.RootProfileFlag>) -> Unit
) {
    val scope = rememberCoroutineScope()
    val res = LocalResources.current
    val jumpIndex = remember { mutableIntStateOf(-1) }
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    val allOptions = remember(selected) {
        Natives.Profile.RootProfileFlag.entries.sortedWith(compareBy<Natives.Profile.RootProfileFlag> { if (selected.contains(it)) 0 else 1 }.thenBy { it.name })
    }
    if (showBottomSheet) {
        val jumpTarget = remember(allOptions, selected) {
            val index = jumpIndex.intValue
            if (index < 0 || index >= selected.size) null else selected[jumpIndex.intValue]
        }
        PopupFeedBack()
        MultiSelectBottomSheet(
            title = stringResource(R.string.profile_flags),
            options = allOptions,
            readOnly = readOnly,
            initialSelection = selected,
            scrollToItem = jumpTarget,
            onDismissRequest = { showBottomSheet = false },
            onConfirm = { caps ->
                scope.launch {
                    closeSelection(caps.toList())
                }
            },
            optionTitle = { it.display },
            optionSubtitle = { res.getString(it.desc) }
        )
    }

    MultiSelectionCard(title = stringResource(R.string.profile_flags), items = selected.map { it.display }, onClick = { index ->
        jumpIndex.intValue = index ?: -1
        showBottomSheet = true
    })
    showBottomSheet //make lsp happy
}

@Composable
private fun SELinuxPanel(
    profile: Natives.Profile, readOnly: Boolean = false, onSELinuxChange: (domain: String, rules: String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    if (showBottomSheet) {
        PopupFeedBack()
        SeLinuxEditBottomSheet(
            readOnly = readOnly,
            onDismissRequest = { showBottomSheet = false },
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
                showBottomSheet = true
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
    showBottomSheet
}

@Composable
fun MultiSelectionCard(
    modifier: Modifier = Modifier, title: String, items: Collection<String>, onClick: (Int?) -> Unit
) {
    val motionScheme = MaterialTheme.motionScheme
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(motionScheme.defaultSpatialSpec())
            .padding(top = 9.dp, bottom = 16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.Transparent
        ),
        shape = MaterialTheme.shapes.extraSmall,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = { onClick(null) })
                .padding(16.dp)
        ) {
            Text(text = title)
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

@Composable
private fun NamespaceDropdown(
    currentNamespace: Int, readOnly: Boolean, onNamespaceChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val setExpanded: (Boolean) -> Unit = { expanded = it }
    val nameSpaceItems = mapOf(
        Natives.Profile.Namespace.INHERITED.ordinal to R.string.profile_namespace_inherited,
        Natives.Profile.Namespace.GLOBAL.ordinal to R.string.profile_namespace_global,
        Natives.Profile.Namespace.INDIVIDUAL.ordinal to R.string.profile_namespace_individual
    )
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "SelectTemplateRotation"
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
                if (readOnly) return@OutlinedTextField
                Icon(
                    modifier = Modifier.rotate(rotationState),
                    painter = painterResource(R.drawable.ic_keyboard_arrow_down_rounded),
                    contentDescription = null
                )
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        if (!readOnly) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { setExpanded(false) },
                shape = MenuDefaults.standaloneGroupShape,
                containerColor = MenuDefaults.groupStandardContainerColor
            ) {
                PopupFeedBack()
                nameSpaceItems.onEachIndexed { index, (ns, resId) ->
                    DropdownMenuItem(
                        text = { Text(stringResource(resId)) },
                        shapes = MenuDefaults.itemShape(index, nameSpaceItems.size),
                        selected = currentNamespace == ns,
                        selectedLeadingIcon = {
                            Icon(
                                Icons.Filled.Check,
                                modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                                contentDescription = null,
                            )
                        },
                        onClick = {
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
private fun RootProfileConfigBreezePreview() {
    var profile by remember { mutableStateOf(Natives.Profile("")) }
    val setProfile: (Natives.Profile) -> Unit = { profile = it }
    RootProfileConfigBreeze(profile = profile) {
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
