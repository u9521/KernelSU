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
internal val displaySettingsFilled: MaterialSymbol = MaterialSymbol(
    name = "display_settings",
    style = SymbolStyle.FilledRounded,
) {
    ImageVector.Builder(
        name = "DisplaySettings",
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
            moveTo(7.5f, 13.75f)
            verticalLineToRelative(0.5f)
            quadToRelative(0f, 0.32f, 0.21f, 0.54f)
            reflectiveQuadTo(8.25f, 15f)
            quadToRelative(0.33f, 0f, 0.54f, -0.21f)
            reflectiveQuadTo(9f, 14.25f)
            verticalLineToRelative(-2.5f)
            quadTo(9f, 11.43f, 8.79f, 11.21f)
            quadTo(8.58f, 11f, 8.25f, 11f)
            quadTo(7.93f, 11f, 7.71f, 11.21f)
            quadTo(7.5f, 11.43f, 7.5f, 11.75f)
            verticalLineToRelative(0.5f)
            horizontalLineTo(6.75f)
            quadToRelative(-0.32f, 0f, -0.54f, 0.21f)
            quadTo(6f, 12.68f, 6f, 13f)
            reflectiveQuadToRelative(0.21f, 0.54f)
            reflectiveQuadToRelative(0.54f, 0.21f)
            horizontalLineTo(7.5f)
            close()
            moveToRelative(3.25f, 0f)
            horizontalLineToRelative(6.5f)
            quadToRelative(0.32f, 0f, 0.54f, -0.21f)
            reflectiveQuadTo(18f, 13f)
            reflectiveQuadTo(17.79f, 12.46f)
            reflectiveQuadTo(17.25f, 12.25f)
            horizontalLineToRelative(-6.5f)
            quadToRelative(-0.32f, 0f, -0.54f, 0.21f)
            quadTo(10f, 12.68f, 10f, 13f)
            reflectiveQuadToRelative(0.21f, 0.54f)
            reflectiveQuadToRelative(0.54f, 0.21f)
            close()
            moveToRelative(5.75f, -4f)
            horizontalLineToRelative(0.75f)
            quadToRelative(0.32f, 0f, 0.54f, -0.21f)
            reflectiveQuadTo(18f, 9f)
            quadTo(18f, 8.67f, 17.79f, 8.46f)
            reflectiveQuadTo(17.25f, 8.25f)
            horizontalLineTo(16.5f)
            verticalLineTo(7.75f)
            quadToRelative(0f, -0.32f, -0.21f, -0.54f)
            reflectiveQuadTo(15.75f, 7f)
            reflectiveQuadTo(15.21f, 7.21f)
            quadTo(15f, 7.43f, 15f, 7.75f)
            verticalLineToRelative(2.5f)
            quadToRelative(0f, 0.32f, 0.21f, 0.54f)
            reflectiveQuadTo(15.75f, 11f)
            reflectiveQuadToRelative(0.54f, -0.21f)
            reflectiveQuadTo(16.5f, 10.25f)
            verticalLineTo(9.75f)
            close()
            moveToRelative(-9.75f, 0f)
            horizontalLineToRelative(6.5f)
            quadToRelative(0.33f, 0f, 0.54f, -0.21f)
            reflectiveQuadTo(14f, 9f)
            quadTo(14f, 8.67f, 13.79f, 8.46f)
            reflectiveQuadTo(13.25f, 8.25f)
            horizontalLineTo(6.75f)
            quadToRelative(-0.32f, 0f, -0.54f, 0.21f)
            reflectiveQuadTo(6f, 9f)
            quadTo(6f, 9.32f, 6.21f, 9.54f)
            reflectiveQuadTo(6.75f, 9.75f)
            close()
            moveTo(4f, 19f)
            quadTo(3.18f, 19f, 2.59f, 18.41f)
            reflectiveQuadTo(2f, 17f)
            verticalLineTo(5f)
            quadTo(2f, 4.17f, 2.59f, 3.59f)
            reflectiveQuadTo(4f, 3f)
            horizontalLineTo(20f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(22f, 5f)
            verticalLineTo(17f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(20f, 19f)
            horizontalLineTo(16f)
            verticalLineToRelative(1f)
            quadToRelative(0f, 0.43f, -0.29f, 0.71f)
            reflectiveQuadTo(15f, 21f)
            horizontalLineTo(9f)
            quadTo(8.58f, 21f, 8.29f, 20.71f)
            quadTo(8f, 20.43f, 8f, 20f)
            verticalLineTo(19f)
            horizontalLineTo(4f)
            close()
        }
    }.build()
}
