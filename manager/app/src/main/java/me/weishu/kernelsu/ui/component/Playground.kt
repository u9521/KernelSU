package me.weishu.kernelsu.ui.component

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.weishu.kernelsu.ui.component.scrollbar.ScrollbarDefaults
import me.weishu.kernelsu.ui.component.scrollbar.VerticalScrollbar
import me.weishu.kernelsu.ui.component.scrollbar.rememberScrollbarAdapter
import me.weishu.kernelsu.ui.screen.ModuleItemPreview
import me.weishu.kernelsu.ui.theme.KernelSUTheme

@Preview(showBackground = true, name = "ModuleItem Preview panel", device = "spec:width=673dp,height=841dp,orientation=landscape")
@Composable
fun ModuleItemInteractivePreview() {
    KernelSUTheme {

        @Composable
        fun LabeledCheckbox(
            label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = modifier
                    .clickable { onCheckedChange(!checked) } // 让整行都可以点击
                .padding(horizontal = 4.dp, vertical = 2.dp)) {
                Checkbox(
                    checked = checked, onCheckedChange = onCheckedChange
                )
                Spacer(Modifier.width(4.dp))
                Text(text = label)
            }
        }


        var sliderPosition by remember { mutableFloatStateOf(1f) }

        var enabled by remember { mutableStateOf(false) }
        var updating by remember { mutableStateOf(true) }
        var remove by remember { mutableStateOf(false) }
        var hasWebUi by remember { mutableStateOf(false) }
        var hasActionScript by remember { mutableStateOf(true) }
        var hasUpdate by remember { mutableStateOf(true) }
        var metamodule by remember { mutableStateOf(true) }


        val windowInfo = LocalWindowInfo.current
        val screenWidth = windowInfo.containerDpSize.width
        val minWidth = 180.dp
        val dynamicWidth = minWidth + (screenWidth - minWidth) * sliderPosition
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Width: ${dynamicWidth.value.toInt()} dp", style = MaterialTheme.typography.titleMedium
            )
            Slider(
                value = sliderPosition, onValueChange = { sliderPosition = it }, modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text("Toggle Properties", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LabeledCheckbox(label = "Enabled", checked = enabled, onCheckedChange = { enabled = it })
                LabeledCheckbox(label = "Updating", checked = updating, onCheckedChange = { updating = it })
                LabeledCheckbox(label = "Remove", checked = remove, onCheckedChange = { remove = it })
                LabeledCheckbox(label = "Has WebUI", checked = hasWebUi, onCheckedChange = { hasWebUi = it })
                LabeledCheckbox(label = "Has Action", checked = hasActionScript, onCheckedChange = { hasActionScript = it })
                LabeledCheckbox(label = "Has Update", checked = hasUpdate, onCheckedChange = { hasUpdate = it })
                LabeledCheckbox(label = "MetaModule", checked = metamodule, onCheckedChange = { metamodule = it })
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .width(dynamicWidth)
                    .border(Dp.Hairline, MaterialTheme.colorScheme.error)
            ) {
                ModuleItemPreview(
                    enabled = enabled,
                    updating = updating,
                    remove = remove,
                    hasWebUi = hasWebUi,
                    hasActionScript = hasActionScript,
                    hasUpdate = hasUpdate,
                    metamodule = metamodule
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview(showBackground = true, name = "SegmentedListGroup Preview", locale = "zh-rCN")
@Composable
fun SegmentedListGroupPreview() {
    MaterialTheme {
        Box {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .verticalScroll(scrollState)
                    .padding(16.dp)

            ) {
                var switchChecked by remember { mutableStateOf(true) }
                var isMiddleItemVisible by remember { mutableStateOf(true) }
                SegmentedListGroup(title = "通用设置 (General Settings)") {

                    item(
                        leadingContent = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        onClick = { isMiddleItemVisible = !isMiddleItemVisible },
                        supportingContent = { Text("点击我测试动态圆角动画") }) {
                        Text("个人资料 (Profile)")
                    }

                    switchItem(
                        visible = isMiddleItemVisible,
                        leadingContent = { Icon(Icons.Default.Notifications, contentDescription = "Notifications") },
                        title = "接收通知 (Notifications)",
                        summary = if (switchChecked) "已开启推送" else "已关闭推送",
                        checked = switchChecked,
                        onCheckedChange = { switchChecked = it })

                    menuItem(
                        leadingContent = { Icon(Icons.Default.LocationOn, contentDescription = "Language") },
                        selected = "简体中文",
                        menuContent = { dismissMenu ->
                            DropdownMenuItem(
                                text = { Text("English") }, onClick = dismissMenu
                            )
                            DropdownMenuItem(
                                text = { Text("简体中文") }, onClick = dismissMenu
                            )
                        },
                        content = { Text("语言 (Language)") })

                    item(
                        leadingContent = { Icon(Icons.Default.Info, contentDescription = "About") },
                        trailingContent = { Text("v114.514.0", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        onClick = {}) {
                        Text("关于版本 (About)")
                    }

                    item(
                        enabled = false, leadingContent = { Icon(Icons.Default.Lock, contentDescription = "Developer") }) {
                        Text("开发者选项 (Developer Options)")
                    }
                }
                SegmentedListGroup(title = "scroll test") {
                    repeat(20) {
                        item {
                            Text("Line ${it + 1}")
                        }
                    }
                }
            }
            VerticalScrollbar(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight(),
                durationMillis = 1500,
                enter = slideInHorizontally { it },
                exit = slideOutHorizontally { it },
                style = ScrollbarDefaults.style.copy(railColor = MaterialTheme.colorScheme.onSurfaceVariant, color = MaterialTheme.colorScheme.primary),
                adapter = rememberScrollbarAdapter(scrollState)
            )
        }
    }
}

data class ColorPreviewItem(
    val name: String, val backgroundColor: Color, val textColor: Color
)

@Composable
private fun ColorSchemeGrid(colorScheme: ColorScheme) {
    val colorItems = remember(colorScheme) {
        listOf(
            // Primary
            ColorPreviewItem("Primary", colorScheme.primary, colorScheme.onPrimary),
            ColorPreviewItem("Primary Container", colorScheme.primaryContainer, colorScheme.onPrimaryContainer),
            ColorPreviewItem("Inverse Primary", colorScheme.inversePrimary, colorScheme.primary),

            // Secondary
            ColorPreviewItem("Secondary", colorScheme.secondary, colorScheme.onSecondary),
            ColorPreviewItem("Secondary Container", colorScheme.secondaryContainer, colorScheme.onSecondaryContainer),

            // Tertiary
            ColorPreviewItem("Tertiary", colorScheme.tertiary, colorScheme.onTertiary),
            ColorPreviewItem("Tertiary Container", colorScheme.tertiaryContainer, colorScheme.onTertiaryContainer),

            // Background & Surface
            ColorPreviewItem("Background", colorScheme.background, colorScheme.onBackground),
            ColorPreviewItem("Surface", colorScheme.surface, colorScheme.onSurface),
            ColorPreviewItem("Surface Variant", colorScheme.surfaceVariant, colorScheme.onSurfaceVariant),
            ColorPreviewItem("Surface Tint", colorScheme.surfaceTint, colorScheme.onPrimary),
            ColorPreviewItem("Inverse Surface", colorScheme.inverseSurface, colorScheme.inverseOnSurface),

            // Error
            ColorPreviewItem("Error", colorScheme.error, colorScheme.onError),
            ColorPreviewItem("Error Container", colorScheme.errorContainer, colorScheme.onErrorContainer),

            // Outline & Scrim (无直接对应的on属性，使用合理的对比色)
            ColorPreviewItem("Outline", colorScheme.outline, colorScheme.surface),
            ColorPreviewItem("Outline Variant", colorScheme.outlineVariant, colorScheme.onSurface),
            ColorPreviewItem("Scrim", colorScheme.scrim, Color.White),

            // Surface Containers
            ColorPreviewItem("Surface Bright", colorScheme.surfaceBright, colorScheme.onSurface),
            ColorPreviewItem("Surface Dim", colorScheme.surfaceDim, colorScheme.onSurface),
            ColorPreviewItem("Surface Container Lowest", colorScheme.surfaceContainerLowest, colorScheme.onSurface),
            ColorPreviewItem("Surface Container Low", colorScheme.surfaceContainerLow, colorScheme.onSurface),
            ColorPreviewItem("Surface Container", colorScheme.surfaceContainer, colorScheme.onSurface),
            ColorPreviewItem("Surface Container High", colorScheme.surfaceContainerHigh, colorScheme.onSurface),
            ColorPreviewItem("Surface Container Highest", colorScheme.surfaceContainerHighest, colorScheme.onSurface),

            // Primary Fixed
            ColorPreviewItem("Primary Fixed", colorScheme.primaryFixed, colorScheme.onPrimaryFixed),
            ColorPreviewItem("Primary Fixed Dim", colorScheme.primaryFixedDim, colorScheme.onPrimaryFixedVariant),

            // Secondary Fixed
            ColorPreviewItem("Secondary Fixed", colorScheme.secondaryFixed, colorScheme.onSecondaryFixed),
            ColorPreviewItem("Secondary Fixed Dim", colorScheme.secondaryFixedDim, colorScheme.onSecondaryFixedVariant),

            // Tertiary Fixed
            ColorPreviewItem("Tertiary Fixed", colorScheme.tertiaryFixed, colorScheme.onTertiaryFixed),
            ColorPreviewItem("Tertiary Fixed Dim", colorScheme.tertiaryFixedDim, colorScheme.onTertiaryFixedVariant)
        )
    }
    Box(Modifier.fillMaxSize()) {
        val lazyGridState = rememberLazyGridState()

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = lazyGridState,
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(colorItems) { item ->
                ColorCard(item)
            }
        }
        VerticalScrollbar(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(),
            adapter = rememberScrollbarAdapter(lazyGridState),
            durationMillis = 1500,
            style = ScrollbarDefaults.style.copy(
                color = MaterialTheme.colorScheme.primary, railColor = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
private fun ColorCard(item: ColorPreviewItem) {
    Surface(
        color = item.backgroundColor, contentColor = item.textColor, shape = RoundedCornerShape(12.dp), modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = item.name, textAlign = TextAlign.Center, fontSize = 13.sp, fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
fun ColorSchemePreview() {
    KernelSUTheme {
        ColorSchemeGrid(colorScheme = MaterialTheme.colorScheme)
    }
}