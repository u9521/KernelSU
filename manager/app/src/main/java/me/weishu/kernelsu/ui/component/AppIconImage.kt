package me.weishu.kernelsu.ui.component

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.ui.util.AppIconCache

@Composable
fun AppIconImage(
    modifier: Modifier = Modifier,
    applicationInfo: ApplicationInfo,
    label: String? = null
) {
    val density = LocalDensity.current
    val context = LocalContext.current

    BoxWithConstraints(modifier = modifier) {
        val targetSizePx = remember(constraints, density) {
            if (constraints.maxWidth != Constraints.Infinity) {
                constraints.maxWidth
            } else {
                with(density) { 48.dp.roundToPx() }
            }
        }
        var appIcon by remember { mutableStateOf<ImageBitmap?>(null) }

        LaunchedEffect(applicationInfo) {
            val loadedIcon = AppIconCache.loadIcon(context, applicationInfo, targetSizePx)
            appIcon = loadedIcon.asImageBitmap()
        }

        val appLabel by produceState(initialValue = label, key1 = applicationInfo) {
            if (label != null) {
                value = label
            } else {
                withContext(Dispatchers.IO) {
                    val pm = context.packageManager
                    val appLabel = pm.getApplicationLabel(applicationInfo).toString()
                    value = appLabel
                }
            }
        }

        Crossfade(
            targetState = appIcon, animationSpec = tween(durationMillis = 150), label = "IconFade"
        ) { icon ->
            if (icon == null) {
                PlaceHolderBox(Modifier.fillMaxSize())
            } else {
                Image(
                    bitmap = icon,
                    contentDescription = appLabel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun AppIconImage(modifier: Modifier = Modifier, packageInfo: PackageInfo, label: String? = null) {
    val appInfo = packageInfo.applicationInfo
    if (appInfo == null) {
        PlaceHolderBox(modifier)
        return
    }
    AppIconImage(modifier, appInfo, label)
}

@Composable
private fun PlaceHolderBox(modifier: Modifier = Modifier) {
    Box(modifier = modifier)
}
