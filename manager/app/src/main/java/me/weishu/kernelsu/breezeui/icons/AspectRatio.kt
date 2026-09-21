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
internal val aspectRatio: MaterialSymbol = MaterialSymbol(
    name = "aspect_ratio",
    style = SymbolStyle(SymbolFamily.Rounded, SymbolFill.Unfilled, 24, 400, 0, 50),
) {
    ImageVector.Builder(
        name = "AspectRatio",
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
            moveTo(17f, 15f)
            horizontalLineTo(15f)
            quadToRelative(-0.42f, 0f, -0.71f, 0.29f)
            reflectiveQuadTo(14f, 16f)
            reflectiveQuadToRelative(0.29f, 0.71f)
            reflectiveQuadTo(15f, 17f)
            horizontalLineToRelative(3f)
            quadToRelative(0.43f, 0f, 0.71f, -0.29f)
            quadTo(19f, 16.43f, 19f, 16f)
            verticalLineTo(13f)
            quadToRelative(0f, -0.43f, -0.29f, -0.71f)
            reflectiveQuadTo(18f, 12f)
            reflectiveQuadToRelative(-0.71f, 0.29f)
            reflectiveQuadTo(17f, 13f)
            verticalLineToRelative(2f)
            close()
            moveTo(7f, 9f)
            horizontalLineTo(9f)
            quadTo(9.43f, 9f, 9.71f, 8.71f)
            reflectiveQuadTo(10f, 8f)
            quadTo(10f, 7.57f, 9.71f, 7.29f)
            reflectiveQuadTo(9f, 7f)
            horizontalLineTo(6f)
            quadTo(5.58f, 7f, 5.29f, 7.29f)
            reflectiveQuadTo(5f, 8f)
            verticalLineToRelative(3f)
            quadToRelative(0f, 0.42f, 0.29f, 0.71f)
            reflectiveQuadTo(6f, 12f)
            reflectiveQuadTo(6.71f, 11.71f)
            quadTo(7f, 11.43f, 7f, 11f)
            verticalLineTo(9f)
            close()
            moveTo(4f, 20f)
            quadTo(3.18f, 20f, 2.59f, 19.41f)
            reflectiveQuadTo(2f, 18f)
            verticalLineTo(6f)
            quadTo(2f, 5.18f, 2.59f, 4.59f)
            reflectiveQuadTo(4f, 4f)
            horizontalLineTo(20f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            quadTo(22f, 5.18f, 22f, 6f)
            verticalLineTo(18f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(20f, 20f)
            horizontalLineTo(4f)
            close()
            moveTo(4f, 18f)
            horizontalLineTo(20f)
            verticalLineTo(6f)
            horizontalLineTo(4f)
            verticalLineTo(18f)
            close()
            moveToRelative(0f, 0f)
            verticalLineTo(6f)
            verticalLineTo(18f)
            close()
        }
    }.build()
}
