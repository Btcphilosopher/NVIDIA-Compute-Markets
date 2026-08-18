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
import com.example.components.MetricCard
import com.example.models.AppLanguage
import com.example.models.RegionalCapacity
import com.example.presentation.ComputeMarketsViewModel
import com.example.presentation.DisplayCurrency
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun CapacityScreen(
    viewModel: ComputeMarketsViewModel,
    currentLanguage: AppLanguage,
    displayCurrency: DisplayCurrency,
    modifier: Modifier = Modifier
) {
    val globalCap = viewModel.globalCapacity.value

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "GLOBAL GPU COMPUTE CAPACITY & LOAD TELEMETRY",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Global cluster allocation, active running nodes, queue times and power consumption",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                fontSize = 11.sp
            )
        }

        // Summary Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    label = "TOTAL GLOBAL POOL",
                    value = "${(globalCap?.totalGpus ?: 4820000) / 1000}k GPUs",
                    subtext = "Available: ${(globalCap?.availableGpus ?: 610000) / 1000}k",
                    accentColor = NvidiaGreenGlow,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "TOTAL POWER DRAW",
                    value = "${globalCap?.totalPowerConsumptionMw?.toInt() ?: 3660} MW",
                    subtext = "Global PUE: ~1.16",
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
                    label = "OVERALL UTILIZATION",
                    value = "${String.format(Locale.US, "%.1f", globalCap?.globalUtilization ?: 87.3)}%",
                    subtext = "Peak Capacity Margin: 12.7%",
                    accentColor = FinancialAmber,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "AVG QUEUE TIME",
                    value = "${globalCap?.averageQueueTimeHours ?: 3.2} Hours",
                    subtext = "Standard Cluster Dispatch",
                    accentColor = FinancialPurple,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Regional Capacity Breakdown
        item {
            Text(
                text = "REGIONAL CAPACITY ALLOCATION & UTILIZATION",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontFamily = FontFamily.Monospace
            )
        }

        items(globalCap?.regions ?: emptyList(), key = { it.region.name }) { regionCap ->
            RegionalCapacityCard(
                cap = regionCap,
                displayCurrency = displayCurrency
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RegionalCapacityCard(
    cap: RegionalCapacity,
    displayCurrency: DisplayCurrency
) {
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
                    text = cap.region.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "UTIL: ${String.format(Locale.US, "%.1f", cap.utilizationPercent)}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (cap.utilizationPercent > 90.0) FinancialAmber else NvidiaGreenGlow,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Capacity Progress Bar
            LinearProgressIndicator(
                progress = { (cap.utilizationPercent / 100.0).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = if (cap.utilizationPercent > 90.0) FinancialAmber else NvidiaGreen,
                trackColor = TerminalSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total: ${cap.totalGpus / 1000}k • Active: ${cap.activeGpus / 1000}k • Avail: ${cap.availableGpus / 1000}k",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${cap.powerCapacityMw.toInt()} MW • ${cap.dominantGpu.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = FinancialCyan,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
