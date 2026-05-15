package com.dersium.feature.financial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dersium.core.domain.model.*
import com.dersium.core.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class FinancialTab { INCOME, EXPENSE }

data class FinancialUiState(
    val tab: FinancialTab = FinancialTab.INCOME,
    val extraIncomes: List<ExtraIncome> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val totalExtraIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val activeSeasonId: Long = 1L,
    val currency: String = "₺",
)

data class AddFinancialItemState(
    val title: String = "",
    val amount: String = "",
    val notes: String = "",
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FinancialViewModel @Inject constructor(
    private val financialRepository: FinancialRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _tab = MutableStateFlow(FinancialTab.INCOME)

    val uiState: StateFlow<FinancialUiState> = userPreferencesRepository.userPreferences
        .flatMapLatest { prefs ->
            combine(
                financialRepository.getAllExtraIncomes(prefs.activeSeasonId),
                financialRepository.getAllExpenses(prefs.activeSeasonId),
                _tab,
            ) { incomes, expenses, tab ->
                FinancialUiState(
                    tab = tab,
                    extraIncomes = incomes,
                    expenses = expenses,
                    totalExtraIncome = incomes.sumOf { it.amount },
                    totalExpense = expenses.sumOf { it.amount },
                    activeSeasonId = prefs.activeSeasonId,
                    currency = prefs.currency,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FinancialUiState())

    fun setTab(t: FinancialTab) { _tab.value = t }

    fun addIncome(title: String, amount: Double, notes: String) {
        viewModelScope.launch {
            financialRepository.insertExtraIncome(
                ExtraIncome(title = title, amount = amount, notes = notes, date = LocalDate.now(), seasonId = uiState.value.activeSeasonId)
            )
        }
    }

    fun addExpense(title: String, amount: Double, notes: String) {
        viewModelScope.launch {
            financialRepository.insertExpense(
                Expense(title = title, amount = amount, notes = notes, date = LocalDate.now(), seasonId = uiState.value.activeSeasonId)
            )
        }
    }

    fun deleteIncome(income: ExtraIncome) { viewModelScope.launch { financialRepository.deleteExtraIncome(income) } }
    fun deleteExpense(expense: Expense) { viewModelScope.launch { financialRepository.deleteExpense(expense) } }
}
