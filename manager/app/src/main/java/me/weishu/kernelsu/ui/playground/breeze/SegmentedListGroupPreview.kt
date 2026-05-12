package me.weishu.kernelsu.ui.playground.breeze

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.ui.component.breeze.SegmentedListGroup

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SegmentedListGroupPreview() {
    var wifiEnabled by remember { mutableStateOf(true) }
    var bluetoothEnabled by remember { mutableStateOf(false) }
    var nfcEnabled by remember { mutableStateOf(true) }
    var airplaneMode by remember { mutableStateOf(false) }
    var customDpi by remember { mutableStateOf(false) }
    var verboseLogging by remember { mutableStateOf(true) }

    var sortOrder by remember { mutableStateOf("Name") }
    var themeMode by remember { mutableStateOf("System") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "SegmentedListGroup — All Item Types",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        SegmentedListGroup(title = "Connectivity") {
            switchItem(
                title = "Wi-Fi",
                summary = "Enable or disable Wi-Fi",
                checked = { wifiEnabled },
                onCheckedChange = { wifiEnabled = it }
            )
            switchItem(
                title = "Bluetooth",
                summary = "Enable or disable Bluetooth",
                checked = { bluetoothEnabled },
                onCheckedChange = { bluetoothEnabled = it }
            )
            switchItem(
                title = "NFC",
                summary = "Near Field Communication",
                checked = { nfcEnabled },
                onCheckedChange = { nfcEnabled = it }
            )
            switchItem(
                title = "Airplane Mode",
                summary = "Disable all wireless radios",
                checked = { airplaneMode },
                onCheckedChange = { airplaneMode = it }
            )
        }

        SegmentedListGroup(title = "Appearance") {
            menuItem(
                content = { Text("Sort Order") },
                selected = { sortOrder },
                menuContent = { dismiss ->
                    DropdownMenuItem(
                        text = { Text("Name") },
                        onClick = { sortOrder = "Name"; dismiss() }
                    )
                    DropdownMenuItem(
                        text = { Text("Size") },
                        onClick = { sortOrder = "Size"; dismiss() }
                    )
                    DropdownMenuItem(
                        text = { Text("Date") },
                        onClick = { sortOrder = "Date"; dismiss() }
                    )
                }
            )
            menuItem(
                content = { Text("Theme Mode") },
                selected = { themeMode },
                menuContent = { dismiss ->
                    DropdownMenuItem(
                        text = { Text("System") },
                        onClick = { themeMode = "System"; dismiss() }
                    )
                    DropdownMenuItem(
                        text = { Text("Light") },
                        onClick = { themeMode = "Light"; dismiss() }
                    )
                    DropdownMenuItem(
                        text = { Text("Dark") },
                        onClick = { themeMode = "Dark"; dismiss() }
                    )
                    DropdownMenuItem(
                        text = { Text("Battery Saver") },
                        onClick = { themeMode = "Battery Saver"; dismiss() }
                    )
                }
            )
        }

        SegmentedListGroup(title = "Developer Options") {
            switchItem(
                title = "Custom DPI",
                summary = "Override screen density",
                checked = { customDpi },
                onCheckedChange = { customDpi = it }
            )
            checkboxItem(
                title = "Verbose Logging",
                summary = "Enable detailed debug logs",
                checked = { verboseLogging },
                onCheckedChange = { verboseLogging = it }
            )
            item(
                onClick = {},
                content = { Text("Manage Developer Settings") }
            )
        }

        Text(
            "Wi-Fi: ${if (wifiEnabled) "ON" else "OFF"}  ·  " +
                    "Sort: $sortOrder  ·  " +
                    "Theme: $themeMode",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSegmentedListGroup() {
    SegmentedListGroupPreview()
}
