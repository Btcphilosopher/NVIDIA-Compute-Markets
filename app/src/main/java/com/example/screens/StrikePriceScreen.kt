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
import com.example.charts.StrikePricePayoffChart
import com.example.components.ChipSelectorRow
import com.example.components.GreeksPanel
import com.example.components.MetricCard
import com.example.models.AppLanguage
import com.example.models.OptionType
import com.example.presentation.ComputeMarketsViewModel
import com.example.presentation.DisplayCurrency
import com.example.presentation.StrikeChartMode
import com.example.ui.theme.*
import com.example.utils.Localization
import java.util.Locale

@Composable
fun StrikePriceScreen(
    viewModel: ComputeMarketsViewModel,
    currentLanguage: AppLanguage,
    displayCurrency: DisplayCurrency,
    modifier: Modifier = Modifier
) {
    val selectedChip = viewModel.selectedChip.value
    val strikeContract = viewModel.currentStrikeContract.value
    val optionType = viewModel.strikeOptionType.value
    val strikePrice = viewModel.strikePriceValue.value
    val maturityDays = viewModel.strikeMaturityDays.value
    val contractSize = viewModel.strikeContractSize.value
    val chartMode = viewModel.strikeChartMode.value

    val spot = strikeContract?.currentSpotPrice ?: 2.84

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "BLACK-76 COMPUTE STRIKE OPTION PRICER",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Institutional pricing engine for reservation collars, caps, floors and strike sensitivity",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                fontSize = 11.sp
            )
        }

        // Chip Selector
        item {
            ChipSelectorRow(
                selectedChip = selectedChip,
                onSelectChip = { viewModel.selectChip(it) }
            )
        }

        // Option Type (Call/Put) & Tenor Row
        item {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = TerminalSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Call / Put Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("CONTRACT TYPE:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                OptionType.CALL_OPTION to "CALL (算力買權)",
                                OptionType.PUT_OPTION to "PUT (算力賣權)"
                            ).forEach { (type, label) ->
                                val isSelected = optionType == type
                                Surface(
                                    onClick = { viewModel.setStrikeOptionType(type) },
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isSelected) NvidiaGreenSurface else TerminalSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NvidiaGreenGlow else TerminalBorder),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) NvidiaGreenGlow else TextMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = TerminalBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Tenor Selector (30D, 90D, 180D, 365D)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("MATURITY TENOR:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(30 to "1M (30D)", 90 to "3M (90D)", 180 to "6M (180D)", 365 to "1Y (365D)").forEach { (days, label) ->
                                val isSelected = maturityDays == days
                                Surface(
                                    onClick = { viewModel.setStrikeMaturity(days) },
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isSelected) FinancialCyan.copy(alpha = 0.2f) else TerminalSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) FinancialCyan else TerminalBorder),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = if (isSelected) FinancialCyan else TextMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Strike Price Slider & Value
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "STRIKE PRICE (K)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = "${displayCurrency.symbol}${String.format(Locale.US, "%.2f", strikePrice * displayCurrency.fxRateToUsd)}/GPU-hr (Spot: ${displayCurrency.symbol}${String.format(Locale.US, "%.2f", spot * displayCurrency.fxRateToUsd)})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = NvidiaGreenGlow,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Slider(
                        value = strikePrice.toFloat(),
                        onValueChange = { viewModel.setStrikePrice((it * 100).toInt() / 100.0) },
                        valueRange = (spot.toFloat() * 0.5f)..(spot.toFloat() * 1.6f),
                        colors = SliderDefaults.colors(
                            thumbColor = NvidiaGreenGlow,
                            activeTrackColor = NvidiaGreen,
                            inactiveTrackColor = TerminalBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Contract Size Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("CONTRACT SIZE:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(1000 to "1k hrs", 10000 to "10k hrs", 100000 to "100k hrs").forEach { (sz, label) ->
                                val isSelected = contractSize == sz
                                Surface(
                                    onClick = { viewModel.setStrikeContractSize(sz) },
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isSelected) FinancialAmber.copy(alpha = 0.2f) else TerminalSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) FinancialAmber else TerminalBorder),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = if (isSelected) FinancialAmber else TextMuted
                                    )
                                }
                            }
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
                    label = "EST. OPTION PREMIUM",
                    value = "${displayCurrency.symbol}${String.format(Locale.US, "%.3f", (strikeContract?.estimatedPricePerGpuHour ?: 0.185) * displayCurrency.fxRateToUsd)}/hr",
                    subtext = "Total: ${displayCurrency.symbol}${String.format(Locale.US, "%,.0f", (strikeContract?.totalPremium ?: 1850.0) * displayCurrency.fxRateToUsd)}",
                    accentColor = NvidiaGreenGlow,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "PROBABILITY OF PROFIT",
                    value = "${String.format(Locale.US, "%.1f", strikeContract?.probabilityOfProfit ?: 58.2)}%",
                    subtext = "Breakeven: ${displayCurrency.symbol}${String.format(Locale.US, "%.2f", (strikeContract?.breakevenPrice ?: 2.98) * displayCurrency.fxRateToUsd)}",
                    accentColor = FinancialCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Interactive Chart Card
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = TerminalSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Chart Mode Toggle Buttons (Price, Implied Vol, Probability, Greeks)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STRIKE PAYOFF & SENSITIVITY",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(
                                StrikeChartMode.PRICE_PAYOFF to "PAYOFF",
                                StrikeChartMode.IMPLIED_VOL to "IV SKEW",
                                StrikeChartMode.PROBABILITY to "PROB",
                                StrikeChartMode.GREEKS to "DELTA"
                            ).forEach { (mode, label) ->
                                val isSelected = chartMode == mode
                                Surface(
                                    onClick = { viewModel.setStrikeChartMode(mode) },
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

                    StrikePricePayoffChart(
                        contract = strikeContract,
                        mode = chartMode,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp),
                        currencySymbol = displayCurrency.symbol
                    )
                }
            }
        }

        // Greeks Sensitivity Panel
        if (strikeContract != null) {
            item {
                GreeksPanel(greeks = strikeContract.greeks)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
