package me.weishu.kernelsu.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val YELLOW = Color(0xFFeed502)
val YELLOW_LIGHT = Color(0xFFffff52)
val SECONDARY_LIGHT = Color(0xffa9817f)

val YELLOW_DARK = Color(0xFFb7a400)
val SECONDARY_DARK = Color(0xFF4c2b2b)

@Composable
fun defaultTopAppBarColors() = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.surfaceContainer, scrolledContainerColor =
        MaterialTheme.colorScheme.surfaceBright
)