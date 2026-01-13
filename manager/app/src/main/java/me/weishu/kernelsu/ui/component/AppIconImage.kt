package me.weishu.kernelsu.ui.component

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.graphics.drawable.Drawable
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.ui.util.DrawablePainter

@Composable
fun AppIconImage(applicationInfo: ApplicationInfo, modifier: Modifier = Modifier, contentDescription: String? = null) {
    val context = LocalContext.current
    val appIconState = produceState<Drawable?>(initialValue = null, key1 = applicationInfo) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val icon = pm.getApplicationIcon(applicationInfo)
            value = icon
        }
    }
    val appLabelState = produceState(initialValue = "", key1 = applicationInfo) {
        if (contentDescription != null) {
            value = contentDescription
        } else {
            withContext(Dispatchers.IO) {
                val pm = context.packageManager
                val label = pm.getApplicationLabel(applicationInfo).toString()
                value = label
            }
        }
    }
    Crossfade(
        targetState = appIconState.value,
        animationSpec = tween(durationMillis = 150),
        label = "IconFade"
    ) { icon ->
        if (icon != null) {
            val painter = remember(icon) { DrawablePainter(icon) }
            Image(
                painter = painter,
                contentDescription = appLabelState.value,
                modifier = modifier
            )
        } else {
            Box(
                modifier = modifier
            )
        }
    }
}


@Composable
fun AppIconImage(packageInfo: PackageInfo, modifier: Modifier = Modifier, contentDescription: String? = null) {
    val appInfo = packageInfo.applicationInfo ?: return
    AppIconImage(appInfo, modifier, contentDescription)
}

