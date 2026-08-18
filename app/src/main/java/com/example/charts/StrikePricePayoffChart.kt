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
import com.example.models.OptionType
import com.example.models.StrikeOptionContract
import com.example.presentation.StrikeChartMode
import com.example.ui.theme.*
import com.example.utils.FinancialMath
import java.util.Locale
import kotlin.math.*

@OptIn(ExperimentalTextApi::class)
@Composable
fun StrikePricePayoffChart(
    contract: StrikeOptionContract?,
    mode: StrikeChartMode,
    modifier: Modifier = Modifier,
    currencySymbol: String = "$"
) {
    if (contract == null) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Computing options surface...", color = TextMuted)
        }
        return
    }

    val textMeasurer = rememberTextMeasurer()
    var inspectedXPercent by remember { mutableStateOf<Float?>(null) }

    val spot = contract.currentSpotPrice
    val strike = contract.strikePrice
    val premium = contract.estimatedPricePerGpuHour
    val isCall = contract.optionType == OptionType.CALL_OPTION

    // Generate strike domain: from 50% spot to 160% spot
    val minK = spot * 0.5
    val maxK = spot * 1.6
    val kRange = maxK - minK
    val steps = 60

    // Compute curve points based on selected mode
    val dataPoints = remember(contract, mode) {
        (0..steps).map { i ->
            val k = minK + (i.toDouble() / steps) * kRange
            val value = when (mode) {
                StrikeChartMode.PRICE_PAYOFF -> {
                    // Payoff at expiry: Call = max(0, S - K) - premium, or current Black-76 value
                    if (isCall) max(0.0, k - strike) - premium else max(0.0, strike - k) - premium
                }
                StrikeChartMode.IMPLIED_VOL -> {
                    val moneyness = k / spot
                    contract.impliedVolatility + 12.0 * (moneyness - 1.0).pow(2) - 4.5 * (moneyness - 1.0)
                }
                StrikeChartMode.PROBABILITY -> {
                    val stdDev = spot * (contract.impliedVolatility / 100.0) * sqrt(contract.maturityDays / 365.0)
                    val z = (k - spot) / max(0.01, stdDev)
                    FinancialMath.stdNormalPdf(z) * 100.0
                }
                StrikeChartMode.GREEKS -> {
                    val (_, greeks) = FinancialMath.calculateBlackOption(
                        type = contract.optionType,
                        F = spot,
                        K = k,
                        T = contract.maturityDays / 365.0,
                        r = 0.045,
                        sigma = contract.impliedVolatility / 100.0
                    )
                    greeks.delta * 100.0 // Delta %
                }
            }
            Pair(k, value)
        }
    }

    val yValues = dataPoints.map { it.second }
    val minY = yValues.minOrNull() ?: 0.0
    val maxY = yValues.maxOrNull() ?: 1.0
    val yRange = max(0.01, maxY - minY)
    val paddedMinY = if (mode == StrikeChartMode.PRICE_PAYOFF) min(minY, -premium * 1.5) else minY * 0.9
    val paddedMaxY = maxY * 1.1
    val paddedYRange = max(0.01, paddedMaxY - paddedMinY)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(dataPoints) {
                detectDragGestures(
                    onDragStart = { offset -> inspectedXPercent = (offset.x / size.width).coerceIn(0f, 1f) },
                    onDrag = { change, _ -> inspectedXPercent = (change.position.x / size.width).coerceIn(0f, 1f) },
                    onDragEnd = { inspectedXPercent = null },
                    onDragCancel = { inspectedXPercent = null }
                )
            }
            .pointerInput(dataPoints) {
                detectTapGestures(
                    onPress = { offset ->
                        inspectedXPercent = (offset.x / size.width).coerceIn(0f, 1f)
                        tryAwaitRelease()
                        inspectedXPercent = null
                    }
                )
            }
    ) {
        val w = size.width
        val h = size.height
        if (w <= 0 || h <= 0) return@Canvas

        val topPadding = 20.dp.toPx()
        val bottomPadding = 30.dp.toPx()
        val leftPadding = 12.dp.toPx()
        val rightPadding = 48.dp.toPx()

        val usableW = w - leftPadding - rightPadding
        val usableH = h - topPadding - bottomPadding

        // Zero P&L line if in Payoff mode
        if (mode == StrikeChartMode.PRICE_PAYOFF) {
            val zeroNormY = ((paddedMaxY - 0.0) / paddedYRange).toFloat()
            val zeroY = topPadding + zeroNormY * usableH
            drawLine(
                color = TextMuted.copy(alpha = 0.6f),
                start = Offset(leftPadding, zeroY),
                end = Offset(w - rightPadding, zeroY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
        }

        // Draw horizontal grid lines
        val gridCount = 4
        for (i in 0..gridCount) {
            val y = topPadding + usableH * (i.toFloat() / gridCount)
            drawLine(
                color = TerminalBorder.copy(alpha = 0.4f),
                start = Offset(leftPadding, y),
                end = Offset(w - rightPadding, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
            )

            val valY = paddedMaxY - (i.toDouble() / gridCount) * paddedYRange
            val labelY = when (mode) {
                StrikeChartMode.PRICE_PAYOFF -> "$currencySymbol${String.format(Locale.US, "%.2f", valY)}"
                StrikeChartMode.IMPLIED_VOL -> "${String.format(Locale.US, "%.1f", valY)}%"
                StrikeChartMode.PROBABILITY -> "${String.format(Locale.US, "%.1f", valY)}"
                StrikeChartMode.GREEKS -> "Δ ${String.format(Locale.US, "%.0f", valY)}%"
            }
            drawText(
                textMeasurer = textMeasurer,
                text = labelY,
                topLeft = Offset(w - rightPadding + 4.dp.toPx(), y - 7.sp.toPx()),
                style = TextStyle(
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            )
        }

        // Draw Strike vertical marker line
        val strikeNormX = ((strike - minK) / kRange).toFloat().coerceIn(0f, 1f)
        val strikeScreenX = leftPadding + strikeNormX * usableW
        drawLine(
            color = FinancialAmber.copy(alpha = 0.8f),
            start = Offset(strikeScreenX, topPadding),
            end = Offset(strikeScreenX, topPadding + usableH),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
        )
        drawText(
            textMeasurer = textMeasurer,
            text = "STRIKE: $currencySymbol${String.format(Locale.US, "%.2f", strike)}",
            topLeft = Offset(strikeScreenX + 4.dp.toPx(), topPadding + 4.dp.toPx()),
            style = TextStyle(
                color = FinancialAmber,
                fontSize = 9.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        )

        // Draw Spot vertical marker line
        val spotNormX = ((spot - minK) / kRange).toFloat().coerceIn(0f, 1f)
        val spotScreenX = leftPadding + spotNormX * usableW
        drawLine(
            color = NvidiaGreenGlow.copy(alpha = 0.8f),
            start = Offset(spotScreenX, topPadding),
            end = Offset(spotScreenX, topPadding + usableH),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
        )
        drawText(
            textMeasurer = textMeasurer,
            text = "SPOT: $currencySymbol${String.format(Locale.US, "%.2f", spot)}",
            topLeft = Offset(spotScreenX + 4.dp.toPx(), topPadding + 18.dp.toPx()),
            style = TextStyle(
                color = NvidiaGreenGlow,
                fontSize = 9.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        )

        // Screen points
        val screenPoints = dataPoints.mapIndexed { idx, pair ->
            val normX = (idx.toFloat() / steps)
            val normY = ((paddedMaxY - pair.second) / paddedYRange).toFloat()
            Offset(leftPadding + normX * usableW, topPadding + normY * usableH)
        }

        // Draw Line Path
        val path = Path()
        path.moveTo(screenPoints.first().x, screenPoints.first().y)
        for (i in 0 until screenPoints.size - 1) {
            val p0 = screenPoints[i]
            val p1 = screenPoints[i + 1]
            val cx = (p0.x + p1.x) / 2f
            path.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
        }

        val lineColor = when (mode) {
            StrikeChartMode.PRICE_PAYOFF -> NvidiaGreen
            StrikeChartMode.IMPLIED_VOL -> FinancialPurple
            StrikeChartMode.PROBABILITY -> FinancialCyan
            StrikeChartMode.GREEKS -> FinancialAmber
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Draw Interactive Crosshair
        inspectedXPercent?.let { pct ->
            val inspX = leftPadding + pct * usableW
            val inspK = minK + pct * kRange
            val idx = (pct * steps).toInt().coerceIn(0, dataPoints.size - 1)
            val pt = screenPoints[idx]
            val value = dataPoints[idx].second

            // Crosshair vertical
            drawLine(
                color = Color.White.copy(alpha = 0.7f),
                start = Offset(inspX, topPadding),
                end = Offset(inspX, topPadding + usableH),
                strokeWidth = 1.2.dp.toPx()
            )

            // Inspection Point
            drawCircle(
                color = TerminalBackground,
                radius = 6.dp.toPx(),
                center = Offset(inspX, pt.y)
            )
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = Offset(inspX, pt.y)
            )

            // Tooltip Text
            val tooltipText = "K: $currencySymbol${String.format(Locale.US, "%.2f", inspK)} | Val: ${String.format(Locale.US, "%.3f", value)}"
            val textLayout = textMeasurer.measure(
                text = tooltipText,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 10.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            )
            val tooltipX = (inspX - textLayout.size.width / 2f).coerceIn(leftPadding, w - rightPadding - textLayout.size.width)
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(tooltipX, topPadding + 32.dp.toPx())
            )
        }
    }
}
