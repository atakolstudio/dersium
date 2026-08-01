package com.dersium.feature.reports

import androidx.compose.runtime.Immutable

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
    ACTIVE("Aktif"), PAYMENT("Ödeme"), PENDING("Bekleyen"), DAILY("Günlük"), SEASON("Sezon"), EXPENSES("Gider/Kâr")
}

data class StudentIncome(val student: Student, val totalIncome: Double, val lessonCount: Int, val paidAmount: Double)
data class MonthlyData(val month: String, val lessonCount: Int, val income: Double, val changePercent: Double? = null)
data class DayData(val day: String, val dayOfWeek: DayOfWeek, val lessonCount: Int, val income: Double)
data class SeasonStats(val season: Season, val lessonCount: Int, val totalIncome: Double, val paidAmount: Double, val pendingAmount: Double, val studentCount: Int, val avgPerLesson: Double, val collectionRate: Double, val isActive: Boolean)
data class CategoryAmount(val label: String, val icon: String, val amount: Double)

@Immutable
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
    val activeSeasonName: String = "",
    val allSeasonStats: List<SeasonStats> = emptyList(),
    val currency: String = "₺",
    val totalExpenses: Double = 0.0,
    val totalExtraIncome: Double = 0.0,
    val netProfit: Double = 0.0,
    val expenseByCategory: List<CategoryAmount> = emptyList(),
    val extraIncomeByCategory: List<CategoryAmount> = emptyList(),
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
                lessonRepository.getAllLessonsAllSeasons(),
                financialRepository.getAllSeasons(),
                financialRepository.getAllExpenses(seasonId),
                financialRepository.getAllExtraIncomes(seasonId),
                _tab,
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                val students = values[0] as List<Student>
                @Suppress("UNCHECKED_CAST")
                val allLessons = values[1] as List<Lesson>
                @Suppress("UNCHECKED_CAST")
                val allSeasons = values[2] as List<Season>
                @Suppress("UNCHECKED_CAST")
                val expenses = values[3] as List<Expense>
                @Suppress("UNCHECKED_CAST")
                val extraIncomes = values[4] as List<ExtraIncome>
                val tab = values[5] as ReportTab
                val lessons = allLessons.filter { it.seasonId == seasonId }
                val paid = lessons.filter { it.isPaid }
                val pending = lessons.filter { !it.isPaid }
                val paidAmt = paid.sumOf { it.fee }
                val pendingAmt = pending.sumOf { it.fee }

                val studentIncomes = students.map { s ->
                    val sl = lessons.filter { it.studentId == s.id }
                    StudentIncome(s, sl.sumOf { it.fee }, sl.size, sl.filter { it.isPaid }.sumOf { it.fee })
                }.sortedByDescending { it.lessonCount }

                val monthly = lessons.groupBy { "${it.date.year}-${it.date.monthValue.toString().padStart(2,'0')}" }
                    .map { (m, ls) -> MonthlyData(m, ls.size, ls.filter { it.isPaid }.sumOf { it.fee }) }
                    .sortedBy { it.month }
                    .let { list ->
                        list.mapIndexed { i, m ->
                            val prevIncome = if (i > 0) list[i - 1].income else null
                            val change = if (prevIncome != null && prevIncome > 0) ((m.income - prevIncome) / prevIncome) * 100 else null
                            m.copy(changePercent = change)
                        }
                    }

                val dayData = DayOfWeek.entries.map { dow ->
                    val ls = lessons.filter { it.date.dayOfWeek == dow }
                    DayData(dow.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("tr")), dow, ls.size, ls.filter { it.isPaid }.sumOf { it.fee })
                }

                val lessonsBySeasonId = allLessons.groupBy { it.seasonId }
                val allSeasonStats = allSeasons.map { season ->
                    val sl = lessonsBySeasonId[season.id] ?: emptyList()
                    val sp = sl.filter { it.isPaid }.sumOf { it.fee }
                    val su = sl.filter { !it.isPaid }.sumOf { it.fee }
                    SeasonStats(season, sl.size, sp+su, sp, su, sl.map { it.studentId }.distinct().size,
                        if (sl.isNotEmpty()) (sp+su)/sl.size else 0.0,
                        if (sp+su > 0) (sp/(sp+su)*100) else 0.0, season.id == seasonId)
                }.sortedByDescending { it.season.startYear }

                val totalExpenses = expenses.sumOf { it.amount }
                val totalExtraIncome = extraIncomes.sumOf { it.amount }
                val expenseByCategory = expenses.groupBy { it.category }
                    .map { (cat, list) -> CategoryAmount(cat.displayName, cat.icon, list.sumOf { it.amount }) }
                    .sortedByDescending { it.amount }
                val extraIncomeByCategory = extraIncomes.groupBy { it.category }
                    .map { (cat, list) -> CategoryAmount(cat.displayName, cat.icon, list.sumOf { it.amount }) }
                    .sortedByDescending { it.amount }

                ReportsUiState(
                    tab = tab, studentIncomes = studentIncomes,
                    averagePerLesson = if (paid.isNotEmpty()) paidAmt / paid.size else 0.0,
                    totalLessons = lessons.size, totalIncome = paidAmt,
                    minIncome = paid.minOfOrNull { it.fee } ?: 0.0,
                    maxIncome = paid.maxOfOrNull { it.fee } ?: 0.0,
                    monthlyData = monthly, activeStudents = students.filter { it.isActive },
                    collectionRate = if (paidAmt+pendingAmt > 0) (paidAmt/(paidAmt+pendingAmt)*100) else 0.0,
                    paidLessons = paid.size, pendingLessons = pending.size,
                    paidAmount = paidAmt, pendingAmount = pendingAmt,
                    dayData = dayData, bestDay = dayData.maxByOrNull { it.income },
                    activeSeasonName = allSeasons.find { it.id == seasonId }?.displayName ?: "",
                    allSeasonStats = allSeasonStats, currency = prefs.currency,
                    totalExpenses = totalExpenses, totalExtraIncome = totalExtraIncome,
                    netProfit = paidAmt + totalExtraIncome - totalExpenses,
                    expenseByCategory = expenseByCategory, extraIncomeByCategory = extraIncomeByCategory,
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportsUiState())

    fun setTab(t: ReportTab) { _tab.value = t }
}
