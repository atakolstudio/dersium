package com.dersium.core.database.dao

import androidx.room.*
import com.dersium.core.database.entity.ExpenseEntity
import com.dersium.core.database.entity.ExtraIncomeEntity
import com.dersium.core.database.entity.SeasonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialDao {

    // Extra Incomes
    @Query("SELECT * FROM extra_incomes WHERE seasonId = :seasonId ORDER BY date DESC")
    fun getAllExtraIncomes(seasonId: Long): Flow<List<ExtraIncomeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExtraIncome(income: ExtraIncomeEntity): Long

    @Update
    suspend fun updateExtraIncome(income: ExtraIncomeEntity)

    @Delete
    suspend fun deleteExtraIncome(income: ExtraIncomeEntity)

    // Expenses
    @Query("SELECT * FROM expenses WHERE seasonId = :seasonId ORDER BY date DESC")
    fun getAllExpenses(seasonId: Long): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    // Summary queries
    @Query("SELECT SUM(amount) FROM extra_incomes WHERE seasonId = :seasonId")
    fun getTotalExtraIncome(seasonId: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM expenses WHERE seasonId = :seasonId")
    fun getTotalExpenses(seasonId: Long): Flow<Double?>

    @Query("SELECT SUM(fee) FROM lessons WHERE seasonId = :seasonId AND paymentStatus = 'PAID'")
    fun getTotalPaidLessons(seasonId: Long): Flow<Double?>

    @Query("SELECT SUM(fee) FROM lessons WHERE seasonId = :seasonId AND paymentStatus = 'PENDING'")
    fun getTotalPendingLessons(seasonId: Long): Flow<Double?>
}

@Dao
interface SeasonDao {

    @Query("SELECT * FROM seasons ORDER BY startYear DESC")
    fun getAllSeasons(): Flow<List<SeasonEntity>>

    @Query("SELECT * FROM seasons WHERE isActive = 1 LIMIT 1")
    fun getActiveSeason(): Flow<SeasonEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeason(season: SeasonEntity): Long

    @Update
    suspend fun updateSeason(season: SeasonEntity)

    @Query("UPDATE seasons SET isActive = 0")
    suspend fun deactivateAllSeasons()

    @Query("UPDATE seasons SET isActive = 1 WHERE id = :seasonId")
    suspend fun activateSeason(seasonId: Long)
}
