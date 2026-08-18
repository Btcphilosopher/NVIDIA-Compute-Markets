package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.AppLanguage
import com.example.presentation.ComputeMarketsViewModel
import com.example.presentation.DisplayCurrency
import com.example.ui.theme.*
import com.example.utils.Localization

@Composable
fun SettingsScreen(
    viewModel: ComputeMarketsViewModel,
    currentLanguage: AppLanguage,
    displayCurrency: DisplayCurrency,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "TERMINAL CONFIGURATION & PREFERENCES",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Language localization, base settlement currency and API network parameters",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                fontSize = 11.sp
            )
        }

        // Language Selection Card
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = TerminalSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "TERMINAL LANGUAGE (語言 / 语言)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    AppLanguage.entries.forEach { lang ->
                        val isSelected = lang == currentLanguage
                        Surface(
                            onClick = { viewModel.setLanguage(lang) },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) NvidiaGreenSurface else TerminalSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NvidiaGreenGlow else TerminalBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${lang.displayName} (${lang.nativeName})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) NvidiaGreenGlow else TextPrimary
                                    )
                                    Text(
                                        text = when(lang) {
                                            AppLanguage.ENGLISH -> "Institutional English interface with Black-76 models"
                                            AppLanguage.TAIWANESE_HOKKIEN -> "臺灣話在地化算力交易終端機介面"
                                            AppLanguage.CHINESE_MANDARIN -> "标准中文算力市场交易与量化分析平台"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.setLanguage(lang) },
                                    colors = RadioButtonDefaults.colors(selectedColor = NvidiaGreenGlow)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Settlement Currency Card
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = TerminalSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "BASE SETTLEMENT CURRENCY (計價幣別)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DisplayCurrency.entries.forEach { curr ->
                            val isSelected = curr == displayCurrency
                            Surface(
                                onClick = { viewModel.setCurrency(curr) },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) NvidiaGreenSurface else TerminalSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NvidiaGreenGlow else TerminalBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${curr.symbol} ${curr.code}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) NvidiaGreenGlow else TextPrimary
                                    )
                                    Text(
                                        text = "1 USD = ${curr.fxRateToUsd}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted,
                                        fontSize = 8.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Terminal Info Card
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = TerminalSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "SYSTEM INFORMATION & DESIGN IDENTITY",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Terminal Version: v3.4.8-RELEASE (NV-QUANT-STATION)", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    Text("• Design System: NVIDIA Institutional Dark Terminal", style = MaterialTheme.typography.bodySmall, color = NvidiaGreenGlow)
                    Text("• Financial Pricing Model: Black-76 Commodity Options Engine", style = MaterialTheme.typography.bodySmall, color = FinancialCyan)
                    Text("• Target Hardware Architecture: Hopper, Blackwell & Rubin", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
