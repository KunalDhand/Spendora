package com.example.testing.utils

import android.graphics.*
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import java.util.*

data class ExportTransaction(
    val type: String,
    val amount: Double,
    val categoryName: String,
    val walletName: String,
    val date: String
)

object PdfExporter {

    fun export(
        file: File,
        transactions: List<ExportTransaction>,
        dateRangeText: String
    ) {
        val document = PdfDocument()
        val paint = Paint()

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        var y = 50f

        // Calculate Summary
        val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }

        // Title
        paint.textSize = 24f
        paint.isFakeBoldText = true
        paint.color = Color.BLACK
        canvas.drawText("Expense Tracker Report", 50f, y, paint)
        
        y += 35f
        paint.textSize = 14f
        paint.isFakeBoldText = false
        paint.color = Color.GRAY
        canvas.drawText("Date Range: $dateRangeText", 50f, y, paint)
        
        y += 40f
        paint.isFakeBoldText = true
        paint.color = Color.BLACK
        paint.textSize = 16f
        canvas.drawText("Financial Summary", 50f, y, paint)
        
        y += 25f
        paint.isFakeBoldText = false
        paint.textSize = 14f
        canvas.drawText("Total Income:  ₹${"%.2f".format(totalIncome)}", 50f, y, paint)
        y += 20f
        canvas.drawText("Total Expense: ₹${"%.2f".format(totalExpense)}", 50f, y, paint)
        y += 20f
        paint.isFakeBoldText = true
        val netBalance = totalIncome - totalExpense
        canvas.drawText("Net Balance:   ₹${"%.2f".format(netBalance)}", 50f, y, paint)
        
        y += 40f
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("Transaction Details", 50f, y, paint)
        
        y += 10f
        paint.strokeWidth = 1.5f
        canvas.drawLine(50f, y, 550f, y, paint)
        
        y += 25f
        paint.textSize = 12f
        
        // TASK 53 — Create Table Header
        paint.isFakeBoldText = true
        canvas.drawText("Date", 50f, y, paint)
        canvas.drawText("Category", 150f, y, paint)
        canvas.drawText("Wallet", 300f, y, paint)
        canvas.drawText("Amount", 500f, y, paint)
        
        paint.isFakeBoldText = false
        y += 20f
        
        paint.strokeWidth = 0.5f
        canvas.drawLine(50f, y - 5f, 550f, y - 5f, paint)
        
        transactions.forEachIndexed { index, tx ->
            if (y > 750f) { // More conservative page break limit
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = 50f
                
                // Re-draw Table Header on new page
                paint.textSize = 12f
                paint.isFakeBoldText = true
                paint.color = Color.BLACK
                canvas.drawText("Date", 50f, y, paint)
                canvas.drawText("Category", 150f, y, paint)
                canvas.drawText("Wallet", 300f, y, paint)
                canvas.drawText("Amount", 500f, y, paint)
                
                paint.isFakeBoldText = false
                y += 20f
                paint.strokeWidth = 0.5f
                canvas.drawLine(50f, y - 5f, 550f, y - 5f, paint)
            }

            // TASK 56 — Alternate row color for readability
            if (index % 2 == 0) {
                paint.color = Color.parseColor("#424242") // Dark Gray for alternate rows
            } else {
                paint.color = Color.BLACK
            }
            
            canvas.drawText(tx.date, 50f, y, paint)
            canvas.drawText(tx.categoryName, 150f, y, paint)
            canvas.drawText(tx.walletName, 300f, y, paint)
            
            // Format amount
            val formattedAmount = String.format("%.2f", tx.amount)
            canvas.drawText("₹$formattedAmount", 500f, y, paint)

            y += 20f
        }

        document.finishPage(page)

        try {
            document.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            document.close()
        }
    }
}
