package me.weishu.kernelsu.ui.playground.breeze

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.ui.component.breeze.BrMenuBox

@Composable
fun BreezeMenuPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "BreezeMenu — Dropdown Menu Box",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        BrMenuBox(
            menuContent = { dismissMenu ->
                DropdownMenuGroup(shapes = MenuDefaults.groupShapes()) {
                    DropdownMenuItem(
                        text = { Text("Item Alpha") },
                        onClick = { dismissMenu() }
                    )
                    DropdownMenuItem(
                        text = { Text("Item Beta") },
                        onClick = { dismissMenu() }
                    )
                    DropdownMenuItem(
                        text = { Text("Item Gamma (disabled)") },
                        onClick = {},
                        enabled = false
                    )
                }

            },
            content = {
                Button(onClick = {}) {
                    Text("Open Menu")
                }
            }
        )

        Spacer(Modifier.height(16.dp))
        Text(
            "The button above triggers a dropdown menu with PopupFeedBack haptics.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBreezeMenu() {
    BreezeMenuPreview()
}
