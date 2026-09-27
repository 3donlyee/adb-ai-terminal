package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.adb.AdbConnectionStatus
import com.example.ui.theme.TerminalBlack
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalCardSurface
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalDarkSurface
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalRed
import com.example.ui.theme.TerminalYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.viewmodel.AdbViewModel

@Composable
fun WirelessPairingScreen(
    viewModel: AdbViewModel,
    onNavigateToTerminal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pairingState by viewModel.pairingState.collectAsState()
    val deviceSpecs by viewModel.deviceSpecs.collectAsState()

    var pairingPortInput by remember { mutableStateOf(pairingState.pairingPort) }
    var pairingCodeInput by remember { mutableStateOf(pairingState.pairingCode) }
    var connectPortInput by remember { mutableStateOf(pairingState.connectPort) }

    val isConnectingOrPairing = pairingState.status == AdbConnectionStatus.PAIRING ||
            pairingState.status == AdbConnectionStatus.CONNECTING

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBlack)
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F262A))
                    .border(1.dp, TerminalCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = TerminalCyan,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "الاقتران اللاسلكي بنمط Shizuku",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Text(
                    text = "Android 13 / ColorOS 13 • تصحيح الأخطاء اللاسلكي",
                    fontSize = 11.sp,
                    color = TerminalCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Connection Status Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, TerminalCardBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = TerminalCardSurface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon = when (pairingState.status) {
                    AdbConnectionStatus.CONNECTED -> Icons.Default.CheckCircle
                    AdbConnectionStatus.PAIRED -> Icons.Default.Link
                    AdbConnectionStatus.PAIRING, AdbConnectionStatus.CONNECTING -> Icons.Default.DeveloperMode
                    AdbConnectionStatus.ERROR -> Icons.Default.Error
                    AdbConnectionStatus.DISCONNECTED -> Icons.Default.Wifi
                }
                val iconColor = when (pairingState.status) {
                    AdbConnectionStatus.CONNECTED -> TerminalGreen
                    AdbConnectionStatus.PAIRED -> TerminalCyan
                    AdbConnectionStatus.PAIRING, AdbConnectionStatus.CONNECTING -> TerminalYellow
                    AdbConnectionStatus.ERROR -> TerminalRed
                    AdbConnectionStatus.DISCONNECTED -> TextMuted
                }

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pairingState.statusMessage,
                        color = TextPrimaryDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "IP الهاتف الحالي: ${pairingState.hostIp}",
                        color = TerminalCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (pairingState.status == AdbConnectionStatus.CONNECTED) {
                    Button(
                        onClick = onNavigateToTerminal,
                        colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "فتح الترمينال",
                            color = TerminalBlack,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step 1: Open Developer Settings in ColorOS 13
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, TerminalCardBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = TerminalCardSurface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الخطوة 1: تفعيل تصحيح الأخطاء اللاسلكي",
                        fontWeight = FontWeight.Bold,
                        color = TerminalCyan,
                        fontSize = 13.sp
                    )

                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                try {
                                    val intent = Intent(Settings.ACTION_SETTINGS)
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "يرجى فتح إعدادات الهاتف يدوياً", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B2A38)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCyan.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Launch,
                            contentDescription = null,
                            tint = TerminalCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "فتح خيارات المطور",
                            color = TerminalCyan,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "في هاتف Oppo Reno 5 (ColorOS 13):\n1. الإعدادات > إعدادات إضافية / النظام > خيارات المطور\n2. قم بتفعيل 'تصحيح الأخطاء اللاسلكي' (Wireless Debugging)\n3. اضغط على 'إقران الجهاز باستخدام رمز إقران' (Pair device with pairing code).",
                    fontSize = 12.sp,
                    color = TextSecondaryDark,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Step 2: Pairing Inputs (Port + 6-digit Code)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, TerminalCardBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = TerminalCardSurface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = null,
                        tint = TerminalYellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "الخطوة 2: إدخال رمز ومنفذ الاقتران",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pairing Port
                    OutlinedTextField(
                        value = pairingPortInput,
                        onValueChange = { pairingPortInput = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("منفذ الاقتران (Port)", fontSize = 11.sp) },
                        placeholder = { Text("مثال: 38291", fontSize = 11.sp, color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(color = TextPrimaryDark, fontFamily = FontFamily.Monospace),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = TerminalDarkSurface,
                            unfocusedContainerColor = TerminalDarkSurface,
                            focusedBorderColor = TerminalYellow,
                            unfocusedBorderColor = TerminalCardBorder
                        )
                    )

                    // 6-digit Code
                    OutlinedTextField(
                        value = pairingCodeInput,
                        onValueChange = { if (it.length <= 6) pairingCodeInput = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("رمز الاقتران (6 أرقام)", fontSize = 11.sp) },
                        placeholder = { Text("مثال: 849201", fontSize = 11.sp, color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(color = TerminalYellow, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = TerminalDarkSurface,
                            unfocusedContainerColor = TerminalDarkSurface,
                            focusedBorderColor = TerminalYellow,
                            unfocusedBorderColor = TerminalCardBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        viewModel.pairDevice(pairingPortInput, pairingCodeInput)
                    },
                    enabled = !isConnectingOrPairing && pairingPortInput.isNotBlank() && pairingCodeInput.length == 6,
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalYellow),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (pairingState.status == AdbConnectionStatus.PAIRING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = TerminalBlack,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "بدء الاقتران بنمط Shizuku (Pair Device)",
                            color = TerminalBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Step 3: Main Connection Port
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, TerminalCardBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = TerminalCardSurface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Cable,
                        contentDescription = null,
                        tint = TerminalGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "الخطوة 3: الاتصال وتفعيل جلسة ADB",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "بعد نجاح الاقتران، ارجع إلى شاشة 'تصحيح الأخطاء اللاسلكي' الرئيسية وانظر إلى المنفذ المكتوب تحت عنوان IP (مثال: 192.168.1.15:43821) وأدخل آخر 5 أرقام هنا:",
                    fontSize = 11.sp,
                    color = TextSecondaryDark,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = connectPortInput,
                    onValueChange = { connectPortInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("منفذ الاتصال الرئيسي (Connect Port)", fontSize = 11.sp) },
                    placeholder = { Text("مثال: 43821", fontSize = 11.sp, color = TextMuted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = TextStyle(color = TerminalGreen, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = TerminalDarkSurface,
                        unfocusedContainerColor = TerminalDarkSurface,
                        focusedBorderColor = TerminalGreen,
                        unfocusedBorderColor = TerminalCardBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        viewModel.connectDevice(connectPortInput)
                    },
                    enabled = !isConnectingOrPairing && connectPortInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (pairingState.status == AdbConnectionStatus.CONNECTING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = TerminalBlack,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "تفعيل جلسة ADB اللاسلكية والاتصال",
                            color = TerminalBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Direct Shizuku Integration
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF384357), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141A24))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = TerminalCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "الربط المباشر مع تطبيق Shizuku (اختياري)",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "إذا قمت بتشغيل Shizuku مسبقاً على هاتفك، يمكنك الضغط هنا للربط المباشر معه وتفعيل كافة الصلاحيات بدون إعادة الاقتران.",
                    fontSize = 11.sp,
                    color = TextSecondaryDark,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { viewModel.connectShizuku() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F3547)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "الربط عبر خدمة Shizuku Binder IPC",
                        color = TerminalCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
