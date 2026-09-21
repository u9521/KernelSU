package me.weishu.kernelsu.breezeui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
internal val brightness2Filled: MaterialSymbol = MaterialSymbol(
    name = "brightness_2",
    style = SymbolStyle.FilledRounded,
) {
    ImageVector.Builder(
        name = "Brightness2",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.NonZero,
        ) {
            moveTo(9.5f, 22f)
            quadTo(8.63f, 22f, 7.75f, 21.83f)
            reflectiveQuadTo(6.08f, 21.3f)
            quadTo(5.8f, 21.18f, 5.63f, 20.93f)
            reflectiveQuadTo(5.45f, 20.38f)
            quadToRelative(0f, -0.23f, 0.1f, -0.43f)
            reflectiveQuadTo(5.85f, 19.6f)
            quadTo(7.6f, 18.23f, 8.55f, 16.23f)
            reflectiveQuadTo(9.5f, 12f)
            quadTo(9.5f, 9.77f, 8.54f, 7.79f)
            quadTo(7.58f, 5.8f, 5.83f, 4.4f)
            quadTo(5.65f, 4.25f, 5.55f, 4.05f)
            reflectiveQuadTo(5.45f, 3.63f)
            quadToRelative(0f, -0.3f, 0.16f, -0.55f)
            reflectiveQuadTo(6.05f, 2.7f)
            quadTo(6.88f, 2.35f, 7.75f, 2.17f)
            reflectiveQuadTo(9.5f, 2f)
            quadToRelative(2.08f, 0f, 3.9f, 0.79f)
            reflectiveQuadToRelative(3.17f, 2.14f)
            quadToRelative(1.35f, 1.35f, 2.14f, 3.17f)
            quadTo(19.5f, 9.92f, 19.5f, 12f)
            reflectiveQuadToRelative(-0.79f, 3.9f)
            reflectiveQuadToRelative(-2.14f, 3.17f)
            quadToRelative(-1.35f, 1.35f, -3.17f, 2.14f)
            reflectiveQuadTo(9.5f, 22f)
            close()
        }
    }.build()
}
