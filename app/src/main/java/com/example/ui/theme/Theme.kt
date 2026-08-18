package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TerminalColorScheme = darkColorScheme(
    primary = NvidiaGreen,
    onPrimary = Color.Black,
    primaryContainer = NvidiaGreenSurface,
    onPrimaryContainer = NvidiaGreenGlow,
    secondary = FinancialCyan,
    onSecondary = Color.Black,
    secondaryContainer = FinancialCyanBg,
    onSecondaryContainer = FinancialCyan,
    tertiary = FinancialAmber,
    onTertiary = Color.Black,
    background = TerminalBackground,
    onBackground = TextPrimary,
    surface = TerminalSurface,
    onSurface = TextPrimary,
    surfaceVariant = TerminalSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = TerminalBorder,
    outlineVariant = TerminalBorderHighlight,
    error = FinancialRed,
    onError = Color.White
)

@Composable
fun ComputeMarketsTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TerminalColorScheme,
        typography = Typography,
        content = content
    )
}

