package me.weishu.kernelsu.ui.component.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.OutlinedTextEdit
import me.weishu.kernelsu.ui.util.checkSelinuxContext
import me.weishu.kernelsu.ui.util.isSepolicyValid

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SeLinuxEditBottomSheet(
    seLinuxContext: String,
    seLinuxRules: String,
    readOnly: Boolean = false,
    onDismissRequest: () -> Unit,
    onSELinuxChange: (String, String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    fun closeSheet() {
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismissRequest()
            }
        }
    }


    ModalBottomSheet(
        modifier = Modifier.padding(WindowInsets.statusBars.only(WindowInsetsSides.Top + WindowInsetsSides.Bottom).asPaddingValues()),
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(left = 16.dp, right = 16.dp).union(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)) },
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        var seContext by remember { mutableStateOf(seLinuxContext) }
        var seRules by remember { mutableStateOf(seLinuxRules) }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title
            Text(
                text = stringResource(id = R.string.profile_selinux_context),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Buttons Area
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (!readOnly) {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Confirm Button
                Button(
                    shapes = ButtonDefaults.shapes(),
                    onClick = {
                        if (readOnly) {
                            closeSheet()
                        } else {
                            onSELinuxChange(seContext, seRules)
                            closeSheet()
                        }
                    }
                ) {
                    Text(stringResource(if (readOnly) R.string.close else android.R.string.ok))
                }
            }
        }
        // Context
        OutlinedTextEdit(
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(id = R.string.profile_selinux_domain)) },
            text = seContext,
            onValueChange = { seContext = it },
            readOnly = readOnly,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Next
            ),
            validator = { context ->
                checkSelinuxContext(context)
            }
        )
        // Rules
        OutlinedTextEdit(
            label = { Text(stringResource(id = R.string.profile_selinux_rules)) },
            modifier = Modifier.fillMaxWidth(),
            text = seRules,
            onValueChange = { seRules = it },
            readOnly = readOnly,
            singleLine = false,
            minLines = 3,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Next
            ),
            defaultSupportingText = { Text("注意，Selinux规则是全局生效的,且会在开机时自动设置，请确保知道你在干什么") },
            validator = { if (isSepolicyValid(it)) null else "SELinux rules are invalid!" }
        )
    }
}