package me.weishu.kernelsu.ui.playground.searchbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import me.weishu.kernelsu.ui.UiMode
import me.weishu.kernelsu.ui.component.material.SearchAppBarBreeze
import me.weishu.kernelsu.ui.theme.KernelSUTheme
import me.weishu.kernelsu.ui.theme.LocalEnableBlur
import me.weishu.kernelsu.ui.util.LocalBlurController
import me.weishu.kernelsu.ui.util.onlyHorizontal
import me.weishu.kernelsu.ui.util.rememberBlurController
import me.weishu.kernelsu.ui.util.topBarHazeEffect
import me.weishu.kernelsu.ui.util.windowBlurBehind

private val sampleItems = List(30) { "Result item #${it + 1}" }

private data class PageItem(val title: String, val subtitle: String, val icon: ImageVector)

private val pageItems = listOf(
    PageItem("Getting Started", "Learn the basics of the app", Icons.Filled.Info),
    PageItem("Configuration", "Adjust your preferences and settings", Icons.Filled.Settings),
    PageItem("Favorites", "View and manage your saved items", Icons.Filled.Star),
    PageItem("Advanced Features", "Explore power-user capabilities", Icons.Filled.Info),
    PageItem("Appearance", "Customize themes, colors, and layout", Icons.Filled.AccountCircle),
    PageItem("Notifications", "Manage alert and push notification settings", Icons.Filled.Notifications),
    PageItem("Security & Privacy", "Configure permissions and encryption", Icons.Filled.Lock),
    PageItem("Storage", "View disk usage and clear cached data", Icons.Filled.Delete),
    PageItem("Developer Options", "Access debugging and experimental tools", Icons.Filled.Build),
    PageItem("Search Index", "Manage full-text search and indexing", Icons.Filled.Search),
    PageItem("Favorites Sync", "Sync saved items across devices", Icons.Filled.Favorite),
    PageItem("System Health", "View performance metrics and diagnostics", Icons.Filled.Info),
)

@Composable
fun SearchBarBreezePreviewImpl() {
    var searchText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val hazeState = rememberHazeState()
    var selectedItem by remember { mutableStateOf<String?>(null) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val searchBarScrollBehavior = SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()
    Scaffold(
        topBar = {
            SearchAppBarBreeze(
                modifier = Modifier.topBarHazeEffect(hazeState, scrollBehavior),
                title = { Text("Search Demo") },
                subtitle = { Text("Search subtitle") },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                searchText = searchText,
                onSearchTextChange = { searchText = it },
                onClearClick = { searchText = "" },
                scrollBehavior = scrollBehavior,
                searchBarScrollBehavior = searchBarScrollBehavior,
                snackbarHostState = snackbarHostState,
                searchContent = { bottomPadding, closeSearch ->
                    SearchResults(
                        query = searchText,
                        bottomPadding = bottomPadding,
                        onItemClick = { item ->
                            selectedItem = item
                            scope.launch {
                                snackbarHostState.showSnackbar("Selected: $item")
                            }
                        }
                    )
                }
            )
        },
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
                .padding(paddingValues.onlyHorizontal())
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .nestedScroll(searchBarScrollBehavior.nestedScrollConnection)
                .padding(horizontal = 16.dp), contentPadding = PaddingValues(top = paddingValues.calculateTopPadding())
        ) {
            item {
                Text(
                    text = "Browse",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
            items(pageItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    ListItem(
                        headlineContent = { Text(item.title) },
                        supportingContent = { Text(item.subtitle) },
                        leadingContent = {
                            Icon(item.icon, contentDescription = null)
                        }
                    )
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Tap a search result to see the blur dialog + snackbar. Scroll the list to collapse the top bar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Blur dialog on search result tap
    selectedItem?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedItem = null },
            title = { Text("Result Detail") },
            text = {
                Column {
                    Text("You selected:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(item, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "This dialog has background blur via windowBlurBehind. Tap outside or press Dismiss to close.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Confirmed: $item")
                    }
                    selectedItem = null
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedItem = null }) {
                    Text("Dismiss")
                }
            },
            modifier = Modifier.windowBlurBehind(radius = 20.dp)
        )
    }
}


@Composable
private fun SearchResults(
    query: String,
    bottomPadding: Dp,
    onItemClick: (String) -> Unit,
) {
    val filtered = if (query.isBlank()) {
        sampleItems
    } else {
        sampleItems.filter { it.contains(query, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = bottomPadding)
    ) {
        items(filtered) { item ->
            Text(
                text = item,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(item) }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
        if (filtered.isEmpty()) {
            item {
                Text(
                    "No results for \"$query\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSearchBarBreeze() {
    val blurController = rememberBlurController()
    KernelSUTheme(uiMode = UiMode.Breeze) {
        CompositionLocalProvider(
            LocalEnableBlur provides true,
            LocalBlurController provides blurController,
        ) {
            SearchBarBreezePreviewImpl()
        }
    }
}

