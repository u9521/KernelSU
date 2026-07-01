package me.weishu.kernelsu.ui.screen.templateeditor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.breeze.BreezeBackButton
import me.weishu.kernelsu.ui.component.breeze.OutlinedTextEdit
import me.weishu.kernelsu.ui.component.breeze.SplitScreenRatioButton
import me.weishu.kernelsu.ui.component.material.ExpressiveScaffold
import me.weishu.kernelsu.ui.component.material.disableDrag
import me.weishu.kernelsu.ui.component.material.expressiveTopBarColors
import me.weishu.kernelsu.ui.component.profile.RootProfileConfigBreeze
import me.weishu.kernelsu.ui.navigation3.breeze.LocalIsDetailPane
import me.weishu.kernelsu.ui.util.onlyHorizontal
import me.weishu.kernelsu.ui.util.topBarHazeEffect

@Composable
fun TemplateEditorScreenBreeze(
    state: TemplateEditorUiState,
    actions: TemplateEditorActions,
) {
    val hazeState = rememberHazeState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior().disableDrag()


    ExpressiveScaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopBar(
                title = if (state.isCreation) {
                    stringResource(R.string.app_profile_template_create)
                } else if (state.readOnly) {
                    stringResource(R.string.app_profile_template_view)
                } else {
                    stringResource(R.string.app_profile_template_edit)
                },
                readOnly = state.readOnly,
                modifier = Modifier.topBarHazeEffect(hazeState, scrollBehavior),
                onBack = actions.onBack,
                onDelete = actions.onDelete,
                onSave = actions.onSave,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        val idConflictError = stringResource(id = R.string.app_profile_template_id_exist)
        val idInvalidError = stringResource(id = R.string.app_profile_template_id_invalid)
        val idTooLongError = stringResource(id = R.string.app_profile_template_id_too_long)
        val keyboardController = LocalSoftwareKeyboardController.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
                .padding(innerPadding.onlyHorizontal())
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(innerPadding.calculateTopPadding() + 16.dp))

            OutlinedTextEdit(
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(id = R.string.app_profile_template_id)) },
                text = state.template.id,
                readOnly = !state.isCreation,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() },
                ),
                validator = { input ->
                    when {
                        isTemplateExist(input) -> idConflictError
                        !isValidTemplateId(input) -> idInvalidError
                        input.length > 255 -> idTooLongError
                        else -> null
                    }
                },
                onValueChange = actions.onIdChange,
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextEdit(
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(id = R.string.app_profile_template_name)) },
                text = state.template.name,
                readOnly = state.readOnly,
                onValueChange = actions.onNameChange,
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextEdit(
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(id = R.string.module_author)) },
                text = state.template.author,
                readOnly = state.readOnly,
                onValueChange = actions.onAuthorChange,
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextEdit(
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(id = R.string.app_profile_template_description)) },
                text = state.template.description,
                readOnly = state.readOnly,
                singleLine = false,
                onValueChange = actions.onDescriptionChange,
            )

            Spacer(Modifier.height(16.dp))

            RootProfileConfigBreeze(
                readOnly = state.readOnly,
                profile = toNativeProfile(state.template),
                onProfileChange = actions.onProfileChange,
            )

            Spacer(
                Modifier
                    .imePadding()
                    .height(16.dp + innerPadding.calculateBottomPadding())
            )
        }
    }
}

@Composable
private fun TopBar(
    title: String,
    readOnly: Boolean,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onDelete: () -> Unit = {},
    onSave: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior,
) {
    LargeFlexibleTopAppBar(
        modifier = modifier,
        title = { Text(title) },
        navigationIcon = {
            if (LocalIsDetailPane.current) return@LargeFlexibleTopAppBar
            BreezeBackButton(
                onClick = onBack,
                collapseFraction = scrollBehavior.state.collapsedFraction,
            )
        },
        actions = {
            SplitScreenRatioButton()
            if (readOnly) return@LargeFlexibleTopAppBar
            IconButton(onClick = onDelete) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete_forever_rounded_filled),
                    contentDescription = stringResource(id = R.string.app_profile_template_delete),
                )
            }
            IconButton(onClick = onSave) {
                Icon(
                    painter = painterResource(R.drawable.ic_save_rounded_filled),
                    contentDescription = stringResource(id = R.string.app_profile_template_save),
                )
            }
        },
        colors = expressiveTopBarColors(),
        windowInsets = TopAppBarDefaults.windowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        scrollBehavior = scrollBehavior,
    )
}
