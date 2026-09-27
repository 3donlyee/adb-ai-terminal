package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AdbStatusCard
import com.example.ui.components.TermuxVirtualKeys
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
import kotlinx.coroutines.launch

@Composable
fun TerminalScreen(
    viewModel: AdbViewModel,
    onNavigateToPairing: () -> Unit,
    onNavigateToAi: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val terminalLogs by viewModel.terminalLogs.collectAsState()
    val isExecuting by viewModel.isExecuting.collectAsState()
    val deviceSpecs by viewModel.deviceSpecs.collectAsState()
    val pairingState by viewModel.pairingState.collectAsState()
    val historyList by viewModel.historyList.collectAsState()

    var commandInput by remember { mutableStateOf("") }
    var historyIndex by remember { mutableIntStateOf(-1) }
    val listState = rememberLazyListState()

    // Auto-scroll to latest output line
    LaunchedEffect(terminalLogs.size) {
        if (terminalLogs.isNotEmpty()) {
            listState.animateScrollToItem(terminalLogs.size - 1)
        }
    }

    val quickCommands = listOf(
        "dumpsys battery",
        "settings get system peak_refresh_rate",
        "pm list packages -3",
        "top -m 5",
        "wm density",
        "getprop ro.product.model",
        "help"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBlack)
            .padding(horizontal = 10.dp)
    ) {
        // Status & Device Banner
        Spacer(modifier = Modifier.height(6.dp))
        AdbStatusCard(
            deviceSpecs = deviceSpecs,
            pairingState = pairingState,
            onNavigateToPairing = onNavigateToPairing
        )

        // Terminal Top Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = TerminalGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "termux@reno5-cph2159",
                    color = TerminalGreen,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Clear button
                IconButton(
                    onClick = { viewModel.clearTerminal() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "مسح",
                        tint = TextSecondaryDark,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Copy all output
                IconButton(
                    onClick = {
                        val clip = ClipData.newPlainText("Terminal Log", terminalLogs.joinToString("\n"))
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "تم نسخ مخرجات الترمينال إلى الحافظة", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "نسخ",
                        tint = TextSecondaryDark,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Ask AI Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF24163B))
                        .border(1.dp, Color(0xFFA78BFA), RoundedCornerShape(8.dp))
                        .clickable { onNavigateToAi(commandInput) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AI Copilot",
                            color = Color(0xFFA78BFA),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Terminal Console Window
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF06090F))
                .border(1.dp, TerminalCardBorder, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(terminalLogs) { _, logLine ->
                    val color = when {
                        logLine.startsWith("reno5@coloros13:~$") -> TerminalGreen
                        logLine.startsWith("[ERROR]") || logLine.contains("Error", ignoreCase = true) || logLine.contains("Failure", ignoreCase = true) -> TerminalRed
                        logLine.startsWith("[SUCCESS]") || logLine.startsWith("[CONNECTED]") -> TerminalGreen
                        logLine.startsWith("[ADB]") || logLine.startsWith("[INFO]") || logLine.startsWith("[SHIZUKU]") -> TerminalCyan
                        logLine.startsWith("==") -> TerminalYellow
                        else -> TextPrimaryDark
                    }

                    Text(
                        text = logLine,
                        color = color,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Quick Command Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickCommands.forEach { cmd ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(TerminalDarkSurface)
                        .border(0.5.dp, TerminalCardBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            commandInput = cmd
                            viewModel.executeCommand(cmd)
                        }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = cmd,
                        color = TerminalCyan,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Termux Virtual Keys Row
        TermuxVirtualKeys(
            onKeyPress = { key ->
                when (key) {
                    "ESC" -> commandInput = ""
                    "TAB" -> {
                        // Simple auto-completion for common prefix
                        when {
                            commandInput.startsWith("p") -> commandInput = "pm list packages "
                            commandInput.startsWith("s") -> commandInput = "settings put "
                            commandInput.startsWith("d") -> commandInput = "dumpsys battery"
                            commandInput.startsWith("w") -> commandInput = "wm density "
                            commandInput.startsWith("g") -> commandInput = "getprop "
                        }
                    }
                    "CLEAR" -> {
                        commandInput = ""
                        viewModel.clearTerminal()
                    }
                    "↑" -> {
                        if (historyList.isNotEmpty()) {
                            if (historyIndex < historyList.size - 1) {
                                historyIndex++
                                commandInput = historyList[historyIndex].command
                            }
                        }
                    }
                    "↓" -> {
                        if (historyIndex > 0) {
                            historyIndex--
                            commandInput = historyList[historyIndex].command
                        } else if (historyIndex == 0) {
                            historyIndex = -1
                            commandInput = ""
                        }
                    }
                    "CTRL", "ALT" -> {}
                    else -> commandInput += key
                }
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Terminal Command Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = commandInput,
                onValueChange = { commandInput = it },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp)),
                placeholder = {
                    Text(
                        text = "اكتب أمر ADB أو sh هنا...",
                        color = TextMuted,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                leadingIcon = {
                    Text(
                        text = "$",
                        color = TerminalGreen,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                },
                textStyle = TextStyle(
                    color = TextPrimaryDark,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (commandInput.isNotBlank()) {
                            val cmd = commandInput
                            commandInput = ""
                            historyIndex = -1
                            viewModel.executeCommand(cmd)
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = TerminalDarkSurface,
                    unfocusedContainerColor = TerminalDarkSurface,
                    focusedBorderColor = TerminalGreen,
                    unfocusedBorderColor = TerminalCardBorder,
                    cursorColor = TerminalGreen
                )
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Execute Button
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isExecuting) Color(0xFF1E2838) else TerminalGreen)
                    .border(1.dp, if (isExecuting) TerminalCardBorder else TerminalGreen, RoundedCornerShape(8.dp))
                    .clickable(enabled = !isExecuting && commandInput.isNotBlank()) {
                        val cmd = commandInput
                        commandInput = ""
                        historyIndex = -1
                        viewModel.executeCommand(cmd)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isExecuting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = TerminalCyan,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "تنفيذ",
                        tint = TerminalBlack,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}
