package com.example.testing.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Int): TransactionEntity?

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("""
        SELECT COALESCE(SUM(
            CASE 
                WHEN UPPER(type) = 'INCOME' THEN amount 
                WHEN UPPER(type) = 'EXPENSE' THEN -amount 
                ELSE 0 
            END
        ), 0.0) 
        FROM transactions 
        WHERE walletId = :walletId
    """)
    fun getWalletBalance(walletId: Int): Flow<Double?>

    @Query("""
        SELECT COALESCE(SUM(
            CASE 
                WHEN UPPER(type) = 'INCOME' THEN amount 
                WHEN UPPER(type) = 'EXPENSE' THEN -amount 
                ELSE 0 
            END
        ), 0.0) 
        FROM transactions
    """)
    fun getTotalBalance(): Flow<Double?>

    // Net Queries
    @Query("""
        SELECT COALESCE(SUM(
            CASE 
                WHEN UPPER(type) = 'INCOME' THEN amount
                WHEN UPPER(type) = 'EXPENSE' THEN -amount
                ELSE 0
            END
        ), 0.0) 
        FROM transactions
        WHERE date(datetime(timestamp / 1000, 'unixepoch', 'localtime')) = date('now', 'localtime')
    """)
    fun getTodayNet(): Flow<Double?>

    @Query("""
        SELECT COALESCE(SUM(
            CASE 
                WHEN UPPER(type) = 'INCOME' THEN amount
                WHEN UPPER(type) = 'EXPENSE' THEN -amount
                ELSE 0
            END
        ), 0.0)
        FROM transactions
        WHERE strftime('%Y-%m', datetime(timestamp / 1000, 'unixepoch', 'localtime')) = 
              strftime('%Y-%m', 'now', 'localtime')
    """)
    fun getMonthlyNetSummary(): Flow<Double?>

    // Daily Queries
    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions
        WHERE UPPER(type) = 'INCOME'
        AND date(datetime(timestamp / 1000, 'unixepoch', 'localtime')) = date('now', 'localtime')
    """)
    fun getTodayIncome(): Flow<Double?>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions
        WHERE UPPER(type) = 'EXPENSE'
        AND date(datetime(timestamp / 1000, 'unixepoch', 'localtime')) = date('now', 'localtime')
    """)
    fun getTodayExpense(): Flow<Double?>

    // Monthly Queries
    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions
        WHERE UPPER(type) = 'INCOME'
        AND strftime('%Y-%m', datetime(timestamp / 1000, 'unixepoch', 'localtime')) = 
              strftime('%Y-%m', 'now', 'localtime')
    """)
    fun getMonthlyIncome(): Flow<Double?>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions
        WHERE UPPER(type) = 'EXPENSE'
        AND strftime('%Y-%m', datetime(timestamp / 1000, 'unixepoch', 'localtime')) = 
              strftime('%Y-%m', 'now', 'localtime')
    """)
    fun getMonthlyExpense(): Flow<Double?>

    @Query("""
        SELECT categoryId, SUM(amount) as total 
        FROM transactions 
        WHERE UPPER(type) = 'EXPENSE'
        GROUP BY categoryId
    """)
    fun getCategorySummary(): Flow<List<CategorySummary>>

    @Query("""
        SELECT type, SUM(amount) as sum
        FROM transactions 
        GROUP BY type
    """)
    fun debugTotals(): Flow<List<TypeSum>>

    @Query("""
        SELECT 
        date(datetime(timestamp / 1000, 'unixepoch', 'localtime')) as date,
        SUM(amount) as total
        FROM transactions
        WHERE UPPER(type) = 'EXPENSE'
        AND timestamp BETWEEN :start AND :end
        GROUP BY date
        ORDER BY date ASC
    """)
    fun getDailyExpensesByRange(start: Long, end: Long): Flow<List<DailyExpense>>

    @Query("""
        SELECT * FROM transactions
        WHERE timestamp BETWEEN :start AND :end
        ORDER BY timestamp DESC
    """)
    suspend fun getTransactionsByDateRange(
        start: Long,
        end: Long
    ): List<TransactionEntity>

    // Net Income (PnL) Queries
    @Query("""
        SELECT 
        date(datetime(timestamp / 1000, 'unixepoch', 'localtime')) as date,
        SUM(
            CASE 
                WHEN UPPER(type) = 'INCOME' THEN amount
                WHEN UPPER(type) = 'EXPENSE' THEN -amount
                ELSE 0
            END
        ) as net
        FROM transactions
        GROUP BY date
        ORDER BY date ASC
    """)
    fun getDailyNet(): Flow<List<NetData>>

    @Query("""
        SELECT 
        strftime('%Y-W%W', datetime(timestamp / 1000, 'unixepoch', 'localtime')) as date,
        SUM(
            CASE 
                WHEN UPPER(type) = 'INCOME' THEN amount
                WHEN UPPER(type) = 'EXPENSE' THEN -amount
                ELSE 0
            END
        ) as net
        FROM transactions
        GROUP BY date
        ORDER BY date ASC
    """)
    fun getWeeklyNet(): Flow<List<NetData>>

    @Query("""
        SELECT COUNT(*) FROM transactions 
        WHERE walletId = :walletId OR toWalletId = :walletId
    """)
    suspend fun getTransactionCountForWallet(walletId: Int): Int

    @Query("""
        SELECT 
        strftime('%Y-%m', datetime(timestamp / 1000, 'unixepoch', 'localtime')) as date,
        SUM(
            CASE 
                WHEN UPPER(type) = 'INCOME' THEN amount
                WHEN UPPER(type) = 'EXPENSE' THEN -amount
                ELSE 0
            END
        ) as net
        FROM transactions
        GROUP BY date
        ORDER BY date ASC
    """)
    fun getMonthlyNet(): Flow<List<NetData>>

    @Query("SELECT * FROM transactions WHERE isCredit = 1 ORDER BY timestamp DESC")
    fun getCreditTransactions(): Flow<List<TransactionEntity>>

    @Query("""
        SELECT COALESCE(SUM(
            CASE 
                WHEN UPPER(type) = 'INCOME' THEN amount 
                WHEN UPPER(type) = 'EXPENSE' THEN -amount 
                ELSE 0 
            END
        ), 0.0)
        FROM transactions
        WHERE personId = :personId AND isCredit = 1
    """)
    suspend fun getPersonCreditSum(personId: Int): Double
}

data class NetData(
    val date: String,
    val net: Double
)

data class DailyExpense(
    val date: String,
    val total: Double
)

data class TypeSum(
    val type: String,
    val sum: Double
)
