package me.weishu.kernelsu.ui.screen

import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.KeyEventBlocker
import me.weishu.kernelsu.ui.util.LocalNavController
import me.weishu.kernelsu.ui.util.LocalSnackbarHost
import me.weishu.kernelsu.ui.util.runModuleAction
import me.weishu.kernelsu.ui.viewmodel.ModuleViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExecuteModuleActionScreen(moduleId: String) {
    val navigator = LocalNavController.current
    val context = LocalContext.current
    var text by rememberSaveable { mutableStateOf("") }
    var tempText: String
    val logContent = rememberSaveable { StringBuilder() }
    val snackBarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var actionResult: Boolean
    var showCloseFAB by rememberSaveable { mutableStateOf(false) }
    val noModule = stringResource(R.string.no_such_module)
    val moduleUnavailable = stringResource(R.string.module_unavailable)
    LaunchedEffect(Unit) {
        if (text.isNotEmpty()) {
            return@LaunchedEffect
        }
        val viewModel = ModuleViewModel()
        if (viewModel.moduleList.isEmpty()) {
            viewModel.loadModuleList()
        }
        val moduleInfo = viewModel.moduleList.find { info -> info.id == moduleId }
        if (moduleInfo == null) {
            Toast.makeText(context, noModule.format(moduleId), Toast.LENGTH_SHORT).show()
            navigator.popBackStack()
            return@LaunchedEffect
        }
        if (!moduleInfo.hasActionScript) {
            navigator.popBackStack()
            return@LaunchedEffect
        }
        if (!moduleInfo.enabled || moduleInfo.update || moduleInfo.remove) {
            Toast.makeText(context, moduleUnavailable.format(moduleInfo.name), Toast.LENGTH_SHORT).show()
            navigator.popBackStack()
            return@LaunchedEffect
        }
        withContext(Dispatchers.IO) {
            runModuleAction(
                moduleId = moduleId,
                onStdout = {
                    tempText = "$it\n"
                    if (tempText.startsWith("[H[J")) { // clear command
                        text = tempText.substring(6)
                    } else {
                        text += tempText
                    }
                    logContent.append(it).append("\n")
                },
                onStderr = {
                    logContent.append(it).append("\n")
                }
            ).let {
                actionResult = it
            }
        }
        if (actionResult) showCloseFAB = true
    }

    Scaffold(
        topBar = {
            TopBar(
                onBack = dropUnlessResumed {
                    navigator.popBackStack()
                },
                onSave = {
                    scope.launch {
                        val format = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault())
                        val date = format.format(Date())
                        val file = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            "KernelSU_module_action_log_${date}.log"
                        )
                        file.writeText(logContent.toString())
                        snackBarHost.showSnackbar("Log saved to ${file.absolutePath}")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackBarHost) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier
                    .padding(bottom = 6.dp + 48.dp + 6.dp /* SnackBar height */)
                    .animateFloatingActionButton(
                        visible = showCloseFAB,
                        alignment = Alignment.CenterEnd,
                    ),
                onClick = {
                    navigator.popBackStack()
                },
                icon = { Icon(Icons.Filled.Close, stringResource(R.string.close)) },
                text = { Text(text = stringResource(R.string.close)) },
            )
        }
    ) { innerPadding ->
        KeyEventBlocker {
            it.key == Key.VolumeDown || it.key == Key.VolumeUp
        }
        Column(
            modifier = Modifier
                .fillMaxSize(1f)
                .padding(innerPadding)
                .verticalScroll(scrollState),
        ) {
            LaunchedEffect(text) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
            Text(
                modifier = Modifier.padding(8.dp),
                text = text,
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                fontFamily = FontFamily.Monospace,
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(onBack: () -> Unit = {}, onSave: () -> Unit = {}) {
    TopAppBar(
        title = { Text(stringResource(R.string.action)) },
        navigationIcon = {
            IconButton(
                onClick = onBack
            ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
        },
        actions = {
            IconButton(onClick = onSave) {
                Icon(
                    painter = painterResource(R.drawable.ic_save_rounded_filled),
                    contentDescription = stringResource(id = R.string.save_log),
                )
            }
        }
    )
}