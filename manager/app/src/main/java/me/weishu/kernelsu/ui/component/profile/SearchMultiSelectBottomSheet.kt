package me.weishu.kernelsu.ui.component.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.weishu.kernelsu.R
import me.weishu.kernelsu.profile.Capabilities


data class ListOption(
    val titleText: String, val subtitleText: String? = null, val data: Any? = null
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MultiSelectSearchBottomSheet(
    title: String,
    options: List<ListOption>,
    initialSelection: List<ListOption>,
    maxSelection: Int = Int.MAX_VALUE,
    readOnly: Boolean = false,
    scrollToItem: ListOption? = null,
    onDismissRequest: () -> Unit,
    onConfirm: (Set<ListOption>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val selectedItems = remember { mutableStateListOf(*initialSelection.toTypedArray()) }
    var searchQuery by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    val filteredList = remember(options, searchQuery) {
        if (searchQuery.isBlank()) options
        else options.filter {
            it.titleText.contains(searchQuery, ignoreCase = true) ||
                    (it.subtitleText?.contains(searchQuery, ignoreCase = true) == true)
        }
    }

    val isLimitEnabled = maxSelection < Int.MAX_VALUE

    val isAllFilteredSelected = remember(selectedItems.size, filteredList) {
        filteredList.isNotEmpty() && selectedItems.containsAll(filteredList)
    }

    LaunchedEffect(scrollToItem) {
        if (scrollToItem != null && searchQuery.isEmpty()) {
            val index = options.indexOf(scrollToItem)
            if (index >= 0) {
                listState.scrollToItem(index)
            }
        }
    }

    fun closeSheet() {
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismissRequest()
            }
        }
    }

    ModalBottomSheet(
        modifier = Modifier.padding(WindowInsets.statusBars.only(WindowInsetsSides.Top + WindowInsetsSides.Bottom).asPaddingValues()),
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(left = 16.dp, right = 16.dp).union(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)) },
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
            )

            // Buttons Area
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val showToggleButton: Boolean
                val isDeselectMode: Boolean

                if (isLimitEnabled) {
                    showToggleButton = selectedItems.isNotEmpty()
                    isDeselectMode = true
                } else {
                    showToggleButton = true
                    isDeselectMode = isAllFilteredSelected
                }

                if (showToggleButton) {
                    TextButton(enabled = !readOnly, onClick = {
                        if (isDeselectMode) {
                            selectedItems.clear()
                        } else {
                            val itemsToAdd = filteredList.filter { !selectedItems.contains(it) }
                            selectedItems.addAll(itemsToAdd)
                        }
                    }) {
                        val actionText = if (isDeselectMode) R.string.deselect_all else R.string.select_all
                        Text(stringResource(actionText, "${selectedItems.size}"))
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Confirm Button
                Button(
                    enabled = !isLimitEnabled || selectedItems.size <= maxSelection,
                    shapes = ButtonDefaults.shapes(),
                    onClick = {
                        if (readOnly) {
                            closeSheet()
                        } else {
                            onConfirm(selectedItems.toSet())
                            closeSheet()
                        }
                    }
                ) {
                    Text(stringResource(if (readOnly) R.string.close else android.R.string.ok))
                }
            }
        }

        // Search box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.search)) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search))
            },
            trailingIcon = {
                val motionScheme = MaterialTheme.motionScheme
                AnimatedVisibility(
                    visible = searchQuery.isNotEmpty(),
                    enter = fadeIn(motionScheme.defaultEffectsSpec()) + scaleIn(motionScheme.defaultSpatialSpec()),
                    exit = fadeOut(motionScheme.defaultEffectsSpec()) + scaleOut(motionScheme.defaultSpatialSpec())
                ) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // List
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f, fill = true)
        ) {
            items(filteredList, key = { it.titleText + (it.subtitleText ?: "") }) { item ->
                val isSelected = selectedItems.contains(item)
                val canToggle = isSelected || (!isLimitEnabled || selectedItems.size < maxSelection)
                val textAlpha = if (canToggle) 1f else 0.38f
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(enabled = canToggle && !readOnly) {
                            if (isSelected) {
                                selectedItems.remove(item)
                            } else {
                                if (!isLimitEnabled || selectedItems.size < maxSelection) {
                                    selectedItems.add(item)
                                }
                            }
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSelected,
                        enabled = canToggle && !readOnly,
                        onCheckedChange = { checked ->
                            if (checked) {
                                if (!isLimitEnabled || selectedItems.size < maxSelection) {
                                    selectedItems.add(item)
                                }
                            } else {
                                selectedItems.remove(item)
                            }
                        }
                    )
                    Column(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .weight(1f)
                    ) {
                        Text(
                            text = item.titleText,
                            style = MaterialTheme.typography.labelLarge,
                            color = LocalContentColor.current.copy(alpha = textAlpha)
                        )
                        if (!item.subtitleText.isNullOrEmpty()) {
                            Text(
                                text = item.subtitleText,
                                style = MaterialTheme.typography.bodySmall,
                                color = LocalContentColor.current.copy(alpha = textAlpha)
                            )
                        }
                    }
                }
            }
        }
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.no_result), color = Color.Gray)
            }
        }
    }
}

@Preview
@Composable
private fun MultiSelectSearchBottomSheetPreview() {
    val allOptions = remember {
        Capabilities.entries
            .sortedBy { it.name }
            .map { cap ->
                ListOption(
                    titleText = cap.display,
                    subtitleText = cap.desc,
                    data = cap
                )
            }
    }
    MultiSelectSearchBottomSheet(
        stringResource(R.string.profile_capabilities), options = allOptions, initialSelection = emptyList(),
        onDismissRequest = {}
    ) {}
}