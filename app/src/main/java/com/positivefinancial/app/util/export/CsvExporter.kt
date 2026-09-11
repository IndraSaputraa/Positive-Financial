package com.positivefinancial.app.util.export

import com.positivefinancial.app.data.local.dao.TransactionWithDetails
import com.positivefinancial.app.data.model.TransactionType
import com.positivefinancial.app.util.Formatters
import java.io.File
import java.io.OutputStreamWriter

/**
 * Writes a CSV that opens cleanly in Excel, Google Sheets, or Numbers.
 * Amounts are plain signed integers (no "Rp" prefix, no thousands
 * separators) so spreadsheet SUM formulas work on them directly.
 */
object CsvExporter {

    fun write(file: File, title: String, transactions: List<TransactionWithDetails>) {
        val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        OutputStreamWriter(file.outputStream(), Charsets.UTF_8).use { writer ->
            writer.write("﻿") // BOM so Excel detects UTF-8 correctly
            writer.write("Positive Financial - $title\n")
            writer.write("Date,Type,Category,Account,From,To,Note,Amount (IDR)\n")

            transactions.forEach { tx ->
                val isTransfer = tx.type == TransactionType.TRANSFER
                val amount = if (tx.type == TransactionType.EXPENSE) -tx.amount else tx.amount
                val row = listOf(
                    Formatters.date(tx.date),
                    tx.type.name,
                    if (isTransfer) "" else (tx.categoryName ?: ""),
                    if (isTransfer) "" else (tx.accountName ?: ""),
                    if (isTransfer) (tx.fromAccountName ?: "") else "",
                    if (isTransfer) (tx.toAccountName ?: "") else "",
                    tx.note,
                    amount.toString()
                )
                writer.write(row.joinToString(",") { escape(it) })
                writer.write("\n")
            }

            writer.write("\n")
            writer.write("${escape("Total Income")},,,,,,,${income}\n")
            writer.write("${escape("Total Expense")},,,,,,,${-expense}\n")
            writer.write("${escape("Net")},,,,,,,${income - expense}\n")
        }
    }

    private fun escape(value: String): String =
        if (value.any { it == ',' || it == '"' || it == '\n' }) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
}
