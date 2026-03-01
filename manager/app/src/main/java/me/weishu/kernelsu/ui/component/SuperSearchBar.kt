package me.weishu.kernelsu.ui.component

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.ui.theme.defaultTopAppBarColors

// Search Status Class
@Stable
data class SearchStatus(
    val label: String,
    val searchText: String = "",
    val current: Status = Status.COLLAPSED,
    val offsetY: Dp = 0.dp,
    val resultStatus: ResultStatus = ResultStatus.DEFAULT
) {
    fun isExpand() = current == Status.EXPANDED
    fun isCollapsed() = current == Status.COLLAPSED
    fun shouldExpand() = current == Status.EXPANDED || current == Status.EXPANDING
    fun shouldCollapsed() = current == Status.COLLAPSED || current == Status.COLLAPSING
    fun isAnimatingExpand() = current == Status.EXPANDING

    // 动画完成回调
    fun onAnimationComplete(): SearchStatus {
        return when (current) {
            Status.EXPANDING -> copy(current = Status.EXPANDED)
            Status.COLLAPSING -> copy(searchText = "", current = Status.COLLAPSED)
            else -> this
        }
    }

    enum class Status { EXPANDED, EXPANDING, COLLAPSED, COLLAPSING }
    enum class ResultStatus { DEFAULT, EMPTY, LOAD, SHOW }
}


private const val TAG = "SearchBar"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SearchAppBar(
    title: @Composable () -> Unit,
    searchStatus: SearchStatus,
    onBackClick: (() -> Unit)? = null,
    onConfirm: (() -> Unit)? = null,
    onSearchStatusChange: (SearchStatus) -> Unit,
    dropdownContent: @Composable (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var onSearch by remember { mutableStateOf(searchStatus.searchText != "") }

    if (onSearch) {
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }
    DisposableEffect(Unit) {
        onDispose {
            keyboardController?.hide()
        }
    }

    LargeFlexibleTopAppBar(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        colors = defaultTopAppBarColors(),
        title = {
            Box {
                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.CenterStart),
                    visible = !onSearch,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    content = { title() }
                )

                AnimatedVisibility(
                    visible = onSearch,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp, bottom = 2.dp, end = if (onBackClick != null) 0.dp else 14.dp)
                            .focusRequester(focusRequester)
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) onSearch = true
                                Log.d(TAG, "onFocusChanged: $focusState")
                            },
                        value = searchStatus.searchText,
                        onValueChange = { onSearchStatusChange(searchStatus.copy(searchText = it)) },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    onSearch = false
                                    keyboardController?.hide()
                                    onSearchStatusChange(searchStatus.copy(searchText = ""))
                                },
                                content = { Icon(Icons.Filled.Close, null) }
                            )
                        },
                        maxLines = 1,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                            onConfirm?.invoke()
                        })
                    )
                }
            }
        },
        navigationIcon = {
            val isNavIconVisible = onBackClick != null
            AnimatedVisibility(
                visible = isNavIconVisible, enter = fadeIn(animationSpec = motionScheme.defaultEffectsSpec()) + expandHorizontally(
                    expandFrom = Alignment.Start, animationSpec = motionScheme.defaultEffectsSpec()
                ), exit = fadeOut(animationSpec = motionScheme.defaultEffectsSpec()) + shrinkHorizontally(
                    shrinkTowards = Alignment.Start, animationSpec = motionScheme.defaultEffectsSpec()
                )
            ) {
                IconButton(
                    onClick = onBackClick ?: {},
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            }
        },
        actions = {
            AnimatedVisibility(
                visible = !onSearch
            ) {
                IconButton(
                    onClick = { onSearch = true },
                    content = { Icon(Icons.Filled.Search, null) }
                )
            }

            if (dropdownContent != null) {
                dropdownContent()
            }

        },
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        scrollBehavior = scrollBehavior
    )
}
