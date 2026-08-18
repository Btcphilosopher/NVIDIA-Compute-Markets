package com.example.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.AppLanguage
import com.example.models.LevelizedCostOfCompute
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalTextApi::class)
@Composable
fun CostBreakdownChart(
    costData: LevelizedCostOfCompute?,
    currentLanguage: AppLanguage,
    modifier: Modifier = Modifier,
    currencySymbol: String = "$"
) {
    if (costData == null) return

    val textMeasurer = rememberTextMeasurer()
    val components = costData.components
    val totalCost = costData.totalRealHourlyCost
    val spot = costData.marketSpotPrice

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 0 || h <= 0) return@Canvas

        val barH = 28.dp.toPx()
        val topPadding = 10.dp.toPx()
        val leftPadding = 4.dp.toPx()
        val rightPadding = 4.dp.toPx()
        val usableW = w - leftPadding - rightPadding

        // 1. Stacked LCOC Bar
        var currentX = leftPadding
        components.forEach { comp ->
            val compW = ((comp.costPerHour / totalCost) * usableW).toFloat()
            drawRoundRect(
                color = Color(comp.colorHex),
                topLeft = Offset(currentX, topPadding),
                size = Size(compW, barH),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
            currentX += compW
        }

        // Draw component legend items below
        val legendTop = topPadding + barH + 18.dp.toPx()
        val colW = usableW / 2f
        val rowH = 22.dp.toPx()

        components.forEachIndexed { idx, comp ->
            val col = idx % 2
            val row = idx / 2
            val lx = leftPadding + col * colW
            val ly = legendTop + row * rowH

            // Legend color box
            drawRoundRect(
                color = Color(comp.colorHex),
                topLeft = Offset(lx, ly + 2.dp.toPx()),
                size = Size(10.dp.toPx(), 10.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )

            val name = when (currentLanguage) {
                AppLanguage.ENGLISH -> comp.nameEn
                AppLanguage.TAIWANESE_HOKKIEN -> comp.nameHokkien
                AppLanguage.CHINESE_MANDARIN -> comp.nameZh
            }
            val textStr = "$name: $currencySymbol${String.format(Locale.US, "%.2f", comp.costPerHour)} (${String.format(Locale.US, "%.0f", comp.percentageOfTotal)}%)"

            val textLayout = textMeasurer.measure(
                text = textStr,
                style = TextStyle(
                    color = TextSecondary,
                    fontSize = 9.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            )
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(lx + 14.dp.toPx(), ly)
            )
        }
    }
}
