package com.example.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.SimulationResult
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.max

@OptIn(ExperimentalTextApi::class)
@Composable
fun ScenarioComparisonChart(
    result: SimulationResult?,
    modifier: Modifier = Modifier,
    currencySymbol: String = "$"
) {
    if (result == null || result.forwardCurveComparison.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Ready to run scenario simulation", color = TextMuted)
        }
        return
    }

    val textMeasurer = rememberTextMeasurer()
    val points = result.forwardCurveComparison

    val allPrices = points.flatMap { listOf(it.baselinePrice, it.simulatedPrice) }
    val maxP = (allPrices.maxOrNull() ?: 4.0) * 1.12
    val minP = (allPrices.minOrNull() ?: 1.0) * 0.88
    val pRange = max(0.01, maxP - minP)

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val n = points.size
        if (n < 2) return@Canvas

        val leftPadding = 12.dp.toPx()
        val rightPadding = 42.dp.toPx()
        val topPadding = 20.dp.toPx()
        val bottomPadding = 32.dp.toPx()

        val usableW = w - leftPadding - rightPadding
        val usableH = h - topPadding - bottomPadding
        val stepX = usableW / (n - 1)

        // Draw horizontal grid
        for (i in 0..3) {
            val y = topPadding + usableH * (i / 3f)
            drawLine(
                color = TerminalBorder.copy(alpha = 0.4f),
                start = Offset(leftPadding, y),
                end = Offset(w - rightPadding, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
            )
            val valY = maxP - (i / 3.0) * pRange
            drawText(
                textMeasurer = textMeasurer,
                text = "$currencySymbol${String.format(Locale.US, "%.2f", valY)}",
                topLeft = Offset(w - rightPadding + 4.dp.toPx(), y - 6.sp.toPx()),
                style = TextStyle(color = TextMuted, fontSize = 9.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
            )
        }

        val basePath = Path()
        val simPath = Path()

        val baseCoords = points.mapIndexed { idx, pt ->
            val x = leftPadding + idx * stepX
            val y = topPadding + (1f - ((pt.baselinePrice - minP) / pRange).toFloat()) * usableH
            Offset(x, y)
        }

        val simCoords = points.mapIndexed { idx, pt ->
            val x = leftPadding + idx * stepX
            val y = topPadding + (1f - ((pt.simulatedPrice - minP) / pRange).toFloat()) * usableH
            Offset(x, y)
        }

        basePath.moveTo(baseCoords.first().x, baseCoords.first().y)
        simPath.moveTo(simCoords.first().x, simCoords.first().y)

        for (i in 0 until n - 1) {
            val b0 = baseCoords[i]
            val b1 = baseCoords[i + 1]
            val bcx = (b0.x + b1.x) / 2f
            basePath.cubicTo(bcx, b0.y, bcx, b1.y, b1.x, b1.y)

            val s0 = simCoords[i]
            val s1 = simCoords[i + 1]
            val scx = (s0.x + s1.x) / 2f
            simPath.cubicTo(scx, s0.y, scx, s1.y, s1.x, s1.y)
        }

        // Draw Baseline (Dashed Muted Line)
        drawPath(
            path = basePath,
            color = TextMuted,
            style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f))
        )

        // Draw Simulated Outcome (Solid Glow Line)
        val simColor = if (result.spotPriceChangePercent >= 0) FinancialAmber else NvidiaGreenGlow
        drawPath(
            path = simPath,
            color = simColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Points & Labels
        points.forEachIndexed { idx, pt ->
            val sCoord = simCoords[idx]
            drawCircle(color = simColor, radius = 4.dp.toPx(), center = sCoord)

            drawText(
                textMeasurer = textMeasurer,
                text = pt.maturityLabel,
                topLeft = Offset(sCoord.x - 10.dp.toPx(), topPadding + usableH + 6.dp.toPx()),
                style = TextStyle(color = TextSecondary, fontSize = 9.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
            )
        }
    }
}
