package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.adb.AdbConnectionStatus
import com.example.data.adb.AdbManager
import com.example.data.adb.AdbResult
import com.example.data.adb.DeviceSpecs
import com.example.data.adb.PairingState
import com.example.data.api.AiAdbCommandResponse
import com.example.data.api.GeminiAdbService
import com.example.data.db.AppDatabase
import com.example.data.db.CommandHistoryEntity
import com.example.data.db.SavedScriptEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AiUiState(
    val isLoading: Boolean = false,
    val prompt: String = "",
    val response: AiAdbCommandResponse? = null,
    val errorMessage: String? = null
)

class AdbViewModel(application: Application) : AndroidViewModel(application) {

    private val adbManager = AdbManager(application)
    private val database = AppDatabase.getDatabase(application)
    private val historyDao = database.commandHistoryDao()
    private val scriptDao = database.savedScriptDao()

    val pairingState: StateFlow<PairingState> = adbManager.pairingState
    val deviceSpecs: StateFlow<DeviceSpecs> = adbManager.deviceSpecs
    val terminalLogs: StateFlow<List<String>> = adbManager.terminalLogs

    val historyList: StateFlow<List<CommandHistoryEntity>> = historyDao.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedScripts: StateFlow<List<SavedScriptEntity>> = scriptDao.getAllScripts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _aiState = MutableStateFlow(AiUiState())
    val aiState: StateFlow<AiUiState> = _aiState.asStateFlow()

    private val _installedPackages = MutableStateFlow<List<String>>(emptyList())
    val installedPackages: StateFlow<List<String>> = _installedPackages.asStateFlow()

    private val _isExecuting = MutableStateFlow(false)
    val isExecuting: StateFlow<Boolean> = _isExecuting.asStateFlow()

    init {
        loadInstalledPackages()
    }

    private fun loadInstalledPackages() {
        viewModelScope.launch {
            _installedPackages.value = adbManager.getInstalledPackages()
        }
    }

    fun executeCommand(commandStr: String, saveToHistory: Boolean = true) {
        if (commandStr.isBlank()) return
        viewModelScope.launch {
            _isExecuting.value = true
            val result = adbManager.executeCommand(commandStr)
            _isExecuting.value = false

            if (saveToHistory) {
                historyDao.insert(
                    CommandHistoryEntity(
                        command = result.command,
                        output = result.output,
                        exitCode = result.exitCode,
                        executionTimeMs = result.executionTimeMs,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun clearTerminal() {
        adbManager.clearTerminal()
    }

    fun requestShizukuPermission() {
        adbManager.requestShizukuPermission()
    }

    fun launchShizukuApp() {
        adbManager.launchShizukuApp()
    }

    fun openWirelessDebuggingSettings() {
        adbManager.openWirelessDebuggingSettings()
    }

    fun refreshShizukuStatus() {
        adbManager.updateShizukuStatus()
    }

    fun askAi(prompt: String) {
        if (prompt.isBlank()) return
        viewModelScope.launch {
            _aiState.value = _aiState.value.copy(
                isLoading = true,
                prompt = prompt,
                errorMessage = null
            )

            val specs = deviceSpecs.value
            val contextString = "Device: ${specs.modelName}, Android: ${specs.androidVersion}, ColorOS: ${specs.colorOsVersion}, Chipset: ${specs.chipset}, Display: ${specs.displaySpecs}"

            val result = GeminiAdbService.generateAdbCommand(prompt, contextString)
            result.onSuccess { response ->
                _aiState.value = _aiState.value.copy(
                    isLoading = false,
                    response = response
                )
            }.onFailure { err ->
                _aiState.value = _aiState.value.copy(
                    isLoading = false,
                    errorMessage = err.localizedMessage ?: "حدث خطأ في توليد الأمر"
                )
            }
        }
    }

    fun setPrompt(text: String) {
        _aiState.value = _aiState.value.copy(prompt = text)
    }

    fun toggleFavorite(id: Long, currentStatus: Boolean) {
        viewModelScope.launch {
            historyDao.setFavorite(id, !currentStatus)
        }
    }

    fun deleteHistory(item: CommandHistoryEntity) {
        viewModelScope.launch {
            historyDao.delete(item)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyDao.clearAll()
        }
    }

    fun saveScript(title: String, desc: String, commands: String, category: String) {
        viewModelScope.launch {
            scriptDao.insert(
                SavedScriptEntity(
                    title = title,
                    description = desc,
                    commands = commands,
                    category = category,
                    isBuiltIn = false
                )
            )
        }
    }

    fun deleteScript(script: SavedScriptEntity) {
        viewModelScope.launch {
            if (!script.isBuiltIn) {
                scriptDao.delete(script)
            }
        }
    }

    // Quick Action Shortcuts for Reno 5 / ColorOS 13
    fun quickForce90Hz() {
        executeCommand("settings put system peak_refresh_rate 90 && settings put system min_refresh_rate 90")
    }

    fun quickReset60Hz() {
        executeCommand("settings put system peak_refresh_rate 60")
    }

    fun quickSetAnimationScale(scale: Float) {
        executeCommand("settings put global window_animation_scale $scale && settings put global transition_animation_scale $scale && settings put global animator_duration_scale $scale")
    }

    fun quickGrantPermission(pkg: String, permission: String) {
        executeCommand("pm grant $pkg $permission")
    }

    fun quickUninstallPackage(pkg: String) {
        executeCommand("pm uninstall -k --user 0 $pkg")
    }

    fun quickReinstallPackage(pkg: String) {
        executeCommand("cmd package install-existing $pkg")
    }
}
