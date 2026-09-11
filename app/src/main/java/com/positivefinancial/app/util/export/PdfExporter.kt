package com.positivefinancial.app.util.export

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.positivefinancial.app.data.local.dao.TransactionWithDetails
import com.positivefinancial.app.data.model.TransactionType
import com.positivefinancial.app.util.Formatters
import java.io.File
import java.io.FileOutputStream

/**
 * Renders a simple paginated PDF report using Android's built-in PdfDocument
 * API (Canvas drawing) so the app doesn't need a PDF library dependency.
 */
object PdfExporter {

    private const val PAGE_WIDTH = 595 // A4 at 72dpi
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 36f
    private const val ROW_HEIGHT = 20f

    private data class Column(val label: String, val width: Float)

    private val columns = listOf(
        Column("Date", 65f),
        Column("Type", 55f),
        Column("Detail", 205f),
        Column("Account", 100f),
        Column("Amount", 98f)
    )

    fun write(file: File, title: String, transactions: List<TransactionWithDetails>) {
        val document = PdfDocument()
        val titlePaint = Paint().apply { color = Color.BLACK; textSize = 18f; isFakeBoldText = true }
        val subtitlePaint = Paint().apply { color = Color.DKGRAY; textSize = 11f }
        val headerPaint = Paint().apply { color = Color.BLACK; textSize = 10f; isFakeBoldText = true }
        val rowPaint = Paint().apply { color = Color.BLACK; textSize = 9f }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 0.5f }

        val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        val colX = mutableListOf<Float>()
        var cursor = MARGIN
        columns.forEach { colX.add(cursor); cursor += it.width }

        var page: PdfDocument.Page? = null
        var canvas: Canvas? = null
        var y = MARGIN
        var pageNumber = 0

        fun drawHeaderRow() {
            columns.forEachIndexed { i, column -> canvas!!.drawText(column.label, colX[i], y, headerPaint) }
            y += 6f
            canvas!!.drawLine(MARGIN, y, (PAGE_WIDTH - MARGIN), y, linePaint)
            y += ROW_HEIGHT
        }

        fun startPage() {
            pageNumber++
            page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
            canvas = page!!.canvas
            y = MARGIN
            if (pageNumber == 1) {
                canvas!!.drawText("Positive Financial", MARGIN, y, titlePaint)
                y += 22f
                canvas!!.drawText(title, MARGIN, y, subtitlePaint)
                y += 16f
                canvas!!.drawText(
                    "Income: ${Formatters.currency(income)}   Expense: ${Formatters.currency(expense)}   " +
                        "Net: ${Formatters.currencySigned(income - expense)}",
                    MARGIN, y, subtitlePaint
                )
                y += 24f
            }
            drawHeaderRow()
        }

        startPage()

        transactions.forEach { tx ->
            if (y > PAGE_HEIGHT - MARGIN - ROW_HEIGHT) {
                document.finishPage(page!!)
                startPage()
            }
            val isTransfer = tx.type == TransactionType.TRANSFER
            val detail = if (isTransfer) {
                "${tx.fromAccountName ?: "?"} -> ${tx.toAccountName ?: "?"}"
            } else {
                tx.categoryName ?: "Uncategorized"
            }
            val account = if (isTransfer) "" else (tx.accountName ?: "")
            val amountText = when (tx.type) {
                TransactionType.EXPENSE -> "-${Formatters.currency(tx.amount)}"
                TransactionType.INCOME -> "+${Formatters.currency(tx.amount)}"
                TransactionType.TRANSFER -> Formatters.currency(tx.amount)
            }
            val values = listOf(Formatters.dateShort(tx.date), tx.type.name, detail, account, amountText)
            values.forEachIndexed { i, text ->
                canvas!!.drawText(truncate(text, columns[i].width, rowPaint), colX[i], y, rowPaint)
            }
            y += ROW_HEIGHT
        }

        document.finishPage(page!!)

        FileOutputStream(file).use { out -> document.writeTo(out) }
        document.close()
    }

    private fun truncate(text: String, maxWidth: Float, paint: Paint): String {
        if (paint.measureText(text) <= maxWidth) return text
        var end = text.length
        while (end > 0 && paint.measureText(text.substring(0, end) + "…") > maxWidth) end--
        return text.substring(0, end) + "…"
    }
}
