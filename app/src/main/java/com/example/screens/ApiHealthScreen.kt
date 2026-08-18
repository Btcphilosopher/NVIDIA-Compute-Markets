package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import com.example.models.ApiHealthState
import com.example.models.AppLanguage
import com.example.presentation.ComputeMarketsViewModel
import com.example.presentation.DisplayCurrency
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ApiHealthScreen(
    viewModel: ComputeMarketsViewModel,
    currentLanguage: AppLanguage,
    displayCurrency: DisplayCurrency,
    modifier: Modifier = Modifier
) {
    val apiState = viewModel.apiHealthState.value

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "NVIDIA COMPUTE API SERVICE HEALTH & TELEMETRY",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Real-time gRPC, REST and WebSocket connection state, heartbeat telemetry and latency benchmarks",
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
                    label = "WEBSOCKET LATENCY",
                    value = "${apiState.webSocketLatencyMs} ms",
                    subtext = "High-Frequency Feed",
                    accentColor = if (apiState.webSocketConnected) NvidiaGreenGlow else FinancialRed,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "REST API LATENCY",
                    value = "${apiState.restApiLatencyMs} ms",
                    subtext = "TLS 1.3 Certified",
                    accentColor = FinancialCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Endpoint & Connection Card
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = TerminalSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "ACTIVE ENDPOINT & PROTOCOL SUITE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = apiState.activeEndpointUrl,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = NvidiaGreenGlow,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date(apiState.lastMarketDataSync))
                    Text(
                        text = "Last Market Sync: $timeStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = TerminalBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Reconnect and Toggle Stream Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.triggerReconnect() },
                            colors = ButtonDefaults.buttonColors(containerColor = NvidiaGreen, contentColor = Color.Black),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reconnect", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reconnect Feed", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.toggleWebSocketStream() },
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (apiState.webSocketConnected) FinancialRed else NvidiaGreenGlow),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (apiState.webSocketConnected) "Disconnect" else "Connect WS",
                                color = if (apiState.webSocketConnected) FinancialRed else NvidiaGreenGlow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Subsystem Telemetry Status List
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = TerminalSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "SUBSYSTEM HEALTH & INTEGRATION STATUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ServiceHealthRow(name = "MarketDataApi (Live WebSocket Feed)", isOnline = apiState.marketDataFeedLive, latency = "${apiState.webSocketLatencyMs}ms")
                    ServiceHealthRow(name = "ForwardCurveApi (Term Structure Service)", isOnline = true, latency = "38ms")
                    ServiceHealthRow(name = "OptionsApi (Black-76 Pricer & Surface)", isOnline = true, latency = "42ms")
                    ServiceHealthRow(name = "CapacityApi (Global Cluster Telemetry)", isOnline = apiState.capacityFeedLive, latency = "${apiState.webSocketLatencyMs + 5}ms")
                    ServiceHealthRow(name = "DatacenterApi (Node Network Coordinates)", isOnline = true, latency = "26ms")
                    ServiceHealthRow(name = "ForecastApi (LCOC & Power Grid Model)", isOnline = apiState.forecastServiceLive, latency = "45ms")
                    ServiceHealthRow(name = "SimulationApi (Monte Carlo Macro Engine)", isOnline = apiState.simulationServiceLive, latency = "120ms")
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ServiceHealthRow(name: String, isOnline: Boolean, latency: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isOnline) NvidiaGreenGlow else FinancialRed)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = name, style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontSize = 11.sp)
        }
        Text(
            text = if (isOnline) "ONLINE ($latency)" else "OFFLINE",
            style = MaterialTheme.typography.labelSmall,
            color = if (isOnline) NvidiaGreenGlow else FinancialRed,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp
        )
    }
    HorizontalDivider(color = TerminalBorder.copy(alpha = 0.3f), thickness = 0.5.dp)
}
