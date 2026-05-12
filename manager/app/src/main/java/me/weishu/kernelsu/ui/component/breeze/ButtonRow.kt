package me.weishu.kernelsu.ui.component.breeze

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp

enum class ButtonType {
    PRIMARY, TONAL
}

enum class ButtonPosition {
    START, END
}

@Immutable
data class ButtonSpec(
    val id: String,
    val text: String? = null,
    val icon: @Composable () -> Unit,
    val onClick: () -> Unit,
    val onLongClick: (() -> Unit)? = null,
    val isVisible: Boolean = true,
    val isEnabled: Boolean = true,
    val type: ButtonType = ButtonType.TONAL,
    val buttonPosition: ButtonPosition
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    visible: Boolean = true,
    isExpanded: Boolean,
    buttonType: ButtonType = ButtonType.TONAL,
    buttonPosition: ButtonPosition,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    val keyFeedback = keyDownFeedBack()
    val longPressFeedback = longPressFeedBack()
    val onButtonClick = {
        keyFeedback()
        onClick()
    }
    val onButtonLongClick: (() -> Unit)? = onLongClick?.let {
        {
            longPressFeedback()
            it()
        }
    }
    val visibleState = remember { MutableTransitionState(visible) }
    visibleState.targetState = visible

    val animationAlignment = when (buttonPosition) {
        ButtonPosition.START -> Alignment.Start
        ButtonPosition.END -> Alignment.End
    }

    val button: @Composable (Boolean) -> Unit = when (buttonType) {
        ButtonType.TONAL -> {
            { skipAnimation ->
                CombinedClickableButton(
                    modifier = modifier.defaultMinSize(minWidth = 52.dp, minHeight = 32.dp),
                    onClick = onButtonClick,
                    onLongClick = onButtonLongClick,
                    enabled = enabled,
                    shapes = ButtonDefaults.shapes(),
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    contentPadding = ButtonDefaults.contentPaddingFor(buttonHeight = ButtonDefaults.MinHeight),
                    content = buttonContent(icon, isExpanded, skipAnimation = skipAnimation, text = text)
                )
            }
        }

        ButtonType.PRIMARY -> {
            { skipAnimation ->
                CombinedClickableButton(
                    modifier = modifier.defaultMinSize(minWidth = 52.dp, minHeight = 32.dp),
                    onClick = onButtonClick,
                    onLongClick = onButtonLongClick,
                    enabled = enabled,
                    shapes = ButtonDefaults.shapes(),
                    colors = ButtonDefaults.buttonColors(),
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
fun CombinedClickableButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val mutableInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    Surface(
        modifier = modifier
            .clip(shape)
            .semantics { role = Role.Button }
            .combinedClickable(
                interactionSource = mutableInteractionSource,
                indication = LocalIndication.current,
                enabled = enabled,
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        shape = shape,
        color = if (enabled) colors.containerColor else colors.disabledContainerColor,
        contentColor = if (enabled) colors.contentColor else colors.disabledContentColor,
        border = border,
    ) {
        ProvideTextStyle(MaterialTheme.typography.labelLarge) {
            Row(
                modifier = Modifier
                    .defaultMinSize(
                        minWidth = ButtonDefaults.MinWidth,
                        minHeight = ButtonDefaults.MinHeight,
                    )
                    .padding(contentPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CombinedClickableButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    shapes: ButtonShapes,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val defaultAnimationSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val pressed by interactionSource.collectIsPressedAsState()
    val shape = shapeByInteraction(shapes, pressed, defaultAnimationSpec)
    CombinedClickableButton(
        modifier = modifier, onClick = onClick, onLongClick = onLongClick, enabled = enabled, shape = shape, colors = colors,
        border = border, contentPadding = contentPadding, interactionSource = interactionSource, content = content
    )
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun buttonContent(
    icon: @Composable (() -> Unit)?,
    isExpanded: Boolean,
    skipAnimation: Boolean = false,
    text: String? = null
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
            if (icon != null && text != null) Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(ButtonDefaults.MinHeight)))
            text?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    softWrap = false
                )
            }
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

        var expandedCount = 0
        var currentWidth = baseWidth

        // Greedily try to expand buttons one by one as long as they fit.
        for (cost in expansionCosts) {
            // Calculate the potential width if we expand this next button
            val nextWidth = currentWidth + cost

            if (nextWidth <= constraints.maxWidth) {
                currentWidth = nextWidth
                expandedCount++
            } else {
                break
            }
        }

        // Get the set of IDs for buttons that are "approved to expand"
        val expandedButtonIds = calcAllButtonsPriority.take(expandedCount).map { it.id }.toSet()

        // --- Phase D: Final Render and Measurement (For all buttons, including those animating out) ---

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

        // --- Phase E: Layout ---
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun shapeByInteraction(
    shapes: ButtonShapes,
    pressed: Boolean,
    animationSpec: FiniteAnimationSpec<Float>,
): Shape {

    val progressState = animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = animationSpec,
        label = "button_shape_morph"
    )

    val defaultShape = shapes.shape as? CornerBasedShape
    val pressedShape = shapes.pressedShape as? CornerBasedShape
    if (defaultShape == null || pressedShape == null) {
        return if (pressed) shapes.pressedShape else shapes.shape
    }

    return remember(defaultShape, pressedShape) {
        object : Shape {
            override fun createOutline(
                size: Size,
                layoutDirection: LayoutDirection,
                density: Density
            ): Outline {
                val progress = progressState.value

                val topStart = lerp(
                    defaultShape.topStart.toPx(size, density),
                    pressedShape.topStart.toPx(size, density),
                    progress
                )
                val topEnd = lerp(
                    defaultShape.topEnd.toPx(size, density),
                    pressedShape.topEnd.toPx(size, density),
                    progress
                )
                val bottomStart = lerp(
                    defaultShape.bottomStart.toPx(size, density),
                    pressedShape.bottomStart.toPx(size, density),
                    progress
                )
                val bottomEnd = lerp(
                    defaultShape.bottomEnd.toPx(size, density),
                    pressedShape.bottomEnd.toPx(size, density),
                    progress
                )

                return RoundedCornerShape(
                    topStart = topStart,
                    topEnd = topEnd,
                    bottomStart = bottomStart,
                    bottomEnd = bottomEnd
                ).createOutline(size, layoutDirection, density)
            }
        }
    }
}
