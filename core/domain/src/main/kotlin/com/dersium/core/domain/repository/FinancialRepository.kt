package com.dersium.core.domain.repository

import com.dersium.core.domain.model.Expense
import com.dersium.core.domain.model.ExtraIncome
import com.dersium.core.domain.model.FinancialSummary
import com.dersium.core.domain.model.Season
import kotlinx.coroutines.flow.Flow

interface FinancialRepository {
    fun getAllExtraIncomes(seasonId: Long): Flow<List<ExtraIncome>>
    fun getAllExpenses(seasonId: Long): Flow<List<Expense>>
    fun getFinancialSummary(seasonId: Long): Flow<FinancialSummary>
    fun getAllSeasons(): Flow<List<Season>>
    fun getActiveSeason(): Flow<Season?>
    suspend fun insertExtraIncome(income: ExtraIncome): Long
    suspend fun updateExtraIncome(income: ExtraIncome)
    suspend fun deleteExtraIncome(income: ExtraIncome)
    suspend fun insertExpense(expense: Expense): Long
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)
    suspend fun insertSeason(season: Season): Long
    suspend fun updateSeason(season: Season)
}
