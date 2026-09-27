package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalDarkSurface
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TextPrimaryDark

@Composable
fun TermuxVirtualKeys(
    onKeyPress: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val keys = listOf(
        "ESC", "TAB", "CTRL", "ALT",
        "|", "/", "-", "~", "$",
        "&&", ";", ">", "↑", "↓", "CLEAR"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(TerminalDarkSurface)
            .border(width = 0.5.dp, color = TerminalCardBorder)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 6.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        keys.forEach { key ->
            KeyButton(label = key, onClick = { onKeyPress(key) })
        }
    }
}

@Composable
private fun KeyButton(
    label: String,
    onClick: () -> Unit
) {
    val isAction = label in listOf("ESC", "TAB", "CTRL", "ALT", "CLEAR", "↑", "↓")
    val bgColor = if (isAction) Color(0xFF1E2838) else Color(0xFF141C29)
    val textColor = if (label == "CLEAR") Color(0xFFF87171) else if (isAction) TerminalGreen else TextPrimaryDark

    Box(
        modifier = Modifier
            .sizeIn(minWidth = 38.dp, minHeight = 32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(0.5.dp, TerminalCardBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}
