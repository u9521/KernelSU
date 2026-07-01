package me.weishu.kernelsu.ui.component.breeze

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import me.weishu.kernelsu.ui.theme.LocalEnableBlur

@Composable
fun BreezeBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    collapseFraction: Float = 0f,
    contentDescription: String? = null,
) {
    BreezeActionButton(onClick = onClick, modifier = modifier, collapseFraction = collapseFraction) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = contentDescription,
        )
    }
}

@Composable
fun BreezeActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    collapseFraction: Float = 0f,
    icon: @Composable () -> Unit,
) {
    val enableBlur = LocalEnableBlur.current
    val eased = collapseFraction.coerceIn(0f, 1f)
    val bgAlpha = if (enableBlur) lerp(start = 1f, stop = 0.6f, fraction = eased) else 1f
    val bgColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = bgAlpha)

    IconButton(
        modifier = modifier.padding(horizontal = 10.dp),
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = bgColor,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        icon()
    }
}
