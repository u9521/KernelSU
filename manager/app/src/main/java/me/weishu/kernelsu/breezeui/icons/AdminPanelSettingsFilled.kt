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
internal val adminPanelSettingsFilled: MaterialSymbol = MaterialSymbol(
    name = "admin_panel_settings",
    style = SymbolStyle.FilledRounded,
) {
    ImageVector.Builder(
        name = "AdminPanelSettings",
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
            moveTo(17f, 22f)
            quadToRelative(-2.07f, 0f, -3.54f, -1.46f)
            reflectiveQuadTo(12f, 17f)
            reflectiveQuadToRelative(1.46f, -3.54f)
            reflectiveQuadTo(17f, 12f)
            reflectiveQuadToRelative(3.54f, 1.46f)
            quadTo(22f, 14.93f, 22f, 17f)
            reflectiveQuadToRelative(-1.46f, 3.54f)
            reflectiveQuadTo(17f, 22f)
            close()
            moveTo(4f, 11.1f)
            verticalLineTo(6.38f)
            quadTo(4f, 5.75f, 4.36f, 5.25f)
            quadTo(4.73f, 4.75f, 5.3f, 4.52f)
            lineToRelative(6f, -2.25f)
            quadTo(11.65f, 2.15f, 12f, 2.15f)
            reflectiveQuadToRelative(0.7f, 0.13f)
            lineToRelative(6f, 2.25f)
            quadToRelative(0.58f, 0.23f, 0.94f, 0.73f)
            reflectiveQuadTo(20f, 6.38f)
            verticalLineTo(9.42f)
            quadToRelative(0f, 0.43f, -0.38f, 0.69f)
            quadToRelative(-0.38f, 0.26f, -0.8f, 0.14f)
            quadToRelative(-0.45f, -0.13f, -0.9f, -0.19f)
            reflectiveQuadTo(17f, 10f)
            quadToRelative(-2.9f, 0f, -4.95f, 2.05f)
            reflectiveQuadTo(10f, 17f)
            quadToRelative(0f, 0.8f, 0.19f, 1.56f)
            reflectiveQuadToRelative(0.54f, 1.54f)
            quadToRelative(0.22f, 0.47f, -0.14f, 0.85f)
            reflectiveQuadTo(9.75f, 21.1f)
            quadTo(8.7f, 20.55f, 7.83f, 19.75f)
            reflectiveQuadTo(6.28f, 18f)
            quadTo(5.2f, 16.52f, 4.6f, 14.76f)
            reflectiveQuadTo(4f, 11.1f)
            close()
            moveToRelative(14.06f, 5.46f)
            quadTo(18.5f, 16.13f, 18.5f, 15.5f)
            reflectiveQuadTo(18.06f, 14.44f)
            reflectiveQuadTo(17f, 14f)
            reflectiveQuadToRelative(-1.06f, 0.44f)
            reflectiveQuadTo(15.5f, 15.5f)
            reflectiveQuadToRelative(0.44f, 1.06f)
            reflectiveQuadTo(17f, 17f)
            reflectiveQuadToRelative(1.06f, -0.44f)
            close()
            moveTo(17f, 20f)
            quadToRelative(0.63f, 0f, 1.18f, -0.24f)
            reflectiveQuadToRelative(0.98f, -0.69f)
            quadToRelative(0.13f, -0.15f, 0.1f, -0.34f)
            quadTo(19.23f, 18.55f, 19.03f, 18.45f)
            quadTo(18.55f, 18.23f, 18.04f, 18.11f)
            reflectiveQuadTo(17f, 18f)
            quadToRelative(-0.52f, 0f, -1.04f, 0.11f)
            reflectiveQuadToRelative(-0.99f, 0.34f)
            quadToRelative(-0.2f, 0.1f, -0.23f, 0.29f)
            reflectiveQuadToRelative(0.1f, 0.34f)
            quadToRelative(0.42f, 0.45f, 0.97f, 0.69f)
            reflectiveQuadTo(17f, 20f)
            close()
        }
    }.build()
}
