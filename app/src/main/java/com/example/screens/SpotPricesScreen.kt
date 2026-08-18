package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.charts.PriceInteractiveChart
import com.example.components.ChipSelectorRow
import com.example.components.MetricCard
import com.example.models.AppLanguage
import com.example.presentation.ComputeMarketsViewModel
import com.example.presentation.DisplayCurrency
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SpotPricesScreen(
    viewModel: ComputeMarketsViewModel,
    currentLanguage: AppLanguage,
    displayCurrency: DisplayCurrency,
    modifier: Modifier = Modifier
) {
    val selectedChip = viewModel.selectedChip.value
    val instruments = viewModel.marketInstruments.value
    val priceHistory = viewModel.priceHistory.value
    val selectedHours = viewModel.selectedTimeframeHours.value

    val inst = instruments.firstOrNull { it.chip == selectedChip }
    val spot = inst?.spotPrice ?: 2.84
    val isUp = (inst?.priceChange24h ?: 0.0) >= 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "INSTITUTIONAL SPOT ORDER BOOK & LIQUIDITY FEED",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Sub-second execution pricing, bid/ask spreads and depth levels",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                fontSize = 11.sp
            )
        }

        item {
            ChipSelectorRow(
                selectedChip = selectedChip,
                onSelectChip = { viewModel.selectChip(it) }
            )
        }

        // Metrics Summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    label = "BEST BID / ASK",
                    value = "${displayCurrency.symbol}${String.format(Locale.US, "%.2f", (spot - 0.01) * displayCurrency.fxRateToUsd)} / ${displayCurrency.symbol}${String.format(Locale.US, "%.2f", (spot + 0.01) * displayCurrency.fxRateToUsd)}",
                    deltaText = "${String.format(Locale.US, "%.1f", inst?.priceChangePercent24h ?: 0.0)}%",
                    isDeltaPositive = isUp,
                    accentColor = NvidiaGreenGlow,
                    modifier = Modifier.weight(1.2f)
                )
                MetricCard(
                    label = "24H VOLUME",
                    value = "1.84M GPU-hr",
                    subtext = "Spread: 0.7%",
                    accentColor = FinancialCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Main Chart Card
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = TerminalSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedChip.name} HIGH-RESOLUTION TICK CHART",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(1 to "1H", 24 to "24H", 168 to "7D").forEach { (h, label) ->
                                val isSelected = selectedHours == h
                                Surface(
                                    onClick = { viewModel.setTimeframe(h) },
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isSelected) NvidiaGreenSurface else TerminalSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NvidiaGreenDim else TerminalBorder),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = if (isSelected) NvidiaGreenGlow else TextMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    PriceInteractiveChart(
                        priceTicks = priceHistory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        currencySymbol = displayCurrency.symbol
                    )
                }
            }
        }

        // Order Book Depth Simulation Table
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = TerminalSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "LIVE ORDER BOOK DEPTH (TOP 5 BIDS / ASKS)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Bids & Asks Headers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("BID SIZE (GPU-hr)", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                        Text("BID (${displayCurrency.symbol})", style = MaterialTheme.typography.labelSmall, color = FinancialGreen, fontSize = 9.sp)
                        Text("ASK (${displayCurrency.symbol})", style = MaterialTheme.typography.labelSmall, color = FinancialRed, fontSize = 9.sp)
                        Text("ASK SIZE (GPU-hr)", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = TerminalBorder)
                    Spacer(modifier = Modifier.height(4.dp))

                    // Top 5 Depth rows
                    listOf(
                        Triple(45000, spot - 0.01, spot + 0.01) to 38000,
                        Triple(120000, spot - 0.02, spot + 0.02) to 95000,
                        Triple(280000, spot - 0.03, spot + 0.03) to 210000,
                        Triple(510000, spot - 0.05, spot + 0.05) to 480000,
                        Triple(1200000, spot - 0.08, spot + 0.08) to 1100000
                    ).forEach { (bids, askSize) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${bids.first / 1000}k", style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontFamily = FontFamily.Monospace)
                            Text(String.format(Locale.US, "%.2f", bids.second * displayCurrency.fxRateToUsd), style = MaterialTheme.typography.bodySmall, color = FinancialGreen, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text(String.format(Locale.US, "%.2f", bids.third * displayCurrency.fxRateToUsd), style = MaterialTheme.typography.bodySmall, color = FinancialRed, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("${askSize / 1000}k", style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
