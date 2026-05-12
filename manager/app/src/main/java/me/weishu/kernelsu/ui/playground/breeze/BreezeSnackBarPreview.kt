package me.weishu.kernelsu.ui.playground.breeze

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.weishu.kernelsu.ui.component.breeze.BreezeSnackBarHost

@Composable
fun BreezeSnackBarPreview() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "BreezeSnackBar — Custom Snackbar Host",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Button(onClick = {
                scope.launch {
                    snackbarHostState.showSnackbar("This is a Breeze snackbar message")
                }
            }) {
                Text("Show Snackbar")
            }

            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Action snackbar",
                        actionLabel = "Undo"
                    )
                }
            }) {
                Text("Show Snackbar with Action")
            }

            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Action snackbar long\n long\nlong\n",
                        actionLabel = "Undo"
                    )
                }
            }) {
                Text("Show long Snackbar with Action")
            }
        }
        Spacer(Modifier.height(500.dp))
        BreezeSnackBarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBreezeSnackBar() {
    BreezeSnackBarPreview()
}
