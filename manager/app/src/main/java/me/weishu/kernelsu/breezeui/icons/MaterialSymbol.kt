package me.weishu.kernelsu.breezeui.icons

import androidx.compose.ui.graphics.vector.ImageVector

/** Material Symbols family, carrying the Google Fonts family it maps to. */
enum class SymbolFamily(val fontFamily: String) {
    /** Only families that are actually generated are declared; add more when needed. */
    Rounded("Material Symbols Rounded"),
}

/** The `FILL` axis: [Filled] is 1, [Unfilled] is 0. */
enum class SymbolFill(val value: Int) { Filled(1), Unfilled(0) }

/**
 * The Material Symbols axes an icon was generated with.
 *
 * Ranges: `opsz` 20..48, `wght` 100..700, `grad` -50..200, `rond` 0..100.
 * The Google Fonts render endpoint currently ignores `rond`, it is pinned here
 * anyway so the generated output cannot drift once that axis is implemented.
 */
data class SymbolStyle(
    val family: SymbolFamily,
    val fill: SymbolFill,
    val opsz: Int,
    val wght: Int,
    val grad: Int,
    val rond: Int,
) {
    /** The Google Fonts family this style is rendered from. */
    val fontFamily: String get() = family.fontFamily

    companion object {
        /** The style used across the app: Material Symbols Rounded, FILL=1. */
        val FilledRounded = SymbolStyle(SymbolFamily.Rounded, SymbolFill.Filled, 24, 400, 0, 50)
    }
}

/**
 * A single Material Symbols icon: its source name, the axes it was generated
 * with and its vector.
 */
class MaterialSymbol(
    val name: String,
    val style: SymbolStyle,
    build: () -> ImageVector,
) {
    val vector: ImageVector by lazy(build)
}
