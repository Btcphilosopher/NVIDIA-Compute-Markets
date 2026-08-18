package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
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
import com.example.components.MetricCard
import com.example.models.AppLanguage
import com.example.models.ComputeRiskDashboard
import com.example.models.RiskFactor
import com.example.models.RiskSeverity
import com.example.presentation.ComputeMarketsViewModel
import com.example.presentation.DisplayCurrency
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun RiskDashboardScreen(
    viewModel: ComputeMarketsViewModel,
    currentLanguage: AppLanguage,
    displayCurrency: DisplayCurrency,
    modifier: Modifier = Modifier
) {
    val riskData = viewModel.riskDashboard.value

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "ENTERPRISE COMPUTE RISK & VaR DASHBOARD",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Value-at-Risk, capacity exhaustion probabilities, grid curtailment and supply chain stress factors",
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
                    label = "1-DAY 95% COMPUTE VaR",
                    value = "-${riskData?.valueAtRisk1Day95 ?: 6.4}%",
                    subtext = "Price Shock Risk",
                    accentColor = FinancialRed,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "CAPACITY EXHAUSTION",
                    value = "${riskData?.capacityExhaustionProb ?: 18.5}%",
                    subtext = "Virginia & Taiwan Nodes",
                    accentColor = FinancialAmber,
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
                    label = "GRID CURTAILMENT RISK",
                    value = "${riskData?.gridCurtailmentRiskProb ?: 14.2}%",
                    subtext = "Peak Summer Load Prob",
                    accentColor = FinancialAmber,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "COMPOSITE RISK INDEX",
                    value = "${riskData?.overallRiskScore ?: 64}/100",
                    subtext = "ELEVATED POSTURE",
                    accentColor = NvidiaGreenGlow,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Risk Factor Breakdown Matrix
        item {
            Text(
                text = "ACTIVE QUANT RISK FACTORS & MITIGATION STRATEGIES",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontFamily = FontFamily.Monospace
            )
        }

        items(riskData?.factors ?: emptyList(), key = { it.title }) { factor ->
            RiskFactorCard(factor = factor)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RiskFactorCard(factor: RiskFactor) {
    val sevColor = when (factor.severity) {
        RiskSeverity.LOW -> NvidiaGreenGlow
        RiskSeverity.MODERATE -> FinancialCyan
        RiskSeverity.ELEVATED -> FinancialAmber
        RiskSeverity.CRITICAL -> FinancialRed
    }

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(sevColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = factor.severity.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = sevColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = factor.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "${factor.scoreOutOf100}/100",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = sevColor,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = factor.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 11.sp)

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = TerminalBorder)
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "• Telemetry Metric: ${factor.metricValue}",
                style = MaterialTheme.typography.bodySmall,
                color = FinancialCyan,
                fontSize = 10.5.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "• Quant Hedging Strategy: ${factor.mitigationStrategy}",
                style = MaterialTheme.typography.bodySmall,
                color = NvidiaGreenGlow,
                fontSize = 10.5.sp
            )
        }
    }
}
