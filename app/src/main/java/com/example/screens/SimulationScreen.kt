package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.charts.ScenarioComparisonChart
import com.example.components.MetricCard
import com.example.models.AppLanguage
import com.example.presentation.ComputeMarketsViewModel
import com.example.presentation.DisplayCurrency
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun SimulationScreen(
    viewModel: ComputeMarketsViewModel,
    currentLanguage: AppLanguage,
    displayCurrency: DisplayCurrency,
    modifier: Modifier = Modifier
) {
    val simInput = viewModel.simulationInput.value
    val simResult = viewModel.simulationResult.value
    val isSimulating = viewModel.isSimulating.value

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "MACROECONOMIC COMPUTE SCENARIO SIMULATOR",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Stress test AI market supply shocks, electricity spikes, and next-gen silicon delivery shifts",
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
                val spotChange = simResult?.spotPriceChangePercent ?: 0.0
                MetricCard(
                    label = "SIMULATED SPOT PRICE",
                    value = "${displayCurrency.symbol}${String.format(Locale.US, "%.2f", (simResult?.simulatedSpotPrice ?: 2.84) * displayCurrency.fxRateToUsd)}/hr",
                    deltaText = "${String.format(Locale.US, "%.1f", spotChange)}%",
                    isDeltaPositive = spotChange >= 0,
                    accentColor = if (spotChange >= 0) FinancialAmber else NvidiaGreenGlow,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "SHORTAGE RISK LEVEL",
                    value = simResult?.shortageRiskLevel ?: "BALANCED",
                    subtext = "Util: ${String.format(Locale.US, "%.1f", simResult?.simulatedUtilization ?: 91.4)}%",
                    accentColor = if ((simResult?.simulatedUtilization ?: 90.0) > 95.0) FinancialRed else FinancialCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Forward Curve Shock Comparison Chart
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
                            text = "FORWARD CURVE SHOCK: BASELINE VS SIMULATED",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("— Baseline", color = TextMuted, style = MaterialTheme.typography.labelSmall, fontSize = 9.5.sp)
                            Text("— Simulated", color = FinancialAmber, style = MaterialTheme.typography.labelSmall, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    ScenarioComparisonChart(
                        result = simResult,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        currencySymbol = displayCurrency.symbol
                    )
                }
            }
        }

        // Interactive Macro Sliders Card
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
                            text = "MACROECONOMIC SHOCK PARAMETERS",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                        IconButton(onClick = { viewModel.resetSimulationInput() }, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.RestartAlt, contentDescription = "Reset", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Slider 1: AI Demand Growth Delta %
                    SimulationSlider(
                        label = "AI Demand Growth Shock",
                        value = simInput.aiDemandGrowthDeltaPercent,
                        valueRange = -50f..100f,
                        unit = "%",
                        onValueChange = {
                            viewModel.updateSimulationInput { inp -> inp.copy(aiDemandGrowthDeltaPercent = it) }
                            viewModel.runSimulation()
                        }
                    )

                    // Slider 2: GPU Supply Delta %
                    SimulationSlider(
                        label = "TSMC / GPU Silicon Supply Shock",
                        value = simInput.gpuSupplyDeltaPercent,
                        valueRange = -50f..100f,
                        unit = "%",
                        onValueChange = {
                            viewModel.updateSimulationInput { inp -> inp.copy(gpuSupplyDeltaPercent = it) }
                            viewModel.runSimulation()
                        }
                    )

                    // Slider 3: Electricity Price Delta %
                    SimulationSlider(
                        label = "Regional Electricity Price Shift",
                        value = simInput.electricityPriceDeltaPercent,
                        valueRange = -40f..80f,
                        unit = "%",
                        onValueChange = {
                            viewModel.updateSimulationInput { inp -> inp.copy(electricityPriceDeltaPercent = it) }
                            viewModel.runSimulation()
                        }
                    )

                    // Slider 4: Datacenter Capacity Expansion %
                    SimulationSlider(
                        label = "Datacenter Shell Expansion Ramp",
                        value = simInput.datacenterCapacityExpansionPercent,
                        valueRange = -20f..80f,
                        unit = "%",
                        onValueChange = {
                            viewModel.updateSimulationInput { inp -> inp.copy(datacenterCapacityExpansionPercent = it) }
                            viewModel.runSimulation()
                        }
                    )

                    // Slider 5: Next-Gen GPU Perf Boost %
                    SimulationSlider(
                        label = "Next-Gen Architecture Perf Boost (TFLOPS/$)",
                        value = simInput.nextGenPerformanceBoostPercent,
                        valueRange = 0f..150f,
                        unit = "%",
                        onValueChange = {
                            viewModel.updateSimulationInput { inp -> inp.copy(nextGenPerformanceBoostPercent = it) }
                            viewModel.runSimulation()
                        }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SimulationSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    unit: String,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 11.sp)
            Text(
                text = "${if (value > 0) "+" else ""}${value.toInt()}$unit",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (value != 0f) NvidiaGreenGlow else TextMuted,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = NvidiaGreenGlow,
                activeTrackColor = NvidiaGreen,
                inactiveTrackColor = TerminalBorder
            ),
            modifier = Modifier.height(24.dp)
        )
    }
}
