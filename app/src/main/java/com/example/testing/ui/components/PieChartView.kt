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
import com.example.testing.ui.viewmodel.CategoryUI
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate

@Composable
fun PieChartView(data: List<CategoryUI>, modifier: Modifier = Modifier) {
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val holeColor = android.graphics.Color.TRANSPARENT // More premium transparent look
    val centerTextColor = MaterialTheme.colorScheme.onSurface.toArgb()
    
    val chartColors = listOf(
        MaterialTheme.colorScheme.primary.toArgb(),
        MaterialTheme.colorScheme.secondary.toArgb(),
        MaterialTheme.colorScheme.tertiary.toArgb(),
        MaterialTheme.colorScheme.error.copy(alpha = 0.7f).toArgb(),
        MaterialTheme.colorScheme.primaryContainer.toArgb(),
        MaterialTheme.colorScheme.secondaryContainer.toArgb(),
    )

    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                description.isEnabled = false
                isDrawHoleEnabled = true
                setHoleColor(holeColor)
                setTransparentCircleColor(android.graphics.Color.TRANSPARENT)
                setEntryLabelColor(textColor)
                setEntryLabelTextSize(0f) // Hide labels on slices for cleaner look
                
                setUsePercentValues(true)
                centerText = "Distribution"
                setCenterTextSize(14f)
                setCenterTextColor(centerTextColor)
                setDrawCenterText(true)
                
                legend.apply {
                    isEnabled = true
                    this.textColor = textColor
                    horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
                    verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
                    orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
                    setDrawInside(false)
                    yEntrySpace = 4f
                    form = com.github.mikephil.charting.components.Legend.LegendForm.CIRCLE
                }

                animateY(1400, com.github.mikephil.charting.animation.Easing.EaseInOutQuad)
                setExtraOffsets(5f, 5f, 5f, 5f)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(350.dp),
        update = { chart ->
            val entries = data.map {
                PieEntry(it.total.toFloat(), it.name)
            }

            val dataSet = PieDataSet(entries, "").apply {
                colors = chartColors
                valueTextColor = textColor
                valueTextSize = 11f
                sliceSpace = 4f
                selectionShift = 8f
                
                // Value labels outside for cleaner look
                xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                valueLineColor = textColor
                valueLinePart1Length = 0.4f
                valueLinePart2Length = 0.4f
            }

            val pieData = PieData(dataSet).apply {
                setValueFormatter(PercentFormatter(chart))
            }
            
            chart.data = pieData
            chart.invalidate()
        }
    )
}
