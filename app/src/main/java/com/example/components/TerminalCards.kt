package com.example.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.models.GpuChip
import com.example.models.MarketRegion
import com.example.models.OptionGreeks
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    deltaText: String? = null,
    isDeltaPositive: Boolean? = null,
    accentColor: Color = TextPrimary,
    subtext: String? = null
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = TerminalSurfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = accentColor
                )
                if (deltaText != null && isDeltaPositive != null) {
                    Text(
                        text = "${if (isDeltaPositive) "▲" else "▼"} $deltaText",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = if (isDeltaPositive) FinancialGreen else FinancialRed,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
            if (subtext != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun GreeksPanel(
    greeks: OptionGreeks,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = TerminalSurfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "BLACK-76 GREEKS SENSITIVITY PROFILE",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GreekItem(name = "Delta (Δ)", value = String.format(Locale.US, "%.3f", greeks.delta), color = FinancialCyan)
                GreekItem(name = "Gamma (Γ)", value = String.format(Locale.US, "%.4f", greeks.gamma), color = NvidiaGreenGlow)
                GreekItem(name = "Vega (ν)", value = String.format(Locale.US, "%.3f", greeks.vega), color = FinancialPurple)
                GreekItem(name = "Theta (θ)", value = String.format(Locale.US, "%.4f", greeks.theta), color = FinancialAmber)
                GreekItem(name = "Rho (ρ)", value = String.format(Locale.US, "%.3f", greeks.rho), color = TextSecondary)
            }
        }
    }
}

@Composable
private fun GreekItem(
    name: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = name, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = color
        )
    }
}

@Composable
fun ChipSelectorRow(
    selectedChip: GpuChip,
    onSelectChip: (GpuChip) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        GpuChip.entries.forEach { chip ->
            val isSelected = chip == selectedChip
            Surface(
                onClick = { onSelectChip(chip) },
                shape = RoundedCornerShape(6.dp),
                color = if (isSelected) NvidiaGreenSurface else TerminalSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) NvidiaGreenGlow else TerminalBorder
                ),
                modifier = Modifier.height(32.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chip.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) NvidiaGreenGlow else TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun RegionSelectorRow(
    selectedRegion: MarketRegion,
    onSelectRegion: (MarketRegion) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        MarketRegion.entries.forEach { region ->
            val isSelected = region == selectedRegion
            Surface(
                onClick = { onSelectRegion(region) },
                shape = RoundedCornerShape(6.dp),
                color = if (isSelected) TerminalSurfaceHighlight else TerminalSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) FinancialCyan else TerminalBorder
                ),
                modifier = Modifier.height(30.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = region.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) FinancialCyan else TextSecondary
                    )
                }
            }
        }
    }
}
