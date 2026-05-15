package com.dersium.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dersium.core.domain.model.*
import com.dersium.core.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val studentCount: Int = 0,
    val todayLessons: Int = 0,
    val recentLessons: List<Lesson> = emptyList(),
    val paidAmount: Double = 0.0,
    val pendingAmount: Double = 0.0,
    val thisMonthIncome: Double = 0.0,
    val extraIncome: Double = 0.0,
    val expenses: Double = 0.0,
    val netAmount: Double = 0.0,
    val activeSeasonName: String = "",
    val activeSeasonId: Long = 1L,
    val currency: String = "₺",
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val lessonRepository: LessonRepository,
    private val financialRepository: FinancialRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = userPreferencesRepository.userPreferences
        .flatMapLatest { prefs ->
            val seasonId = prefs.activeSeasonId
            val today = LocalDate.now()
            combine(
                studentRepository.getActiveStudents(seasonId),
                lessonRepository.getAllLessons(seasonId),          // sadece aktif sezon
                lessonRepository.getLessonsByDate(today),
                financialRepository.getFinancialSummary(seasonId), // sadece aktif sezon
                financialRepository.getActiveSeason(),
            ) { students, allLessons, todayLessons, summary, season ->
                // Sadece aktif sezonun derslerini say
                val currentMonthLessons = allLessons.filter {
                    it.date.year == today.year && it.date.monthValue == today.monthValue
                }
                // Bugünkü dersler de sezon filtreli
                val todaySeasonLessons = todayLessons.filter { it.seasonId == seasonId }

                HomeUiState(
                    isLoading = false,
                    studentCount = students.size,
                    todayLessons = todaySeasonLessons.size,
                    recentLessons = allLessons.take(5),
                    paidAmount = summary.paidAmount,
                    pendingAmount = summary.pendingAmount,
                    thisMonthIncome = currentMonthLessons.filter { it.isPaid }.sumOf { it.fee },
                    extraIncome = summary.totalExtraIncome,
                    expenses = summary.totalExpenses,
                    netAmount = summary.paidAmount + summary.totalExtraIncome - summary.totalExpenses,
                    activeSeasonName = season?.displayName ?: "",
                    activeSeasonId = seasonId,
                    currency = prefs.currency,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun markLessonPaid(lessonId: Long) {
        viewModelScope.launch {
            lessonRepository.updatePaymentStatus(lessonId, PaymentStatus.PAID)
        }
    }
}
