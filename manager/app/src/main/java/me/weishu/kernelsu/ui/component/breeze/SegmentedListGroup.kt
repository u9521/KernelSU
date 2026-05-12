package me.weishu.kernelsu.ui.component.breeze

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.ui.component.material.ExpressiveSwitch

/**
 * Data class holding the configuration for a single item within the SegmentedListGroup.
 * This serves as an intermediate model between the DSL builder and the actual Composable rendering.
 */
data class SegmentedItemData(
    val key: Any?,
    val visible: Boolean,
    val onClick: (() -> Unit)?,
    val switchChecked: (() -> Boolean)? = null,
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
    fun item(
        key: Any? = null,
        visible: Boolean = true,
        onClick: (() -> Unit)? = null,
        switchChecked: (() -> Boolean)? = null,
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
                switchChecked = switchChecked,
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
     * @param selected A lambda returning the current value to display in the trailing position (defers state read).
     */
    fun menuItem(
        key: Any? = null,
        visible: Boolean = true,
        content: @Composable (() -> Unit),
        selected: (() -> String?)? = null,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        leadingContent: @Composable (() -> Unit)? = null,
        overlineContent: @Composable (() -> Unit)? = null,
        supportingContent: @Composable (() -> Unit)? = null,
        trailingContent: @Composable (() -> Unit)? = null,
        verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
        colors: ListItemColors? = null,
        elevation: ListItemElevation? = null,
        contentPadding: PaddingValues? = null,
        interactionSource: MutableInteractionSource? = null,
        menuContent: @Composable (ColumnScope.(dismissMenu: () -> Unit) -> Unit)?
    ) {
        items.add(
            SegmentedItemData(
                key = key ?: items.size,
                visible = visible,
                onClick = null, // onClick is handled by the BrMenuBox wrapper logic
                switchChecked = null,
                modifier = modifier,
                enabled = enabled,
                leadingContent = leadingContent,
                trailingContent = trailingContent ?: selected?.let { getSelected ->
                    {
                        getSelected()?.let { value ->
                            Text(
                                value, style = MaterialTheme.typography.bodyMedium, color = if (enabled) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
                                }
                            )
                        }
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
    fun switchItem(
        key: Any? = null,
        visible: Boolean = true,
        leadingContent: (@Composable () -> Unit)? = null,
        title: String,
        summary: String? = null,
        checked: () -> Boolean,
        enabled: Boolean = true,
        onCheckedChange: (Boolean) -> Unit
    ) {
        item(
            key = key,
            visible = visible,
            enabled = enabled,
            switchChecked = checked,
            onClick = { onCheckedChange(!checked()) }, // Allow clicking the whole row to toggle
            leadingContent = leadingContent,
            trailingContent = {
                val performSwitchHapticFeedback = switchHapticFeedBack()
                Crossfade(
                    targetState = enabled,
                    label = "SwitchEnableCrossfade"
                ) { isEnabled ->
                    val currentChecked = checked()
                    ExpressiveSwitch(
                        checked = currentChecked,
                        enabled = isEnabled,
                        onCheckedChange = {
                            performSwitchHapticFeedback(it)
                            onCheckedChange(it)
                        },
                    )
                }
            },
            supportingContent = summary?.let { text ->
                { Text(text = text) }
            },
            content = {
                Text(text = title)
            }
        )
    }

    fun checkboxItem(
        key: Any? = null,
        visible: Boolean = true,
        leadingContent: (@Composable () -> Unit)? = null,
        title: String,
        summary: String? = null,
        checked: () -> Boolean,
        enabled: Boolean = true,
        onCheckedChange: (Boolean) -> Unit
    ) {
        item(
            key = key,
            visible = visible,
            enabled = enabled,
            onClick = { onCheckedChange(!checked()) },
            leadingContent = leadingContent ?: {
                val interactionSource = remember { MutableInteractionSource() }
                val performSwitchHapticFeedback = switchHapticFeedBack()
                val currentChecked = checked()
                Checkbox(
                    checked = currentChecked,
                    enabled = enabled,
                    onCheckedChange = {
                        performSwitchHapticFeedback(it)
                        onCheckedChange(it)
                    },
                    interactionSource = interactionSource,
                )
            },
            supportingContent = summary?.let { text ->
                { Text(text = text) }
            },
            content = {
                Text(text = title)
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

    val performSwitchHapticFeedback = switchHapticFeedBack()
    val visibleItems = items.filter { it.visible }

    Column(modifier = modifier) {
        if (title.isNotEmpty()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
        }

        items.forEachIndexed { _, itemData ->
            AnimatedVisibility(
                visible = itemData.visible,
                enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(spring(stiffness = Spring.StiffnessMediumLow)),
                exit = shrinkVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut(spring(stiffness = Spring.StiffnessMediumLow)),
            ) {
                SegmentedListItemRow(
                    itemData = itemData,
                    isFirst = itemData.key == visibleItems.firstOrNull()?.key,
                    isLast = itemData.key == visibleItems.lastOrNull()?.key,
                    hapticFeedback = performSwitchHapticFeedback,
                )
            }
        }
    }
}

@Composable
private fun SegmentedListItemRow(
    modifier: Modifier = Modifier,
    itemData: SegmentedItemData,
    isFirst: Boolean,
    isLast: Boolean,
    hapticFeedback: (Boolean) -> Unit,
) {
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
        val switchChecked = itemData.switchChecked
        val onItemClick = if (switchChecked != null && itemData.onClick != null) {
            {
                hapticFeedback(!switchChecked())
                itemData.onClick.invoke()
            }
        } else {
            itemData.onClick ?: {}
        }

        SegmentedListItem(
            onClick = onItemClick,
            modifier = modifier
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
            modifier = modifier
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
