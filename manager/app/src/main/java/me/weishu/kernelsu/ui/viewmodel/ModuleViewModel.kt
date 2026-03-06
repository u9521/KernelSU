package me.weishu.kernelsu.ui.viewmodel

import android.content.Intent
import android.os.SystemClock
import android.util.Log
import androidx.annotation.StringRes
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.R
import me.weishu.kernelsu.data.model.Module
import me.weishu.kernelsu.data.repository.ModuleRepository
import me.weishu.kernelsu.data.repository.ModuleRepositoryImpl
import me.weishu.kernelsu.ksuApp
import me.weishu.kernelsu.ui.component.SearchStatus
import me.weishu.kernelsu.ui.util.HanziToPinyin
import me.weishu.kernelsu.ui.util.toggleModule
import me.weishu.kernelsu.ui.util.undoUninstallModule
import me.weishu.kernelsu.ui.util.uninstallModule
import me.weishu.kernelsu.ui.webui.WebUIActivity
import java.text.Collator
import java.util.Locale

class ModuleViewModel(
    private val repo: ModuleRepository = ModuleRepositoryImpl()
) : ViewModel() {

    sealed interface ModuleIntent {
        data class Toggle(val module: ModuleInfo) : ModuleIntent
        data class RequestUninstall(val module: ModuleInfo) : ModuleIntent
        data class ConfirmUninstall(val module: ModuleInfo) : ModuleIntent
        data class UndoUninstall(val module: ModuleInfo) : ModuleIntent

        data class RequestUpdate(val module: ModuleInfo, val updateInfo: ModuleUpdateInfo?) : ModuleIntent
        data class ConfirmUpdate(val module: ModuleInfo, val updateInfo: ModuleUpdateInfo) : ModuleIntent

        data object DismissDialog : ModuleIntent

        data class OpenWebUI(val module: ModuleInfo) : ModuleIntent
        data class OpenAction(val module: ModuleInfo) : ModuleIntent
    }

    sealed interface ModuleUiEffect {
        data class ShowSnackbar(
            @get:StringRes val messageRes: Int,
            val formatArgs: List<Any> = emptyList(),
            @get:StringRes val actionLabelRes: Int? = null,
            val withReboot: Boolean = false
        ) : ModuleUiEffect

        data class ShowToast(@get:StringRes val messageRes: Int) : ModuleUiEffect

        data class StartDownload(
            val url: String,
            val fileName: String,
            val moduleName: String
        ) : ModuleUiEffect

        data class RunModuleAction(val moduleId: String) : ModuleUiEffect
        data class LaunchIntent(val intent: Intent) : ModuleUiEffect
    }

    typealias ModuleInfo = Module
    typealias ModuleUpdateInfo = me.weishu.kernelsu.data.model.ModuleUpdateInfo

    companion object {
        private const val TAG = "ModuleViewModel"
    }

    private data class ModuleUpdateSignature(
        val updateJson: String,
        val versionCode: Int,
        val enabled: Boolean,
        val update: Boolean,
        val remove: Boolean
    )

    private data class ModuleUpdateCache(
        val signature: ModuleUpdateSignature,
        val info: ModuleUpdateInfo
    )

    private val _uiState = MutableStateFlow(ModuleUiState())
    val uiState: StateFlow<ModuleUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<ModuleUiEffect>()
    val effect = _effect.asSharedFlow()

    private val updateInfoMutex = Mutex()
    private var updateInfoCache: MutableMap<String, ModuleUpdateCache> = mutableMapOf()
    private val updateInfoInFlight = mutableSetOf<String>()

    var isNeedRefresh = false
        private set

    fun onIntent(intent: ModuleIntent) {
        viewModelScope.launch {
            when (intent) {
                is ModuleIntent.Toggle -> handleToggle(intent.module)

                is ModuleIntent.RequestUninstall -> {
                    _uiState.update { it.copy(dialogState = ModuleDialogState.Uninstall(intent.module)) }
                }

                is ModuleIntent.ConfirmUninstall -> handleConfirmUninstall(intent.module)
                is ModuleIntent.UndoUninstall -> handleUndoUninstall(intent.module)

                is ModuleIntent.RequestUpdate -> handleRequestUpdate(intent.module, intent.updateInfo)
                is ModuleIntent.ConfirmUpdate -> handleConfirmUpdate(intent.module, intent.updateInfo)

                is ModuleIntent.DismissDialog -> {
                    _uiState.update { it.copy(dialogState = null) }
                }

                is ModuleIntent.OpenWebUI -> handleOpenWebUI(intent.module)
                is ModuleIntent.OpenAction -> {
                    markNeedRefresh()
                    _effect.emit(ModuleUiEffect.RunModuleAction(intent.module.id))
                }
            }
        }
    }

    private fun isModuleBusy(moduleId: String): Boolean {
        return _uiState.value.operatingModules.contains(moduleId)
    }

    private suspend fun <T> withModuleLock(moduleId: String, block: suspend () -> T): T? {
        if (isModuleBusy(moduleId)) return null

        _uiState.update { it.copy(operatingModules = it.operatingModules + moduleId) }
        return try {
            block()
        } finally {
            _uiState.update { it.copy(operatingModules = it.operatingModules - moduleId) }
        }
    }

    private fun handleToggle(module: ModuleInfo) {
        viewModelScope.launch {
            withModuleLock(module.id) {
                val success = withContext(Dispatchers.IO) {
                    toggleModule(module.id, !module.enabled)
                }

                if (success) {
                    fetchModuleList()
                    _effect.emit(
                        ModuleUiEffect.ShowSnackbar(
                            messageRes = R.string.reboot_to_apply,
                            actionLabelRes = R.string.reboot,
                            withReboot = true
                        )
                    )
                } else {
                    val msgRes = if (module.enabled) R.string.module_failed_to_disable else R.string.module_failed_to_enable
                    _effect.emit(ModuleUiEffect.ShowSnackbar(messageRes = msgRes, formatArgs = listOf(module.name)))
                }
            }
        }
    }

    private fun handleConfirmUninstall(module: ModuleInfo) {
        viewModelScope.launch {
            _uiState.update { it.copy(dialogState = null) }

            withModuleLock(module.id) {
                val success = withContext(Dispatchers.IO) {
                    uninstallModule(module.id)
                }

                if (success) fetchModuleList()

                val msgRes = if (success) R.string.module_uninstall_success else R.string.module_uninstall_failed
                _effect.emit(
                    ModuleUiEffect.ShowSnackbar(
                        messageRes = msgRes,
                        formatArgs = listOf(module.name),
                        actionLabelRes = if (success) R.string.reboot else null,
                        withReboot = success
                    )
                )
            }
        }
    }

    private fun handleUndoUninstall(module: ModuleInfo) {
        viewModelScope.launch {
            withModuleLock(module.id) {
                val success = withContext(Dispatchers.IO) {
                    undoUninstallModule(module.id)
                }

                if (success) fetchModuleList()

                val msgRes = if (success) R.string.module_undo_uninstall_success else R.string.module_undo_uninstall_failed
                _effect.emit(ModuleUiEffect.ShowSnackbar(messageRes = msgRes, formatArgs = listOf(module.name)))
            }
        }
    }

    private fun handleRequestUpdate(module: ModuleInfo, updateInfo: ModuleUpdateInfo?) {
        if (updateInfo == null) return

        _uiState.update {
            it.copy(dialogState = ModuleDialogState.Update(module, updateInfo, changelog = null))
        }

        viewModelScope.launch {
            val changelog = withContext(Dispatchers.IO) {
                runCatching {
                    if (updateInfo.changelog.isNotEmpty()) {
                        ksuApp.okhttpClient.newCall(
                            okhttp3.Request.Builder().url(updateInfo.changelog).build()
                        ).execute().body.string()
                    } else ""
                }.getOrElse { "" }
            }

            _uiState.update { currentState ->
                val currentDialog = currentState.dialogState
                if (currentDialog is ModuleDialogState.Update && currentDialog.module.id == module.id) {
                    currentState.copy(dialogState = currentDialog.copy(changelog = changelog))
                } else {
                    currentState
                }
            }
        }
    }

    private fun handleConfirmUpdate(module: ModuleInfo, updateInfo: ModuleUpdateInfo) {
        _uiState.update { it.copy(dialogState = null) }

        val fileName = "${module.name}-${updateInfo.version}.zip"
        viewModelScope.launch {
            _effect.emit(ModuleUiEffect.StartDownload(updateInfo.downloadUrl, fileName, module.name))
        }
    }

    private fun handleOpenWebUI(module: ModuleInfo) {
        if (!module.hasWebUi) return
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setClass(ksuApp, WebUIActivity::class.java)
            data = "kernelsu://webui/${module.id}".toUri()
            putExtra("id", module.id)
            putExtra("name", module.name)
        }
        viewModelScope.launch {
            _effect.emit(ModuleUiEffect.LaunchIntent(intent))
        }
    }

    fun markNeedRefresh() {
        isNeedRefresh = true
    }

    fun setSortEnabledFirst(enabled: Boolean) {
        _uiState.update { it.copy(sortEnabledFirst = enabled) }
        viewModelScope.launch {
            updateSearchText(_uiState.value.searchStatus.searchText)
        }
    }

    fun setSortActionFirst(enabled: Boolean) {
        _uiState.update { it.copy(sortActionFirst = enabled) }
        viewModelScope.launch {
            updateSearchText(_uiState.value.searchStatus.searchText)
        }
    }

    fun setCheckModuleUpdate(enabled: Boolean) {
        _uiState.update { it.copy(checkModuleUpdate = enabled) }
    }

    fun updateSearchStatus(status: SearchStatus) {
        _uiState.update { it.copy(searchStatus = status) }
    }

    suspend fun updateSearchText(text: String) {
        _uiState.update {
            it.copy(
                searchStatus = it.searchStatus.copy(searchText = text)
            )
        }

        if (text.isEmpty()) {
            _uiState.update {
                it.copy(
                    searchStatus = it.searchStatus.copy(resultStatus = SearchStatus.ResultStatus.DEFAULT),
                    searchResults = emptyList()
                )
            }
            updateModuleList()
            return
        }

        _uiState.update {
            it.copy(searchStatus = it.searchStatus.copy(resultStatus = SearchStatus.ResultStatus.LOAD))
        }

        val result = withContext(Dispatchers.IO) {
            val modules = _uiState.value.modules
            modules.filter {
                it.id.contains(text, true) || it.name.contains(text, true) ||
                        it.description.contains(text, true) || it.author.contains(text, true) ||
                        HanziToPinyin.getInstance().toPinyinString(it.name).contains(text, true)
            }.let { filteredModules ->
                val comparator = moduleComparator(_uiState.value)
                filteredModules.sortedWith(comparator)
            }
        }

        _uiState.update {
            it.copy(
                searchResults = result,
                searchStatus = it.searchStatus.copy(
                    resultStatus = if (result.isEmpty()) SearchStatus.ResultStatus.EMPTY else SearchStatus.ResultStatus.SHOW
                )
            )
        }
    }

    private fun updateModuleList() {
        viewModelScope.launch(Dispatchers.IO) {
            val state = _uiState.value

            val sorted = state.modules.sortedWith(moduleComparator(state))

            _uiState.update { it.copy(moduleList = sorted) }
        }
    }

    private fun moduleComparator(state: ModuleUiState): Comparator<Module> {
        return compareBy<Module>(
            {
                val executable = it.hasWebUi || it.hasActionScript
                when {
                    it.metamodule && it.enabled -> 0
                    state.sortEnabledFirst && state.sortActionFirst -> when {
                        it.enabled && executable -> 1
                        it.enabled -> 2
                        executable -> 3
                        else -> 4
                    }

                    state.sortEnabledFirst && !state.sortActionFirst -> if (it.enabled) 1 else 2
                    !state.sortEnabledFirst && state.sortActionFirst -> if (executable) 1 else 2
                    else -> 1
                }
            },
            { if (state.sortEnabledFirst) !it.enabled else 0 },
            { if (state.sortActionFirst) !(it.hasWebUi || it.hasActionScript) else 0 },
        ).thenBy(Collator.getInstance(Locale.getDefault()), Module::id)
    }

    suspend fun loadModuleList() {
        val parsedModules = withContext(Dispatchers.IO) {
            repo.getModules().getOrElse {
                Log.e(TAG, "fetchModuleList: ", it)
                emptyList()
            }
        }

        withContext(Dispatchers.Main) {
            _uiState.update {
                it.copy(
                    modules = parsedModules,
                )
            }
            // Trigger recalculation of moduleList
            updateModuleList()
            isNeedRefresh = false
        }
    }

    fun fetchModuleList(checkUpdate: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }

            val start = SystemClock.elapsedRealtime()

            loadModuleList()

            if (checkUpdate) syncModuleUpdateInfo(_uiState.value.modules)

            _uiState.update { it.copy(isRefreshing = false) }

            Log.i(TAG, "load cost: ${SystemClock.elapsedRealtime() - start}, modules: ${_uiState.value.modules}")
        }
    }

    private fun Module.toSignature(): ModuleUpdateSignature {
        return ModuleUpdateSignature(
            updateJson = updateJson,
            versionCode = versionCode,
            enabled = enabled,
            update = update,
            remove = remove
        )
    }

    suspend fun syncModuleUpdateInfo(modules: List<Module>) {
        if (!_uiState.value.checkModuleUpdate) return

        val modulesToFetch = mutableListOf<Triple<String, Module, ModuleUpdateSignature>>()
        val removedIds = mutableSetOf<String>()

        updateInfoMutex.withLock {
            val ids = modules.map { it.id }.toSet()
            updateInfoCache.keys.filter { it !in ids }.forEach { removedId ->
                removedIds += removedId
                updateInfoCache.remove(removedId)
                updateInfoInFlight.remove(removedId)
            }

            modules.forEach { module ->
                val signature = module.toSignature()
                val cached = updateInfoCache[module.id]
                if ((cached == null || cached.signature != signature) && updateInfoInFlight.add(module.id)) {
                    modulesToFetch += Triple(module.id, module, signature)
                }
            }
        }

        val fetchedEntries = coroutineScope {
            modulesToFetch.map { (id, module, signature) ->
                async(Dispatchers.IO) {
                    id to ModuleUpdateCache(signature, checkUpdate(module))
                }
            }.awaitAll()
        }

        val changedEntries = mutableListOf<Pair<String, ModuleUpdateInfo>>()
        updateInfoMutex.withLock {
            fetchedEntries.forEach { (id, entry) ->
                val existing = updateInfoCache[id]
                if (existing == null || existing.signature != entry.signature || existing.info != entry.info) {
                    updateInfoCache[id] = entry
                    changedEntries += id to entry.info
                }
                updateInfoInFlight.remove(id)
            }
        }

        if (removedIds.isEmpty() && changedEntries.isEmpty()) {
            return
        }

        withContext(Dispatchers.Main) {
            _uiState.update { state ->
                val newMap = state.updateInfo.toMutableMap()
                removedIds.forEach { newMap.remove(it) }
                changedEntries.forEach { (id, info) ->
                    newMap[id] = info
                }
                state.copy(updateInfo = newMap)
            }
        }
    }

    private suspend fun checkUpdate(m: Module): ModuleUpdateInfo {
        return repo.checkUpdate(m).getOrDefault(ModuleUpdateInfo.Empty)
    }
}
