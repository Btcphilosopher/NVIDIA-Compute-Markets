package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.charts.DatacenterMapCanvas
import com.example.components.DatacenterDetailBottomSheet
import com.example.components.MetricCard
import com.example.models.AppLanguage
import com.example.models.DatacenterNode
import com.example.presentation.ComputeMarketsViewModel
import com.example.presentation.DisplayCurrency
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun DatacentersScreen(
    viewModel: ComputeMarketsViewModel,
    currentLanguage: AppLanguage,
    displayCurrency: DisplayCurrency,
    modifier: Modifier = Modifier
) {
    val datacenters = viewModel.datacenters.value
    val selectedDatacenter = viewModel.selectedDatacenter.value

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "GLOBAL HYPERSCALE COMPUTE VAULTS & NODES",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Direct Liquid Cooling telemetry, substation interconnects and network fiber latency",
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
                    label = "ACTIVE CLUSTERS",
                    value = "${datacenters.size} Tier-1 Vaults",
                    subtext = "Global Interconnect: NVLink",
                    accentColor = NvidiaGreenGlow,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "TOP EFFICIENCY PUE",
                    value = "1.11 PUE",
                    subtext = "Oregon Columbia Hydro",
                    accentColor = FinancialCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Interactive Global Map Canvas
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
                            text = "TAP NODE TO INSPECT TELEMETRY",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "EQUIRECTANGULAR GEODETIC GRID",
                            style = MaterialTheme.typography.labelSmall,
                            color = FinancialCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    DatacenterMapCanvas(
                        datacenters = datacenters,
                        selectedDatacenter = selectedDatacenter,
                        onSelectDatacenter = { viewModel.selectDatacenter(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        currencySymbol = displayCurrency.symbol
                    )
                }
            }
        }

        // Datacenter List Nodes
        item {
            Text(
                text = "DATACENTER NODES TELEMETRY DIRECTORY",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontFamily = FontFamily.Monospace
            )
        }

        items(datacenters, key = { it.id }) { dc ->
            DatacenterListItem(
                dc = dc,
                isSelected = selectedDatacenter?.id == dc.id,
                displayCurrency = displayCurrency,
                onClick = { viewModel.selectDatacenter(dc) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal Bottom Sheet when a datacenter is selected
    if (selectedDatacenter != null) {
        DatacenterDetailBottomSheet(
            datacenter = selectedDatacenter,
            currentLanguage = currentLanguage,
            onDismiss = { viewModel.selectDatacenter(null) },
            currencySymbol = displayCurrency.symbol
        )
    }
}

@Composable
private fun DatacenterListItem(
    dc: DatacenterNode,
    isSelected: Boolean,
    displayCurrency: DisplayCurrency,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) NvidiaGreenSurface else TerminalSurfaceCard,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) NvidiaGreenGlow else TerminalBorder
        ),
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
                        text = dc.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) NvidiaGreenGlow else TextPrimary
                    )
                    Text(
                        text = dc.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                Text(
                    text = "${displayCurrency.symbol}${String.format(Locale.US, "%.2f", dc.spotPricePerHour * displayCurrency.fxRateToUsd)}/hr",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = NvidiaGreenGlow,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${dc.totalGpus / 1000}k GPUs • ${dc.dominantGpu.name} • PUE ${dc.pue}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "UTIL: ${String.format(Locale.US, "%.1f", dc.utilizationRate)}% • ${dc.networkLatencyMs}ms",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (dc.utilizationRate > 90.0) FinancialAmber else FinancialCyan,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
