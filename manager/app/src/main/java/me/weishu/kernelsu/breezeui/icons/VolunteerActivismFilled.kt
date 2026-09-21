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
internal val volunteerActivismFilled: MaterialSymbol = MaterialSymbol(
    name = "volunteer_activism",
    style = SymbolStyle.FilledRounded,
) {
    ImageVector.Builder(
        name = "VolunteerActivism",
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
            moveTo(13.38f, 21.83f)
            quadToRelative(0.28f, 0.07f, 0.64f, 0.06f)
            reflectiveQuadToRelative(0.61f, -0.11f)
            lineTo(22f, 19f)
            quadToRelative(0f, -0.85f, -0.6f, -1.43f)
            reflectiveQuadTo(20f, 17f)
            horizontalLineTo(13.15f)
            quadToRelative(-0.07f, 0f, -0.17f, -0.01f)
            reflectiveQuadTo(12.83f, 16.95f)
            lineTo(11.35f, 16.43f)
            quadToRelative(-0.2f, -0.07f, -0.28f, -0.25f)
            reflectiveQuadTo(11.05f, 15.8f)
            quadTo(11.1f, 15.63f, 11.3f, 15.53f)
            reflectiveQuadTo(11.7f, 15.5f)
            lineToRelative(1.13f, 0.42f)
            quadToRelative(0.1f, 0.05f, 0.16f, 0.06f)
            reflectiveQuadTo(13.18f, 16f)
            horizontalLineTo(15.8f)
            quadToRelative(0.48f, 0f, 0.84f, -0.33f)
            reflectiveQuadTo(17f, 14.83f)
            quadToRelative(0f, -0.35f, -0.21f, -0.68f)
            quadTo(16.58f, 13.83f, 16.23f, 13.7f)
            lineTo(9.3f, 11.13f)
            quadTo(9.13f, 11.08f, 8.95f, 11.04f)
            reflectiveQuadTo(8.6f, 11f)
            horizontalLineTo(7f)
            verticalLineToRelative(9.02f)
            lineToRelative(6.38f, 1.8f)
            close()
            moveTo(1f, 20f)
            quadToRelative(0f, 0.82f, 0.59f, 1.41f)
            reflectiveQuadTo(3f, 22f)
            quadToRelative(0.83f, 0f, 1.41f, -0.59f)
            reflectiveQuadTo(5f, 20f)
            verticalLineTo(13f)
            quadTo(5f, 12.18f, 4.41f, 11.59f)
            reflectiveQuadTo(3f, 11f)
            quadTo(2.18f, 11f, 1.59f, 11.59f)
            quadTo(1f, 12.18f, 1f, 13f)
            verticalLineToRelative(7f)
            close()
            moveTo(15.26f, 12.06f)
            quadTo(14.9f, 11.93f, 14.6f, 11.65f)
            lineTo(11.85f, 8.95f)
            quadTo(11.08f, 8.2f, 10.54f, 7.29f)
            quadTo(10f, 6.38f, 10f, 5.3f)
            quadTo(10f, 3.92f, 10.96f, 2.96f)
            reflectiveQuadTo(13.3f, 2f)
            quadToRelative(0.8f, 0f, 1.5f, 0.34f)
            reflectiveQuadTo(16f, 3.25f)
            quadTo(16.5f, 2.67f, 17.2f, 2.34f)
            reflectiveQuadTo(18.7f, 2f)
            quadToRelative(1.38f, 0f, 2.34f, 0.96f)
            reflectiveQuadTo(22f, 5.3f)
            quadToRelative(0f, 1.07f, -0.52f, 1.99f)
            reflectiveQuadToRelative(-1.3f, 1.66f)
            lineToRelative(-2.77f, 2.7f)
            quadToRelative(-0.3f, 0.28f, -0.66f, 0.41f)
            reflectiveQuadTo(16f, 12.2f)
            reflectiveQuadTo(15.26f, 12.06f)
            close()
        }
    }.build()
}
