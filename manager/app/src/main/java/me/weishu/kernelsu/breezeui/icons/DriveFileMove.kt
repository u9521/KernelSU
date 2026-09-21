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
internal val driveFileMove: MaterialSymbol = MaterialSymbol(
    name = "drive_file_move",
    style = SymbolStyle(SymbolFamily.Rounded, SymbolFill.Unfilled, 24, 400, 0, 50),
) {
    ImageVector.Builder(
        name = "DriveFileMove",
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
            moveTo(4f, 20f)
            quadTo(3.18f, 20f, 2.59f, 19.41f)
            reflectiveQuadTo(2f, 18f)
            verticalLineTo(6f)
            quadTo(2f, 5.18f, 2.59f, 4.59f)
            reflectiveQuadTo(4f, 4f)
            horizontalLineTo(9.18f)
            quadToRelative(0.4f, 0f, 0.76f, 0.15f)
            reflectiveQuadToRelative(0.64f, 0.43f)
            lineTo(12f, 6f)
            horizontalLineToRelative(8f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            quadTo(22f, 7.18f, 22f, 8f)
            verticalLineTo(18f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(20f, 20f)
            horizontalLineTo(4f)
            close()
            moveTo(4f, 18f)
            horizontalLineTo(20f)
            verticalLineTo(8f)
            horizontalLineTo(11.18f)
            lineToRelative(-2f, -2f)
            horizontalLineTo(4f)
            verticalLineTo(18f)
            close()
            moveToRelative(0f, 0f)
            verticalLineTo(6f)
            verticalLineTo(8f)
            verticalLineTo(18f)
            close()
            moveToRelative(8.2f, -4f)
            lineToRelative(-0.93f, 0.92f)
            quadTo(11f, 15.2f, 11f, 15.63f)
            reflectiveQuadToRelative(0.28f, 0.7f)
            reflectiveQuadToRelative(0.7f, 0.28f)
            reflectiveQuadToRelative(0.7f, -0.28f)
            lineTo(15.3f, 13.7f)
            quadTo(15.6f, 13.4f, 15.6f, 13f)
            reflectiveQuadTo(15.3f, 12.3f)
            lineTo(12.68f, 9.67f)
            quadTo(12.4f, 9.4f, 11.98f, 9.4f)
            reflectiveQuadToRelative(-0.7f, 0.28f)
            reflectiveQuadTo(11f, 10.38f)
            reflectiveQuadToRelative(0.28f, 0.7f)
            lineTo(12.2f, 12f)
            horizontalLineTo(9f)
            quadTo(8.58f, 12f, 8.29f, 12.29f)
            reflectiveQuadTo(8f, 13f)
            reflectiveQuadToRelative(0.29f, 0.71f)
            quadTo(8.58f, 14f, 9f, 14f)
            horizontalLineToRelative(3.2f)
            close()
        }
    }.build()
}
