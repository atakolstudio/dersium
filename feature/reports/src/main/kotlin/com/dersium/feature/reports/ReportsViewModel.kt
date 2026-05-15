package com.dersium.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dersium.core.domain.model.*
import com.dersium.core.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

enum class ReportTab(val label: String) {
    STUDENT("Öğrenci"), AVERAGE("Ortalama"), MONTHLY("Aylık"),
    ACTIVE("Aktif"), PAYMENT("Ödeme"), PENDING("Bekleyen"), DAILY("Günlük"), SEASON("Sezon")
}

data class StudentIncome(val student: Student, val totalIncome: Double, val lessonCount: Int, val paidAmount: Double)
data class MonthlyData(val month: String, val lessonCount: Int, val income: Double)
data class DayData(val day: String, val dayOfWeek: DayOfWeek, val lessonCount: Int, val income: Double)

data class ReportsUiState(
    val tab: ReportTab = ReportTab.STUDENT,
    val studentIncomes: List<StudentIncome> = emptyList(),
    val averagePerLesson: Double = 0.0,
    val totalLessons: Int = 0,
    val totalIncome: Double = 0.0,
    val minIncome: Double = 0.0,
    val maxIncome: Double = 0.0,
    val monthlyData: List<MonthlyData> = emptyList(),
    val activeStudents: List<Student> = emptyList(),
    val collectionRate: Double = 100.0,
    val paidLessons: Int = 0,
    val pendingLessons: Int = 0,
    val paidAmount: Double = 0.0,
    val pendingAmount: Double = 0.0,
    val dayData: List<DayData> = emptyList(),
    val bestDay: DayData? = null,
    val seasonName: String = "",
    val currency: String = "₺",
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val lessonRepository: LessonRepository,
    private val financialRepository: FinancialRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _tab = MutableStateFlow(ReportTab.STUDENT)

    val uiState: StateFlow<ReportsUiState> = userPreferencesRepository.userPreferences
        .flatMapLatest { prefs ->
            val seasonId = prefs.activeSeasonId
            combine(
                studentRepository.getAllStudents(seasonId),
                lessonRepository.getAllLessons(seasonId),
                financialRepository.getActiveSeason(),
                _tab,
            ) { students, lessons, season, tab ->
                val paidLessons = lessons.filter { it.isPaid }
                val pendingLessons = lessons.filter { !it.isPaid }
                val paidAmount = paidLessons.sumOf { it.fee }
                val pendingAmount = pendingLessons.sumOf { it.fee }
                val totalIncome = paidAmount
                val avgPerLesson = if (paidLessons.isNotEmpty()) totalIncome / paidLessons.size else 0.0

                // Student incomes
                val studentIncomes = students.map { s ->
                    val sLessons = lessons.filter { it.studentId == s.id }
                    StudentIncome(
                        student = s,
                        totalIncome = sLessons.sumOf { it.fee },
                        lessonCount = sLessons.size,
                        paidAmount = sLessons.filter { it.isPaid }.sumOf { it.fee },
                    )
                }.sortedByDescending { it.lessonCount }

                // Monthly
                val monthly = lessons.groupBy { "${it.date.year}-${it.date.monthValue.toString().padStart(2, '0')}" }
                    .map { (month, ls) -> MonthlyData(month, ls.size, ls.filter { it.isPaid }.sumOf { it.fee }) }
                    .sortedBy { it.month }

                // Day of week
                val dayMap = lessons.groupBy { it.date.dayOfWeek }
                val dayData = DayOfWeek.entries.map { dow ->
                    val ls = dayMap[dow] ?: emptyList()
                    DayData(
                        day = dow.getDisplayName(TextStyle.SHORT, Locale("tr")),
                        dayOfWeek = dow,
                        lessonCount = ls.size,
                        income = ls.filter { it.isPaid }.sumOf { it.fee },
                    )
                }

                ReportsUiState(
                    tab = tab,
                    studentIncomes = studentIncomes,
                    averagePerLesson = avgPerLesson,
                    totalLessons = lessons.size,
                    totalIncome = totalIncome,
                    minIncome = paidLessons.minOfOrNull { it.fee } ?: 0.0,
                    maxIncome = paidLessons.maxOfOrNull { it.fee } ?: 0.0,
                    monthlyData = monthly,
                    activeStudents = students.filter { it.isActive },
                    collectionRate = if (totalIncome + pendingAmount > 0) (paidAmount / (paidAmount + pendingAmount) * 100) else 100.0,
                    paidLessons = paidLessons.size,
                    pendingLessons = pendingLessons.size,
                    paidAmount = paidAmount,
                    pendingAmount = pendingAmount,
                    dayData = dayData,
                    bestDay = dayData.maxByOrNull { it.income },
                    seasonName = season?.displayName ?: "",
                    currency = prefs.currency,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportsUiState())

    fun setTab(t: ReportTab) { _tab.value = t }
}
