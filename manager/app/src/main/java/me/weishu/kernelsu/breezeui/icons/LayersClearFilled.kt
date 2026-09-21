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
internal val layersClearFilled: MaterialSymbol = MaterialSymbol(
    name = "layers_clear",
    style = SymbolStyle.FilledRounded,
) {
    ImageVector.Builder(
        name = "LayersClear",
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
            moveTo(18.98f, 7.43f)
            quadTo(19.75f, 8.02f, 19.75f, 9f)
            reflectiveQuadToRelative(-0.77f, 1.57f)
            lineTo(16.8f, 12.25f)
            quadToRelative(-0.3f, 0.22f, -0.68f, 0.21f)
            reflectiveQuadTo(15.48f, 12.18f)
            lineTo(9.05f, 5.7f)
            quadTo(8.73f, 5.38f, 8.76f, 4.93f)
            quadTo(8.8f, 4.47f, 9.15f, 4.2f)
            lineTo(10.78f, 2.95f)
            quadTo(11.33f, 2.52f, 12f, 2.52f)
            reflectiveQuadToRelative(1.23f, 0.43f)
            lineToRelative(5.75f, 4.48f)
            close()
            moveTo(19.1f, 21.4f)
            lineTo(15.8f, 18.1f)
            lineToRelative(-2.57f, 2f)
            quadTo(12.68f, 20.53f, 12f, 20.53f)
            reflectiveQuadTo(10.78f, 20.1f)
            lineTo(4.03f, 14.85f)
            quadTo(3.63f, 14.55f, 3.64f, 14.06f)
            reflectiveQuadTo(4.05f, 13.27f)
            quadToRelative(0.28f, -0.2f, 0.6f, -0.2f)
            reflectiveQuadToRelative(0.6f, 0.2f)
            lineTo(12f, 18.5f)
            lineToRelative(2.35f, -1.82f)
            lineTo(12.6f, 14.98f)
            horizontalLineToRelative(0.72f)
            lineToRelative(-0.1f, 0.05f)
            quadTo(12.68f, 15.45f, 12f, 15.46f)
            reflectiveQuadTo(10.78f, 15.05f)
            lineTo(5.03f, 10.58f)
            quadTo(4.25f, 9.98f, 4.25f, 9f)
            quadToRelative(0f, -0.98f, 0.78f, -1.57f)
            lineTo(5.08f, 7.38f)
            lineTo(2.1f, 4.42f)
            quadTo(1.8f, 4.13f, 1.79f, 3.71f)
            reflectiveQuadTo(2.08f, 3f)
            reflectiveQuadTo(2.79f, 2.7f)
            reflectiveQuadTo(3.5f, 3f)
            lineToRelative(17f, 17f)
            quadToRelative(0.28f, 0.27f, 0.28f, 0.7f)
            reflectiveQuadTo(20.5f, 21.4f)
            quadToRelative(-0.27f, 0.28f, -0.7f, 0.28f)
            reflectiveQuadTo(19.1f, 21.4f)
            close()
            moveToRelative(0.88f, -8.13f)
            quadToRelative(0.38f, 0.3f, 0.38f, 0.78f)
            quadToRelative(0f, 0.48f, -0.38f, 0.8f)
            lineToRelative(-0.3f, 0.25f)
            quadTo(19.38f, 15.35f, 19f, 15.33f)
            reflectiveQuadToRelative(-0.65f, -0.3f)
            quadTo(18.03f, 14.7f, 18.05f, 14.24f)
            reflectiveQuadToRelative(0.4f, -0.74f)
            lineToRelative(0.3f, -0.23f)
            quadToRelative(0.28f, -0.2f, 0.61f, -0.2f)
            reflectiveQuadToRelative(0.61f, 0.2f)
            close()
        }
    }.build()
}
