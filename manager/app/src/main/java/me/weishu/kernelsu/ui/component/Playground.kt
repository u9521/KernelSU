package me.weishu.kernelsu.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.ui.screen.ModuleItemPreview
import me.weishu.kernelsu.ui.theme.KernelSUTheme

@Preview(showBackground = true, name = "ModuleItem Preview panel", device = "spec:width=673dp,height=841dp,orientation=landscape")
@Composable
fun ModuleItemInteractivePreview() {
    KernelSUTheme {

        @Composable
        fun LabeledCheckbox(
            label: String,
            checked: Boolean,
            onCheckedChange: (Boolean) -> Unit,
            modifier: Modifier = Modifier
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
                    .clickable { onCheckedChange(!checked) } // 让整行都可以点击
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = onCheckedChange
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
                text = "Width: ${dynamicWidth.value.toInt()} dp",
                style = MaterialTheme.typography.titleMedium
            )
            Slider(
                value = sliderPosition,
                onValueChange = { sliderPosition = it },
                modifier = Modifier.fillMaxWidth()
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
@Preview(showBackground = true, name = "SegmentedListGroup Preview")
@Composable
fun SegmentedListGroupPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(16.dp)
        ) {
            var switchChecked by remember { mutableStateOf(true) }
            var isMiddleItemVisible by remember { mutableStateOf(true) }

            SegmentedListGroup(title = "通用设置 (General Settings)") {

                item(
                    leadingContent = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    onClick = { isMiddleItemVisible = !isMiddleItemVisible },
                    supportingContent = { Text("点击我测试动态圆角动画") }
                ) {
                    Text("个人资料 (Profile)")
                }

                switchItem(
                    visible = isMiddleItemVisible,
                    leadingContent = { Icon(Icons.Default.Notifications, contentDescription = "Notifications") },
                    title = "接收通知 (Notifications)",
                    summary = if (switchChecked) "已开启推送" else "已关闭推送",
                    checked = switchChecked,
                    onCheckedChange = { switchChecked = it }
                )

                menuItem(
                    leadingContent = { Icon(Icons.Default.LocationOn, contentDescription = "Language") },
                    selected = "简体中文",
                    menuContent = { dismissMenu ->
                        DropdownMenuItem(
                            text = { Text("English") },
                            onClick = dismissMenu
                        )
                        DropdownMenuItem(
                            text = { Text("简体中文") },
                            onClick = dismissMenu
                        )
                    }, content = { Text("语言 (Language)") }
                )

                item(
                    leadingContent = { Icon(Icons.Default.Info, contentDescription = "About") },
                    trailingContent = { Text("v114.514.0", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    onClick = {}
                ) {
                    Text("关于版本 (About)")
                }

                item(
                    enabled = false,
                    leadingContent = { Icon(Icons.Default.Lock, contentDescription = "Developer") }
                ) {
                    Text("开发者选项 (Developer Options)")
                }
            }
        }
    }
}