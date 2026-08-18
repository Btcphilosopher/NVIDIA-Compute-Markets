package com.example.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
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
import com.example.models.VolatilitySurfaceData
import com.example.presentation.VolSurfaceViewMode
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.*

@OptIn(ExperimentalTextApi::class)
@Composable
fun VolatilitySurfaceChart(
    surfaceData: VolatilitySurfaceData?,
    viewMode: VolSurfaceViewMode,
    modifier: Modifier = Modifier,
    currencySymbol: String = "$"
) {
    if (surfaceData == null || surfaceData.strikes.isEmpty() || surfaceData.maturitiesMonths.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Computing Volatility Surface...", color = TextMuted)
        }
        return
    }

    val textMeasurer = rememberTextMeasurer()

    // 3D rotation angles
    var rotX by remember { mutableStateOf(35f) }
    var rotY by remember { mutableStateOf(-45f) }

    val strikes = surfaceData.strikes
    val maturities = surfaceData.maturitiesMonths
    val grid = surfaceData.grid // [maturityIdx][strikeIdx]

    val allIvs = grid.flatten()
    val minIv = allIvs.minOrNull() ?: 20.0
    val maxIv = allIvs.maxOrNull() ?: 60.0
    val ivRange = max(0.1, maxIv - minIv)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(viewMode) {
                if (viewMode == VolSurfaceViewMode.SURFACE_3D) {
                    detectDragGestures { _, dragAmount ->
                        rotY = (rotY + dragAmount.x * 0.4f).coerceIn(-80f, -10f)
                        rotX = (rotX - dragAmount.y * 0.4f).coerceIn(15f, 65f)
                    }
                }
            }
    ) {
        val w = size.width
        val h = size.height
        if (w <= 0 || h <= 0) return@Canvas

        when (viewMode) {
            VolSurfaceViewMode.SURFACE_3D -> {
                // 3D Isometric / Orthographic projection
                val centerX = w * 0.48f
                val centerY = h * 0.65f
                val scale = min(w, h) * 0.52f

                val radX = rotX * (Math.PI / 180.0)
                val radY = rotY * (Math.PI / 180.0)

                // Project (x, y, z) where:
                // x: Strike (-1 to +1)
                // y: Maturity (-1 to +1)
                // z: IV height (0 to 1)
                fun project3D(sx: Double, my: Double, ivNorm: Double): Offset {
                    // Isometric rotation formula
                    val isoX = sx * cos(radY) - my * sin(radY)
                    val isoY = sx * sin(radY) * sin(radX) + my * cos(radY) * sin(radX) - (ivNorm * 1.3) * cos(radX)

                    val px = centerX + (isoX * scale).toFloat()
                    val py = centerY + (isoY * scale).toFloat()
                    return Offset(px, py)
                }

                val numM = maturities.size
                val numK = strikes.size

                // Precompute 3D projected vertices
                val projectedGrid = Array(numM) { mIdx ->
                    val my = (mIdx.toDouble() / (numM - 1).coerceAtLeast(1)) * 2.0 - 1.0
                    Array(numK) { kIdx ->
                        val sx = (kIdx.toDouble() / (numK - 1).coerceAtLeast(1)) * 2.0 - 1.0
                        val iv = grid[mIdx][kIdx]
                        val ivNorm = (iv - minIv) / ivRange
                        project3D(sx, my, ivNorm)
                    }
                }

                // Draw surface quads with gradient fill & wireframe
                for (m in 0 until numM - 1) {
                    for (k in 0 until numK - 1) {
                        val p00 = projectedGrid[m][k]
                        val p10 = projectedGrid[m + 1][k]
                        val p11 = projectedGrid[m + 1][k + 1]
                        val p01 = projectedGrid[m][k + 1]

                        val quadPath = Path().apply {
                            moveTo(p00.x, p00.y)
                            lineTo(p10.x, p10.y)
                            lineTo(p11.x, p11.y)
                            lineTo(p01.x, p01.y)
                            close()
                        }

                        val avgIv = (grid[m][k] + grid[m + 1][k] + grid[m + 1][k + 1] + grid[m][k + 1]) / 4.0
                        val normColor = ((avgIv - minIv) / ivRange).toFloat().coerceIn(0f, 1f)

                        // Color ramp from cyan (low vol) to nvidia green to amber/red (high vol)
                        val quadColor = when {
                            normColor < 0.35f -> lerp(FinancialCyan, NvidiaGreen, normColor / 0.35f)
                            normColor < 0.70f -> lerp(NvidiaGreen, FinancialAmber, (normColor - 0.35f) / 0.35f)
                            else -> lerp(FinancialAmber, FinancialRed, (normColor - 0.70f) / 0.30f)
                        }

                        drawPath(
                            path = quadPath,
                            color = quadColor.copy(alpha = 0.65f)
                        )

                        // Wireframe stroke
                        drawPath(
                            path = quadPath,
                            color = TerminalBorderHighlight.copy(alpha = 0.8f),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                }

                // Draw axis labels
                val strikeAxisStart = projectedGrid[0][0]
                val strikeAxisEnd = projectedGrid[0][numK - 1]
                drawText(
                    textMeasurer = textMeasurer,
                    text = "STRIKE ($currencySymbol)",
                    topLeft = Offset(strikeAxisEnd.x - 20.dp.toPx(), strikeAxisEnd.y + 12.dp.toPx()),
                    style = TextStyle(color = NvidiaGreenGlow, fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                )

                val maturityAxisEnd = projectedGrid[numM - 1][0]
                drawText(
                    textMeasurer = textMeasurer,
                    text = "EXPIRY (TENOR)",
                    topLeft = Offset(maturityAxisEnd.x - 30.dp.toPx(), maturityAxisEnd.y + 12.dp.toPx()),
                    style = TextStyle(color = FinancialCyan, fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                )
            }

            VolSurfaceViewMode.HEATMAP_2D -> {
                // 2D Contour Heatmap Grid
                val topPadding = 24.dp.toPx()
                val bottomPadding = 36.dp.toPx()
                val leftPadding = 48.dp.toPx()
                val rightPadding = 24.dp.toPx()

                val gridW = w - leftPadding - rightPadding
                val gridH = h - topPadding - bottomPadding

                val numM = maturities.size
                val numK = strikes.size
                val cellW = gridW / numK
                val cellH = gridH / numM

                for (m in 0 until numM) {
                    for (k in 0 until numK) {
                        val iv = grid[m][k]
                        val normColor = ((iv - minIv) / ivRange).toFloat().coerceIn(0f, 1f)

                        val cellColor = when {
                            normColor < 0.35f -> lerp(FinancialCyan, NvidiaGreen, normColor / 0.35f)
                            normColor < 0.70f -> lerp(NvidiaGreen, FinancialAmber, (normColor - 0.35f) / 0.35f)
                            else -> lerp(FinancialAmber, FinancialRed, (normColor - 0.70f) / 0.30f)
                        }

                        val cellX = leftPadding + k * cellW
                        val cellY = topPadding + m * cellH

                        drawRect(
                            color = cellColor.copy(alpha = 0.85f),
                            topLeft = Offset(cellX, cellY),
                            size = androidx.compose.ui.geometry.Size(cellW, cellH)
                        )
                        drawRect(
                            color = TerminalBackground,
                            topLeft = Offset(cellX, cellY),
                            size = androidx.compose.ui.geometry.Size(cellW, cellH),
                            style = Stroke(width = 1.dp.toPx())
                        )

                        // Draw IV text inside cell
                        val ivText = "${String.format(Locale.US, "%.1f", iv)}%"
                        val textLayout = textMeasurer.measure(
                            text = ivText,
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 9.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        )
                        drawText(
                            textLayoutResult = textLayout,
                            topLeft = Offset(cellX + (cellW - textLayout.size.width) / 2f, cellY + (cellH - textLayout.size.height) / 2f)
                        )
                    }

                    // Y Axis (Maturity label)
                    val mLabel = "${maturities[m].toInt()}M"
                    drawText(
                        textMeasurer = textMeasurer,
                        text = mLabel,
                        topLeft = Offset(8.dp.toPx(), topPadding + m * cellH + cellH / 3f),
                        style = TextStyle(color = TextSecondary, fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    )
                }

                // X Axis (Strikes)
                for (k in 0 until numK) {
                    val kLabel = "$currencySymbol${String.format(Locale.US, "%.2f", strikes[k])}"
                    drawText(
                        textMeasurer = textMeasurer,
                        text = kLabel,
                        topLeft = Offset(leftPadding + k * cellW + 2.dp.toPx(), topPadding + gridH + 8.dp.toPx()),
                        style = TextStyle(color = TextMuted, fontSize = 8.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    )
                }
            }

            VolSurfaceViewMode.STRIKE_SMILE_SLICE -> {
                // Multi-curve Volatility Smile across maturities
                val leftPadding = 20.dp.toPx()
                val rightPadding = 40.dp.toPx()
                val topPadding = 20.dp.toPx()
                val bottomPadding = 30.dp.toPx()

                val usableW = w - leftPadding - rightPadding
                val usableH = h - topPadding - bottomPadding

                val numK = strikes.size
                val stepX = usableW / (numK - 1).coerceAtLeast(1)

                val colors = listOf(FinancialCyan, NvidiaGreen, FinancialAmber, FinancialPurple, FinancialRed, Color.White, FinancialBlue)

                maturities.forEachIndexed { mIdx, mMonths ->
                    val path = Path()
                    val curveColor = colors[mIdx % colors.size]

                    for (k in 0 until numK) {
                        val iv = grid[mIdx][k]
                        val normX = leftPadding + k * stepX
                        val normY = topPadding + (1f - ((iv - minIv) / ivRange).toFloat()) * usableH

                        if (k == 0) path.moveTo(normX, normY) else path.lineTo(normX, normY)

                        drawCircle(color = curveColor, radius = 3.dp.toPx(), center = Offset(normX, normY))
                    }

                    drawPath(
                        path = path,
                        color = curveColor,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Curve Legend Label
                    val legendText = "${mMonths.toInt()}M"
                    drawText(
                        textMeasurer = textMeasurer,
                        text = legendText,
                        topLeft = Offset(w - rightPadding + 4.dp.toPx(), topPadding + mIdx * 14.dp.toPx()),
                        style = TextStyle(color = curveColor, fontSize = 9.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    )
                }
            }
        }
    }
}
