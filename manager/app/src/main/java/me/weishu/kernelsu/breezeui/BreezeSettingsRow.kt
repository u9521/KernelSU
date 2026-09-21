package me.weishu.kernelsu.breezeui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.R
import me.weishu.kernelsu.breezeui.icons.MaterialSymbols
import me.weishu.kernelsu.ui.component.material.SegmentedSwitchItem
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * The Breeze switch, rendered inside the Material and Miuix settings screens.
 *
 * Upstream files only ever call this one composable (a single inserted line); the
 * "UI mode" dropdown itself stays untouched. Turning the switch on hands the whole
 * UI over to Breeze, turning it off returns to the theme of the hosting screen.
 */
@Composable
fun BreezeSettingsRowMaterial() {
    SegmentedSwitchItem(
        icon = MaterialSymbols.Filled.Rounded.Style,
        title = BREEZE_LABEL,
        summary = stringResource(id = R.string.settings_ui_mode_summary),
        checked = BreezeMode.isEnabled(),
        onCheckedChange = { checked ->
            BreezeMode.select(if (checked) BreezeMode.VALUE else BreezeMode.MATERIAL)
        },
    )
}

@Composable
fun BreezeSettingsRowMiuix() {
    val colorScheme = MiuixTheme.colorScheme
    SwitchPreference(
        title = BREEZE_LABEL,
        summary = stringResource(id = R.string.settings_ui_mode_summary),
        startAction = {
            Icon(
                MaterialSymbols.Filled.Rounded.Style,
                modifier = Modifier.padding(end = 6.dp),
                contentDescription = BREEZE_LABEL,
                tint = colorScheme.onBackground,
            )
        },
        checked = BreezeMode.isEnabled(),
        onCheckedChange = { checked ->
            BreezeMode.select(if (checked) BreezeMode.VALUE else BreezeMode.MIUIX)
        },
    )
}

/** Product name, deliberately not translated. */
private const val BREEZE_LABEL = "Breeze"
