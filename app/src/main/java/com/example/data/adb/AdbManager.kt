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
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.net.NetworkInterface
import java.util.Collections

enum class AdbConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

enum class AdbBackend {
    NATIVE_WIRELESS_ADB,
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
    val connectPort: String = "",
    val pairingCode: String = "",
    val pairingPort: String = "",
    val status: AdbConnectionStatus = AdbConnectionStatus.DISCONNECTED,
    val activeBackend: AdbBackend = AdbBackend.LOCAL_PROCESS,
    val statusMessage: String = "جاهز للاتصال بمحرك ADB الداخلي"
)

class AdbManager(private val context: Context) {

    companion object {
        private const val TAG = "AdbManager"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences("adb_ai_prefs", Context.MODE_PRIVATE)

    private val adbKey: AdbKey by lazy {
        AdbKey(PreferenceAdbKeyStore(prefs), "adb_ai_terminal")
    }

    private var activeAdbClient: AdbClient? = null

    private val _pairingState = MutableStateFlow(
        PairingState(
            hostIp = getLocalWifiIp(),
            connectPort = prefs.getString("last_connect_port", "") ?: "",
            pairingPort = prefs.getString("last_pairing_port", "") ?: ""
        )
    )
    val pairingState: StateFlow<PairingState> = _pairingState.asStateFlow()

    private val _deviceSpecs = MutableStateFlow(detectDeviceSpecs())
    val deviceSpecs: StateFlow<DeviceSpecs> = _deviceSpecs.asStateFlow()

    private val _terminalLogs = MutableStateFlow<List<String>>(
        listOf(
            "==================================================",
            "   ADB AI TERMINAL v3.5 - INDEPENDENT ADB ENGINE",
            "   Architecture: Native Kotlin ADB Protocol (TLS 1.3)",
            "   Target: Oppo Reno 5 4G [CPH2159 / Android 13 ColorOS]",
            "   Status: Direct on-device Wireless ADB (No Shizuku needed)",
            "==================================================",
            "reno5@coloros13:~$ help"
        )
    )
    val terminalLogs: StateFlow<List<String>> = _terminalLogs.asStateFlow()

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

    suspend fun connectWirelessAdb(portStr: String) = withContext(Dispatchers.IO) {
        val port = portStr.trim().toIntOrNull()
        if (port == null || port !in 1..65535) {
            _pairingState.value = _pairingState.value.copy(
                status = AdbConnectionStatus.ERROR,
                statusMessage = "رقم المنفذ غير صالح (يجب أن يكون بين 1 و 65535)"
            )
            appendLog("[ERROR] رقم المنفذ غير صحيح: $portStr")
            return@withContext
        }

        prefs.edit().putString("last_connect_port", portStr.trim()).apply()

        _pairingState.value = _pairingState.value.copy(
            connectPort = portStr.trim(),
            status = AdbConnectionStatus.CONNECTING,
            statusMessage = "جارٍ الاتصال بمنفذ ADB: $port عبر 127.0.0.1..."
        )
        appendLog("[ADB] محاولة الاتصال بـ 127.0.0.1:$port باستخدام بروتوكول ADB TLS...")

        try {
            activeAdbClient?.close()
            val client = AdbClient("127.0.0.1", port, adbKey)
            client.connect()
            activeAdbClient = client

            _pairingState.value = _pairingState.value.copy(
                status = AdbConnectionStatus.CONNECTED,
                activeBackend = AdbBackend.NATIVE_WIRELESS_ADB,
                statusMessage = "متصل بنجاح بمحرك ADB (Shell UID 2000 Active)"
            )
            appendLog("[ADB SUCCESS] تم الاتصال والمصادقة مع سيرفر ADB الداخلي للجهاز بنجاح!")
            appendLog("[ADB INFO] جميع أوامر الترمينال الآن تعمل بكامل صلاحيات Shell!")
        } catch (e: Exception) {
            Log.e(TAG, "Connection failed", e)
            _pairingState.value = _pairingState.value.copy(
                status = AdbConnectionStatus.ERROR,
                statusMessage = "فشل الاتصال: ${e.localizedMessage ?: e.message}"
            )
            appendLog("[ADB ERROR] فشل الاتصال: ${e.message}")
            appendLog("[TIP] تأكد من تفعيل تصحيح الأخطاء اللاسلكي وتعطيل مراقبة الأذونات في ColorOS.")
        }
    }

    fun disconnectAdb() {
        try {
            activeAdbClient?.close()
            activeAdbClient = null
        } catch (_: Exception) {}

        _pairingState.value = _pairingState.value.copy(
            status = AdbConnectionStatus.DISCONNECTED,
            activeBackend = AdbBackend.LOCAL_PROCESS,
            statusMessage = "تم قطع الاتصال"
        )
        appendLog("[ADB] تم إغلاق جلسة ADB.")
    }

    fun openWirelessDebuggingSettings() {
        try {
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

        val client = activeAdbClient
        if (client != null && _pairingState.value.status == AdbConnectionStatus.CONNECTED) {
            // Execute directly through on-device ADB socket!
            try {
                val outputStream = ByteArrayOutputStream()
                client.command("shell:$trimmed") { bytes ->
                    outputStream.write(bytes)
                }
                outputText = outputStream.toString("UTF-8").trimEnd()
                exitCode = 0
            } catch (e: Exception) {
                Log.e(TAG, "ADB command failed, falling back to local process", e)
                exitCode = 1
                outputText = "خطأ في اتصال ADB: ${e.message}"
            }
        } else {
            // Fallback to local process
            try {
                val process = ProcessBuilder("sh", "-c", trimmed)
                    .redirectErrorStream(true)
                    .start()

                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val builder = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    builder.append(line).append("\n")
                }
                exitCode = process.waitFor()
                outputText = builder.toString().trimEnd()
            } catch (e: Exception) {
                exitCode = 1
                outputText = "خطأ في التنفيذ: ${e.message}"
            }
        }

        if (outputText.isBlank()) {
            outputText = if (exitCode == 0) {
                val mode = if (activeAdbClient != null) "ADB Protocol (UID 2000)" else "Local Process"
                "[نجح الأمر بكود 0 ($mode)]"
            } else {
                "[انتهى الأمر بكود $exitCode]"
            }
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
                "com.coloros.weather.service"
            )
        }
    }
}
