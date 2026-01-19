package me.weishu.kernelsu.ui.component

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun OutlinedTextEdit(
    label: @Composable () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    defaultSupportingText: @Composable () -> Unit = {},
    enabled: Boolean = true,
    readOnly: Boolean = false,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    validator: (String) -> String? = { null },
    onValueChange: (String) -> Unit
) {
    var errorReason by remember { mutableStateOf<String?>(null) }
    val isError = !errorReason.isNullOrBlank()
    var rawText by remember { mutableStateOf(text) }

    LaunchedEffect(text) {
        if (text != rawText && errorReason == null) {
            rawText = text
        }
    }
    OutlinedTextField(
        value = rawText,
        onValueChange = { newInput ->
            rawText = newInput
            val validationResult = validator(newInput)
            if (validationResult == null) {
                errorReason = null
                onValueChange(newInput)
            } else {
                errorReason = validationResult
            }
        },
        modifier = modifier,
        label = label,
        isError = isError,
        supportingText = {
            if (isError) {
                Text(
                    text = errorReason ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error
                )
            } else {
                defaultSupportingText()
            }
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines
    )
}