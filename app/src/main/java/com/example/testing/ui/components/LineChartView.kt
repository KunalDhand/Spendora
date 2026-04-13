package com.example.testing.ui.components

import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.testing.data.local.DailyExpense
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter

import com.example.testing.utils.TimeFrame

@Composable
fun LineChartView(
    data: List<DailyExpense>,
    timeFrame: TimeFrame,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val secondaryColor = MaterialTheme.colorScheme.secondary.toArgb()
    
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f).toArgb()

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                description.isEnabled = false
                legend.apply {
                    isEnabled = true
                    this.textColor = textColor
                    form = com.github.mikephil.charting.components.Legend.LegendForm.CIRCLE
                }
                
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
                }
                
                axisRight.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(false)
                setScaleEnabled(false)
                
                animateXY(1000, 1000, com.github.mikephil.charting.animation.Easing.EaseInOutQuad)
                setExtraOffsets(0f, 10f, 0f, 10f)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp),
        update = { chart ->
            val entries = data.mapIndexed { index, item ->
                Entry(index.toFloat(), item.total.toFloat())
            }

            val dataSet = LineDataSet(entries, "Spending trend").apply {
                color = primaryColor
                setCircleColor(primaryColor)
                lineWidth = 3f
                circleRadius = 0f // No circles for a cleaner "modern fintech" look
                setDrawCircleHole(false)
                setDrawValues(false) // Cleaner look without values on chart
                
                setDrawFilled(true)
                // Using a gradient if possible or just soft opacity
                fillColor = primaryColor
                fillAlpha = 30
                
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setDrawHorizontalHighlightIndicator(false)
                highLightColor = secondaryColor
            }

            chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    val item = data.getOrNull(index) ?: return ""
                    
                    return try {
                        when (timeFrame) {
                            TimeFrame.WEEK -> item.date.substring(item.date.length - 2)
                            TimeFrame.MONTH -> item.date.substring(item.date.length - 2)
                            else -> item.date.substring(5, 7) // MM
                        }
                    } catch (e: Exception) {
                        item.date
                    }
                }
            }

            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    )
}
