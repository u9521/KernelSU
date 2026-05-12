package me.weishu.kernelsu.ui.component.material

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ExpandedFullScreenContainedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarColors
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarScrollBehavior
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Surface
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberContainedSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import me.weishu.kernelsu.ui.component.breeze.BreezeSnackBarHost
import me.weishu.kernelsu.ui.theme.LocalEnableBlur
import me.weishu.kernelsu.ui.util.LocalSnackbarHost
import me.weishu.kernelsu.ui.util.blurOverlay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SearchAppBarBreeze(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    subtitle: (@Composable () -> Unit)? = null,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onClearClick: () -> Unit,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    searchBarScrollBehavior: SearchBarScrollBehavior? = null,
    searchContent: @Composable BoxScope.(bottomPadding: Dp, closeSearch: () -> Unit) -> Unit = { _, _ -> }
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }

    val scope = rememberCoroutineScope()
    val searchBarState = rememberContainedSearchBarState()
    val textFieldState = rememberTextFieldState()
    val currentQuery = textFieldState.text.toString()
    val isSearchExpanded = searchBarState.currentValue != SearchBarValue.Collapsed || searchBarState.targetValue != SearchBarValue.Collapsed
    var previousSearchBarValue by remember { mutableStateOf(searchBarState.currentValue) }
    val clearSearchText: () -> Unit = {
        textFieldState.setTextAndPlaceCursorAtEnd("")
        onClearClick()
    }
    val collapseAndClear: () -> Unit = {
        clearSearchText()
        scope.launch { searchBarState.animateToCollapsed() }
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    DisposableEffect(Unit) {
        onDispose {
            keyboardController?.hide()
        }
    }

    LaunchedEffect(searchText) {
        val current = textFieldState.text.toString()
        if (current != searchText) {
            textFieldState.setTextAndPlaceCursorAtEnd(searchText)
        }
    }

    LaunchedEffect(textFieldState, searchText) {
        snapshotFlow { textFieldState.text.toString() }
            .distinctUntilChanged()
            .collect { value ->
                if (value != searchText) {
                    onSearchTextChange(value)
                }
            }
    }

    LaunchedEffect(searchBarState) {
        snapshotFlow { searchBarState.currentValue }
            .distinctUntilChanged()
            .collect { value ->
                val collapsedFromExpanded =
                    previousSearchBarValue != SearchBarValue.Collapsed && value == SearchBarValue.Collapsed
                previousSearchBarValue = value
                if (collapsedFromExpanded) {
                    clearSearchText()
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            }
    }

    val blurEnabled = LocalEnableBlur.current
    // Keep color animation in sync with top app bar collapse progress.
    val collapseFraction = (scrollBehavior?.state?.collapsedFraction ?: 0f).coerceIn(0f, 1f)
    val easedCollapseFraction = FastOutLinearInEasing.transform(collapseFraction)
    // In blur mode the backdrop is handled by haze, so app bar layers stay transparent.
    val topBarBackgroundColor = if (blurEnabled) {
        Color.Transparent
    } else {
        lerp(
            start = MaterialTheme.colorScheme.surfaceContainer,
            stop = MaterialTheme.colorScheme.surfaceBright,
            fraction = easedCollapseFraction,
        )
    }
    val containedSearchBarColors = SearchBarDefaults.containedColors(state = searchBarState)
    // Input field keeps a readable surface; blur mode gradually increases transparency while collapsing.
    val appBarInputFieldContainerColors = if (blurEnabled) {
        val background = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 1f - (0.5f * easedCollapseFraction))
        containedSearchBarColors.inputFieldColors.copy(
            focusedContainerColor = background,
            unfocusedContainerColor = background,
            disabledContainerColor = background
        )
    } else {
        containedSearchBarColors.inputFieldColors.copy(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }
    // Force AppBarWithSearch internal containers transparent; background is drawn by parent Column.
    val transparentAppBarWithSearchColors = SearchBarDefaults.appBarWithSearchColors(
        searchBarColors = containedSearchBarColors.copy(
            containerColor = Color.Transparent,
            dividerColor = Color.Transparent,
        ),
        scrolledSearchBarContainerColor = Color.Transparent,
        appBarContainerColor = Color.Transparent,
        scrolledAppBarContainerColor = Color.Transparent,
    )
    // Use dedicated full-screen colors so expanded search has a stable, non-transparent surface.
    val fullScreenSearchBarColors = SearchBarColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        dividerColor = Color.Transparent,
        inputFieldColors = appBarInputFieldContainerColors.copy(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    )

    val inputField: @Composable (TextFieldColors) -> Unit = { textFieldColors ->
        SearchBarDefaults.InputField(
            modifier = Modifier.fillMaxWidth(),
            textFieldState = textFieldState,
            searchBarState = searchBarState,
            colors = textFieldColors,
            onSearch = {
                focusManager.clearFocus()
                keyboardController?.hide()
            },
            leadingIcon = {
                if (isSearchExpanded) {
                    IconButton(
                        onClick = { collapseAndClear() },
                        content = { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                    )
                } else {
                    Icon(Icons.Filled.Search, null)
                }
            },
            trailingIcon = {
                if (isSearchExpanded && currentQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { clearSearchText() },
                        content = { Icon(Icons.Filled.Close, null) }
                    )
                }
            },
            interactionSource = interactionSource
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(topBarBackgroundColor)
    ) {
        LargeFlexibleTopAppBar(
            title = title,
            subtitle = subtitle,
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent),
            navigationIcon = { if (navigationIcon != null) navigationIcon() },
            actions = { if (actions != null) actions() },
            windowInsets = TopAppBarDefaults.windowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
            scrollBehavior = scrollBehavior
        )

        AppBarWithSearch(
            state = searchBarState,
            inputField = { inputField(appBarInputFieldContainerColors) },
            colors = transparentAppBarWithSearchColors,
            scrollBehavior = searchBarScrollBehavior,
            windowInsets = SearchBarDefaults.windowInsets.only(WindowInsetsSides.Horizontal),
            contentPadding = PaddingValues(horizontal = 8.dp)
        )
    }
    ExpandedFullScreenContainedSearchBar(
        modifier = Modifier.blurOverlay(),
        state = searchBarState,
        collapsedShape = RectangleShape,
        inputField = {
            inputField(fullScreenSearchBarColors.inputFieldColors)
        },
        windowInsets = { SearchBarDefaults.fullScreenWindowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal) },
        colors = fullScreenSearchBarColors,
        content = {
            val snackBarHostState = LocalSnackbarHost.current
            val bottomPadding = SearchBarDefaults.fullScreenWindowInsets.asPaddingValues().calculateBottomPadding()
            Box(Modifier.fillMaxSize()) {
                Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
                    if (currentQuery.isNotEmpty()) {
                        searchContent(bottomPadding, collapseAndClear)
                    }
                }
                BreezeSnackBarHost(
                    hostState = snackBarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .safeContentPadding()
                )
            }
        })
}
