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
internal val wysiwyg: MaterialSymbol = MaterialSymbol(
    name = "wysiwyg",
    style = SymbolStyle(SymbolFamily.Rounded, SymbolFill.Unfilled, 24, 400, 0, 50),
) {
    ImageVector.Builder(
        name = "Wysiwyg",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
        autoMirror = true,
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
            moveTo(5f, 21f)
            quadTo(4.18f, 21f, 3.59f, 20.41f)
            reflectiveQuadTo(3f, 19f)
            verticalLineTo(5f)
            quadTo(3f, 4.17f, 3.59f, 3.59f)
            reflectiveQuadTo(5f, 3f)
            horizontalLineTo(19f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(21f, 5f)
            verticalLineTo(19f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(19f, 21f)
            horizontalLineTo(5f)
            close()
            moveTo(5f, 19f)
            horizontalLineTo(19f)
            verticalLineTo(7f)
            horizontalLineTo(5f)
            verticalLineTo(19f)
            close()
            moveTo(8f, 12f)
            quadTo(7.58f, 12f, 7.29f, 11.71f)
            quadTo(7f, 11.43f, 7f, 11f)
            reflectiveQuadTo(7.29f, 10.29f)
            reflectiveQuadTo(8f, 10f)
            horizontalLineToRelative(8f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(17f, 11f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(16f, 12f)
            horizontalLineTo(8f)
            close()
            moveToRelative(0f, 4f)
            quadTo(7.58f, 16f, 7.29f, 15.71f)
            reflectiveQuadTo(7f, 15f)
            reflectiveQuadTo(7.29f, 14.29f)
            reflectiveQuadTo(8f, 14f)
            horizontalLineToRelative(4f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(13f, 15f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(12f, 16f)
            horizontalLineTo(8f)
            close()
        }
    }.build()
}
