package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.charts.ForwardCurveChart
import com.example.charts.PriceInteractiveChart
import com.example.components.ChipSelectorRow
import com.example.components.MetricCard
import com.example.models.AppLanguage
import com.example.presentation.ComputeMarketsViewModel
import com.example.presentation.DisplayCurrency
import com.example.presentation.TerminalScreen
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun GpuDetailScreen(
    viewModel: ComputeMarketsViewModel,
    currentLanguage: AppLanguage,
    displayCurrency: DisplayCurrency,
    onNavigate: (TerminalScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedChip = viewModel.selectedChip.value
    val instruments = viewModel.marketInstruments.value
    val priceHistory = viewModel.priceHistory.value
    val forwardCurve = viewModel.forwardCurve.value
    val costBreakdown = viewModel.costBreakdown.value

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
            ChipSelectorRow(
                selectedChip = selectedChip,
                onSelectChip = { viewModel.selectChip(it) }
            )
        }

        // Header Title & Specs
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(NvidiaGreenSurface)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = selectedChip.architecture,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NvidiaGreenGlow,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "NVIDIA ${selectedChip.name}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "${selectedChip.vramGb}GB ${selectedChip.memoryType} • ${selectedChip.tdpWatts}W TDP • ${selectedChip.fp8Tflops.toInt()} FP8 TFLOPS",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${displayCurrency.symbol}${String.format(Locale.US, "%.2f", spot * displayCurrency.fxRateToUsd)}/hr",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = NvidiaGreenGlow,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${if (isUp) "▲" else "▼"}${String.format(Locale.US, "%.1f", inst?.priceChangePercent24h ?: 0.0)}% (24h)",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isUp) FinancialGreen else FinancialRed,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Summary Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    label = "AVAILABLE IN POOL",
                    value = "${(inst?.availableCapacityGpus ?: 159000) / 1000}k GPUs",
                    subtext = "Total: ${(inst?.totalCapacityGpus ?: 1850000) / 1000}k",
                    accentColor = FinancialCyan,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "DISPATCH QUEUE",
                    value = "${inst?.queueWaitTimeHours ?: 1.4} Hours",
                    subtext = "Util: ${String.format(Locale.US, "%.1f", inst?.utilizationRate ?: 91.4)}%",
                    accentColor = FinancialAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Spot Price Chart Card
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = TerminalSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "${selectedChip.name} INTRADAY PRICE FEED",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    PriceInteractiveChart(
                        priceTicks = priceHistory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp),
                        currencySymbol = displayCurrency.symbol
                    )
                }
            }
        }

        // Forward Curve Card
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
                            text = "${selectedChip.name} FORWARD MATURITIES",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Button(
                            onClick = { onNavigate(TerminalScreen.STRIKE_PRICE) },
                            colors = ButtonDefaults.buttonColors(containerColor = NvidiaGreen, contentColor = Color.Black),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Trade Options", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    ForwardCurveChart(
                        curveData = forwardCurve,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        currencySymbol = displayCurrency.symbol
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
