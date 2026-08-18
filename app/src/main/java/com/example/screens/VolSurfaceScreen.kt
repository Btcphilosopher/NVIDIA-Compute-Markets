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
import com.example.charts.VolatilitySurfaceChart
import com.example.components.ChipSelectorRow
import com.example.components.MetricCard
import com.example.models.AppLanguage
import com.example.presentation.ComputeMarketsViewModel
import com.example.presentation.DisplayCurrency
import com.example.presentation.VolSurfaceViewMode
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun VolSurfaceScreen(
    viewModel: ComputeMarketsViewModel,
    currentLanguage: AppLanguage,
    displayCurrency: DisplayCurrency,
    modifier: Modifier = Modifier
) {
    val selectedChip = viewModel.selectedChip.value
    val surfaceData = viewModel.volSurfaceData.value
    val viewMode = viewModel.volSurfaceViewMode.value

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "GPU COMPUTE VOLATILITY SURFACE (STRIKE × TENOR)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Implied volatility smiles, skew decay, and 3D orthographic surface visualization",
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
                    label = "ATM VOLATILITY",
                    value = "${String.format(Locale.US, "%.1f", surfaceData?.atmVol ?: 34.8)}%",
                    subtext = "Hopper/Blackwell Regime",
                    accentColor = NvidiaGreenGlow,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "SKEW GRADIENT",
                    value = "${String.format(Locale.US, "%.2f", surfaceData?.skewSlope ?: -0.32)}/K",
                    subtext = "Term Structure: Inverted",
                    accentColor = FinancialCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Main Surface Canvas Card
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = TerminalSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // View Mode Switcher: 3D Surface, 2D Heatmap, Smile Slices
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (viewMode == VolSurfaceViewMode.SURFACE_3D) "DRAG TO ROTATE 3D SURFACE" else "IMPLIED VOL SURFACE",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(
                                VolSurfaceViewMode.SURFACE_3D to "3D ISOMETRIC",
                                VolSurfaceViewMode.HEATMAP_2D to "2D HEATMAP",
                                VolSurfaceViewMode.STRIKE_SMILE_SLICE to "SMILE SLICES"
                            ).forEach { (mode, label) ->
                                val isSelected = viewMode == mode
                                Surface(
                                    onClick = { viewModel.setVolSurfaceViewMode(mode) },
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isSelected) NvidiaGreenSurface else TerminalSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NvidiaGreenDim else TerminalBorder),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) NvidiaGreenGlow else TextMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    VolatilitySurfaceChart(
                        surfaceData = surfaceData,
                        viewMode = viewMode,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        currencySymbol = displayCurrency.symbol
                    )
                }
            }
        }

        // Volatility Surface Interpretation Card
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = TerminalSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "QUANTITATIVE VOLATILITY REGIME COMMENTARY",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "The compute volatility surface exhibits pronounced put/downside skew and downward term slope. Short-dated options reflect high catalyst variance around frontier model training runs, while long-dated 1Y-2Y contracts price in predictable TSMC advanced wafer capacity expansion and architectural hardware depreciation.",
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
