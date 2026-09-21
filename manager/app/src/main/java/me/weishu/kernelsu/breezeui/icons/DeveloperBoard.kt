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
internal val developerBoard: MaterialSymbol = MaterialSymbol(
    name = "developer_board",
    style = SymbolStyle(SymbolFamily.Rounded, SymbolFill.Unfilled, 24, 400, 0, 50),
) {
    ImageVector.Builder(
        name = "DeveloperBoard",
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
            moveTo(4f, 21f)
            quadTo(3.18f, 21f, 2.59f, 20.41f)
            reflectiveQuadTo(2f, 19f)
            verticalLineTo(5f)
            quadTo(2f, 4.17f, 2.59f, 3.59f)
            reflectiveQuadTo(4f, 3f)
            horizontalLineTo(18f)
            quadToRelative(0.82f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(20f, 5f)
            verticalLineTo(7f)
            horizontalLineToRelative(1f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(22f, 8f)
            quadToRelative(0f, 0.42f, -0.29f, 0.71f)
            reflectiveQuadTo(21f, 9f)
            horizontalLineTo(20f)
            verticalLineToRelative(2f)
            horizontalLineToRelative(1f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(22f, 12f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(21f, 13f)
            horizontalLineTo(20f)
            verticalLineToRelative(2f)
            horizontalLineToRelative(1f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(22f, 16f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(21f, 17f)
            horizontalLineTo(20f)
            verticalLineToRelative(2f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(18f, 21f)
            horizontalLineTo(4f)
            close()
            moveTo(4f, 19f)
            horizontalLineTo(18f)
            verticalLineTo(5f)
            horizontalLineTo(4f)
            verticalLineTo(19f)
            close()
            moveTo(7f, 17f)
            horizontalLineToRelative(3f)
            quadToRelative(0.43f, 0f, 0.71f, -0.29f)
            quadTo(11f, 16.43f, 11f, 16f)
            verticalLineTo(14f)
            quadToRelative(0f, -0.43f, -0.29f, -0.71f)
            reflectiveQuadTo(10f, 13f)
            horizontalLineTo(7f)
            quadTo(6.58f, 13f, 6.29f, 13.29f)
            reflectiveQuadTo(6f, 14f)
            verticalLineToRelative(2f)
            quadToRelative(0f, 0.43f, 0.29f, 0.71f)
            reflectiveQuadTo(7f, 17f)
            close()
            moveToRelative(6f, -7f)
            horizontalLineToRelative(2f)
            quadToRelative(0.43f, 0f, 0.71f, -0.29f)
            reflectiveQuadTo(16f, 9f)
            verticalLineTo(8f)
            quadTo(16f, 7.57f, 15.71f, 7.29f)
            reflectiveQuadTo(15f, 7f)
            horizontalLineTo(13f)
            quadTo(12.58f, 7f, 12.29f, 7.29f)
            reflectiveQuadTo(12f, 8f)
            verticalLineTo(9f)
            quadToRelative(0f, 0.42f, 0.29f, 0.71f)
            reflectiveQuadTo(13f, 10f)
            close()
            moveTo(7f, 12f)
            horizontalLineToRelative(3f)
            quadToRelative(0.43f, 0f, 0.71f, -0.29f)
            quadTo(11f, 11.43f, 11f, 11f)
            verticalLineTo(8f)
            quadTo(11f, 7.57f, 10.71f, 7.29f)
            reflectiveQuadTo(10f, 7f)
            horizontalLineTo(7f)
            quadTo(6.58f, 7f, 6.29f, 7.29f)
            reflectiveQuadTo(6f, 8f)
            verticalLineToRelative(3f)
            quadToRelative(0f, 0.42f, 0.29f, 0.71f)
            reflectiveQuadTo(7f, 12f)
            close()
            moveToRelative(6f, 5f)
            horizontalLineToRelative(2f)
            quadToRelative(0.43f, 0f, 0.71f, -0.29f)
            quadTo(16f, 16.43f, 16f, 16f)
            verticalLineTo(12f)
            quadToRelative(0f, -0.43f, -0.29f, -0.71f)
            reflectiveQuadTo(15f, 11f)
            horizontalLineTo(13f)
            quadToRelative(-0.42f, 0f, -0.71f, 0.29f)
            reflectiveQuadTo(12f, 12f)
            verticalLineToRelative(4f)
            quadToRelative(0f, 0.43f, 0.29f, 0.71f)
            reflectiveQuadTo(13f, 17f)
            close()
            moveTo(4f, 5f)
            verticalLineTo(19f)
            verticalLineTo(5f)
            close()
        }
    }.build()
}
