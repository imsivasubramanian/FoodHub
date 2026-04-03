package com.example.foodhub

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.Date

object PdfGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f

    fun generateAndSharePdf(context: Context) {
        val dbHelper = DatabaseHelper(context)
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()
        val tablePaint = Paint()

        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas: Canvas = page.canvas
        var yPos = 50f

        // --- Title ---
        titlePaint.textSize = 24f
        titlePaint.isFakeBoldText = true
        titlePaint.color = Color.rgb(255, 87, 34) // FoodHub Orange
        canvas.drawText("FoodHub User Data Report", MARGIN, yPos, titlePaint)
        yPos += 30f

        paint.textSize = 12f
        paint.color = Color.GRAY
        canvas.drawText("Generated on: ${Date()}", MARGIN, yPos, paint)
        yPos += 40f

        // --- User Profile Section ---
        yPos = drawSectionTitle(canvas, "1. USER PROFILE", yPos)
        val profile = dbHelper.getUserProfile()
        paint.color = Color.BLACK
        paint.textSize = 14f
        if (profile != null) {
            val details = listOf(
                "Name: ${profile["name"]}",
                "Email: ${profile["email"]}",
                "Phone: ${profile["phone"]}",
                "Address: ${profile["address"]}"
            )
            for (line in details) {
                canvas.drawText(line, MARGIN + 20, yPos, paint)
                yPos += 20f
            }
        } else {
            canvas.drawText("No profile data found.", MARGIN + 20, yPos, paint)
            yPos += 20f
        }
        yPos += 20f

        // --- Registered Users Table ---
        yPos = drawSectionTitle(canvas, "2. REGISTERED USERS", yPos)
        val usersCursor = dbHelper.getAllUsers()
        val userHeaders = arrayOf("ID", "Name", "Email")
        val userColumnWidths = floatArrayOf(50f, 200f, 250f)
        yPos = drawTable(canvas, yPos, userHeaders, userColumnWidths, usersCursor, arrayOf("id", "name", "email"))
        usersCursor.close()
        yPos += 30f

        // Check if we need a new page
        if (yPos > 600) {
            pdfDocument.finishPage(page)
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            yPos = 50f
        }

        // --- Food Menu Table ---
        yPos = drawSectionTitle(canvas, "3. FOOD MENU", yPos)
        val foodCursor = dbHelper.getAllFood()
        val foodHeaders = arrayOf("ID", "Item Name", "Price")
        val foodColumnWidths = floatArrayOf(50f, 300f, 150f)
        yPos = drawTable(canvas, yPos, foodHeaders, foodColumnWidths, foodCursor, arrayOf("id", "name", "price"))
        foodCursor.close()
        yPos += 30f

        // --- Cart Items Table ---
        yPos = drawSectionTitle(canvas, "4. CURRENT CART", yPos)
        val cartCursor = dbHelper.getCartItems()
        val cartHeaders = arrayOf("ID", "Product", "Quantity")
        val cartColumnWidths = floatArrayOf(50f, 300f, 150f)
        yPos = drawTable(canvas, yPos, cartHeaders, cartColumnWidths, cartCursor, arrayOf("id", "food_name", "quantity"))
        cartCursor.close()

        pdfDocument.finishPage(page)

        // Save and Share
        val pdfFile = File(context.cacheDir, "FoodHub_Data_Report.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(pdfFile))
            pdfDocument.close()
            sharePdf(context, pdfFile)
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun drawSectionTitle(canvas: Canvas, title: String, y: Float): Float {
        val paint = Paint().apply {
            textSize = 16f
            isFakeBoldText = true
            color = Color.BLACK
        }
        canvas.drawText(title, MARGIN, y, paint)
        canvas.drawLine(MARGIN, y + 5, PAGE_WIDTH - MARGIN, y + 5, paint)
        return y + 25f
    }

    private fun drawTable(
        canvas: Canvas,
        startY: Float,
        headers: Array<String>,
        columnWidths: FloatArray,
        cursor: android.database.Cursor,
        columnNames: Array<String>
    ): Float {
        var y = startY
        val paint = Paint().apply { textSize = 12f; color = Color.BLACK }
        val headerPaint = Paint().apply { 
            textSize = 12f; 
            isFakeBoldText = true; 
            color = Color.WHITE 
        }
        val bgPaint = Paint().apply { color = Color.DKGRAY }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }

        // Draw Header Background
        canvas.drawRect(MARGIN, y - 15, PAGE_WIDTH - MARGIN, y + 10, bgPaint)

        // Draw Headers
        var x = MARGIN + 10
        for (i in headers.indices) {
            canvas.drawText(headers[i], x, y, headerPaint)
            x += columnWidths[i]
        }
        y += 25f

        // Draw Rows
        if (cursor.moveToFirst()) {
            do {
                x = MARGIN + 10
                for (i in columnNames.indices) {
                    val value = try {
                        cursor.getString(cursor.getColumnIndexOrThrow(columnNames[i]))
                    } catch (e: Exception) {
                        "N/A"
                    }
                    canvas.drawText(value ?: "", x, y, paint)
                    x += columnWidths[i]
                }
                canvas.drawLine(MARGIN, y + 5, PAGE_WIDTH - MARGIN, y + 5, linePaint)
                y += 25f
                if (y > PAGE_HEIGHT - MARGIN) break // Basic overflow prevention for this simple example
            } while (cursor.moveToNext())
        } else {
            canvas.drawText("No data available in this table.", MARGIN + 20, y, paint)
            y += 25f
        }

        return y + 10f
    }

    private fun sharePdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "com.example.foodhub.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Open Data Report"))
    }
}
