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
internal val developerModeFilled: MaterialSymbol = MaterialSymbol(
    name = "developer_mode",
    style = SymbolStyle.FilledRounded,
) {
    ImageVector.Builder(
        name = "DeveloperMode",
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
            moveTo(7f, 23f)
            quadTo(6.18f, 23f, 5.59f, 22.41f)
            reflectiveQuadTo(5f, 21f)
            verticalLineTo(3f)
            quadTo(5f, 2.17f, 5.59f, 1.59f)
            reflectiveQuadTo(7f, 1f)
            horizontalLineTo(17f)
            quadToRelative(0.82f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(19f, 3f)
            verticalLineTo(6.1f)
            quadToRelative(0.45f, 0.18f, 0.73f, 0.55f)
            reflectiveQuadTo(20f, 7.5f)
            verticalLineToRelative(2f)
            quadToRelative(0f, 0.47f, -0.27f, 0.85f)
            reflectiveQuadTo(19f, 10.9f)
            verticalLineTo(13f)
            horizontalLineTo(15f)
            quadToRelative(-2.5f, 0f, -4.25f, 1.75f)
            reflectiveQuadTo(9f, 19f)
            verticalLineToRelative(4f)
            horizontalLineTo(7f)
            close()
            moveTo(20.1f, 21.9f)
            quadToRelative(-0.3f, 0.3f, -0.7f, 0.29f)
            reflectiveQuadTo(18.7f, 21.88f)
            quadToRelative(-0.27f, -0.3f, -0.29f, -0.7f)
            reflectiveQuadToRelative(0.29f, -0.7f)
            lineTo(20.18f, 19f)
            lineTo(18.7f, 17.52f)
            quadTo(18.43f, 17.25f, 18.43f, 16.84f)
            quadToRelative(0f, -0.41f, 0.27f, -0.71f)
            quadToRelative(0.3f, -0.3f, 0.71f, -0.3f)
            reflectiveQuadToRelative(0.71f, 0.3f)
            lineTo(22.3f, 18.3f)
            quadToRelative(0.3f, 0.3f, 0.3f, 0.7f)
            reflectiveQuadToRelative(-0.3f, 0.7f)
            lineToRelative(-2.2f, 2.2f)
            close()
            moveTo(13.88f, 21.88f)
            lineTo(11.7f, 19.7f)
            quadTo(11.4f, 19.4f, 11.4f, 19f)
            reflectiveQuadToRelative(0.3f, -0.7f)
            lineToRelative(2.2f, -2.2f)
            quadToRelative(0.3f, -0.3f, 0.7f, -0.29f)
            reflectiveQuadToRelative(0.7f, 0.31f)
            quadToRelative(0.28f, 0.3f, 0.29f, 0.7f)
            quadToRelative(0.01f, 0.4f, -0.29f, 0.7f)
            lineTo(13.83f, 19f)
            lineToRelative(1.47f, 1.48f)
            quadToRelative(0.28f, 0.27f, 0.28f, 0.69f)
            reflectiveQuadTo(15.3f, 21.88f)
            quadToRelative(-0.3f, 0.3f, -0.71f, 0.3f)
            reflectiveQuadToRelative(-0.71f, -0.3f)
            close()
            moveTo(12.71f, 5.71f)
            quadTo(13f, 5.43f, 13f, 5f)
            reflectiveQuadTo(12.71f, 4.29f)
            reflectiveQuadTo(12f, 4f)
            reflectiveQuadTo(11.29f, 4.29f)
            reflectiveQuadTo(11f, 5f)
            reflectiveQuadToRelative(0.29f, 0.71f)
            reflectiveQuadTo(12f, 6f)
            reflectiveQuadTo(12.71f, 5.71f)
            close()
        }
    }.build()
}
