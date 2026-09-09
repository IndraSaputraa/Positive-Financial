package com.positivefinancial.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.positivefinancial.app.data.local.entity.TransactionEntity
import com.positivefinancial.app.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

private const val DETAILS_SELECT = """
    SELECT t.id AS id, t.type AS type, t.amount AS amount, t.note AS note, t.date AS date,
        t.accountId AS accountId, a.name AS accountName, a.iconKey AS accountIconKey, a.colorHex AS accountColorHex,
        t.categoryId AS categoryId, c.name AS categoryName, c.iconKey AS categoryIconKey, c.colorHex AS categoryColorHex,
        t.fromAccountId AS fromAccountId, fa.name AS fromAccountName,
        t.toAccountId AS toAccountId, ta.name AS toAccountName
    FROM transactions t
    LEFT JOIN accounts a ON t.accountId = a.id
    LEFT JOIN categories c ON t.categoryId = c.id
    LEFT JOIN accounts fa ON t.fromAccountId = fa.id
    LEFT JOIN accounts ta ON t.toAccountId = ta.id
"""

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query(
        DETAILS_SELECT + """
        ORDER BY t.date DESC, t.id DESC
        LIMIT :limit
        """
    )
    fun getRecent(limit: Int): Flow<List<TransactionWithDetails>>

    @Query(
        DETAILS_SELECT + """
        WHERE (:type IS NULL OR t.type = :type)
        AND (:accountId IS NULL OR t.accountId = :accountId OR t.fromAccountId = :accountId OR t.toAccountId = :accountId)
        AND (:categoryId IS NULL OR t.categoryId = :categoryId)
        AND (:startDate IS NULL OR t.date >= :startDate)
        AND (:endDate IS NULL OR t.date <= :endDate)
        AND (
            :query IS NULL OR :query = ''
            OR t.note LIKE '%' || :query || '%'
            OR a.name LIKE '%' || :query || '%'
            OR c.name LIKE '%' || :query || '%'
            OR fa.name LIKE '%' || :query || '%'
            OR ta.name LIKE '%' || :query || '%'
        )
        ORDER BY t.date DESC, t.id DESC
        """
    )
    fun getFiltered(
        type: TransactionType?,
        accountId: Long?,
        categoryId: Long?,
        startDate: Long?,
        endDate: Long?,
        query: String?
    ): Flow<List<TransactionWithDetails>>

    @Query(
        DETAILS_SELECT + """
        WHERE t.type = 'TRANSFER' AND t.toAccountId = :cardAccountId
        ORDER BY t.date DESC, t.id DESC
        """
    )
    fun getCardPaymentHistory(cardAccountId: Long): Flow<List<TransactionWithDetails>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'INCOME' AND date BETWEEN :start AND :end")
    fun getTotalIncome(start: Long, end: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE' AND date BETWEEN :start AND :end")
    fun getTotalExpense(start: Long, end: Long): Flow<Long>

    @Query(
        """
        SELECT c.id AS categoryId, c.name AS categoryName, c.iconKey AS iconKey, c.colorHex AS colorHex,
            SUM(t.amount) AS total
        FROM transactions t
        LEFT JOIN categories c ON t.categoryId = c.id
        WHERE t.type = 'EXPENSE' AND t.date BETWEEN :start AND :end
        GROUP BY t.categoryId
        ORDER BY total DESC
        """
    )
    fun getExpenseBreakdown(start: Long, end: Long): Flow<List<CategorySpendingRow>>

    @Query(
        """
        SELECT strftime('%Y-%m', date / 1000, 'unixepoch') AS yearMonth,
            SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) AS income,
            SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) AS expense
        FROM transactions
        WHERE date >= :since
        GROUP BY yearMonth
        ORDER BY yearMonth ASC
        """
    )
    fun getMonthlySummary(since: Long): Flow<List<MonthlySummaryRow>>
}
