package com.example.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.models.DatacenterNode
import com.example.ui.theme.*
import com.example.utils.Localization
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatacenterDetailBottomSheet(
    datacenter: DatacenterNode?,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    currencySymbol: String = "$"
) {
    if (datacenter == null) return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = TerminalSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = TerminalBorderHighlight) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = datacenter.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${datacenter.location} • ID: ${datacenter.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = NvidiaGreenGlow
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NvidiaGreenSurface)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "UTIL: ${String.format(Locale.US, "%.1f", datacenter.utilizationRate)}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NvidiaGreenGlow
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = TerminalBorder)
            Spacer(modifier = Modifier.height(14.dp))

            // 4 Grid Telemetry Boxes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryBox(
                    title = "GPU CAPACITY",
                    value = "${datacenter.totalGpus / 1000}k",
                    subtitle = "${datacenter.availableGpus / 1000}k Available",
                    modifier = Modifier.weight(1f)
                )
                TelemetryBox(
                    title = "POWER LOAD",
                    value = "${datacenter.currentPowerDrawMw.toInt()} MW",
                    subtitle = "Capacity ${datacenter.totalPowerMw.toInt()} MW",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryBox(
                    title = "SPOT HOURLY",
                    value = "$currencySymbol${String.format(Locale.US, "%.2f", datacenter.spotPricePerHour)}",
                    subtitle = "Primary: ${datacenter.dominantGpu.name}",
                    modifier = Modifier.weight(1f)
                )
                TelemetryBox(
                    title = "PUE EFFICIENCY",
                    value = String.format(Locale.US, "%.2f", datacenter.pue),
                    subtitle = "${datacenter.networkLatencyMs}ms Fabric Latency",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Infrastructure Details
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = TerminalSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "FACILITY & POWER SPECIFICATIONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Cooling System: ${datacenter.coolingTech}", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    Spacer(modifier = Modifier.height(3.dp))
                    Text("• Power Infrastructure: ${datacenter.powerSource}", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    Spacer(modifier = Modifier.height(3.dp))
                    Text("• Current Dispatch Queue: ${datacenter.queueWaitHours} hours estimated wait", style = MaterialTheme.typography.bodySmall, color = FinancialAmber)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TelemetryBox(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = TerminalSurfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 10.sp)
        }
    }
}
