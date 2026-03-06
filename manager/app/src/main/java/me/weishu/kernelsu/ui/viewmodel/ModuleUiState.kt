package me.weishu.kernelsu.ui.viewmodel

import androidx.compose.runtime.Immutable
import me.weishu.kernelsu.data.model.Module
import me.weishu.kernelsu.data.model.ModuleUpdateInfo
import me.weishu.kernelsu.ui.component.SearchStatus

@Immutable
sealed interface ModuleDialogState {
    data class Uninstall(val module: Module) : ModuleDialogState
    data class Update(
        val module: Module,
        val updateInfo: ModuleUpdateInfo,
        val changelog: String? = null // null means loading
    ) : ModuleDialogState
}

data class ModuleUiState(
    val isRefreshing: Boolean = false,
    val modules: List<Module> = emptyList(),
    val moduleList: List<Module> = emptyList(),
    val updateInfo: Map<String, ModuleUpdateInfo> = emptyMap(),
    val searchStatus: SearchStatus = SearchStatus(""),
    val searchResults: List<Module> = emptyList(),
    val sortEnabledFirst: Boolean = false,
    val sortActionFirst: Boolean = false,
    val checkModuleUpdate: Boolean = true,

    val dialogState: ModuleDialogState? = null,
    val operatingModules: Set<String> = emptySet()
)