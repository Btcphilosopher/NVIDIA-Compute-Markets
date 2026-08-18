package com.example.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.ForwardCurveData
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.max

@OptIn(ExperimentalTextApi::class)
@Composable
fun ForwardCurveChart(
    curveData: ForwardCurveData?,
    modifier: Modifier = Modifier,
    currencySymbol: String = "$"
) {
    if (curveData == null || curveData.points.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No forward term structure loaded", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val textMeasurer = rememberTextMeasurer()
    val points = curveData.points
    val prices = points.map { it.price }
    val minPrice = (prices.minOrNull() ?: 1.0) * 0.92
    val maxPrice = (prices.maxOrNull() ?: 4.0) * 1.08
    val priceRange = max(0.01, maxPrice - minPrice)

    // Animation transition when switching chips
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 600),
        label = "curve_anim"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val n = points.size
        if (n < 2) return@Canvas

        val leftPadding = 12.dp.toPx()
        val rightPadding = 24.dp.toPx()
        val topPadding = 24.dp.toPx()
        val bottomPadding = 36.dp.toPx()

        val usableW = w - leftPadding - rightPadding
        val usableH = h - topPadding - bottomPadding
        val stepX = usableW / (n - 1)

        // Draw horizontal grid lines
        val gridCount = 4
        for (i in 0..gridCount) {
            val y = topPadding + usableH * (i.toFloat() / gridCount)
            drawLine(
                color = TerminalBorder.copy(alpha = 0.5f),
                start = Offset(leftPadding, y),
                end = Offset(w - rightPadding, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )

            val priceVal = maxPrice - (i.toDouble() / gridCount) * priceRange
            val priceLabel = "$currencySymbol${String.format(Locale.US, "%.2f", priceVal)}"
            drawText(
                textMeasurer = textMeasurer,
                text = priceLabel,
                topLeft = Offset(w - rightPadding + 4.dp.toPx(), y - 8.sp.toPx()),
                style = TextStyle(
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            )
        }

        // Compute screen coordinates for curve points
        val screenCoords = points.mapIndexed { idx, pt ->
            val x = leftPadding + idx * stepX
            val normalizedY = ((maxPrice - pt.price) / priceRange).toFloat()
            val y = topPadding + normalizedY * usableH
            Offset(x, y)
        }

        // Draw Area Under Curve (Contango/Backwardation gradient)
        val areaPath = Path()
        val linePath = Path()

        areaPath.moveTo(screenCoords.first().x, topPadding + usableH)
        areaPath.lineTo(screenCoords.first().x, screenCoords.first().y)
        linePath.moveTo(screenCoords.first().x, screenCoords.first().y)

        for (i in 0 until screenCoords.size - 1) {
            val p0 = screenCoords[i]
            val p1 = screenCoords[i + 1]
            val cx = (p0.x + p1.x) / 2f
            linePath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
            areaPath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
        }

        areaPath.lineTo(screenCoords.last().x, topPadding + usableH)
        areaPath.close()

        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    FinancialCyan.copy(alpha = 0.35f * animProgress),
                    FinancialCyan.copy(alpha = 0.05f * animProgress),
                    Color.Transparent
                ),
                startY = topPadding,
                endY = topPadding + usableH
            )
        )

        // Draw Forward Curve Line
        drawPath(
            path = linePath,
            color = FinancialCyan,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Draw Point Markers and Maturity Labels
        points.forEachIndexed { idx, pt ->
            val coord = screenCoords[idx]

            // Node Circle
            drawCircle(
                color = TerminalBackground,
                radius = 5.5.dp.toPx(),
                center = coord
            )
            drawCircle(
                color = FinancialCyan,
                radius = 4.dp.toPx(),
                center = coord
            )
            drawCircle(
                color = Color.White,
                radius = 1.5.dp.toPx(),
                center = coord
            )

            // Price badge above node
            val priceStr = "$currencySymbol${String.format(Locale.US, "%.2f", pt.price)}"
            val textLayout = textMeasurer.measure(
                text = priceStr,
                style = TextStyle(
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            )
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(coord.x - textLayout.size.width / 2f, coord.y - 20.dp.toPx())
            )

            // Maturity X-Axis Label below
            val maturityLayout = textMeasurer.measure(
                text = pt.maturityCode,
                style = TextStyle(
                    color = if (idx == 0) NvidiaGreenGlow else TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = if (idx == 0) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                )
            )
            drawText(
                textLayoutResult = maturityLayout,
                topLeft = Offset(coord.x - maturityLayout.size.width / 2f, topPadding + usableH + 8.dp.toPx())
            )
        }
    }
}
