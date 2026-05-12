package me.weishu.kernelsu.ui.component.statustag

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StatusTagBreeze(
    label: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    contentColor: Color
) {
    Box(
        modifier = modifier
            .padding(end = 4.dp)
            .background(
                color = backgroundColor,
                shape = MaterialTheme.shapes.extraSmall
            )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp),
            lineHeight = MaterialTheme.typography.labelSmallEmphasized.fontSize,
            fontSize = MaterialTheme.typography.labelSmallEmphasized.fontSize,
            color = contentColor,
        )
    }
}
