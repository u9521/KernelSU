package me.weishu.kernelsu.ui

import androidx.compose.runtime.staticCompositionLocalOf

enum class UiMode(val value: String) {
    Miuix("miuix"),
    Material("material"),
    Breeze("breeze");

    companion object {
        fun fromValue(value: String): UiMode = when (value) {
            Material.value -> Material
            Breeze.value -> Breeze
            Miuix.value -> Miuix
            else -> Breeze
        }

        val DEFAULT_VALUE = Breeze.value
    }
}

val LocalUiMode = staticCompositionLocalOf { UiMode.Breeze }
