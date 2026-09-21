package me.weishu.kernelsu.breezeui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import me.weishu.kernelsu.data.repository.SettingsRepositoryImpl
import me.weishu.kernelsu.ksuApp
import me.weishu.kernelsu.ui.theme.ColorMode

/**
 * Breeze's on/off state and the switch that flips it.
 *
 * Breeze deliberately does not add a constant to the upstream `UiMode` enum: Kotlin
 * requires every `when (uiMode)` to be exhaustive, so a third constant would force
 * Breeze branches into all of them and would break again the moment upstream
 * reshapes the enum. Instead the `ui_mode` preference — already a plain string —
 * gains a third value that only this object interprets.
 *
 * `SuFilePathHandler` has to be taught that value as well: it decides whether the
 * module WebUI gets served `internal/colors.css`, and upstream only accepts
 * `material` or a Monet color mode there, so Breeze keeps a two-line delta in that
 * handler. Dropping it silently leaves module WebUIs without Breeze's palette.
 * `UiMode` itself stays exactly as upstream ships it.
 */
object BreezeMode {
    /** `ui_mode` values. `breeze` is Breeze's own; the other two are upstream's. */
    const val VALUE = "breeze"
    const val MIUIX = "miuix"
    const val MATERIAL = "material"

    private const val SETTINGS_PREFS = "settings"
    private const val KEY_UI_MODE = "ui_mode"

    private fun prefs() = ksuApp.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)

    fun isEnabled(): Boolean = currentMode() == VALUE

    /** The current UI mode: `breeze` unless the user picked Material or Miuix. */
    fun currentMode(): String = prefs().getString(KEY_UI_MODE, VALUE) ?: VALUE

    /**
     * Switches the whole app to [mode], keeping the monet/theme combination coherent
     * the same way the upstream settings screen does.
     */
    fun select(mode: String) {
        val repo = SettingsRepositoryImpl()
        val oldIsMiuix = repo.uiMode == MIUIX
        val newIsMiuix = mode == MIUIX

        if (oldIsMiuix != newIsMiuix) {
            val currentThemeMode = repo.themeMode
            val colorMode = ColorMode.fromValue(currentThemeMode)
            repo.themeMode = if (newIsMiuix) {
                // Breeze and Material are both Monet-capable, Miuix keeps its own switch.
                val baseMode = if (colorMode == ColorMode.DARK_AMOLED) ColorMode.DARK.value else currentThemeMode
                if (repo.miuixMonet && !colorMode.isMonet) {
                    ColorMode.fromValue(baseMode).toMonetMode()
                } else if (!repo.miuixMonet && colorMode.isMonet) {
                    ColorMode.fromValue(baseMode).toNonMonetMode()
                } else {
                    baseMode
                }
            } else {
                if (colorMode.isMonet) colorMode.toNonMonetMode() else currentThemeMode
            }
        }

        repo.uiMode = mode
    }

    /**
     * Observes `ui_mode` so switching Breeze on or off takes effect immediately.
     * Reading the preference through `MainActivityViewModel` is not enough: switching
     * Miuix -> breeze leaves the upstream enum unchanged, so no state would be emitted.
     */
    @Composable
    fun rememberIsEnabled(): Boolean {
        val context = LocalContext.current
        val prefs = remember(context) { context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE) }
        var enabled by remember(prefs) { mutableStateOf(prefs.getString(KEY_UI_MODE, VALUE) == VALUE) }

        DisposableEffect(prefs) {
            val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == KEY_UI_MODE) {
                    enabled = prefs.getString(KEY_UI_MODE, VALUE) == VALUE
                }
            }
            prefs.registerOnSharedPreferenceChangeListener(listener)
            onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
        }

        return enabled
    }
}
