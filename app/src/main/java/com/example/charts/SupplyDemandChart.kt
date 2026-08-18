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
import com.example.models.SupplyDemandOverview
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.max

@OptIn(ExperimentalTextApi::class)
@Composable
fun SupplyDemandChart(
    data: SupplyDemandOverview?,
    modifier: Modifier = Modifier
) {
    if (data == null || data.historyAndForecast.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Computing Supply/Demand forecast...", color = TextMuted)
        }
        return
    }

    val textMeasurer = rememberTextMeasurer()
    val points = data.historyAndForecast

    val allVals = points.flatMap { listOf(it.demandUnitsGpu, it.supplyCapacityUnitsGpu) }
    val maxVal = (allVals.maxOrNull() ?: 5000.0) * 1.1
    val minVal = (allVals.minOrNull() ?: 1000.0) * 0.9
    val range = max(1.0, maxVal - minVal)

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val n = points.size
        if (n < 2) return@Canvas

        val leftPadding = 12.dp.toPx()
        val rightPadding = 36.dp.toPx()
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
            val valY = maxVal - (i / 3.0) * range
            drawText(
                textMeasurer = textMeasurer,
                text = "${(valY / 1000.0).toInt()}M",
                topLeft = Offset(w - rightPadding + 4.dp.toPx(), y - 6.sp.toPx()),
                style = TextStyle(color = TextMuted, fontSize = 9.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
            )
        }

        // Paths for Demand & Supply
        val demandPath = Path()
        val supplyPath = Path()

        val demandCoords = points.mapIndexed { idx, pt ->
            val x = leftPadding + idx * stepX
            val y = topPadding + (1f - ((pt.demandUnitsGpu - minVal) / range).toFloat()) * usableH
            Offset(x, y)
        }

        val supplyCoords = points.mapIndexed { idx, pt ->
            val x = leftPadding + idx * stepX
            val y = topPadding + (1f - ((pt.supplyCapacityUnitsGpu - minVal) / range).toFloat()) * usableH
            Offset(x, y)
        }

        demandPath.moveTo(demandCoords.first().x, demandCoords.first().y)
        supplyPath.moveTo(supplyCoords.first().x, supplyCoords.first().y)

        for (i in 0 until n - 1) {
            val d0 = demandCoords[i]
            val d1 = demandCoords[i + 1]
            val dcx = (d0.x + d1.x) / 2f
            demandPath.cubicTo(dcx, d0.y, dcx, d1.y, d1.x, d1.y)

            val s0 = supplyCoords[i]
            val s1 = supplyCoords[i + 1]
            val scx = (s0.x + s1.x) / 2f
            supplyPath.cubicTo(scx, s0.y, scx, s1.y, s1.x, s1.y)
        }

        // Draw Demand Line (Purple/Amber)
        drawPath(
            path = demandPath,
            color = FinancialPurple,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw Supply Line (Nvidia Green)
        drawPath(
            path = supplyPath,
            color = NvidiaGreen,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw Points & X-Labels
        points.forEachIndexed { idx, pt ->
            val dCoord = demandCoords[idx]
            val sCoord = supplyCoords[idx]

            drawCircle(color = FinancialPurple, radius = 3.5.dp.toPx(), center = dCoord)
            drawCircle(color = NvidiaGreen, radius = 3.5.dp.toPx(), center = sCoord)

            drawText(
                textMeasurer = textMeasurer,
                text = pt.timeLabel,
                topLeft = Offset(dCoord.x - 12.dp.toPx(), topPadding + usableH + 6.dp.toPx()),
                style = TextStyle(color = TextSecondary, fontSize = 9.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
            )
        }
    }
}
