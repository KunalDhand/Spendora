package com.example.testing.ui.components

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.testing.data.local.NetData
import com.example.testing.ui.theme.getExpenseColor
import com.example.testing.ui.theme.getIncomeColor
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter

@Composable
fun NetLineChart(
    data: List<NetData>,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f).toArgb()
    
    val incomeGreen = getIncomeColor().toArgb()
    val expenseRed = getExpenseColor().toArgb()

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                description.isEnabled = false
                legend.isEnabled = false
                
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    this.textColor = textColor
                    granularity = 1f
                    setLabelCount(5, false)
                    setDrawAxisLine(true)
                    axisLineColor = gridColor
                }
                
                axisLeft.apply {
                    this.textColor = textColor
                    setDrawGridLines(true)
                    this.gridColor = gridColor
                    setDrawAxisLine(false)
                    setLabelCount(5, true)
                    // Ensure zero line is visible
                    setDrawZeroLine(true)
                    zeroLineColor = gridColor
                }
                
                axisRight.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(false)
                setScaleEnabled(false)
                
                animateX(1200, com.github.mikephil.charting.animation.Easing.EaseInOutQuad)
                setExtraOffsets(0f, 10f, 0f, 10f)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        update = { chart ->
            val entries = data.mapIndexed { index, item ->
                Entry(index.toFloat(), item.net.toFloat())
            }

            val dataSet = LineDataSet(entries, "Net Income").apply {
                lineWidth = 3f
                circleRadius = 4f
                setDrawCircleHole(true)
                circleHoleColor = surfaceColor
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setDrawHorizontalHighlightIndicator(false)
                
                val colors = data.map { if (it.net >= 0) incomeGreen else expenseRed }
                setCircleColors(colors)
            }

            // Apply LinearGradient to the line paint to split green/red at zero
            chart.post {
                val transformer = chart.getTransformer(com.github.mikephil.charting.components.YAxis.AxisDependency.LEFT)
                val zeroPos = transformer.getPixelForValues(0f, 0f)
                val zeroY = zeroPos.y.toFloat()
                val height = chart.height.toFloat()
                
                if (height > 0) {
                    // Normalize zero position for the gradient
                    val zeroRatio = (zeroY / height).coerceIn(0f, 1f)
                    
                    val linePaint = chart.renderer.paintRender
                    linePaint.shader = LinearGradient(
                        0f, 0f, 0f, height,
                        intArrayOf(incomeGreen, incomeGreen, expenseRed, expenseRed),
                        floatArrayOf(0f, zeroRatio, zeroRatio, 1f),
                        Shader.TileMode.CLAMP
                    )
                    
                    // Note: Filling with multi-color is complex in MPAndroidChart.
                    // This will color the line perfectly.
                    chart.invalidate()
                }
            }

            chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    if (index >= 0 && index < data.size) {
                        val date = data[index].date
                        return try {
                            if (date.contains("-W")) {
                                date.substring(date.indexOf("-W") + 1)
                            } else if (date.count { it == '-' } == 2) {
                                date.substring(5) // MM-DD
                            } else {
                                date
                            }
                        } catch (e: Exception) {
                            date
                        }
                    }
                    return ""
                }
            }

            chart.data = LineData(dataSet)
            
            if (entries.isNotEmpty()) {
                chart.setVisibleXRangeMaximum(7f)
                chart.moveViewToX(entries.size.toFloat())
            }
            
            chart.invalidate()
        }
    )
}
