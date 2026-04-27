package com.example.testing.ui.components

import android.graphics.*
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider
import com.github.mikephil.charting.renderer.LineChartRenderer
import com.github.mikephil.charting.utils.ViewPortHandler

class NetLineChartRenderer(
    val incomeColor: Int,
    val expenseColor: Int,
    chart: LineDataProvider,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : LineChartRenderer(chart, animator, viewPortHandler) {

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f // Matches the lineWidth set in dataSet
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    override fun drawLinear(c: Canvas, dataSet: com.github.mikephil.charting.interfaces.datasets.ILineDataSet) {
        // We override the default drawing to split the line and fill at the zero-axis
        val trans = mChart.getTransformer(dataSet.axisDependency)
        val phaseY = mAnimator.phaseY
        
        val entryCount = dataSet.entryCount
        if (entryCount < 2) return

        val mLinePath = Path()
        val mFillPath = Path()

        // Calculate the pixel position of the zero line
        val zeroY = trans.getPixelForValues(0f, 0f).y.toFloat()

        for (i in 0 until entryCount - 1) {
            val start = dataSet.getEntryForIndex(i)
            val end = dataSet.getEntryForIndex(i + 1)

            val startPix = trans.getPixelForValues(start.x, start.y * phaseY)
            val endPix = trans.getPixelForValues(end.x, end.y * phaseY)

            val x1 = startPix.x.toFloat()
            val y1 = startPix.y.toFloat()
            val x2 = endPix.x.toFloat()
            val y2 = endPix.y.toFloat()

            // Draw line segment with appropriate color
            // Simple logic: if segment is mostly positive, green; if mostly negative, red.
            // For a perfect split, we'd find the intersection with zeroY.
            
            if (y1 <= zeroY && y2 <= zeroY) {
                // Both points above zero (pixel Y is smaller)
                linePaint.color = incomeColor
                c.drawLine(x1, y1, x2, y2, linePaint)
            } else if (y1 > zeroY && y2 > zeroY) {
                // Both points below zero
                linePaint.color = expenseColor
                c.drawLine(x1, y1, x2, y2, linePaint)
            } else {
                // Segment crosses zero - split it
                val t = (zeroY - y1) / (y2 - y1)
                val xi = x1 + t * (x2 - x1)
                
                linePaint.color = if (y1 <= zeroY) incomeColor else expenseColor
                c.drawLine(x1, y1, xi, zeroY, linePaint)
                
                linePaint.color = if (y2 <= zeroY) incomeColor else expenseColor
                c.drawLine(xi, zeroY, x2, y2, linePaint)
            }
        }
        
        // Note: This is a simplified per-segment renderer. 
        // For CUBIC_BEZIER it's much harder to split perfectly.
        // For now, let's stick to LINEAR for perfect zero-crossing colors or 
        // use a Shader for the whole line.
    }
    
    // Using a Shader is actually more robust for MPAndroidChart
}
