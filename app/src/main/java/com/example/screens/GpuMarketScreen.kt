package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.example.models.AppLanguage
import com.example.models.GpuChip
import com.example.models.GpuMarketInstrument
import com.example.models.MarketStatus
import com.example.presentation.ComputeMarketsViewModel
import com.example.presentation.DisplayCurrency
import com.example.presentation.TerminalScreen
import com.example.ui.theme.*
import com.example.utils.Localization
import java.util.Locale

@Composable
fun GpuMarketScreen(
    viewModel: ComputeMarketsViewModel,
    currentLanguage: AppLanguage,
    displayCurrency: DisplayCurrency,
    onNavigate: (TerminalScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val instruments = viewModel.marketInstruments.value

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "GLOBAL GPU COMPUTE INSTRUMENT MATRIX",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Real-time pricing, forward maturities, utilization and liquidity depth",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                fontSize = 11.sp
            )
        }

        items(instruments, key = { it.chip.name }) { item ->
            GpuInstrumentCard(
                instrument = item,
                currency = displayCurrency,
                currentLanguage = currentLanguage,
                onClick = {
                    viewModel.selectChip(item.chip)
                    onNavigate(TerminalScreen.STRIKE_PRICE)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GpuInstrumentCard(
    instrument: GpuMarketInstrument,
    currency: DisplayCurrency,
    currentLanguage: AppLanguage,
    onClick: () -> Unit
) {
    val chip = instrument.chip
    val isUp = instrument.priceChange24h >= 0

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = TerminalSurfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Chip Name + Status Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(NvidiaGreenSurface)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = chip.architecture,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NvidiaGreenGlow,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NVIDIA ${chip.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${chip.vramGb}GB ${chip.memoryType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (instrument.marketStatus == MarketStatus.CAPACITY_CONSTRAINED) FinancialAmber.copy(alpha = 0.2f) else NvidiaGreenSurface)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (instrument.marketStatus == MarketStatus.CAPACITY_CONSTRAINED) "CONSTRAINED" else "ACTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (instrument.marketStatus == MarketStatus.CAPACITY_CONSTRAINED) FinancialAmber else NvidiaGreenGlow,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing & Forward Matrix Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(text = "SPOT RATE", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${currency.symbol}${String.format(Locale.US, "%.2f", instrument.spotPrice * currency.fxRateToUsd)}/hr",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = NvidiaGreenGlow,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${if (isUp) "▲" else "▼"}${String.format(Locale.US, "%.1f", instrument.priceChangePercent24h)}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isUp) FinancialGreen else FinancialRed,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Forward Rates
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "1M FWD", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                        Text(
                            text = "${currency.symbol}${String.format(Locale.US, "%.2f", instrument.forward1M * currency.fxRateToUsd)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = FinancialCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "12M FWD", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                        Text(
                            text = "${currency.symbol}${String.format(Locale.US, "%.2f", instrument.forward12M * currency.fxRateToUsd)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = FinancialCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = TerminalBorder)
            Spacer(modifier = Modifier.height(8.dp))

            // Capacity & Compute Specs Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "UTIL: ${String.format(Locale.US, "%.1f", instrument.utilizationRate)}% • Avail: ${instrument.availableCapacityGpus / 1000}k",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 10.5.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Queue: ${instrument.queueWaitTimeHours}h • IV: ${instrument.impliedVol}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = FinancialAmber,
                    fontSize = 10.5.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
