package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.adb.AdbConnectionStatus
import com.example.data.adb.DeviceSpecs
import com.example.data.adb.PairingState
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalCardSurface
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalRed
import com.example.ui.theme.TerminalYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun AdbStatusCard(
    deviceSpecs: DeviceSpecs,
    pairingState: PairingState,
    onNavigateToPairing: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = pairingState.status == AdbConnectionStatus.CONNECTED
    val isConnecting = pairingState.status == AdbConnectionStatus.CONNECTING
    val isError = pairingState.status == AdbConnectionStatus.ERROR
    val statusColor = when {
        isConnected -> TerminalGreen
        isConnecting -> TerminalYellow
        isError -> TerminalRed
        else -> TextMuted
    }

    val statusText = when {
        isConnected -> "متصل بنجاح (ADB Shell UID 2000)"
        isConnecting -> "جارٍ الاتصال بـ ADB..."
        isError -> "فشل الاتصال (تحقق من المنفذ)"
        else -> "غير متصل (اضغط للاتصال)"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, TerminalCardBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onNavigateToPairing),
        colors = CardDefaults.cardColors(containerColor = TerminalCardSurface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = "Device",
                        tint = TerminalCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = deviceSpecs.modelName,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark,
                        fontSize = 14.sp
                    )
                }

                // Connection badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${deviceSpecs.colorOsVersion} • ${deviceSpecs.chipset.split("(").firstOrNull()?.trim() ?: "Snapdragon 720G"}",
                    color = TextSecondaryDark,
                    fontSize = 11.sp
                )

                Text(
                    text = "IP: ${pairingState.hostIp}",
                    color = TerminalCyan,
                    fontSize = 11.sp
                )
            }
        }
    }
}
