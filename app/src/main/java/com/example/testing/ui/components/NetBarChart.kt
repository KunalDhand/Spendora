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
import com.example.testing.ui.theme.getExpenseColor
import com.example.testing.ui.theme.getIncomeColor
import com.example.testing.data.local.NetData
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter

@Composable
fun NetBarChart(
    data: List<NetData>,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val incomeGreen = getIncomeColor().toArgb()
    val expenseRed = getExpenseColor().toArgb()
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f).toArgb()

    AndroidView(
        factory = { context ->
            BarChart(context).apply {
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
                }
                
                axisRight.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(false)
                setScaleEnabled(false)
                
                animateY(1200, com.github.mikephil.charting.animation.Easing.EaseInOutQuad)
                setExtraOffsets(0f, 10f, 0f, 10f)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        update = { chart ->
            val entries = data.mapIndexed { index, item ->
                BarEntry(index.toFloat(), item.net.toFloat())
            }

            val dataSet = BarDataSet(entries, "Net Results").apply {
                colors = data.map { if (it.net >= 0) incomeGreen else expenseRed }
                valueTextColor = textColor
                valueTextSize = 10f
                setDrawValues(false) // Cleaner look without values on bars
                
                highLightAlpha = 50
                setDrawIcons(false)
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

            chart.data = BarData(dataSet).apply {
                barWidth = 0.5f // Thinner bars for a more modern look
            }
            
            // Adjust viewport for horizontal scrolling if there's many items
            if (entries.isNotEmpty()) {
                chart.setVisibleXRangeMaximum(7f)
                chart.moveViewToX(entries.size.toFloat())
            }
            
            chart.invalidate()
        }
    )
}
