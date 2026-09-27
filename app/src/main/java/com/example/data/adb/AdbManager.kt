package com.example.data.adb

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.service.quicksettings.TileService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.NetworkInterface
import java.util.Collections

enum class AdbConnectionStatus {
    DISCONNECTED,
    PAIRING,
    PAIRED,
    CONNECTING,
    CONNECTED,
    ERROR
}

enum class AdbBackend {
    SHIZUKU_IPC,
    WIRELESS_ADB,
    LOCAL_PROCESS
}

data class DeviceSpecs(
    val modelName: String,
    val brand: String,
    val androidVersion: String,
    val sdkInt: Int,
    val colorOsVersion: String,
    val chipset: String,
    val displaySpecs: String,
    val localIp: String,
    val isOppoReno5: Boolean
)

data class AdbResult(
    val command: String,
    val output: String,
    val exitCode: Int,
    val executionTimeMs: Long,
    val isError: Boolean = exitCode != 0
)

data class PairingState(
    val hostIp: String = "127.0.0.1",
    val pairingPort: String = "",
    val pairingCode: String = "",
    val connectPort: String = "",
    val status: AdbConnectionStatus = AdbConnectionStatus.DISCONNECTED,
    val activeBackend: AdbBackend = AdbBackend.LOCAL_PROCESS,
    val statusMessage: String = "جاهز للربط",
    val isShizukuInstalled: Boolean = false,
    val isShizukuRunning: Boolean = false,
    val isShizukuPermissionGranted: Boolean = false,
    val shizukuUid: Int = -1,
    val shizukuVersion: Int = -1
)

class AdbManager(private val context: Context) {

    companion object {
        const val SHIZUKU_REQUEST_CODE = 8001
        const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences("adb_ai_prefs", Context.MODE_PRIVATE)

    private val _pairingState = MutableStateFlow(
        PairingState(
            hostIp = getLocalWifiIp(),
            pairingPort = prefs.getString("last_pairing_port", "") ?: "",
            connectPort = prefs.getString("last_connect_port", "") ?: "",
            isShizukuInstalled = checkShizukuInstalled()
        )
    )
    val pairingState: StateFlow<PairingState> = _pairingState.asStateFlow()

    private val _deviceSpecs = MutableStateFlow(detectDeviceSpecs())
    val deviceSpecs: StateFlow<DeviceSpecs> = _deviceSpecs.asStateFlow()

    private val _terminalLogs = MutableStateFlow<List<String>>(
        listOf(
            "==================================================",
            "   ADB AI TERMINAL v3.0 - SHIZUKU PROTOCOL READY",
            "   Target: Oppo Reno 5 4G [CPH2159 / Android 13 ColorOS]",
            "   Integration: Native Shizuku IPC Binder & Wireless ADB",
            "==================================================",
            "reno5@coloros13:~$ help"
        )
    )
    val terminalLogs: StateFlow<List<String>> = _terminalLogs.asStateFlow()

    // Shizuku Listeners
    private val permissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == SHIZUKU_REQUEST_CODE) {
            val granted = grantResult == PackageManager.PERMISSION_GRANTED
            updateShizukuStatus()
            if (granted) {
                appendLog("[SHIZUKU] تم منح الصلاحية بنجاح! UID: ${runCatching { Shizuku.getUid() }.getOrDefault(2000)}")
            } else {
                appendLog("[SHIZUKU] تم رفض الصلاحية من قبل المستخدم")
            }
        }
    }

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        updateShizukuStatus()
        appendLog("[SHIZUKU] تم استقبال اتصال Binder بنجاح (Service Active)")
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        updateShizukuStatus()
        appendLog("[SHIZUKU] انقطع اتصال Shizuku Binder")
    }

    init {
        try {
            Shizuku.addRequestPermissionResultListener(permissionListener)
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
        } catch (_: Exception) {}
        updateShizukuStatus()
    }

    fun updateShizukuStatus() {
        val installed = checkShizukuInstalled()
        val isRunning = try {
            Shizuku.pingBinder()
        } catch (_: Exception) {
            false
        }

        val hasPermission = if (isRunning) {
            try {
                if (Shizuku.isPreV11()) {
                    false
                } else {
                    Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
                }
            } catch (_: Exception) {
                false
            }
        } else {
            false
        }

        val uid = if (isRunning && hasPermission) {
            try { Shizuku.getUid() } catch (_: Exception) { -1 }
        } else {
            -1
        }

        val version = if (isRunning) {
            try { Shizuku.getVersion() } catch (_: Exception) { -1 }
        } else {
            -1
        }

        val newStatus = when {
            isRunning && hasPermission -> AdbConnectionStatus.CONNECTED
            isRunning && !hasPermission -> AdbConnectionStatus.PAIRED
            else -> if (_pairingState.value.status == AdbConnectionStatus.CONNECTED && _pairingState.value.activeBackend == AdbBackend.WIRELESS_ADB) {
                AdbConnectionStatus.CONNECTED
            } else {
                AdbConnectionStatus.DISCONNECTED
            }
        }

        val statusMsg = when {
            isRunning && hasPermission -> "متصل بنجاح عبر Shizuku (UID: $uid - Shell Mode)"
            isRunning && !hasPermission -> "خدمة Shizuku تعمل! اضغط لطلب الإذن والمصادقة"
            installed -> "تطبيق Shizuku مثبت، يرجى تشغيل الخدمة أو الاقتران"
            else -> "جاهز للاقتران أو الاتصال بـ ADB"
        }

        _pairingState.value = _pairingState.value.copy(
            isShizukuInstalled = installed,
            isShizukuRunning = isRunning,
            isShizukuPermissionGranted = hasPermission,
            shizukuUid = uid,
            shizukuVersion = version,
            status = newStatus,
            activeBackend = if (isRunning && hasPermission) AdbBackend.SHIZUKU_IPC else _pairingState.value.activeBackend,
            statusMessage = statusMsg
        )
    }

    fun requestShizukuPermission() {
        if (!Shizuku.pingBinder()) {
            appendLog("[SHIZUKU] الخدمة غير نشطة حالياً. يرجى فتح تطبيق Shizuku وتشغيلها أولاً.")
            return
        }

        try {
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                updateShizukuStatus()
                appendLog("[SHIZUKU] الصلاحيات ممنوحة بالفعل!")
            } else {
                Shizuku.requestPermission(SHIZUKU_REQUEST_CODE)
                appendLog("[SHIZUKU] تم إرسال طلب الصلاحية...")
            }
        } catch (e: Exception) {
            appendLog("[ERROR] فشل طلب صلاحيات Shizuku: ${e.message}")
        }
    }

    fun launchShizukuApp() {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(SHIZUKU_PACKAGE)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        } else {
            // Open GitHub release or Play Store
            try {
                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/thedjchi/Shizuku/releases/latest"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    fun openWirelessDebuggingSettings() {
        try {
            // Shizuku's exact tile intent
            val intent = Intent(TileService.ACTION_QS_TILE_PREFERENCES).apply {
                setPackage("com.android.settings")
                putExtra(
                    Intent.EXTRA_COMPONENT_NAME,
                    ComponentName("com.android.settings", "com.android.settings.development.qstile.DevelopmentTiles\$WirelessDebugging")
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
                    putExtra(":settings:fragment_args_key", "toggle_adb_wireless")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                try {
                    val intent = Intent(Settings.ACTION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {}
            }
        }
    }

    fun appendLog(line: String) {
        val current = _terminalLogs.value.toMutableList()
        current.add(line)
        if (current.size > 1500) {
            _terminalLogs.value = current.takeLast(1000)
        } else {
            _terminalLogs.value = current
        }
    }

    fun clearTerminal() {
        _terminalLogs.value = listOf(
            "reno5@coloros13:~$ terminal cleared"
        )
    }

    private fun checkShizukuInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo(SHIZUKU_PACKAGE, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun getLocalWifiIp(): String {
        return try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (intf.name.contains("wlan") || intf.name.contains("ap")) {
                    val addrs = Collections.list(intf.inetAddresses)
                    for (addr in addrs) {
                        if (!addr.isLoopbackAddress && addr.hostAddress?.indexOf(':') == -1) {
                            return addr.hostAddress ?: "127.0.0.1"
                        }
                    }
                }
            }
            "127.0.0.1"
        } catch (e: Exception) {
            "127.0.0.1"
        }
    }

    private fun detectDeviceSpecs(): DeviceSpecs {
        val model = Build.MODEL ?: "CPH2159"
        val brand = Build.BRAND ?: "OPPO"
        val isReno5 = model.contains("CPH2159", ignoreCase = true) ||
                model.contains("Reno5", ignoreCase = true) ||
                model.contains("Reno 5", ignoreCase = true)

        var colorOs = "ColorOS 13.0 (Android 13)"
        try {
            val p = Runtime.getRuntime().exec("getprop ro.build.version.oplusrom")
            val reader = BufferedReader(InputStreamReader(p.inputStream))
            val line = reader.readLine()
            if (!line.isNullOrBlank()) {
                colorOs = "ColorOS $line"
            }
        } catch (_: Exception) {}

        return DeviceSpecs(
            modelName = if (isReno5) "OPPO Reno5 4G ($model)" else "$brand $model",
            brand = brand,
            androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            sdkInt = Build.VERSION.SDK_INT,
            colorOsVersion = colorOs,
            chipset = "Qualcomm Snapdragon 720G (SM7125, Adreno 618)",
            displaySpecs = "6.4\" 90Hz FHD+ Super AMOLED (1080x2400)",
            localIp = getLocalWifiIp(),
            isOppoReno5 = isReno5
        )
    }

    private fun createShizukuProcess(cmd: Array<String>): Process {
        val method = Shizuku::class.java.getDeclaredMethod(
            "newProcess",
            Array<String>::class.java,
            Array<String>::class.java,
            String::class.java
        )
        method.isAccessible = true
        return method.invoke(null, cmd, null, null) as Process
    }

    /**
     * Executes command using Shizuku Binder Process (UID 2000 Shell) or standard ProcessBuilder
     */
    suspend fun executeCommand(commandStr: String): AdbResult = withContext(Dispatchers.IO) {
        val trimmed = commandStr.trim()
        if (trimmed.isEmpty()) {
            return@withContext AdbResult("", "أمر فارغ", 0, 0)
        }

        appendLog("reno5@coloros13:~$ $trimmed")
        val startTime = SystemClock.elapsedRealtime()

        if (trimmed.equals("clear", ignoreCase = true)) {
            clearTerminal()
            return@withContext AdbResult(trimmed, "تم مسح الشاشة", 0, 0)
        }

        var outputText = ""
        var exitCode = 0

        val isShizukuAvailable = try {
            Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (_: Exception) {
            false
        }

        try {
            val process: Process = if (isShizukuAvailable) {
                // Execute with real ADB UID 2000 privileges
                createShizukuProcess(arrayOf("sh", "-c", trimmed))
            } else {
                ProcessBuilder("sh", "-c", trimmed)
                    .redirectErrorStream(true)
                    .start()
            }

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errReader = BufferedReader(InputStreamReader(process.errorStream))
            val builder = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                builder.append(line).append("\n")
            }
            while (errReader.readLine().also { line = it } != null) {
                builder.append(line).append("\n")
            }

            exitCode = process.waitFor()
            outputText = builder.toString().trimEnd()

            if (outputText.isBlank()) {
                outputText = if (exitCode == 0) {
                    val authMode = if (isShizukuAvailable) "Shizuku Shell UID 2000" else "Local Process"
                    "[نجح التنفيذ بكود 0 ($authMode)]"
                } else {
                    "[فشل الأمر بكود $exitCode]"
                }
            }
        } catch (e: Exception) {
            exitCode = 1
            outputText = "خطأ في تنفيذ الأمر: ${e.localizedMessage ?: e.message}"
        }

        val elapsed = SystemClock.elapsedRealtime() - startTime

        for (outLine in outputText.lines()) {
            appendLog(outLine)
        }

        AdbResult(
            command = trimmed,
            output = outputText,
            exitCode = exitCode,
            executionTimeMs = elapsed,
            isError = exitCode != 0
        )
    }

    fun getInstalledPackages(): List<String> {
        return try {
            val pkgs = context.packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            pkgs.map { it.packageName }.sorted()
        } catch (e: Exception) {
            listOf(
                "com.heytap.browser",
                "com.oppo.market",
                "com.heytap.habit.analysis",
                "com.coloros.gamespace",
                "com.oplus.cosa",
                "com.coloros.weather.service",
                "moe.shizuku.privileged.api"
            )
        }
    }
}
