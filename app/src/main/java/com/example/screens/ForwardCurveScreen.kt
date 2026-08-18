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
import com.example.charts.ForwardCurveChart
import com.example.components.ChipSelectorRow
import com.example.components.MetricCard
import com.example.models.AppLanguage
import com.example.presentation.ComputeMarketsViewModel
import com.example.presentation.DisplayCurrency
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun ForwardCurveScreen(
    viewModel: ComputeMarketsViewModel,
    currentLanguage: AppLanguage,
    displayCurrency: DisplayCurrency,
    modifier: Modifier = Modifier
) {
    val selectedChip = viewModel.selectedChip.value
    val forwardCurve = viewModel.forwardCurve.value

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "GPU COMPUTE FORWARD TERM STRUCTURE",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Delivery contracts, backwardation slopes, annualized basis and implied repo curves",
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
                    label = "CURVE STRUCTURE",
                    value = "BACKWARDATION",
                    subtext = "Annualized Slope: ${forwardCurve?.curveSlope ?: -18.7}%",
                    accentColor = FinancialCyan,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "12M BASIS SPREAD",
                    value = "-$0.53/hr",
                    subtext = "Repo Rate: 4.8%",
                    accentColor = FinancialAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Interactive Forward Curve Chart
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = TerminalSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "${selectedChip.name} MATURITY PROFILE (SPOT → 36M)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ForwardCurveChart(
                        curveData = forwardCurve,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp),
                        currencySymbol = displayCurrency.symbol
                    )
                }
            }
        }

        // Forward Term Points Table
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = TerminalSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "DELIVERY MATURITY & SPREAD BREAKDOWN",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Table Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("MATURITY", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                        Text("PRICE (${displayCurrency.symbol})", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                        Text("SPOT DELTA", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                        Text("ANNUAL BASIS", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = TerminalBorder)
                    Spacer(modifier = Modifier.height(4.dp))

                    forwardCurve?.points?.forEach { pt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(pt.maturityLabel, style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text("${displayCurrency.symbol}${String.format(Locale.US, "%.2f", pt.price * displayCurrency.fxRateToUsd)}", style = MaterialTheme.typography.bodySmall, color = FinancialCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("${if (pt.spotDelta >= 0) "+" else ""}${String.format(Locale.US, "%.2f", pt.spotDelta)}", style = MaterialTheme.typography.bodySmall, color = if (pt.spotDelta >= 0) FinancialGreen else FinancialRed, fontFamily = FontFamily.Monospace)
                            Text("${String.format(Locale.US, "%.1f", pt.basisSpreadAnnualized)}%", style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontFamily = FontFamily.Monospace)
                        }
                        HorizontalDivider(color = TerminalBorder.copy(alpha = 0.3f), thickness = 0.5.dp)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
