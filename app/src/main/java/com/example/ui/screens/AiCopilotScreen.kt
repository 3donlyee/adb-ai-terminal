package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun AiCopilotScreen(
    viewModel: AdbViewModel,
    onExecuteInTerminal: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val aiState by viewModel.aiState.collectAsState()
    val deviceSpecs by viewModel.deviceSpecs.collectAsState()
    var promptInput by remember { mutableStateOf(aiState.prompt) }

    val presetPrompts = listOf(
        "⚡ تثبيت تردد 90Hz في جميع التطبيقات" to "أريد إجبار شاشة Oppo Reno 5 على العمل بتردد 90Hz دائم في كافة التطبيقات والألعاب",
        "🚀 تسريع حركات ColorOS إلى 0.5x" to "أريد تسريع الرسوم المتحركة وحركات واجهة ColorOS 13 إلى النصف لتسريع الهاتف",
        "🗑️ حذف تطبيقات HeyTap الإعلانية" to "احذف تطبيقات Oppo و HeyTap غير الضرورية للمستخدم الحالي لتسريع الرام",
        "🛡️ منح صلاحية Write Secure Settings" to "أريد منح صلاحية WRITE_SECURE_SETTINGS و DUMP لتطبيق Shizuku",
        "🔋 فحص صحة البطارية والشاحن" to "اعرض لي معلومات تفصيلية عن حالة البطارية ودرجة الحرارة وسرعة الشحن",
        "📱 تغيير كثافة الشاشة DPI إلى 400" to "أريد تغيير كثافة الشاشة DPI إلى 400 لعرض مساحة أكبر على الشاشة",
        "💤 منع ColorOS من قتل التطبيقات بالخلفية" to "كيف أضيف تطبيقي للقائمة البيضاء لتجاوز إيقاف التطبيقات في الخلفية"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBlack)
            .padding(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF24163B))
                    .border(1.dp, TerminalPurple, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = TerminalPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "مساعد ADB AI الذكي",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Text(
                    text = "مخصص لـ Oppo Reno 5 4G • ColorOS 13 • Snapdragon 720G",
                    fontSize = 11.sp,
                    color = TerminalCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Preset Prompt Chips
        Text(
            text = "أوامر سريعة مقترحة لهاتفك:",
            fontSize = 12.sp,
            color = TextSecondaryDark,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(presetPrompts) { (label, fullQuery) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(TerminalCardSurface)
                        .border(1.dp, TerminalCardBorder, RoundedCornerShape(16.dp))
                        .clickable {
                            promptInput = fullQuery
                            viewModel.askAi(fullQuery)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        color = TerminalCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Input Field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp)),
                placeholder = {
                    Text(
                        text = "اكتب طلبك بالعربية (مثال: احذف متجر أوبو أو سرع الشاشة)...",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                },
                textStyle = TextStyle(color = TextPrimaryDark, fontSize = 13.sp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    if (promptInput.isNotBlank()) {
                        viewModel.askAi(promptInput)
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = TerminalDarkSurface,
                    unfocusedContainerColor = TerminalDarkSurface,
                    focusedBorderColor = TerminalPurple,
                    unfocusedBorderColor = TerminalCardBorder,
                    cursorColor = TerminalPurple
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (promptInput.isNotBlank()) {
                        viewModel.askAi(promptInput)
                    }
                },
                enabled = !aiState.isLoading && promptInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TerminalPurple,
                    disabledContainerColor = Color(0xFF2A223B)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(52.dp)
            ) {
                if (aiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "إرسال",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // AI Response Area
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (aiState.response != null) {
                val resp = aiState.response!!
                item {
                    val safetyColor = when (resp.safetyLevel) {
                        "SAFE" -> TerminalGreen
                        "CAUTION" -> TerminalYellow
                        else -> TerminalRed
                    }
                    val safetyText = when (resp.safetyLevel) {
                        "SAFE" -> "🟢 آمن ومضمون (Safe)"
                        "CAUTION" -> "🟡 يحتاج انتباه (Caution)"
                        else -> "🔴 متقدم / يتطلب حذر (Risk)"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, TerminalCardBorder, RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = TerminalCardSurface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Top Safety Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(safetyColor.copy(alpha = 0.15f))
                                        .border(1.dp, safetyColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = safetyText,
                                        color = safetyColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = "تم التوليد بنجاح",
                                    color = TextSecondaryDark,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Generated Command Box
                            Text(
                                text = "أمر ADB المولد:",
                                color = TextSecondaryDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF070B12))
                                    .border(1.dp, TerminalCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = resp.command,
                                        color = TerminalGreen,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        modifier = Modifier.weight(1f)
                                    )

                                    IconButton(
                                        onClick = {
                                            val clip = ClipData.newPlainText("ADB Command", resp.command)
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "تم نسخ الأمر إلى الحافظة", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "نسخ",
                                            tint = TerminalCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onExecuteInTerminal(resp.command) },
                                    colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = TerminalBlack,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "تنفيذ في الترمينال",
                                        color = TerminalBlack,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                Button(
                                    onClick = {
                                        viewModel.saveScript(
                                            title = promptInput.take(30),
                                            desc = resp.explanation.take(60),
                                            commands = resp.command,
                                            category = "AI Generated"
                                        )
                                        Toast.makeText(context, "تم حفظ الأمر في مكتبة السكريبتات", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TerminalDarkSurface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BookmarkBorder,
                                        contentDescription = null,
                                        tint = TerminalCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "حفظ",
                                        color = TextPrimaryDark,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Arabic Explanation
                            Text(
                                text = "شرح الأمر وتأثيره على ColorOS 13:",
                                color = TextSecondaryDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = resp.explanation,
                                color = TextPrimaryDark,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )

                            // Revert Command (if present)
                            if (resp.revertCommand.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "أمر التراجع (إلغاء التعديل والعودة للأصل):",
                                    color = TerminalYellow,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF1B1B14))
                                        .border(1.dp, TerminalYellow.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                        .padding(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = resp.revertCommand,
                                            color = TerminalYellow,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Text(
                                            text = "تنفيذ التراجع",
                                            color = TerminalYellow,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .clickable { onExecuteInTerminal(resp.revertCommand) }
                                                .padding(4.dp)
                                        )
                                    }
                                }
                            }

                            if (resp.tips.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "💡 نصيحة: ${resp.tips}",
                                    color = TextSecondaryDark,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            } else if (aiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = TerminalPurple)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "جارٍ تحليل الطلب وبرمجة أمر ADB لهاتف Oppo Reno 5...",
                                color = TextSecondaryDark,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "اطلب أي تعديل تريده وسيقوم الذكاء الاصطناعي بتوليد أوامر ADB وشرحها بدقة",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
