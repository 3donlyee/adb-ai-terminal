package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AiCopilotScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SystemToolsScreen
import com.example.ui.screens.TerminalScreen
import com.example.ui.screens.WirelessPairingScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TerminalBlack
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalDarkSurface
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.AdbViewModel

sealed class AppNavTab(val index: Int, val titleRes: Int, val icon: ImageVector) {
    object Terminal : AppNavTab(0, R.string.terminal_tab, Icons.Default.Terminal)
    object AiCopilot : AppNavTab(1, R.string.ai_copilot_tab, Icons.Default.AutoAwesome)
    object Tools : AppNavTab(2, R.string.tools_tab, Icons.Default.Build)
    object WirelessPair : AppNavTab(3, R.string.wireless_pair_tab, Icons.Default.Wifi)
    object History : AppNavTab(4, R.string.history_tab, Icons.Default.History)
}

class MainActivity : ComponentActivity() {

    private val viewModel: AdbViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: AdbViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        AppNavTab.Terminal,
        AppNavTab.AiCopilot,
        AppNavTab.Tools,
        AppNavTab.WirelessPair,
        AppNavTab.History
    )

    // Handle system back button: return to Terminal if on other tabs
    if (selectedTab != 0) {
        BackHandler {
            selectedTab = 0
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBlack)
            .statusBarsPadding(),
        bottomBar = {
            NavigationBar(
                containerColor = TerminalDarkSurface,
                tonalElevation = 4.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab.index
                    val activeColor = if (tab is AppNavTab.AiCopilot) Color(0xFFA78BFA) else TerminalCyan

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab.index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = stringResource(tab.titleRes)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(tab.titleRes),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = activeColor,
                            selectedTextColor = activeColor,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = Color(0xFF1B2536)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> TerminalScreen(
                    viewModel = viewModel,
                    onNavigateToPairing = { selectedTab = 3 },
                    onNavigateToAi = { prefill ->
                        if (prefill.isNotBlank()) {
                            viewModel.setPrompt(prefill)
                        }
                        selectedTab = 1
                    }
                )
                1 -> AiCopilotScreen(
                    viewModel = viewModel,
                    onExecuteInTerminal = { command ->
                        viewModel.executeCommand(command)
                        selectedTab = 0
                    }
                )
                2 -> SystemToolsScreen(
                    viewModel = viewModel,
                    onExecuteInTerminal = { command ->
                        viewModel.executeCommand(command)
                        selectedTab = 0
                    }
                )
                3 -> WirelessPairingScreen(
                    viewModel = viewModel,
                    onNavigateToTerminal = { selectedTab = 0 }
                )
                4 -> HistoryScreen(
                    viewModel = viewModel,
                    onExecuteInTerminal = { command ->
                        viewModel.executeCommand(command)
                        selectedTab = 0
                    }
                )
            }
        }
    }
}
