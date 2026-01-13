package me.weishu.kernelsu.ui.util

import android.graphics.drawable.Drawable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.withSave

class DrawablePainter(private val drawable: Drawable) : Painter() {

    override val intrinsicSize: Size
        get() = Size(
            width = drawable.intrinsicWidth.toFloat(), height = drawable.intrinsicHeight.toFloat()
        )

    init {
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    }

    override fun DrawScope.onDraw() {
        drawContext.canvas.withSave {
            drawable.setBounds(0, 0, size.width.toInt(), size.height.toInt())
            drawable.draw(drawContext.canvas.nativeCanvas)
        }
    }
}
