package me.weishu.kernelsu.ui.component.scrollbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun VerticalScrollbar(
    modifier: Modifier = Modifier,
    adapter: ScrollbarAdapter,
    style: ScrollbarStyle = ScrollbarDefaults.style,
    reverseLayout: Boolean = false,
    durationMillis: Long = Long.MAX_VALUE,
    enter: EnterTransition = slideInHorizontally { it },
    exit: ExitTransition = slideOutHorizontally { it }
) {
    ScrollbarImpl(
        adapter = adapter,
        modifier = modifier,
        style = style,
        reverseLayout = reverseLayout,
        isVertical = true,
        durationMillis = durationMillis,
        enter = enter,
        exit = exit
    )
}

@Composable
fun HorizontalScrollbar(
    modifier: Modifier = Modifier,
    adapter: ScrollbarAdapter,
    style: ScrollbarStyle = ScrollbarDefaults.style,
    reverseLayout: Boolean = false,
    durationMillis: Long = Long.MAX_VALUE,
    enter: EnterTransition = slideInVertically { it },
    exit: ExitTransition = slideOutVertically { it }
) {
    ScrollbarImpl(
        adapter = adapter,
        modifier = modifier,
        style = style,
        reverseLayout = reverseLayout,
        isVertical = false,
        durationMillis = durationMillis,
        enter = enter,
        exit = exit
    )
}

@Composable
fun rememberScrollbarAdapter(
    lazyListState: LazyListState,
): ScrollbarAdapter {
    return remember(lazyListState) {
        LazyListScrollbarAdapter(lazyListState)
    }
}

@Composable
fun rememberScrollbarAdapter(
    lazyGridState: LazyGridState
): ScrollbarAdapter {
    return remember(lazyGridState) {
        LazyGridScrollbarAdapter(lazyGridState)
    }
}

@Composable
fun rememberScrollbarAdapter(
    scrollState: ScrollState
): ScrollbarAdapter {
    return remember(scrollState) {
        ScrollableScrollbarAdapter(scrollState)
    }
}


@Immutable
data class ScrollbarStyle(
    val minimalHeight: Dp, val thickness: Dp, val shape: Shape, val railShape: Shape, val color: Color, val railColor: Color = Color.Transparent
)

object ScrollbarDefaults {
    val style = ScrollbarStyle(
        minimalHeight = 16.dp,
        thickness = 8.dp,
        shape = RoundedCornerShape(4.dp),
        railShape = RoundedCornerShape(4.dp),
        color = Color.Gray.copy(alpha = 0.5f),
        railColor = Color.Gray.copy(alpha = 0.25f),
    )
}

@Composable
private fun ScrollbarImpl(
    adapter: ScrollbarAdapter,
    modifier: Modifier,
    style: ScrollbarStyle,
    reverseLayout: Boolean,
    isVertical: Boolean,
    durationMillis: Long,
    enter: EnterTransition,
    exit: ExitTransition
) {
    val coroutineScope = rememberCoroutineScope()
    var containerSize by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    val canScroll = adapter.contentSize > adapter.viewportSize

    val sliderAdapter = remember(adapter, containerSize, style.minimalHeight, reverseLayout, isVertical) {
        with(density) {
            SliderAdapter(
                adapter = adapter,
                trackSize = containerSize,
                minHeight = style.minimalHeight.toPx(),
                reverseLayout = reverseLayout,
                isVertical = isVertical,
                coroutineScope = coroutineScope
            )
        }
    }

    var isVisible by remember { mutableStateOf(durationMillis == Long.MAX_VALUE) }
    var isInteracting by remember { mutableStateOf(false) }

    if (durationMillis != Long.MAX_VALUE) {
        LaunchedEffect(sliderAdapter.thumbPixelRange, isInteracting, durationMillis) {
            isVisible = true
            if (!isInteracting) {
                delay(durationMillis)
                isVisible = false
            }
        }
    } else {
        isVisible = true
    }

    val scrollThickness = with(density) { style.thickness.roundToPx() }

    val measurePolicy = remember(scrollThickness, isVertical) {
        if (isVertical) {
            MeasurePolicy { measurables, constraints ->
                containerSize = constraints.maxHeight
                if (measurables.isEmpty()) return@MeasurePolicy layout(0, 0) {}

                val width = constraints.constrainWidth(scrollThickness)
                val placeable = measurables.first().measure(
                    Constraints.fixed(width, constraints.maxHeight)
                )
                layout(placeable.width, placeable.height) { placeable.place(0, 0) }
            }
        } else {
            MeasurePolicy { measurables, constraints ->
                containerSize = constraints.maxWidth
                if (measurables.isEmpty()) return@MeasurePolicy layout(0, 0) {}

                val height = constraints.constrainHeight(scrollThickness)
                val placeable = measurables.first().measure(
                    Constraints.fixed(constraints.maxWidth, height)
                )
                layout(placeable.width, placeable.height) { placeable.place(0, 0) }
            }
        }
    }

    val innerMeasurePolicy = remember(sliderAdapter, isVertical) {
        MeasurePolicy { measurables, constraints ->
            // measurables[0] Rail
            val railPlaceable = measurables[0].measure(constraints)

            val pixelRange = sliderAdapter.thumbPixelRange
            val thumbSize = pixelRange.size.coerceAtLeast(0)

            // measurables[1] Thumb
            val thumbPlaceable = measurables[1].measure(
                Constraints.fixed(
                    width = if (isVertical) constraints.maxWidth else thumbSize,
                    height = if (isVertical) thumbSize else constraints.maxHeight
                )
            )

            layout(constraints.maxWidth, constraints.maxHeight) {
                railPlaceable.place(0, 0)
                if (isVertical) {
                    thumbPlaceable.place(0, pixelRange.first)
                } else {
                    thumbPlaceable.place(pixelRange.first, 0)
                }
            }
        }
    }

    Layout(
        content = {
            AnimatedVisibility(
                visible = isVisible && canScroll, enter = enter, exit = exit
            ) {
                Layout(
                    content = {
                        // Rail
                        Box(Modifier.background(style.railColor, style.railShape))

                        // Thumb
                        Box(
                            Modifier
                                .background(style.color, style.shape)
                                .pointerInput(Unit) {
                                    awaitEachGesture {
                                        awaitFirstDown()
                                        isInteracting = true
                                        waitForUpOrCancellation()
                                        isInteracting = false
                                    }
                                }
                                .scrollbarDrag(sliderAdapter)
                        )
                    },
                    measurePolicy = innerMeasurePolicy
                )
            }
        },
        modifier = modifier,
        measurePolicy = measurePolicy
    )
}