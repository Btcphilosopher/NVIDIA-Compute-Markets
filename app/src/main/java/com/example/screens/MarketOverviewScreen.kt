package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.charts.ForwardCurveChart
import com.example.charts.PriceInteractiveChart
import com.example.components.ChipSelectorRow
import com.example.components.MetricCard
import com.example.models.AppLanguage
import com.example.models.GpuChip
import com.example.presentation.ComputeMarketsViewModel
import com.example.presentation.DisplayCurrency
import com.example.presentation.TerminalScreen
import com.example.ui.theme.*
import com.example.utils.Localization
import java.util.Locale

@Composable
fun MarketOverviewScreen(
    viewModel: ComputeMarketsViewModel,
    currentLanguage: AppLanguage,
    displayCurrency: DisplayCurrency,
    onNavigate: (TerminalScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val instruments = viewModel.marketInstruments
    val selectedChip = viewModel.selectedChip
    val priceHistory = viewModel.priceHistory
    val forwardCurve = viewModel.forwardCurve
    val globalCapacity = viewModel.globalCapacity
    val selectedHours = viewModel.selectedTimeframeHours

    val currInst = instruments.value.firstOrNull { it.chip == selectedChip.value }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section 1: Hero Metric Summary Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val spot = currInst?.spotPrice ?: 2.84
                val isUp = (currInst?.priceChange24h ?: 0.0) >= 0
                MetricCard(
                    label = "${selectedChip.value.name} SPOT",
                    value = "${displayCurrency.symbol}${String.format(Locale.US, "%.2f", spot * displayCurrency.fxRateToUsd)}/hr",
                    deltaText = "${String.format(Locale.US, "%.1f", currInst?.priceChangePercent24h ?: 0.0)}%",
                    isDeltaPositive = isUp,
                    accentColor = NvidiaGreenGlow,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "12M FORWARD",
                    value = "${displayCurrency.symbol}${String.format(Locale.US, "%.2f", (currInst?.forward12M ?: (spot * 0.81)) * displayCurrency.fxRateToUsd)}/hr",
                    subtext = "Backwardation -18.7%",
                    accentColor = FinancialCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    label = "GLOBAL UTILIZATION",
                    value = "${globalCapacity.value?.globalUtilization ?: 87.3}%",
                    subtext = "Active: ${(globalCapacity.value?.activeRunningGpus ?: 4210000) / 1000}k GPUs",
                    accentColor = FinancialAmber,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "IMPLIED VOLATILITY",
                    value = "${currInst?.impliedVol ?: 34.8}%",
                    subtext = "ATM 90D Tenor",
                    accentColor = FinancialPurple,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Section 2: Chip Selector Row
        item {
            Column {
                Text(
                    text = "SELECT COMPUTE INSTRUMENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                ChipSelectorRow(
                    selectedChip = selectedChip.value,
                    onSelectChip = { viewModel.selectChip(it) }
                )
            }
        }

        // Section 3: Interactive Spot Price Chart Card
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
                        Column {
                            Text(
                                text = "${selectedChip.value.name} COMPUTE SPOT RATE",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Institutional order flow & stochastic pricing feed",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }

                        // Timeframe Buttons (1H, 24H, 7D, 30D)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(1 to "1H", 24 to "24H", 168 to "7D", 720 to "30D").forEach { (h, label) ->
                                val isSelected = selectedHours.value == h
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
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) NvidiaGreenGlow else TextMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    PriceInteractiveChart(
                        priceTicks = priceHistory.value,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        currencySymbol = displayCurrency.symbol
                    )
                }
            }
        }

        // Section 4: Forward Term Structure Preview
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
                            text = "FORWARD TERM STRUCTURE (SPOT → 36M)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        TextButton(
                            onClick = { onNavigate(TerminalScreen.FORWARD_CURVE) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Full Term Table →", color = NvidiaGreenGlow, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    ForwardCurveChart(
                        curveData = forwardCurve.value,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        currencySymbol = displayCurrency.symbol
                    )
                }
            }
        }

        // Section 5: Fast Institutional Module Jumps
        item {
            Text(
                text = "INSTITUTIONAL WORKSTATION MODULES",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WorkstationModuleButton(
                    title = Localization.t("nav_strike_price", currentLanguage),
                    subtitle = "Black-76 Options",
                    icon = Icons.Default.Calculate,
                    onClick = { onNavigate(TerminalScreen.STRIKE_PRICE) },
                    modifier = Modifier.weight(1f)
                )
                WorkstationModuleButton(
                    title = Localization.t("nav_vol_surface", currentLanguage),
                    subtitle = "3D Vol Surface",
                    icon = Icons.Default.Grain,
                    onClick = { onNavigate(TerminalScreen.VOL_SURFACE) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WorkstationModuleButton(
                    title = Localization.t("nav_datacenters", currentLanguage),
                    subtitle = "Global Map & Latency",
                    icon = Icons.Default.Public,
                    onClick = { onNavigate(TerminalScreen.DATACENTERS) },
                    modifier = Modifier.weight(1f)
                )
                WorkstationModuleButton(
                    title = Localization.t("nav_simulation", currentLanguage),
                    subtitle = "Scenario Stress Test",
                    icon = Icons.Default.Science,
                    onClick = { onNavigate(TerminalScreen.SIMULATION) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun WorkstationModuleButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = TerminalSurfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
        modifier = modifier.height(64.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(NvidiaGreenSurface, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = NvidiaGreenGlow, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextMuted, fontSize = 10.sp)
            }
        }
    }
}
