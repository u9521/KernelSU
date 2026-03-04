package me.weishu.kernelsu.ui.webui

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun HandleWebUIEventMaterial(
    webUIState: WebUIState,
    fileLauncher: ActivityResultLauncher<Intent>
) {
    when (val event = webUIState.uiEvent) {
        is WebUIEvent.ShowAlert -> {
            val showDialog = remember(event) { mutableStateOf(true) }

            if (showDialog.value) {
                AlertDialog(onDismissRequest = {}, title = null, text = {
                    Text(event.message)
                }, confirmButton = {
                    Button(
                        onClick = {
                            webUIState.onAlertResult()
                            showDialog.value = false
                        }, shapes = ButtonDefaults.shapes()
                    ) {
                        Text(stringResource(R.string.confirm))
                    }
                })
            }
        }

        is WebUIEvent.ShowConfirm -> {
            val showDialog = remember(event) { mutableStateOf(true) }

            if (showDialog.value) {
                AlertDialog(onDismissRequest = {
                    webUIState.onConfirmResult(false)
                    showDialog.value = false
                }, text = {
                    Text(event.message)
                }, confirmButton = {
                    Button(
                        onClick = {
                            webUIState.onConfirmResult(true)
                            showDialog.value = false
                        }, shapes = ButtonDefaults.shapes()
                    ) {
                        Text(stringResource(R.string.confirm))
                    }
                }, dismissButton = {
                    OutlinedButton(
                        shapes = ButtonDefaults.shapes(), onClick = {
                            webUIState.onConfirmResult(false)
                            showDialog.value = false
                        }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                })
            }
        }

        is WebUIEvent.ShowPrompt -> {
            val showDialog = remember(event) { mutableStateOf(true) }
            val (text, onTextChange) = remember(event) { mutableStateOf(event.defaultValue) }

            if (showDialog.value) {
                AlertDialog(onDismissRequest = {
                    webUIState.onPromptResult(null)
                    showDialog.value = false
                }, text = {
                    Column {
                        Text(event.message)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = text, onValueChange = onTextChange, modifier = Modifier.fillMaxWidth()
                        )
                    }
                }, confirmButton = {
                    Button(
                        onClick = {
                            webUIState.onPromptResult(text)
                            showDialog.value = false
                        }, shapes = ButtonDefaults.shapes()
                    ) {
                        Text(stringResource(R.string.confirm))
                    }
                }, dismissButton = {
                    OutlinedButton(
                        shapes = ButtonDefaults.shapes(), onClick = {
                            webUIState.onPromptResult(null)
                            showDialog.value = false
                        }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                })
            }
        }

        is WebUIEvent.ShowFileChooser -> {
            LaunchedEffect(event) {
                try {
                    fileLauncher.launch(event.intent)
                } catch (_: Exception) {
                    webUIState.onFileChooserResult(null)
                }
            }
        }

        else -> {}
    }
}