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
internal val flashOnFilled: MaterialSymbol = MaterialSymbol(
    name = "flash_on",
    style = SymbolStyle.FilledRounded,
) {
    ImageVector.Builder(
        name = "FlashOn",
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
            moveTo(10.15f, 20.06f)
            quadTo(10f, 19.88f, 10f, 19.6f)
            verticalLineTo(14f)
            horizontalLineTo(9f)
            quadTo(8.18f, 14f, 7.59f, 13.41f)
            reflectiveQuadTo(7f, 12f)
            verticalLineTo(4f)
            quadTo(7f, 3.17f, 7.59f, 2.59f)
            reflectiveQuadTo(9f, 2f)
            horizontalLineToRelative(5.85f)
            quadToRelative(0.8f, 0f, 1.29f, 0.63f)
            reflectiveQuadTo(16.43f, 4f)
            lineTo(15f, 9f)
            horizontalLineToRelative(1.13f)
            quadToRelative(0.9f, 0f, 1.34f, 0.8f)
            reflectiveQuadToRelative(-0.09f, 1.55f)
            lineToRelative(-6f, 8.67f)
            quadToRelative(-0.15f, 0.23f, -0.39f, 0.3f)
            reflectiveQuadToRelative(-0.46f, 0f)
            reflectiveQuadTo(10.15f, 20.06f)
            close()
        }
    }.build()
}
