package com.dersium.core.data.repository

import com.dersium.core.data.mapper.toDomain
import com.dersium.core.data.mapper.toEntity
import com.dersium.core.database.dao.FinancialDao
import com.dersium.core.database.dao.SeasonDao
import com.dersium.core.domain.model.*
import com.dersium.core.domain.repository.FinancialRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class FinancialRepositoryImpl @Inject constructor(
    private val financialDao: FinancialDao,
    private val seasonDao: SeasonDao,
) : FinancialRepository {

    override fun getAllExtraIncomes(seasonId: Long): Flow<List<ExtraIncome>> =
        financialDao.getAllExtraIncomes(seasonId).map { it.map { e -> e.toDomain() } }

    override fun getAllExpenses(seasonId: Long): Flow<List<Expense>> =
        financialDao.getAllExpenses(seasonId).map { it.map { e -> e.toDomain() } }

    override fun getFinancialSummary(seasonId: Long): Flow<FinancialSummary> =
        combine(
            financialDao.getTotalPaidLessons(seasonId),
            financialDao.getTotalPendingLessons(seasonId),
            financialDao.getTotalExtraIncome(seasonId),
            financialDao.getTotalExpenses(seasonId),
        ) { paid, pending, extraIncome, expenses ->
            val paidAmount = paid ?: 0.0
            val pendingAmount = pending ?: 0.0
            FinancialSummary(
                totalLessonIncome = paidAmount + pendingAmount,
                totalExtraIncome = extraIncome ?: 0.0,
                totalExpenses = expenses ?: 0.0,
                pendingAmount = pendingAmount,
                paidAmount = paidAmount,
            )
        }

    override fun getAllSeasons(): Flow<List<Season>> =
        seasonDao.getAllSeasons().map { it.map { e -> e.toDomain() } }

    override fun getActiveSeason(): Flow<Season?> =
        seasonDao.getActiveSeason().map { it?.toDomain() }

    override suspend fun insertExtraIncome(income: ExtraIncome): Long =
        financialDao.insertExtraIncome(income.toEntity())

    override suspend fun updateExtraIncome(income: ExtraIncome) =
        financialDao.updateExtraIncome(income.toEntity())

    override suspend fun deleteExtraIncome(income: ExtraIncome) =
        financialDao.deleteExtraIncome(income.toEntity())

    override suspend fun insertExpense(expense: Expense): Long =
        financialDao.insertExpense(expense.toEntity())

    override suspend fun updateExpense(expense: Expense) =
        financialDao.updateExpense(expense.toEntity())

    override suspend fun deleteExpense(expense: Expense) =
        financialDao.deleteExpense(expense.toEntity())

    override suspend fun insertSeason(season: Season): Long =
        seasonDao.insertSeason(season.toEntity())

    override suspend fun updateSeason(season: Season) =
        seasonDao.updateSeason(season.toEntity())
}
