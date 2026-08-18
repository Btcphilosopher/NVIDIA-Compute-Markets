package com.example.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
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
import com.example.models.PriceTick
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalTextApi::class)
@Composable
fun PriceInteractiveChart(
    priceTicks: List<PriceTick>,
    modifier: Modifier = Modifier,
    currencySymbol: String = "$",
    accentColor: Color = NvidiaGreen
) {
    if (priceTicks.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Loading chart telemetry...", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val textMeasurer = rememberTextMeasurer()

    val prices = priceTicks.map { it.price }
    val minPrice = prices.minOrNull() ?: 0.0
    val maxPrice = prices.maxOrNull() ?: 1.0
    val priceRange = max(0.01, maxPrice - minPrice)
    val paddedMin = minPrice - priceRange * 0.08
    val paddedMax = maxPrice + priceRange * 0.08
    val paddedRange = paddedMax - paddedMin

    val selectedTick = selectedIndex?.let { idx ->
        if (idx in priceTicks.indices) priceTicks[idx] else null
    }

    Column(modifier = modifier) {
        // Top Inspection Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedTick != null) {
                val timeStr = remember(selectedTick.timestamp) {
                    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(selectedTick.timestamp))
                }
                Text(
                    text = "TIME: $timeStr | VOL: ${selectedTick.volumeGpuHours} GPU-hr",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = "$currencySymbol${String.format(Locale.US, "%.2f", selectedTick.price)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = NvidiaGreenGlow
                )
            } else {
                val latest = priceTicks.lastOrNull()
                Text(
                    text = "INSTITUTIONAL HIGH-FREQUENCY FEED",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                if (latest != null) {
                    Text(
                        text = "LAST: $currencySymbol${String.format(Locale.US, "%.2f", latest.price)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary
                    )
                }
            }
        }

        // Canvas Area
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(priceTicks) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val stepX = size.width / (priceTicks.size - 1).coerceAtLeast(1)
                            val idx = (offset.x / stepX).toInt().coerceIn(0, priceTicks.size - 1)
                            selectedIndex = idx
                        },
                        onDrag = { change, _ ->
                            val stepX = size.width / (priceTicks.size - 1).coerceAtLeast(1)
                            val idx = (change.position.x / stepX).toInt().coerceIn(0, priceTicks.size - 1)
                            selectedIndex = idx
                        },
                        onDragEnd = { selectedIndex = null },
                        onDragCancel = { selectedIndex = null }
                    )
                }
                .pointerInput(priceTicks) {
                    detectTapGestures(
                        onPress = { offset ->
                            val stepX = size.width / (priceTicks.size - 1).coerceAtLeast(1)
                            val idx = (offset.x / stepX).toInt().coerceIn(0, priceTicks.size - 1)
                            selectedIndex = idx
                            tryAwaitRelease()
                            selectedIndex = null
                        }
                    )
                }
        ) {
            val w = size.width
            val h = size.height
            val n = priceTicks.size
            if (n < 2) return@Canvas

            val stepX = w / (n - 1)

            // Grid Lines (Horizontal)
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = h * (i.toFloat() / gridLines)
                drawLine(
                    color = TerminalBorder.copy(alpha = 0.5f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )

                val priceVal = paddedMax - (i.toDouble() / gridLines) * paddedRange
                val priceText = "$currencySymbol${String.format(Locale.US, "%.2f", priceVal)}"
                drawText(
                    textMeasurer = textMeasurer,
                    text = priceText,
                    topLeft = Offset(w - 65.dp.toPx(), y - 14.sp.toPx()),
                    style = TextStyle(
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                )
            }

            // Build Path
            val linePath = Path()
            val fillPath = Path()

            val points = priceTicks.mapIndexed { idx, tick ->
                val x = idx * stepX
                val normalizedY = ((paddedMax - tick.price) / paddedRange).toFloat()
                val y = normalizedY * h
                Offset(x, y)
            }

            linePath.moveTo(points.first().x, points.first().y)
            fillPath.moveTo(points.first().x, h)
            fillPath.lineTo(points.first().x, points.first().y)

            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val controlX = (p0.x + p1.x) / 2f
                linePath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                fillPath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
            }

            fillPath.lineTo(points.last().x, h)
            fillPath.close()

            // Draw Area Fill Gradient
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.35f),
                        accentColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = h
                )
            )

            // Draw Price Line
            drawPath(
                path = linePath,
                color = accentColor,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Draw Crosshair on Tap/Drag
            selectedIndex?.let { idx ->
                if (idx in points.indices) {
                    val pt = points[idx]
                    // Vertical Crosshair line
                    drawLine(
                        color = TextSecondary.copy(alpha = 0.8f),
                        start = Offset(pt.x, 0f),
                        end = Offset(pt.x, h),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )
                    // Horizontal Crosshair line
                    drawLine(
                        color = TextSecondary.copy(alpha = 0.5f),
                        start = Offset(0f, pt.y),
                        end = Offset(w, pt.y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                    // Circle Marker
                    drawCircle(
                        color = TerminalBackground,
                        radius = 6.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = accentColor,
                        radius = 4.5.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = pt
                    )
                }
            }
        }
    }
}
