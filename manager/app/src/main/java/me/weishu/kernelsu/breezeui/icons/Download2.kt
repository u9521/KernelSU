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
internal val download2: MaterialSymbol = MaterialSymbol(
    name = "download_2",
    style = SymbolStyle(SymbolFamily.Rounded, SymbolFill.Unfilled, 24, 400, 0, 50),
) {
    ImageVector.Builder(
        name = "Download2",
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
            moveTo(5f, 22f)
            quadTo(4.58f, 22f, 4.29f, 21.71f)
            quadTo(4f, 21.43f, 4f, 21f)
            reflectiveQuadTo(4.29f, 20.29f)
            reflectiveQuadTo(5f, 20f)
            horizontalLineTo(19f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(20f, 21f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(19f, 22f)
            horizontalLineTo(5f)
            close()
            moveToRelative(6.56f, -4.73f)
            quadToRelative(-0.21f, -0.1f, -0.36f, -0.3f)
            lineTo(6.25f, 10.63f)
            quadTo(5.88f, 10.13f, 6.15f, 9.56f)
            reflectiveQuadTo(7.05f, 9f)
            horizontalLineTo(9f)
            verticalLineTo(3f)
            quadTo(9f, 2.57f, 9.29f, 2.29f)
            quadTo(9.58f, 2f, 10f, 2f)
            horizontalLineToRelative(4f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(15f, 3f)
            verticalLineTo(9f)
            horizontalLineToRelative(1.95f)
            quadToRelative(0.63f, 0f, 0.9f, 0.56f)
            reflectiveQuadToRelative(-0.1f, 1.06f)
            lineTo(12.8f, 16.98f)
            quadToRelative(-0.15f, 0.2f, -0.36f, 0.3f)
            quadToRelative(-0.21f, 0.1f, -0.44f, 0.1f)
            quadToRelative(-0.22f, 0f, -0.44f, -0.1f)
            close()
            moveTo(12f, 14.75f)
            lineTo(14.9f, 11f)
            horizontalLineTo(13f)
            verticalLineTo(4f)
            horizontalLineTo(11f)
            verticalLineToRelative(7f)
            horizontalLineTo(9.1f)
            lineTo(12f, 14.75f)
            close()
            moveTo(12f, 11f)
            close()
        }
    }.build()
}
