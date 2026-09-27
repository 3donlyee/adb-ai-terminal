package com.example.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.adb.AdbConnectionStatus
import com.example.ui.theme.TerminalBlack
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalCardSurface
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalDarkSurface
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalPurple
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

    val isConnected = pairingState.isShizukuRunning && pairingState.isShizukuPermissionGranted
    val isRunningWaitingAuth = pairingState.isShizukuRunning && !pairingState.isShizukuPermissionGranted

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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0E2238))
                        .border(1.dp, TerminalCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = TerminalCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "اقتران Shizuku الرسمي المباشر",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = "Shizuku IPC Binder Engine • Android 13",
                        fontSize = 11.sp,
                        color = TerminalCyan
                    )
                }
            }

            // Refresh Status Button
            Button(
                onClick = { viewModel.refreshShizukuStatus() },
                colors = ButtonDefaults.buttonColors(containerColor = TerminalDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "تحديث",
                    tint = TerminalCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("تحديث", color = TerminalCyan, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Connection Status Banner
        val bannerBg = when {
            isConnected -> Color(0xFF0D2818)
            isRunningWaitingAuth -> Color(0xFF28240D)
            else -> TerminalCardSurface
        }
        val bannerBorder = when {
            isConnected -> TerminalGreen
            isRunningWaitingAuth -> TerminalYellow
            else -> TerminalCardBorder
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, bannerBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = bannerBg)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.CheckCircle else if (isRunningWaitingAuth) Icons.Default.VpnKey else Icons.Default.Link,
                        contentDescription = null,
                        tint = if (isConnected) TerminalGreen else if (isRunningWaitingAuth) TerminalYellow else TextMuted,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isConnected) "متصل عبر خدمة Shizuku بنجاح!" else if (isRunningWaitingAuth) "خدمة Shizuku تعمل بانتظار الإذن!" else "حالة الخدمة: غير متصل بعد",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark,
                            fontSize = 14.sp
                        )
                        Text(
                            text = pairingState.statusMessage,
                            color = if (isConnected) TerminalGreen else TextSecondaryDark,
                            fontSize = 12.sp
                        )
                    }
                }

                if (isConnected) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "UID: ${if (pairingState.shizukuUid >= 0) pairingState.shizukuUid else 2000} (Shell Mode Active)",
                            color = TerminalGreen,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Button(
                            onClick = onNavigateToTerminal,
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Terminal, contentDescription = null, tint = TerminalBlack, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("فتح الترمينال الآن", color = TerminalBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                } else if (isRunningWaitingAuth) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.requestShizukuPermission() },
                        colors = ButtonDefaults.buttonColors(containerColor = TerminalYellow),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = TerminalBlack, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("منح الصلاحية لتطبيق ADB AI Terminal", color = TerminalBlack, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Critical Oppo ColorOS 13 Notice
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, TerminalRed.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF241113))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = TerminalRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "شرط أساسي لهواتف OPPO (ColorOS 13):",
                        fontWeight = FontWeight.Bold,
                        color = TerminalRed,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "في نظام ColorOS 13 على Reno 5، يمنع النظام الاتصال بأوامر ADB ما لم تقم بتفعيل خيار:\n👉 'تعطيل مراقبة الأذونات' (Disable permission monitoring)\nالموجود في أسفل قائمة خيارات المطور.",
                    fontSize = 12.sp,
                    color = TextPrimaryDark,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Easy 3-Step Setup Guide
        Text(
            text = "خطوات الاقتران الصحيحة بنمط Shizuku:",
            fontWeight = FontWeight.Bold,
            color = TextPrimaryDark,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Step 1 Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, TerminalCardBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = TerminalCardSurface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "1. فتح تصحيح الأخطاء اللاسلكي",
                        fontWeight = FontWeight.Bold,
                        color = TerminalCyan,
                        fontSize = 13.sp
                    )

                    Button(
                        onClick = { viewModel.openWirelessDebuggingSettings() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF162536)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Launch, contentDescription = null, tint = TerminalCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("فتح الإعدادات فوراً", color = TerminalCyan, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "قم بتفعيل: تصحيح أخطاء USB + تصحيح الأخطاء اللاسلكي + تعطيل مراقبة الأذونات.",
                    fontSize = 11.sp,
                    color = TextSecondaryDark
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Step 2 Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, TerminalCardBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = TerminalCardSurface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "2. تشغيل خدمة Shizuku",
                        fontWeight = FontWeight.Bold,
                        color = TerminalYellow,
                        fontSize = 13.sp
                    )

                    Button(
                        onClick = { viewModel.launchShizukuApp() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2312)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalYellow),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = if (pairingState.isShizukuInstalled) Icons.Default.Launch else Icons.Default.Download,
                            contentDescription = null,
                            tint = TerminalYellow,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (pairingState.isShizukuInstalled) "فتح تطبيق Shizuku" else "تحميل Shizuku APK",
                            color = TerminalYellow,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "في تطبيق Shizuku: اضغط اقتران (Pairing) وأدخل الرمز المكون من 6 أرقام، ثم اضغط 'بدء' (Start) لتشغيل الخدمة.",
                    fontSize = 11.sp,
                    color = TextSecondaryDark
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Step 3 Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, TerminalCardBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = TerminalCardSurface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "3. تفويض الصلاحية لـ ADB AI Terminal",
                        fontWeight = FontWeight.Bold,
                        color = TerminalGreen,
                        fontSize = 13.sp
                    )

                    Button(
                        onClick = {
                            viewModel.requestShizukuPermission()
                            viewModel.refreshShizukuStatus()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = TerminalBlack, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ربط الـ Binder الآن", color = TerminalBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "بمجرد تشغيل Shizuku، اضغط الزر أعلاه للمصادقة واستلام كامل صلاحيات المطورين (UID 2000).",
                    fontSize = 11.sp,
                    color = TextSecondaryDark
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
