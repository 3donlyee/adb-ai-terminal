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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
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

    var connectPortInput by remember(pairingState.connectPort) {
        mutableStateOf(pairingState.connectPort)
    }

    val isConnected = pairingState.status == AdbConnectionStatus.CONNECTED
    val isConnecting = pairingState.status == AdbConnectionStatus.CONNECTING
    val isError = pairingState.status == AdbConnectionStatus.ERROR

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
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0E2238))
                        .border(1.dp, TerminalCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = null,
                        tint = TerminalCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "محرك ADB اللاسلكي المباشر",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = "Native On-Device TLS 1.3 Engine • مستقل بالكامل",
                        fontSize = 11.sp,
                        color = TerminalCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Connection Status Banner
        val bannerBg = when {
            isConnected -> Color(0xFF0D2818)
            isConnecting -> Color(0xFF28240D)
            isError -> Color(0xFF281113)
            else -> TerminalCardSurface
        }
        val bannerBorder = when {
            isConnected -> TerminalGreen
            isConnecting -> TerminalYellow
            isError -> TerminalRed
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
                    if (isConnecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(26.dp),
                            color = TerminalYellow,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.CheckCircle else if (isError) Icons.Default.Error else Icons.Default.Link,
                            contentDescription = null,
                            tint = if (isConnected) TerminalGreen else if (isError) TerminalRed else TextMuted,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isConnected) "متصل بنجاح بسيرفر ADB الداخلي!" else if (isConnecting) "جارٍ الاتصال والمصادقة..." else if (isError) "حدث خطأ في الاتصال" else "الحالة: غير متصل",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark,
                            fontSize = 14.sp
                        )
                        Text(
                            text = pairingState.statusMessage,
                            color = if (isConnected) TerminalGreen else if (isError) TerminalRed else TextSecondaryDark,
                            fontSize = 12.sp
                        )
                    }
                }

                if (isConnected) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onNavigateToTerminal,
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Terminal, contentDescription = null, tint = TerminalBlack, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("فتح الترمينال الآن", color = TerminalBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { viewModel.disconnectAdb() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF331616)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.LinkOff, contentDescription = null, tint = TerminalRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("قطع الاتصال", color = TerminalRed, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Direct Connect Port Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, TerminalCardBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = TerminalCardSurface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "الاتصال المباشر بمنفذ تصحيح الأخطاء اللاسلكي",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "في شاشة تصحيح الأخطاء اللاسلكي، ستجد عنوان IP والمنفذ مثل (192.168.1.5:38475). أدخل رقم المنفذ الأخير فقط (38475):",
                    fontSize = 11.sp,
                    color = TextSecondaryDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = connectPortInput,
                        onValueChange = { if (it.length <= 5) connectPortInput = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("منفذ الاتصال (Connect Port)", fontSize = 11.sp) },
                        placeholder = { Text("مثال: 38475", color = TextMuted, fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        textStyle = TextStyle(
                            color = TerminalCyan,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = TerminalDarkSurface,
                            unfocusedContainerColor = TerminalDarkSurface,
                            focusedBorderColor = TerminalCyan,
                            unfocusedBorderColor = TerminalCardBorder
                        )
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            if (connectPortInput.isNotBlank()) {
                                viewModel.connectWirelessAdb(connectPortInput.trim())
                            } else {
                                Toast.makeText(context, "يرجى إدخال رقم المنفذ أولاً", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isConnecting,
                        colors = ButtonDefaults.buttonColors(containerColor = TerminalCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Text(
                            text = if (isConnected) "إعادة الاتصال" else "اتصال بـ ADB",
                            color = TerminalBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Open Developer Settings Button
        Button(
            onClick = { viewModel.openWirelessDebuggingSettings() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF162536)),
            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCyan),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
        ) {
            Icon(imageVector = Icons.Default.Launch, contentDescription = null, tint = TerminalCyan, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("فتح شاشة تصحيح الأخطاء اللاسلكي فوراً", color = TerminalCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Reno 5 / ColorOS 13 Critical Guide
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, TerminalYellow.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF221E0F))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = TerminalYellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "سر نجاح الاتصال على Reno 5 (ColorOS 13):",
                        fontWeight = FontWeight.Bold,
                        color = TerminalYellow,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "1. **تعطيل مراقبة الأذونات:** في نهاية قائمة خيارات المطور (Developer options)، فعّل خيار \"تعطيل مراقبة الأذونات\" (Disable permission monitoring).\n\n" +
                            "2. **نافذة عائمة أو تقسيم الشاشة (Split Screen):** في أندرويد 13، إذا أغلقت نافذة الإعدادات تتغير المنافذ فوراً! افتح تطبيقنا والإعدادات جنباً إلى جنب في الشاشة المقسمة أو النافذة العائمة.\n\n" +
                            "3. **تطبيقنا لا يحتاج لأي برامج إضافية:** المحرك مدمج ذاتياً بالكامل عبر بروتوكول ADB TLS 1.3 وسيعمل معك من أول نقرة اتصال.",
                    fontSize = 12.sp,
                    color = TextPrimaryDark,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
