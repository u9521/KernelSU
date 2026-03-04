package me.weishu.kernelsu.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ExpandedFullScreenContainedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarScrollBehavior
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberContainedSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * A wrapper around Material 3 Expressive Search Bar components.
 *
 * @param searchStatus The state holder.
 * @param onSearchStatusChange Callback to update state.
 * @param scrollBehavior Pass `TopAppBarDefaults.enterAlwaysScrollBehavior()` or similar from the parent Scaffold.
 * @param defaultContent Shown when ResultStatus.DEFAULT (optional).
 * @param emptyContent Shown when ResultStatus.EMPTY (optional).
 * @param loadingContent Shown when ResultStatus.LOAD (optional).
 * @param content The result list shown when ResultStatus.SHOW.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    searchStatus: SearchStatus,
    onSearchStatusChange: (SearchStatus) -> Unit,
    scrollBehavior: SearchBarScrollBehavior,
    defaultContent: @Composable (() -> Unit)? = null,
    emptyContent: @Composable (() -> Unit)? = null,
    loadingContent: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val ime = LocalSoftwareKeyboardController.current
    val currentSearchStatus by rememberUpdatedState(searchStatus)
    val currentOnSearchStatusChange by rememberUpdatedState(onSearchStatusChange)

    val searchBarState = rememberContainedSearchBarState()
    val textFieldState = rememberTextFieldState(searchStatus.searchText)

    val appBarWithSearchColors = SearchBarDefaults.appBarWithSearchColors(
        searchBarColors = SearchBarDefaults.containedColors(state = searchBarState),
        scrolledAppBarContainerColor = Color.Transparent,
        appBarContainerColor = Color.Transparent,
    )

    var searchBarHeightPx by remember { mutableIntStateOf(0) }

    LaunchedEffect(scrollBehavior, density) {
        snapshotFlow {
            val visibleHeightPx = (searchBarHeightPx + scrollBehavior.scrollOffset).coerceAtLeast(0f)
            with(density) { visibleHeightPx.toDp() }
        }
            .distinctUntilChanged()
            .collectLatest { heightDp ->
                if (currentSearchStatus.offsetY != heightDp) {
                    currentOnSearchStatusChange(currentSearchStatus.copy(offsetY = heightDp))
                }
            }
    }


    // State Synchronization
    // SearchStatus (External) -> SearchBarState (Internal)
    LaunchedEffect(searchStatus.current) {
        if (searchStatus.isExpand() && searchBarState.currentValue == SearchBarValue.Collapsed) {
            searchBarState.animateToExpanded()
        } else if (searchStatus.isCollapsed() && searchBarState.currentValue == SearchBarValue.Expanded) {
            searchBarState.animateToCollapsed()
            currentOnSearchStatusChange(currentSearchStatus.copy(searchText = ""))
        }
    }

    // SearchStatus.searchText (External) -> TextFieldState (Internal)
    LaunchedEffect(searchStatus.searchText) {
        if (textFieldState.text.toString() != searchStatus.searchText) {
            textFieldState.setTextAndPlaceCursorAtEnd(searchStatus.searchText)
        }
    }

    // TextFieldState (Internal) -> SearchStatus.searchText (External)
    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text }
            .distinctUntilChanged()
            .collectLatest { newText ->
                if (newText.toString() != currentSearchStatus.searchText) {
                    currentOnSearchStatusChange(currentSearchStatus.copy(searchText = newText.toString()))
                }
            }
    }

    // SearchBarState (Internal) -> SearchStatus.current (External)
    LaunchedEffect(searchBarState.currentValue) {
        val newStatus = when (searchBarState.currentValue) {
            SearchBarValue.Expanded -> SearchStatus.Status.EXPANDED
            SearchBarValue.Collapsed -> SearchStatus.Status.COLLAPSED
        }
        if (currentSearchStatus.current != newStatus) {
            val text = if (newStatus == SearchStatus.Status.COLLAPSED) "" else currentSearchStatus.searchText
            currentOnSearchStatusChange(currentSearchStatus.copy(current = newStatus, searchText = text))
        }
    }

    // Input Field
    val inputField = @Composable {
        SearchBarDefaults.InputField(
            modifier = Modifier.fillMaxWidth(),
            textFieldState = textFieldState,
            searchBarState = searchBarState,
            colors = appBarWithSearchColors.searchBarColors.inputFieldColors,
            onSearch = { ime?.hide() },
            placeholder = { Text(text = searchStatus.label) },
            leadingIcon = {
                if (searchBarState.currentValue == SearchBarValue.Expanded) {
                    IconButton(onClick = {

                        onSearchStatusChange(
                            searchStatus.copy(searchText = "", current = SearchStatus.Status.COLLAPSED)
                        )
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(android.R.string.cancel),
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                    )
                }
            },
            trailingIcon = {
                AnimatedVisibility(
                    visible = textFieldState.text.isNotEmpty(),
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                ) {
                    IconButton({
                        onSearchStatusChange(searchStatus.copy(searchText = ""))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clean",
                        )
                    }
                }
            },
        )
    }

    // Layout
    AppBarWithSearch(
        windowInsets = SearchBarDefaults.windowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
        scrollBehavior = scrollBehavior,
        modifier = modifier.onSizeChanged { searchBarHeightPx = it.height },
        state = searchBarState,
        colors = appBarWithSearchColors,
        inputField = inputField,
    )

    ExpandedFullScreenContainedSearchBar(
        state = searchBarState,
        inputField = inputField,
        colors = appBarWithSearchColors.searchBarColors.copy(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        when (searchStatus.resultStatus) {
            SearchStatus.ResultStatus.DEFAULT -> defaultContent?.let { it() }
            SearchStatus.ResultStatus.EMPTY -> emptyContent?.let { it() }
            SearchStatus.ResultStatus.LOAD -> loadingContent?.let { it() } ?: content()
            SearchStatus.ResultStatus.SHOW -> content()
        }
    }
}

