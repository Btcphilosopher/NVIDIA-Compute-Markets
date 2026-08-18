package com.example.charts

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.DatacenterNode
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.hypot

@OptIn(ExperimentalTextApi::class)
@Composable
fun DatacenterMapCanvas(
    datacenters: List<DatacenterNode>,
    selectedDatacenter: DatacenterNode?,
    onSelectDatacenter: (DatacenterNode) -> Unit,
    modifier: Modifier = Modifier,
    currencySymbol: String = "$"
) {
    if (datacenters.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Loading Datacenter Nodes...", color = TextMuted)
        }
        return
    }

    val textMeasurer = rememberTextMeasurer()

    // Pulsing animation for node activity
    val infiniteTransition = rememberInfiniteTransition(label = "dc_pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_rad"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(datacenters) {
                detectTapGestures { tapOffset ->
                    val w = size.width
                    val h = size.height
                    // Find closest datacenter node to tap
                    var closest: DatacenterNode? = null
                    var minDist = Float.MAX_VALUE

                    datacenters.forEach { dc ->
                        // Convert lat/lng to screen space (Equirectangular projection)
                        val normX = ((dc.longitude + 180.0) / 360.0).toFloat()
                        val normY = ((90.0 - dc.latitude) / 180.0).toFloat()
                        val nodeX = normX * w
                        val nodeY = normY * h

                        val dist = hypot(tapOffset.x - nodeX, tapOffset.y - nodeY)
                        if (dist < 40.dp.toPx() && dist < minDist) {
                            minDist = dist
                            closest = dc
                        }
                    }

                    closest?.let { onSelectDatacenter(it) }
                }
            }
    ) {
        val w = size.width
        val h = size.height
        if (w <= 0 || h <= 0) return@Canvas

        // Draw World Map Grid / Latitude-Longitude Grid
        val latLines = 6
        val lonLines = 12

        for (i in 1 until latLines) {
            val y = h * (i.toFloat() / latLines)
            drawLine(
                color = TerminalBorder.copy(alpha = 0.25f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f), 0f)
            )
        }

        for (j in 1 until lonLines) {
            val x = w * (j.toFloat() / lonLines)
            drawLine(
                color = TerminalBorder.copy(alpha = 0.25f),
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f), 0f)
            )
        }

        // Draw continent outlines approximation / Tech grid dots
        for (gx in 0..24) {
            for (gy in 0..14) {
                val dotX = w * (gx / 24f)
                val dotY = h * (gy / 14f)
                drawCircle(
                    color = TerminalBorder.copy(alpha = 0.3f),
                    radius = 1.2f,
                    center = Offset(dotX, dotY)
                )
            }
        }

        // Precompute node positions
        val nodePositions = datacenters.map { dc ->
            val normX = ((dc.longitude + 180.0) / 360.0).toFloat()
            val normY = ((90.0 - dc.latitude) / 180.0).toFloat()
            val nodeX = normX * w
            val nodeY = normY * h
            Pair(dc, Offset(nodeX, nodeY))
        }

        // Draw Interconnect Fiber / NVLink backbone lines between nodes
        for (i in nodePositions.indices) {
            for (j in i + 1 until nodePositions.size) {
                val p1 = nodePositions[i].second
                val p2 = nodePositions[j].second
                val dist = hypot(p1.x - p2.x, p1.y - p2.y)
                if (dist < w * 0.55f) {
                    drawLine(
                        color = FinancialCyan.copy(alpha = 0.15f),
                        start = p1,
                        end = p2,
                        strokeWidth = 1.2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                }
            }
        }

        // Draw Datacenter Nodes
        nodePositions.forEach { (dc, pos) ->
            val isSelected = selectedDatacenter?.id == dc.id

            // Pulse Ring for active nodes
            drawCircle(
                color = if (isSelected) NvidiaGreenGlow.copy(alpha = pulseAlpha) else FinancialCyan.copy(alpha = pulseAlpha * 0.6f),
                radius = pulseRadius * (if (isSelected) 1.5f else 1.0f),
                center = pos,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Outer ring
            drawCircle(
                color = if (isSelected) NvidiaGreenGlow else FinancialCyan,
                radius = if (isSelected) 8.dp.toPx() else 5.5.dp.toPx(),
                center = pos
            )

            // Core center
            drawCircle(
                color = TerminalBackground,
                radius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx(),
                center = pos
            )
            drawCircle(
                color = if (isSelected) Color.White else NvidiaGreen,
                radius = if (isSelected) 3.dp.toPx() else 2.dp.toPx(),
                center = pos
            )

            // Label Box
            val labelText = "${dc.id.substringAfterLast('-')} (${(dc.totalGpus / 1000)}k)"
            val textLayout = textMeasurer.measure(
                text = labelText,
                style = TextStyle(
                    color = if (isSelected) NvidiaGreenGlow else TextPrimary,
                    fontSize = if (isSelected) 10.sp else 8.5.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                )
            )

            val labelX = (pos.x - textLayout.size.width / 2f).coerceIn(4.dp.toPx(), w - textLayout.size.width - 4.dp.toPx())
            val labelY = if (pos.y > h * 0.75f) pos.y - 20.dp.toPx() else pos.y + 10.dp.toPx()

            // Draw label background pill
            drawRoundRect(
                color = TerminalSurface.copy(alpha = 0.85f),
                topLeft = Offset(labelX - 4.dp.toPx(), labelY - 2.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(textLayout.size.width + 8.dp.toPx(), textLayout.size.height + 4.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(labelX, labelY)
            )
        }
    }
}
