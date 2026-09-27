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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

data class DebloatApp(
    val name: String,
    val packageName: String,
    val description: String,
    val isSafe: Boolean = true
)

@Composable
fun SystemToolsScreen(
    viewModel: AdbViewModel,
    onExecuteInTerminal: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("الشاشة والأداء", "إزالة البرامج الزائدة", "الصلاحيات المتقدمة", "البطارية والذاكرة")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBlack)
            .padding(12.dp)
    ) {
        // Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = null,
                tint = TerminalCyan,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "أدوات مطور النظام (ColorOS 13 & Reno 5 Tweaks)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = TerminalDarkSurface,
            contentColor = TerminalCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = TerminalCyan
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, TerminalCardBorder, RoundedCornerShape(10.dp))
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) TerminalCyan else TextSecondaryDark
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> DisplayAndPerformanceTab(viewModel, onExecuteInTerminal)
            1 -> DebloaterTab(viewModel, onExecuteInTerminal)
            2 -> PermissionsTab(viewModel, onExecuteInTerminal)
            3 -> BatteryAndSystemTab(viewModel, onExecuteInTerminal)
        }
    }
}

@Composable
private fun DisplayAndPerformanceTab(
    viewModel: AdbViewModel,
    onExecuteInTerminal: (String) -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Refresh Rate (90Hz AMOLED)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalCardSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalCardBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.DisplaySettings, contentDescription = null, tint = TerminalCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "معدل التحديث (90Hz Super AMOLED)",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "هاتف Reno 5 يدعم 90Hz، لكن ColorOS قد يخفض التردد لـ 60Hz في بعض البرامج. يمكنك فرض 90Hz دائم:",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.quickForce90Hz()
                                Toast.makeText(context, "تم إرسال أمر تثبيت 90Hz", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تثبيت 90Hz دائم", color = TerminalBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.quickReset60Hz()
                                Toast.makeText(context, "تم إرجاع التردد التلقائي 60Hz", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalDarkSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("الافتراضي (60Hz)", color = TextPrimaryDark, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Animation Scales
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalCardSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalCardBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = TerminalGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "سرعة رسوم واجهة ColorOS 13",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "تقليل مقياس الرسوم المتحركة يجعل فتح التطبيقات والتنقل فائق السرعة:",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0.0f to "إيقاف (0x)", 0.25f to "0.25x", 0.5f to "0.5x سريع", 1.0f to "1.0x أصلي").forEach { (scale, label) ->
                            Button(
                                onClick = {
                                    viewModel.quickSetAnimationScale(scale)
                                    Toast.makeText(context, "تم ضبط الرسوم على $label", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (scale == 0.5f) TerminalGreen else TerminalDarkSurface
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = label,
                                    color = if (scale == 0.5f) TerminalBlack else TextPrimaryDark,
                                    fontSize = 11.sp,
                                    fontWeight = if (scale == 0.5f) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // Screen Density (DPI)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalCardSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalCardBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "كثافة الشاشة (Window Manager Density)",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "تعديل الـ DPI يغير حجم النصوص والأزرار لعرض محتوى أكثر بدون روت:",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("wm density reset" to "أصلي (Reset)", "wm density 400" to "400 DPI", "wm density 360" to "360 DPI").forEach { (cmd, lbl) ->
                            Button(
                                onClick = {
                                    viewModel.executeCommand(cmd)
                                    Toast.makeText(context, "تم تنفيذ: $cmd", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TerminalDarkSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = lbl, color = TextPrimaryDark, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DebloaterTab(
    viewModel: AdbViewModel,
    onExecuteInTerminal: (String) -> Unit
) {
    val context = LocalContext.current
    val debloatApps = remember {
        listOf(
            DebloatApp("متصفح أوبو (HeyTap Browser)", "com.heytap.browser", "متصفح افتراضي يستهلك الرام ويعرض إعلانات"),
            DebloatApp("متجر أوبو (App Market)", "com.oppo.market", "متجر إضافي غير ضروري بجانب Google Play"),
            DebloatApp("تحليلات السلوك (HeyTap Habit)", "com.heytap.habit.analysis", "خدمة تتبع نشاط المستخدم في الخلفية"),
            DebloatApp("مساحة الألعاب (Game Space)", "com.coloros.gamespace", "تطبيق تحسين الألعاب الخاص بـ ColorOS"),
            DebloatApp("خدمات التجوال (Oplus Roaming)", "com.oplus.cosa", "خدمات اتصال شريحة افتراضية للتجوال"),
            DebloatApp("خدمة البريد (Postman Service)", "com.oplus.postmanservice", "خدمة إشعارات ترويجية")
        )
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "إزالة تطبيقات ColorOS الزائدة (للمستخدم 0 - بدون روت وآمن):",
                color = TextSecondaryDark,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        items(debloatApps) { app ->
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalCardSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalCardBorder, RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.name,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark,
                            fontSize = 13.sp
                        )
                        Text(
                            text = app.packageName,
                            fontFamily = FontFamily.Monospace,
                            color = TerminalCyan,
                            fontSize = 10.sp
                        )
                        Text(
                            text = app.description,
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Uninstall
                        Button(
                            onClick = {
                                viewModel.quickUninstallPackage(app.packageName)
                                Toast.makeText(context, "تم إرسال أمر إزالة: ${app.name}", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF331B1B)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalRed.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = TerminalRed, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إزالة", color = TerminalRed, fontSize = 11.sp)
                        }

                        // Restore
                        Button(
                            onClick = {
                                viewModel.quickReinstallPackage(app.packageName)
                                Toast.makeText(context, "تم إرسال أمر استعادة: ${app.name}", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalDarkSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Restore, contentDescription = "استعادة", tint = TerminalGreen, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionsTab(
    viewModel: AdbViewModel,
    onExecuteInTerminal: (String) -> Unit
) {
    val context = LocalContext.current
    var customPackageInput by remember { mutableStateOf("moe.shizuku.privileged.api") }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalCardSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalCardBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "منح صلاحيات النظام المحمية (ADB Permissions)",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "أدخل اسم حزمة التطبيق الذي ترغب في منحه الصلاحيات:",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customPackageInput,
                        onValueChange = { customPackageInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("اسم الحزمة (Package Name)", fontSize = 11.sp) },
                        textStyle = TextStyle(color = TerminalGreen, fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = TerminalDarkSurface,
                            unfocusedContainerColor = TerminalDarkSurface,
                            focusedBorderColor = TerminalGreen,
                            unfocusedBorderColor = TerminalCardBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val perms = listOf(
                        "WRITE_SECURE_SETTINGS" to "android.permission.WRITE_SECURE_SETTINGS",
                        "DUMP" to "android.permission.DUMP",
                        "PACKAGE_USAGE_STATS" to "android.permission.PACKAGE_USAGE_STATS",
                        "SYSTEM_ALERT_WINDOW" to "android.permission.SYSTEM_ALERT_WINDOW"
                    )

                    perms.forEach { (label, permStr) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = label, color = TextPrimaryDark, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            Button(
                                onClick = {
                                    viewModel.quickGrantPermission(customPackageInput.trim(), permStr)
                                    Toast.makeText(context, "تم إرسال أمر منح $label", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TerminalDarkSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalGreen),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("منح الصلاحية", color = TerminalGreen, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BatteryAndSystemTab(
    viewModel: AdbViewModel,
    onExecuteInTerminal: (String) -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalCardSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalCardBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.BatteryChargingFull, contentDescription = null, tint = TerminalYellow)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تشخيص البطارية والشاحن السريع VOOC",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "يعرض حالة حرارة البطارية ونسبة الشحن وتيار الشحن الفعلي في الترمينال:",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            viewModel.executeCommand("dumpsys battery")
                            onExecuteInTerminal("dumpsys battery")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TerminalYellow),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("قراءة بيانات dumpsys battery في الترمينال", color = TerminalBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalCardSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalCardBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "تجاوز قيود قتل الخلفية في ColorOS 13",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ColorOS يمتلك نظام إدارة طاقة صارم يغلق البرامج في الخلفية. يمكنك استثناء أي تطبيق من Doze:",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val cmd = "dumpsys deviceidle whitelist +com.aistudio.adbai.termx"
                            viewModel.executeCommand(cmd)
                            Toast.makeText(context, "تمت إضافة التطبيق للقائمة البيضاء", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TerminalDarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("استثناء ADB AI من توفير الطاقة", color = TerminalCyan, fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalCardSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalCardBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "أخذ لقطة شاشة صامتة (Screencap)",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "التقاط صورة لشاشة الجهاز وحفظها برمجياً:",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val timestamp = System.currentTimeMillis()
                            val cmd = "screencap -p /sdcard/Pictures/shot_$timestamp.png"
                            viewModel.executeCommand(cmd)
                            Toast.makeText(context, "تم التقاط الصورة وحفظها في Pictures", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("أخذ لقطة الشاشة الآن", color = TerminalBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
