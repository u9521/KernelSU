package me.weishu.kernelsu.ui.playground.choosekmidialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.ui.UiMode
import me.weishu.kernelsu.ui.component.breeze.PopupFeedBack
import me.weishu.kernelsu.ui.component.choosekmidialog.ChooseKmiDialogBreeze
import me.weishu.kernelsu.ui.theme.KernelSUTheme
import me.weishu.kernelsu.ui.theme.LocalEnableBlur
import me.weishu.kernelsu.ui.util.LocalBlurController
import me.weishu.kernelsu.ui.util.blurOverlay
import me.weishu.kernelsu.ui.util.rememberBlurController

@Composable
fun ChooseKmiDialogBreezePreview() {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "ChooseKmiDialogBreeze",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(onClick = { showDialog = true }) {
            Text("Open Dialog")
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Click to open the KMI selection dialog with blur effect.",
            style = MaterialTheme.typography.bodyMedium
        )
    }

    if (showDialog) {
        PopupFeedBack()
    }

    ChooseKmiDialogBreeze(
        show = showDialog,
        onDismissRequest = { showDialog = false },
        onSelected = { showDialog = false }
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewChooseKmiDialogBreeze() {
    val blurController = rememberBlurController()
    KernelSUTheme(uiMode = UiMode.Breeze) {
        CompositionLocalProvider(
            LocalEnableBlur provides true,
            LocalBlurController provides blurController,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .blurOverlay()
            ) {
                ChooseKmiDialogBreezePreview()
            }
        }
    }
}
