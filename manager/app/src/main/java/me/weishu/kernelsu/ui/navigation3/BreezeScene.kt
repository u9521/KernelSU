package me.weishu.kernelsu.ui.navigation3

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.SnapSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.weishu.kernelsu.ui.navigation3.BreezeListDetailScene.Companion.BINDING_KEY
import me.weishu.kernelsu.ui.navigation3.BreezeListDetailScene.Companion.DETAIL_KEY
import me.weishu.kernelsu.ui.navigation3.BreezeListDetailScene.Companion.LIST_KEY
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt

const val TopLevelRouteOrdinal = "Breeze.TopLevelRoute.Ordinal"
const val TopLevelRouteHome = "Breeze.TopLevelRoute.Home"

/**
 * A [Scene] that displays a list and a detail [NavEntry] side-by-side in a specific split ratio.
 */
class BreezeListDetailScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    val listEntry: NavEntry<T>,
    val detailEntry: NavEntry<T>?,
    val listRatio: MutableFloatState,
    val onBack: () -> Unit
) : Scene<T> {
    override val entries: List<NavEntry<T>> = listOfNotNull(listEntry, detailEntry)

    override val content: @Composable (() -> Unit) = {
        CompositionLocalProvider(LocalContentRatio provides listRatio) {
            BreezeListDetailLayout(
                modifier = Modifier.fillMaxSize(),
                showDetail = detailEntry != null,
                onBack = onBack,

                // 1. List Content
                listContent = {
                    CompositionLocalProvider(LocalIsListPane provides true, LocalHasDetailPane provides (detailEntry != null)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clipToBounds()
                        ) {
                            listEntry.Content()
                        }
                    }
                },

                // 2. Detail Content with internal animation
                detailContent = {
                    CompositionLocalProvider(LocalIsDetailPane provides true) {
                        AnimatedContent(
                            targetState = detailEntry,
                            contentKey = { it?.contentKey ?: "empty" },
                            transitionSpec = {
                                if (initialState == null) {
                                    EnterTransition.None togetherWith ExitTransition.None
                                } else if (targetState == null) {
                                    EnterTransition.None togetherWith fadeOut(
                                        animationSpec = SnapSpec(delay = ANIMATION_DURATION)
                                    )
                                } else {
                                    slideInHorizontally { width -> width } togetherWith slideOutHorizontally { width -> -width }
                                }
                            },
                            label = "DetailContentTransition"
                        ) { targetEntry ->
                            if (targetEntry != null) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    targetEntry.Content()
                                }
                            } else {
                                Box(Modifier.fillMaxSize())
                            }
                        }
                    }
                }
            )
        }
    }


    companion object {
        internal const val LIST_KEY = "Breeze.Scene.List"
        internal const val DETAIL_KEY = "Breeze.Scene.Detail"
        internal const val BINDING_KEY = "Breeze.Scene.BindingKey"

        /**
         * Helper to tag a [NavEntry] as the **List** pane.
         * The [key] serves as a binding ID to associate this list with a compatible Detail pane.
         */
        fun listPane(key: String) = mapOf(LIST_KEY to true, BINDING_KEY to key)

        /**
         * Helper to tag a [NavEntry] as the **Detail** pane.
         * The [key] must match the List pane's binding ID to establish a valid List-Detail pair.
         */
        fun detailPane(key: String) = mapOf(DETAIL_KEY to true, BINDING_KEY to key)
    }
}

val LocalContentRatio = compositionLocalOf<MutableFloatState?> { null }
val LocalIsListPane = compositionLocalOf { false }
val LocalIsDetailPane = compositionLocalOf { false }
val LocalHasDetailPane = compositionLocalOf { false }

fun <T : Any> injectMetadataStrategy(
    delegate: SceneStrategy<T>,
    computeExtras: SceneStrategyScope<T>.(originalScene: Scene<T>) -> Map<String, Any>
): SceneStrategy<T> {
    return SceneStrategy { entries ->
        val originalScene = with(delegate) { calculateScene(entries) } ?: return@SceneStrategy null

        val extraMetadata = computeExtras(originalScene)

        object : Scene<T> by originalScene {
            override val metadata: Map<String, Any>
                get() = originalScene.metadata + extraMetadata
        }
    }
}

@Composable
fun <T : Any> rememberBreezeListDetailSceneStrategy(
    isRail: Boolean,
    isRtl: Boolean,
    navAnimSpec: FiniteAnimationSpec<IntOffset>,
    listContentRatio: MutableFloatState = remember { mutableFloatStateOf(0.4f) },
): SceneStrategy<T> {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    return remember(windowSizeClass, listContentRatio, isRail, isRtl, navAnimSpec) {
        val baseStrategy = BreezeListDetailSceneStrategy<T>(windowSizeClass, listContentRatio)

        injectMetadataStrategy(baseStrategy) { scene ->
            val targetEntry = scene.entries.lastOrNull()
            if (targetEntry?.metadata?.get(TopLevelRouteOrdinal) !is Int) return@injectMetadataStrategy emptyMap()
            calcNavAnim(isRail, isRtl, navAnimSpec)
        }
    }
}

class BreezeListDetailSceneStrategy<T : Any>(
    val windowSizeClass: WindowSizeClass,
    val listContentRatio: MutableFloatState,
) : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {

        if (!windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)) {
            return null
        }

        val lastEntry = entries.lastOrNull() ?: return null
        val listEntry: NavEntry<T>
        val detailEntry: NavEntry<T>?

        if (lastEntry.metadata.containsKey(DETAIL_KEY)) {
            // Rule: Detail must be strictly preceded by its bound List
            val previousEntry = entries.getOrNull(entries.lastIndex - 1) ?: return null
            val isList = previousEntry.metadata.containsKey(LIST_KEY)
            val listKey = previousEntry.metadata[BINDING_KEY]
            val detailKey = lastEntry.metadata[BINDING_KEY]

            if (isList && listKey == detailKey) {
                listEntry = previousEntry
                detailEntry = lastEntry
            } else {
                return null
            }
        } else if (lastEntry.metadata.containsKey(LIST_KEY)) {
            listEntry = lastEntry
            detailEntry = null
        } else {
            return null
        }

        val sceneKey = listEntry.contentKey
        val consumedCount = if (detailEntry != null) 2 else 1

        return BreezeListDetailScene(
            key = sceneKey,
            previousEntries = entries.dropLast(consumedCount),
            listEntry = listEntry,
            detailEntry = detailEntry,
            listRatio = listContentRatio,
            onBack = onBack
        )
    }
}


private const val ANIMATION_DURATION = 400

@Composable
fun BreezeListDetailLayout(
    modifier: Modifier = Modifier,
    listContent: @Composable () -> Unit,
    detailContent: @Composable () -> Unit,
    showDetail: Boolean,
    onBack: () -> Unit
) {
    val listContentRatio = LocalContentRatio.current?.floatValue ?: 0.4f
    val transitionState = rememberPredictiveBackState(
        targetState = showDetail,
        onBack = onBack,
        animationSpec = tween(ANIMATION_DURATION)
    )

    val transition = rememberTransition(transitionState, label = "ListDetailTransition")
    val detailScreenRatio = 1f - listContentRatio

    val progress by transition.animateFloat(
        label = "LayoutProgress",
        transitionSpec = {
            if (targetState) {
                tween(ANIMATION_DURATION)
            } else {
                // Custom easing for 1:1 finger tracking
                tween(
                    durationMillis = ANIMATION_DURATION,
                    easing = { fraction -> (fraction / detailScreenRatio).coerceIn(0f, 1f) }
                )
            }
        }
    ) { state -> if (state) 1f else 0f }

    Layout(
        contents = listOf(listContent, detailContent),
        modifier = modifier
    ) { (listMeasurables, detailMeasurables), constraints ->
        val totalWidth = constraints.maxWidth
        val totalHeight = constraints.maxHeight
        val listTargetWidth = (totalWidth * listContentRatio).roundToInt()

        // Measure detail (fixed width logic)
        val detailPlaceable = detailMeasurables.firstOrNull()?.measure(
            Constraints.fixed(width = totalWidth - listTargetWidth, height = totalHeight)
        )

        // Measure list (dynamic width based on progress)
        val currentListWidth = (totalWidth + (listTargetWidth - totalWidth) * progress).roundToInt()
        val listPlaceable = listMeasurables.firstOrNull()?.measure(
            Constraints.fixed(width = currentListWidth, height = totalHeight)
        )

        layout(totalWidth, totalHeight) {
            listPlaceable?.placeRelative(0, 0)
            detailPlaceable?.placeRelative(currentListWidth, 0)
        }
    }
}

@Composable
fun rememberPredictiveBackState(
    targetState: Boolean,
    animationSpec: FiniteAnimationSpec<Float> = tween(),
    onBack: () -> Unit
): SeekableTransitionState<Boolean> {
    val transitionState = remember { SeekableTransitionState(targetState) }
    var inPredictiveBack by remember { mutableStateOf(false) }
    var backProgress by remember { mutableFloatStateOf(0f) }

    PredictiveBackHandler(enabled = targetState) { progressFlow ->
        backProgress = 0f
        inPredictiveBack = true
        try {
            progressFlow.collect { backEvent -> backProgress = backEvent.progress }
            onBack()
        } catch (_: CancellationException) {
            // no-op
        } finally {
            inPredictiveBack = false
        }
    }

    LaunchedEffect(targetState, inPredictiveBack) {
        if (inPredictiveBack) {
            snapshotFlow { backProgress }.collectLatest { progress ->
                transitionState.seekTo(fraction = progress, targetState = false)
            }
        } else {
            val totalDuration = animationSpec.vectorize(Float.VectorConverter).getDurationNanos(
                initialValue = AnimationVector1D(0f),
                targetValue = AnimationVector1D(1f),
                initialVelocity = AnimationVector1D(0f)
            ) / 1_000_000L

            val currentFraction = transitionState.fraction
            val isCommit = transitionState.currentState != targetState
            val isRunning = transitionState.targetState != transitionState.currentState
            val seekTarget = if (isRunning) transitionState.targetState else targetState

            val (remainingDuration, finalFraction) = if (isCommit) {
                ((1f - currentFraction) * totalDuration).roundToInt() to 1f
            } else {
                (currentFraction * totalDuration).roundToInt() to 0f
            }

            if (remainingDuration > 0) {
                animate(
                    initialValue = currentFraction,
                    targetValue = finalFraction,
                    animationSpec = tween(remainingDuration)
                ) { value, _ ->
                    this@LaunchedEffect.launch {
                        if (value != finalFraction) {
                            transitionState.seekTo(fraction = value, targetState = seekTarget)
                        } else {
                            transitionState.snapTo(targetState)
                        }
                    }
                }
            } else {
                transitionState.snapTo(targetState)
            }
        }
    }
    return transitionState
}

@Composable
fun <T : Any> rememberBreezeSinglePaneSceneStrategy(
    isRail: Boolean,
    isRtl: Boolean,
    navAnimSpec: FiniteAnimationSpec<IntOffset>
): SceneStrategy<T> {
    return remember(isRail, isRtl, navAnimSpec) {
        injectMetadataStrategy(SinglePaneSceneStrategy()) { scene ->
            val targetEntry = scene.entries.lastOrNull()
            if (targetEntry?.metadata?.get(TopLevelRouteOrdinal) !is Int) return@injectMetadataStrategy emptyMap()
            calcNavAnim(isRail, isRtl, navAnimSpec)
        }
    }
}

private fun calcNavAnim(
    isRail: Boolean,
    isRtl: Boolean,
    navAnimSpec: FiniteAnimationSpec<IntOffset>,
): Map<String, Any> {

    fun shouldReverse(initialEntry: NavEntry<*>?, targetEntry: NavEntry<*>?): Boolean {
        val sourceOrdinal = initialEntry?.metadata?.get(TopLevelRouteOrdinal) as? Int
        val targetOrdinal = targetEntry?.metadata?.get(TopLevelRouteOrdinal) as? Int
        val targetIsHome = targetEntry?.metadata?.get(TopLevelRouteHome) as? Boolean ?: false
        if (sourceOrdinal == null || targetOrdinal == null) return false
        return targetOrdinal < sourceOrdinal && !targetIsHome
    }

    val enterMap = NavDisplay.transitionSpec {
        val isReverse = shouldReverse(initialState.entries.lastOrNull(), targetState.entries.lastOrNull())
        if (isRail) {
            slideVertical(isReverse, animationSpec = navAnimSpec)
        } else {
            slideHorizontal(isReverse != isRtl, animationSpec = navAnimSpec)
        }
    }

    val popMap = NavDisplay.popTransitionSpec {
        val isReverse = shouldReverse(initialState.entries.lastOrNull(), targetState.entries.lastOrNull())
        if (isRail) {
            slideVertical(!isReverse, animationSpec = navAnimSpec)
        } else {
            slideHorizontal(isReverse == isRtl, animationSpec = navAnimSpec)
        }
    }

    val predictiveMap = NavDisplay.predictivePopTransitionSpec {
        val isReverse = shouldReverse(initialState.entries.lastOrNull(), targetState.entries.lastOrNull())
        if (isRail) {
            slideVertical(!isReverse, animationSpec = navAnimSpec)
        } else {
            slideHorizontal(isReverse == isRtl, animationSpec = navAnimSpec)
        }
    }
    return enterMap + popMap + predictiveMap
}