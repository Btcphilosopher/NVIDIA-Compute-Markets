package com.example.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.AppLanguage
import com.example.models.GpuChip
import com.example.models.GpuMarketInstrument
import com.example.presentation.TerminalScreen
import com.example.ui.theme.*
import com.example.utils.Localization
import java.util.Locale

@Composable
fun MarketTickerTopBar(
    instruments: List<GpuMarketInstrument>,
    selectedChip: GpuChip,
    onSelectChip: (GpuChip) -> Unit,
    currentLanguage: AppLanguage,
    onSetLanguage: (AppLanguage) -> Unit,
    isWebSocketLive: Boolean,
    onOpenCommandPalette: () -> Unit,
    onOpenAiTerminal: () -> Unit,
    onNavigateTo: (TerminalScreen) -> Unit,
    modifier: Modifier = Modifier,
    currencySymbol: String = "$"
) {
    var showLangMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TerminalBackground)
            .statusBarsPadding()
            .padding(top = 2.dp)
    ) {
        // Upper App Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onNavigateTo(TerminalScreen.OVERVIEW) }
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(NvidiaGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NV",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = Localization.t("app_title", currentLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = Localization.t("terminal_subtitle", currentLanguage),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            // Quick Actions: Command Palette (Ctrl+K), AI Quant, Status Dot, Lang Switcher
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Command Palette Button (Ctrl+K)
                Surface(
                    onClick = onOpenCommandPalette,
                    shape = RoundedCornerShape(6.dp),
                    color = TerminalSurfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
                    modifier = Modifier.height(30.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = NvidiaGreenGlow,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Ctrl+K",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                // AI Terminal Assistant Button
                Surface(
                    onClick = onOpenAiTerminal,
                    shape = RoundedCornerShape(6.dp),
                    color = NvidiaGreenSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NvidiaGreenDim),
                    modifier = Modifier.height(30.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Quant",
                            tint = NvidiaGreenGlow,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AI QUANT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NvidiaGreenGlow
                        )
                    }
                }

                // Language Switcher Dropdown
                Box {
                    Surface(
                        onClick = { showLangMenu = true },
                        shape = RoundedCornerShape(6.dp),
                        color = TerminalSurfaceCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Language",
                                tint = TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentLanguage.code.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showLangMenu,
                        onDismissRequest = { showLangMenu = false },
                        modifier = Modifier.background(TerminalSurface)
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${lang.displayName} (${lang.nativeName})",
                                        color = if (lang == currentLanguage) NvidiaGreenGlow else TextPrimary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = {
                                    onSetLanguage(lang)
                                    showLangMenu = false
                                }
                            )
                        }
                    }
                }

                // WebSocket Status Dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isWebSocketLive) NvidiaGreenGlow else FinancialRed)
                )
            }
        }

        // Live Market Ticker Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalSurfaceVariant)
                .padding(vertical = 4.dp, horizontal = 6.dp)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Live Feed Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(TerminalBackground)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isWebSocketLive) NvidiaGreenGlow else FinancialRed)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = Localization.t("live_feed", currentLanguage),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isWebSocketLive) NvidiaGreenGlow else FinancialRed,
                    fontSize = 9.sp
                )
            }

            // Ticker Items
            instruments.forEach { item ->
                val isSelected = item.chip == selectedChip
                val isUp = item.priceChange24h >= 0

                Surface(
                    onClick = {
                        onSelectChip(item.chip)
                        onNavigateTo(TerminalScreen.GPU_DETAIL)
                    },
                    shape = RoundedCornerShape(4.dp),
                    color = if (isSelected) NvidiaGreenSurface else TerminalSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) NvidiaGreenDim else TerminalBorder
                    ),
                    modifier = Modifier.height(26.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = item.chip.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) NvidiaGreenGlow else TextPrimary
                        )

                        AnimatedContent(
                            targetState = item.spotPrice,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "ticker_price"
                        ) { price ->
                            Text(
                                text = "$currencySymbol${String.format(Locale.US, "%.2f", price)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = "${if (isUp) "▲" else "▼"}${String.format(Locale.US, "%.1f", item.priceChangePercent24h)}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = if (isUp) FinancialGreen else FinancialRed
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = TerminalBorder, thickness = 1.dp)
    }
}
