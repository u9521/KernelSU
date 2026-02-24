package me.weishu.kernelsu.ui.component.module

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

enum class ButtonType {
    PRIMARY, TONAL
}

enum class ButtonGroup {
    START, END
}

@Immutable
data class ButtonSpec(
    val id: String,
    val text: @Composable () -> String,
    val icon: @Composable () -> Unit,
    val onClick: () -> Unit,
    val isVisible: Boolean = true,
    val isEnabled: Boolean = true,
    val type: ButtonType = ButtonType.TONAL,
    val buttonGroup: ButtonGroup
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    visible: Boolean = true,
    isExpanded: Boolean,
    buttonType: ButtonType = ButtonType.TONAL,
    buttonGroup: ButtonGroup,
    onClick: () -> Unit,
) {
    val visibleState = remember { MutableTransitionState(visible) }
    visibleState.targetState = visible

    val animationAlignment = when (buttonGroup) {
        ButtonGroup.START -> Alignment.Start
        ButtonGroup.END -> Alignment.End
    }

    val button: @Composable (Boolean) -> Unit = when (buttonType) {
        ButtonType.TONAL -> {
            { skipAnimation ->
                FilledTonalButton(
                    modifier = modifier.defaultMinSize(minWidth = 52.dp, minHeight = 32.dp),
                    onClick = onClick,
                    enabled = enabled,
                    shapes = ButtonDefaults.shapes(),
                    contentPadding = ButtonDefaults.contentPaddingFor(buttonHeight = ButtonDefaults.MinHeight),
                    content = buttonContent(icon, isExpanded, skipAnimation = skipAnimation, text = text)
                )
            }
        }

        ButtonType.PRIMARY -> {
            { skipAnimation ->
                Button(
                    modifier = modifier.defaultMinSize(minWidth = 52.dp, minHeight = 32.dp),
                    onClick = onClick,
                    enabled = enabled,
                    shapes = ButtonDefaults.shapes(),
                    contentPadding = ButtonDefaults.contentPaddingFor(buttonHeight = ButtonDefaults.MinHeight),
                    content = buttonContent(icon, isExpanded, skipAnimation = skipAnimation, text = text)
                )
            }
        }
    }
    val animationSpec = MaterialTheme.motionScheme.fastSpatialSpec<IntSize>()
    AnimatedVisibility(
        visibleState = visibleState, enter = fadeIn() + expandHorizontally(expandFrom = animationAlignment, animationSpec = animationSpec),
        exit = fadeOut() + shrinkHorizontally(shrinkTowards = animationAlignment, animationSpec = animationSpec)
    ) {
        val isButtonAnimationStable = this.transition.currentState == this.transition.targetState
        button(!isButtonAnimationStable)
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun buttonContent(
    icon: @Composable (() -> Unit)?,
    isExpanded: Boolean,
    skipAnimation: Boolean = false,
    text: String
): @Composable (RowScope.() -> Unit) {
    val expandedState = remember { MutableTransitionState(isExpanded) }
    expandedState.targetState = isExpanded
    val content: @Composable RowScope.() -> Unit = {
        icon?.let { it() }
        val animationSpec = MaterialTheme.motionScheme.fastSpatialSpec<IntSize>()
        AnimatedVisibility(
            visibleState = expandedState,
            enter = if (skipAnimation) fadeIn() else fadeIn() + expandHorizontally(expandFrom = Alignment.Start, animationSpec = animationSpec),
            exit = if (skipAnimation) fadeOut() else fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start, animationSpec = animationSpec)
        ) {
            if (icon != null) Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(ButtonDefaults.MinHeight)))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                softWrap = false
            )
        }
    }
    return content
}


/**
 * A custom row layout designed to arrange two groups of buttons (`startButtons` and `endButtons`)
 * with a priority-based expansion mechanism.
 *
 * It calculates the available width and greedily "expands" buttons
 *    (e.g., showing both text and icon) one by one until the space runs out. Unexpanded buttons
 *    will remain in a "collapsed" state (e.g., icon only).
 *    By default, `startButtons` have a higher priority to expand than `endButtons`.
 * @param modifier The modifier to be applied to the layout.
 * @param centerSpacing The minimum gap maintained between the start group and the end group
 *                      when both have visible elements.
 * @param startButtons The list of buttons to be placed on the left side, stacked left-to-right.
 * @param endButtons The list of buttons to be placed on the right side, stacked right-to-left.
 * @param buttonFactory A composable factory to generate the UI for each button. It provides the
 *                      [ButtonSpec] and whether the layout has approved it to be `isExpanded`.
 */
@Composable
fun EnumeratedPriorityButtonRow(
    modifier: Modifier = Modifier,
    centerSpacing: Dp = 0.dp,
    startButtons: List<ButtonSpec>,
    endButtons: List<ButtonSpec>,
    buttonFactory: @Composable (spec: ButtonSpec, isExpanded: Boolean) -> Unit
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val centerSpacingPx = centerSpacing.roundToPx()

        // --- Phase A: Prepare Logical Data (Filter out isVisible=false items) ---
        // These lists are only used to calculate "which buttons are eligible to expand"
        // and "how much space is needed".
        val calcStartButtons = startButtons.filter { it.isVisible }
        val calcEndButtons = endButtons.filter { it.isVisible }

        // The list that determines expansion priority: Start (left) buttons first, then End (right) buttons.
        // Swap the order here if you want the End group to have higher expansion priority.
        val calcAllButtonsPriority = calcStartButtons + calcEndButtons

        // --- Phase B: Baseline Measurement (Only for logical lists) ---
        // Purpose: Calculate a threshold table to determine how many buttons can be expanded
        // within the current width.

        // 1. Measure Collapsed state (Icon only)
        val collapsedPlaceables = subcompose("metrics_collapsed") {
            calcAllButtonsPriority.forEach { spec -> buttonFactory(spec, false) }
        }.map { it.measure(Constraints()) }

        // 2. Measure Expanded state (Icon + Text)
        val expandedPlaceables = subcompose("metrics_expanded") {
            calcAllButtonsPriority.forEach { spec -> buttonFactory(spec, true) }
        }.map { it.measure(Constraints()) }

        // --- Phase C: Calculate Thresholds ---

        // Keep the center gap only if both sides have visible content.
        val effectiveCenterGap = if (calcStartButtons.isNotEmpty() && calcEndButtons.isNotEmpty()) centerSpacingPx else 0

        // Base width = Total collapsed width of all logical buttons + Center gap
        val baseWidth = collapsedPlaceables.sumOf { it.width } + effectiveCenterGap

        // Calculate the additional width cost of expanding each button
        val expansionCosts = calcAllButtonsPriority.indices.map { index ->
            (expandedPlaceables[index].width - collapsedPlaceables[index].width).coerceAtLeast(0)
        }

        // Generate the threshold table
        val thresholds = ArrayList<Int>(calcAllButtonsPriority.size + 1)
        var currentWidthAcc = baseWidth
        thresholds.add(currentWidthAcc) // 0 expanded

        for (cost in expansionCosts) {
            currentWidthAcc += cost
            thresholds.add(currentWidthAcc)
        }

        // --- Phase D: Decision ---
        var expandedCount = 0
        for (i in thresholds.indices.reversed()) {
            if (thresholds[i] <= constraints.maxWidth) {
                expandedCount = i
                break
            }
        }

        // Get the set of IDs for buttons that are "approved to expand"
        val expandedButtonIds = calcAllButtonsPriority.take(expandedCount).map { it.id }.toSet()

        // --- Phase E: Final Render and Measurement (For all buttons, including those animating out) ---

        // 1. Render Start group
        val finalStartPlaceables = subcompose("final_start") {
            startButtons.forEach { spec ->
                key(spec.id) {
                    buttonFactory(spec, expandedButtonIds.contains(spec.id))
                }
            }
        }.map { it.measure(constraints) }

        // 2. Render End group
        val finalEndPlaceables = subcompose("final_end") {
            endButtons.forEach { spec ->
                key(spec.id) {
                    buttonFactory(spec, expandedButtonIds.contains(spec.id))
                }
            }
        }.map { it.measure(constraints) }

        val allFinalPlaceables = finalStartPlaceables + finalEndPlaceables
        val maxHeight = if (allFinalPlaceables.isNotEmpty()) allFinalPlaceables.maxOf { it.height } else 0

        // --- Phase F: Layout ---
        layout(constraints.maxWidth, maxHeight) {
            var currentX = 0

            // 1. Place Start group: Stack from left to right
            finalStartPlaceables.forEach { placeable ->
                placeable.placeRelative(x = currentX, y = (maxHeight - placeable.height) / 2)
                currentX += placeable.width
            }

            // 2. Place End group: Stack from right to left
            // We need to iterate endButtons in reverse order because the last element should be at the far right.
            currentX = constraints.maxWidth
            finalEndPlaceables.reversed().forEach { placeable ->
                currentX -= placeable.width
                placeable.placeRelative(x = currentX, y = (maxHeight - placeable.height) / 2)
            }
        }
    }
}