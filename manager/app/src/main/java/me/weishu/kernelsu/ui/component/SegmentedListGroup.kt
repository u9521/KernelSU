package me.weishu.kernelsu.ui.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.ui.component.popUps.BrMenuBox

/**
 * Data class holding the configuration for a single item within the SegmentedListGroup.
 * This serves as an intermediate model between the DSL builder and the actual Composable rendering.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
data class SegmentedItemData(
    val key: Any?,
    val visible: Boolean,
    val onClick: (() -> Unit)?,
    val noPressShape: Boolean = true, // Determines if the press ripple/shape follows the container shape
    val modifier: Modifier,
    val enabled: Boolean,
    val leadingContent: @Composable (() -> Unit)?,
    val trailingContent: @Composable (() -> Unit)?,
    val overlineContent: @Composable (() -> Unit)?,
    val supportingContent: @Composable (() -> Unit)?,
    val verticalAlignment: Alignment.Vertical?,
    val onLongClick: (() -> Unit)?,
    val onLongClickLabel: String?,
    val colors: ListItemColors?,
    val elevation: ListItemElevation?,
    val contentPadding: PaddingValues?,
    val interactionSource: MutableInteractionSource?,
    // If not null, this item acts as a trigger for a popup menu
    val menuContent: @Composable (ColumnScope.(dismissMenu: () -> Unit) -> Unit)?,
    val content: @Composable () -> Unit,
)

/**
 * Scope for building a list of segmented items.
 * This class provides the DSL functions (item, menuItem, switchItem) used inside SegmentedListGroup.
 */
class SegmentedListScope {
    val items = mutableListOf<SegmentedItemData>()

    /**
     * Adds a standard clickable item to the list.
     */
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    fun item(
        key: Any? = null,
        visible: Boolean = true,
        onClick: (() -> Unit)? = null,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        leadingContent: @Composable (() -> Unit)? = null,
        trailingContent: @Composable (() -> Unit)? = null,
        overlineContent: @Composable (() -> Unit)? = null,
        supportingContent: @Composable (() -> Unit)? = null,
        verticalAlignment: Alignment.Vertical? = Alignment.CenterVertically,
        onLongClick: (() -> Unit)? = null,
        onLongClickLabel: String? = null,
        colors: ListItemColors? = null,
        elevation: ListItemElevation? = null,
        contentPadding: PaddingValues? = null,
        interactionSource: MutableInteractionSource? = null,
        content: @Composable () -> Unit
    ) {
        items.add(
            SegmentedItemData(
                key = key ?: items.size,
                visible = visible,
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                leadingContent = leadingContent,
                trailingContent = trailingContent,
                overlineContent = overlineContent,
                supportingContent = supportingContent,
                verticalAlignment = verticalAlignment,
                onLongClick = onLongClick,
                onLongClickLabel = onLongClickLabel,
                colors = colors,
                elevation = elevation,
                contentPadding = contentPadding,
                interactionSource = interactionSource,
                menuContent = null,
                content = content
            )
        )
    }

    /**
     * Adds an item that displays a popup menu when clicked.
     *
     * @param selected A string to display in the trailing position (e.g., current value of the setting).
     * @param menuContent The Composable content to display inside the popup menu.
     */
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    fun menuItem(
        key: Any? = null,
        visible: Boolean = true,
        content: @Composable () -> Unit,
        selected: String? = null,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        leadingContent: @Composable (() -> Unit)? = null,
        overlineContent: @Composable (() -> Unit)? = null,
        supportingContent: @Composable (() -> Unit)? = null,
        verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
        colors: ListItemColors? = null,
        elevation: ListItemElevation? = null,
        contentPadding: PaddingValues? = null,
        interactionSource: MutableInteractionSource? = null,
        menuContent: @Composable ColumnScope.(dismissMenu: () -> Unit) -> Unit
    ) {
        items.add(
            SegmentedItemData(
                key = key ?: items.size,
                visible = visible,
                onClick = null, // onClick is handled by the BrMenuBox wrapper logic
                modifier = modifier,
                enabled = enabled,
                leadingContent = leadingContent,
                trailingContent = {
                    // Display the selected value text if provided
                    selected?.let {
                        Text(
                            selected, style = MaterialTheme.typography.bodyMedium, color = if (enabled) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            }
                        )
                    }
                },
                overlineContent = overlineContent,
                supportingContent = supportingContent,
                verticalAlignment = verticalAlignment,
                onLongClick = null,
                onLongClickLabel = null,
                colors = colors,
                elevation = elevation,
                contentPadding = contentPadding,
                interactionSource = interactionSource,
                menuContent = menuContent,
                content = content
            )
        )
    }

    /**
     * Convenience method to add a switch/toggle item.
     * Automatically handles the trailing Switch composable and onClick toggling.
     */
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    fun switchItem(
        key: Any? = null,
        visible: Boolean = true,
        leadingContent: (@Composable () -> Unit)? = null,
        title: @Composable () -> String,
        summary: (@Composable () -> String?)? = null,
        checked: Boolean,
        enabled: Boolean = true,
        onCheckedChange: (Boolean) -> Unit
    ) {
        item(
            key = key,
            visible = visible,
            enabled = enabled,
            onClick = { onCheckedChange(!checked) }, // Allow clicking the whole row to toggle
            leadingContent = leadingContent,
            trailingContent = {
                Crossfade(
                    targetState = enabled,
                    label = "SwitchEnableCrossfade"
                ) { isEnabled ->
                    Switch(
                        checked = checked,
                        enabled = isEnabled,
                        onCheckedChange = onCheckedChange,
                    )
                }
            },
            supportingContent = summary?.let { text ->
                { text()?.let { Text(text = it) } }
            },
            content = {
                Text(text = title())
            }
        )
    }
}

/**
 * A container that renders a list of items as a cohesive group.
 * It automatically handles corner rounding:
 * - Top item gets top rounded corners.
 * - Bottom item gets bottom rounded corners.
 * - Middle items are squared.
 *
 * @param title Optional header title for the group.
 * @param content The DSL block to add items.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SegmentedListGroup(
    modifier: Modifier = Modifier,
    title: String = "",
    content: SegmentedListScope.() -> Unit
) {
    // Initialize the scope and collect items from the DSL
    val scope = remember { SegmentedListScope() }
    scope.items.clear()
    scope.content()
    val items = scope.items

    if (items.isEmpty()) return

    // Track when the initial composition is loaded to manage animations
    var isLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isLoaded = true }

    // Identify which items are currently visible to calculate correct corner shapes
    val visibleItems = items.filter { it.visible }
    val firstVisibleKey = visibleItems.firstOrNull()?.key
    val lastVisibleKey = visibleItems.lastOrNull()?.key

    Column(modifier = modifier
        .padding(horizontal = 16.dp)
        .padding(bottom = 16.dp)) {
        if (title.isNotEmpty()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
        }

        Column(verticalArrangement = Arrangement.Top) {
            items.forEachIndexed { index, itemData ->
                SplicedAnimatedVisibility(
                    visible = itemData.visible,
                    loaded = isLoaded,
                    index = index,
                    totalCount = items.size,
                ) {
                    val isFirst = itemData.key == firstVisibleKey
                    val isLast = itemData.key == lastVisibleKey
                    MaterialTheme.shapes.extraSmall
                    // Use 'Large' shape (16.dp) for outer corners and 'ExtraSmall' shape (4.dp) for inner/connected corners
                    val targetTopRadius = if (isFirst) 16.dp else 4.dp
                    val targetBottomRadius = if (isLast) 16.dp else 4.dp

                    // Animate the corner changes (e.g., when the top item is hidden, the next item animates to round corners)
                    val animatedTopRadius by animateDpAsState(
                        targetValue = targetTopRadius,
                        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
                        label = "TopCornerRadius"
                    )
                    val animatedBottomRadius by animateDpAsState(
                        targetValue = targetBottomRadius,
                        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
                        label = "BottomCornerRadius"
                    )

                    val animatedShape = RoundedCornerShape(
                        topStart = animatedTopRadius,
                        topEnd = animatedTopRadius,
                        bottomStart = animatedBottomRadius,
                        bottomEnd = animatedBottomRadius
                    )

                    // Apply the shape to the item. If it's clickable, we might want the press changes the shape.
                    val actualShapes = ListItemDefaults.shapes(shape = animatedShape).let {
                        if (itemData.onClick != null && !itemData.noPressShape) {
                            return@let it
                        }
                        it.copy(pressedShape = animatedShape)
                    }

                    val topPadding = if (isFirst) 0.dp else ListItemDefaults.SegmentedGap

                    val itemContent: @Composable (Modifier) -> Unit = { styleModifier ->
                        SegmentedListItem(
                            onClick = itemData.onClick ?: {},
                            modifier = itemData.modifier
                                .then(styleModifier)
                                .defaultMinSize(minHeight = 72.dp),
                            shapes = actualShapes,
                            enabled = itemData.enabled,
                            leadingContent = itemData.leadingContent,
                            trailingContent = itemData.trailingContent,
                            overlineContent = itemData.overlineContent,
                            supportingContent = itemData.supportingContent,
                            verticalAlignment = itemData.verticalAlignment ?: ListItemDefaults.verticalAlignment(),
                            onLongClick = itemData.onLongClick,
                            onLongClickLabel = itemData.onLongClickLabel,
                            colors = itemData.colors ?: ListItemDefaults.segmentedColors(
                                containerColor = MaterialTheme.colorScheme.surfaceBright
                            ),
                            elevation = itemData.elevation ?: ListItemDefaults.elevation(),
                            contentPadding = itemData.contentPadding ?: ListItemDefaults.ContentPadding,
                            interactionSource = itemData.interactionSource,
                            content = itemData.content
                        )
                    }

                    // If the item has menu content, wrap it in a BrMenuBox (popup trigger)
                    if (itemData.menuContent != null) {
                        BrMenuBox(
                            modifier = Modifier
                                .padding(top = topPadding)
                                .fillMaxWidth()
                                .clip(animatedShape),
                            enabled = itemData.enabled,
                            menuContent = itemData.menuContent,
                            content = {
                                itemContent(Modifier.fillMaxWidth())
                            }
                        )
                    } else {
                        // Otherwise, render the standard item with padding
                        itemContent(Modifier.padding(top = topPadding))
                    }
                }
            }
        }
    }
}