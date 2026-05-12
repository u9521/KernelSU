package me.weishu.kernelsu.ui.component.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import me.weishu.kernelsu.ui.component.breeze.PopupFeedBack
import me.weishu.kernelsu.ui.component.breeze.keyDownFeedBack
import me.weishu.kernelsu.ui.component.markdown.MarkdownContent
import me.weishu.kernelsu.ui.util.windowBlurBehind

@Composable
fun LoadingDialogBreeze(showDialog: MutableState<Boolean>) {
    if (!showDialog.value) return
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = false)
    ) {
        Surface(
            modifier = Modifier
                .size(100.dp)
                .windowBlurBehind(), shape = RoundedCornerShape(8.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
            ) {
                LoadingIndicator()
            }
        }
    }
}


@Composable
fun ConfirmDialogBreeze(
    visuals: ConfirmDialogVisuals,
    confirm: () -> Unit,
    dismiss: () -> Unit,
    showDialog: MutableState<Boolean>
) {
    val keyFeedback = keyDownFeedBack()
    if (!showDialog.value) return
    PopupFeedBack()
    AlertDialog(
        modifier = Modifier.windowBlurBehind(),
        onDismissRequest = {
            dismiss()
            showDialog.value = false
        },
        title = { Text(visuals.title) },
        text = {
            visuals.content?.let { content ->
                when {
                    visuals.isMarkdown -> MarkdownContent(content = content, isMarkdown = true)
                    visuals.isHtml -> MarkdownContent(content = content, isMarkdown = false)
                    else -> Text(text = content)
                }
            }
        },
        confirmButton = {
            Button(
                shapes = ButtonDefaults.shapes(), onClick = {
                    keyFeedback()
                    confirm()
                }) {
                Text(text = visuals.confirm ?: stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            OutlinedButton(shapes = ButtonDefaults.shapes(), onClick = {
                keyFeedback()
                dismiss()
            }) {
                Text(text = visuals.dismiss ?: stringResource(id = android.R.string.cancel))
            }
        }
    )
}

