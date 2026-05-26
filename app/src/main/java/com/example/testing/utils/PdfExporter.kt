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
    val date: String,
    val time: String,
    val personName: String?,
    val note: String?,
    val tags: String,
    val isCredit: Boolean
)

object PdfExporter {

    fun export(
        file: File,
        transactions: List<ExportTransaction>,
        dateRangeText: String
    ) {
        val pageWidth = 842 // Landscape A4
        val pageHeight = 595
        val document = PdfDocument()
        val paint = Paint()
        val margin = 40f

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        // Column Definitions
        val colDate = margin
        val colEntity = margin + 85f
        val colCategory = margin + 185f
        val colWallet = margin + 300f
        val colTags = margin + 460f
        val colTypeStatus = margin + 630f
        val colAmount = pageWidth - margin

        val colWidthEntity = 95f
        val colWidthCategory = 110f
        val colWidthWallet = 150f
        val colWidthTags = 160f

        fun drawHeaders(canvas: Canvas, currentY: Float) {
            paint.color = Color.parseColor("#1A237E")
            paint.textSize = 10f
            paint.isFakeBoldText = true
            
            canvas.drawText("Date/Time", colDate, currentY, paint)
            canvas.drawText("Entity", colEntity, currentY, paint)
            canvas.drawText("Category", colCategory, currentY, paint)
            canvas.drawText("Wallet", colWallet, currentY, paint)
            canvas.drawText("Tags", colTags, currentY, paint)
            canvas.drawText("Type/Status", colTypeStatus, currentY, paint)
            
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Amount", colAmount, currentY, paint)
            paint.textAlign = Paint.Align.LEFT
            
            val lineY = currentY + 8f
            paint.strokeWidth = 1.2f
            canvas.drawLine(margin, lineY, pageWidth - margin, lineY, paint)
        }

        // Summary Calculations
        val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val netBalance = totalIncome - totalExpense

        // Header Section
        var y = 60f
        paint.color = Color.BLACK
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("Spendora Financial Report", margin, y, paint)
        
        y += 22f
        paint.textSize = 11f
        paint.isFakeBoldText = false
        paint.color = Color.GRAY
        canvas.drawText("Period: $dateRangeText", margin, y, paint)
        canvas.drawText("Generated: ${java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())}", pageWidth - margin - 180f, y, paint)

        // Summary Box
        y += 25f
        paint.color = Color.parseColor("#F5F5F5")
        canvas.drawRoundRect(margin, y, pageWidth - margin, y + 55f, 10f, 10f, paint)
        
        y += 32f
        paint.textSize = 11f
        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#2E7D32")
        canvas.drawText("Total Income: ₹${"%.2f".format(totalIncome)}", margin + 20f, y, paint)
        
        paint.color = Color.parseColor("#C62828")
        canvas.drawText("Total Expense: ₹${"%.2f".format(totalExpense)}", margin + 250f, y, paint)
        
        paint.color = if (netBalance >= 0) Color.parseColor("#2E7D32") else Color.parseColor("#C62828")
        canvas.drawText("Net Balance: ₹${"%.2f".format(netBalance)}", margin + 480f, y, paint)

        y += 65f
        drawHeaders(canvas, y)
        y += 30f

        transactions.forEachIndexed { index, tx ->
            val mergedTypeStatus = when {
                tx.type == "TRANSFER" -> "Transfer"
                tx.isCredit && tx.type == "INCOME" -> "Borrow"
                tx.isCredit && tx.type == "EXPENSE" -> "Lent"
                else -> tx.type.lowercase().replaceFirstChar { it.uppercase() }
            }

            paint.textSize = 9f
            val entityLines = wrapText(tx.personName ?: "-", colWidthEntity, paint)
            val categoryLines = wrapText(tx.categoryName, colWidthCategory, paint)
            val walletLines = wrapText(tx.walletName, colWidthWallet, paint)
            
            // Tag Processing
            val tagList = if (tx.tags.isNotBlank() && tx.tags != "-") tx.tags.split(",").map { it.trim() } else emptyList()
            val tagRows = mutableListOf<List<TagInfo>>()
            if (tagList.isNotEmpty()) {
                var currentTagRow = mutableListOf<TagInfo>()
                var currentX = 0f
                tagList.forEach { tag ->
                    val textWidth = paint.measureText(tag)
                    val chipWidth = textWidth + 10f
                    if (currentX + chipWidth > colWidthTags) {
                        tagRows.add(currentTagRow)
                        currentTagRow = mutableListOf()
                        currentX = 0f
                    }
                    currentTagRow.add(TagInfo(tag, chipWidth))
                    currentX += chipWidth + 4f
                }
                if (currentTagRow.isNotEmpty()) tagRows.add(currentTagRow)
            }

            val mainRowLines = maxOf(entityLines.size, categoryLines.size, walletLines.size, tagRows.size.coerceAtLeast(1))
            
            val notePaint = Paint(paint).apply { textSize = 8.5f }
            val noteContentWidth = pageWidth - margin - colEntity
            val noteLines = if (!tx.note.isNullOrBlank()) {
                wrapText(tx.note, noteContentWidth, notePaint)
            } else emptyList()

            // Calculate height for this transaction
            var txHeight = mainRowLines * 13f + 10f
            if (noteLines.isNotEmpty()) txHeight += (noteLines.size * 13f)
            txHeight += 6f // extra padding at bottom

            // Check for page break
            if (y + txHeight > pageHeight - margin) {
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = 50f
                drawHeaders(canvas, y)
                y += 30f
            }

            // Alternating background
            if (index % 2 != 0) {
                paint.color = Color.parseColor("#F8F9FA")
                canvas.drawRect(margin, y - 16f, pageWidth - margin, y + txHeight - 16f, paint)
            }

            // Main Row Content
            paint.color = Color.BLACK
            paint.textSize = 9f
            canvas.drawText(tx.date, colDate, y - 2f, paint)
            paint.color = Color.GRAY
            paint.textSize = 8.5f
            canvas.drawText(tx.time, colDate, y + 10f, paint)
            
            paint.color = Color.BLACK
            paint.textSize = 9f
            entityLines.forEachIndexed { i, line -> canvas.drawText(line, colEntity, y + (i * 13f), paint) }
            categoryLines.forEachIndexed { i, line -> canvas.drawText(line, colCategory, y + (i * 13f), paint) }
            walletLines.forEachIndexed { i, line -> canvas.drawText(line, colWallet, y + (i * 13f), paint) }

            // Draw Tags as square chips in their column
            if (tagRows.isNotEmpty()) {
                tagRows.forEachIndexed { rowIndex, row ->
                    var startX = colTags
                    val rowY = y + (rowIndex * 13f)
                    row.forEach { tagInfo ->
                        paint.color = Color.parseColor("#EEEEEE")
                        canvas.drawRoundRect(startX, rowY - 8f, startX + tagInfo.width, rowY + 5f, 3f, 3f, paint)
                        paint.color = Color.parseColor("#455A64")
                        paint.textSize = 7.5f
                        canvas.drawText(tagInfo.name, startX + 5f, rowY + 1f, paint)
                        startX += tagInfo.width + 4f
                    }
                }
            } else {
                paint.color = Color.GRAY
                paint.textSize = 9f
                canvas.drawText("-", colTags, y, paint)
            }

            paint.textSize = 9f
            paint.color = when(mergedTypeStatus) {
                "Borrow", "Lent" -> Color.parseColor("#E65100")
                "Income" -> Color.parseColor("#2E7D32")
                "Expense" -> Color.parseColor("#C62828")
                else -> Color.parseColor("#1565C0")
            }
            canvas.drawText(mergedTypeStatus, colTypeStatus, y, paint)

            paint.color = Color.BLACK
            paint.isFakeBoldText = true
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("₹${"%.2f".format(tx.amount)}", colAmount, y, paint)
            paint.textAlign = Paint.Align.LEFT
            paint.isFakeBoldText = false

            var currentYOffset = y + (mainRowLines * 13f) 

            // Notes (Still below main row if present)
            if (noteLines.isNotEmpty()) {
                paint.textSize = 8.5f
                paint.color = Color.DKGRAY
                noteLines.forEach { line ->
                    canvas.drawText(line, colEntity, currentYOffset, paint)
                    currentYOffset += 13f
                }
            }

            y += txHeight
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

    private fun wrapText(text: String, width: Float, paint: Paint): List<String> {
        val result = mutableListOf<String>()
        val paragraphs = text.split("\n")
        for (para in paragraphs) {
            val words = para.split(" ")
            var currentLine = StringBuilder()
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "${currentLine} $word"
                if (paint.measureText(testLine) <= width) {
                    currentLine.append(if (currentLine.isEmpty()) word else " $word")
                } else {
                    if (currentLine.isNotEmpty()) {
                        result.add(currentLine.toString())
                        currentLine = StringBuilder(word)
                    } else {
                        var part = word
                        while(paint.measureText(part) > width && part.isNotEmpty()) {
                            var charCount = 1
                            while(charCount <= part.length && paint.measureText(part.substring(0, charCount)) <= width) {
                                charCount++
                            }
                            result.add(part.substring(0, charCount - 1))
                            part = part.substring(charCount - 1)
                        }
                        currentLine = StringBuilder(part)
                    }
                }
            }
            if (currentLine.isNotEmpty()) result.add(currentLine.toString())
        }
        return result
    }

    private data class TagInfo(val name: String, val width: Float)
}
