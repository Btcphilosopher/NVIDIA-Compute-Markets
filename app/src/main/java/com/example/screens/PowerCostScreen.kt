package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.charts.CostBreakdownChart
import com.example.components.ChipSelectorRow
import com.example.components.MetricCard
import com.example.models.AppLanguage
import com.example.presentation.ComputeMarketsViewModel
import com.example.presentation.DisplayCurrency
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun PowerCostScreen(
    viewModel: ComputeMarketsViewModel,
    currentLanguage: AppLanguage,
    displayCurrency: DisplayCurrency,
    modifier: Modifier = Modifier
) {
    val selectedChip = viewModel.selectedChip.value
    val costBreakdown = viewModel.costBreakdown.value

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "LEVELIZED COST OF COMPUTE (LCOC) & POWER ECONOMICS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Comprehensive bottom-up cost modeling: Capex amortization, power, liquid cooling, and datacenter ops",
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

        // Summary Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    label = "REAL HOURLY COST",
                    value = "${displayCurrency.symbol}${String.format(Locale.US, "%.2f", (costBreakdown?.totalRealHourlyCost ?: 1.92) * displayCurrency.fxRateToUsd)}/GPU-hr",
                    subtext = "Gross Margin: ${String.format(Locale.US, "%.1f", costBreakdown?.grossMarginPercentage ?: 32.4)}%",
                    accentColor = NvidiaGreenGlow,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "ELECTRICITY RATE",
                    value = "$0.072/kWh",
                    subtext = "Rack Density: ${costBreakdown?.rackPowerDensityKw?.toInt() ?: 40} kW",
                    accentColor = FinancialCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Stacked LCOC Bar Chart Card
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = TerminalSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "${selectedChip.name} HOURLY COST DECOMPOSITION",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    CostBreakdownChart(
                        costData = costBreakdown,
                        currentLanguage = currentLanguage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        currencySymbol = displayCurrency.symbol
                    )
                }
            }
        }

        // Capital Investment & Depreciation Profile
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = TerminalSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "CAPITAL EXPENDITURE & DEPRECIATION SCHEDULE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Server Acquisition Cost (8-GPU Node):", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("${displayCurrency.symbol}${String.format(Locale.US, "%,.0f", (costBreakdown?.serverAcquisitionCostUsd ?: 300000.0) * displayCurrency.fxRateToUsd)}", style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Economic Useful Life:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("${costBreakdown?.usefulLifeYears ?: 3.0} Years (26,280 hours)", style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Terminal Residual Salvage Value:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("${displayCurrency.symbol}${String.format(Locale.US, "%,.0f", (costBreakdown?.residualSalvageValueUsd ?: 45000.0) * displayCurrency.fxRateToUsd)} (15%)", style = MaterialTheme.typography.bodySmall, color = FinancialGreen, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
