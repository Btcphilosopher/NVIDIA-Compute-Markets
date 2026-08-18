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
import com.example.charts.SupplyDemandChart
import com.example.components.MetricCard
import com.example.components.RegionSelectorRow
import com.example.models.AppLanguage
import com.example.presentation.ComputeMarketsViewModel
import com.example.presentation.DisplayCurrency
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun SupplyDemandScreen(
    viewModel: ComputeMarketsViewModel,
    currentLanguage: AppLanguage,
    displayCurrency: DisplayCurrency,
    modifier: Modifier = Modifier
) {
    val selectedRegion = viewModel.selectedRegion.value
    val supplyDemand = viewModel.supplyDemand.value

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "GLOBAL COMPUTE SUPPLY & DEMAND DYNAMICS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Multi-quarter capacity additions, demand trajectories, and structural deficit forecasting",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                fontSize = 11.sp
            )
        }

        item {
            RegionSelectorRow(
                selectedRegion = selectedRegion,
                onSelectRegion = { viewModel.setRegion(it) }
            )
        }

        // Summary Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    label = "PROJECTED SHORTAGE (3M)",
                    value = "${(supplyDemand?.projectedShortageNext3MonthsGpus ?: 210000) / 1000}k GPUs",
                    subtext = "Critical Horizon",
                    accentColor = FinancialAmber,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "QUARTERLY GROWTH",
                    value = "+${supplyDemand?.forecastQuarterlyCapacityGrowthPercent ?: 12.4}%",
                    subtext = "TSMC & Superhub Adds",
                    accentColor = NvidiaGreenGlow,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Main Supply vs Demand Chart
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
                            text = "${selectedRegion.displayName} TRAJECTORY (2025-2026)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(FinancialPurple, RoundedCornerShape(2.dp)))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Demand", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(NvidiaGreen, RoundedCornerShape(2.dp)))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Supply", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    SupplyDemandChart(
                        data = supplyDemand,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                    )
                }
            }
        }

        // Structural Balance Assessment
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = TerminalSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "STRUCTURAL MARKET BALANCE & BOTTLENECK ANALYSIS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Global compute demand continues to outpace available interconnect-ready datacenter space. While TSMC advanced packaging bottlenecks are easing with CoWoS-L line expansion, regional power grid substation connections (50MW-200MW+) represent the primary constraint through late 2026.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
