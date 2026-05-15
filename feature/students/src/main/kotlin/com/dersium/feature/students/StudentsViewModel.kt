package com.dersium.feature.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dersium.core.domain.model.Student
import com.dersium.core.domain.repository.LessonRepository
import com.dersium.core.domain.repository.StudentRepository
import com.dersium.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class StudentsUiState(
    val isLoading: Boolean = true,
    val students: List<Student> = emptyList(),
    val filteredStudents: List<Student> = emptyList(),
    val searchQuery: String = "",
    val showOnlyActive: Boolean = false,
    val isPremium: Boolean = false,
    val maxFreeStudents: Int = 5,
    val activeSeasonId: Long = 1L,
    val activeSeasonName: String = "",
    val currency: String = "₺",
    // Per-student aggregated data for the enriched card
    val lessonCountMap: Map<Long, Int> = emptyMap(),
    val paidAmountMap: Map<Long, Double> = emptyMap(),
    val pendingAmountMap: Map<Long, Double> = emptyMap(),
    val lastLessonDateMap: Map<Long, LocalDate?> = emptyMap(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StudentsViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val lessonRepository: LessonRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _searchQuery   = MutableStateFlow("")
    private val _showOnlyActive = MutableStateFlow(false)

    val uiState: StateFlow<StudentsUiState> = userPreferencesRepository.userPreferences
        .flatMapLatest { prefs ->
            val seasonId = prefs.activeSeasonId
            combine(
                studentRepository.getAllStudents(seasonId),
                lessonRepository.getAllLessons(seasonId),
                _searchQuery.combine(_showOnlyActive) { q, a -> Pair(q, a) },
            ) { students, allLessons, (query, activeOnly) ->
                val filtered = students
                    .filter { if (activeOnly) it.isActive else true }
                    .filter {
                        if (query.isBlank()) true
                        else it.fullName.contains(query, ignoreCase = true) ||
                             it.school.contains(query, ignoreCase = true)
                    }

                val lessonCountMap   = allLessons.groupBy { it.studentId }.mapValues { it.value.size }
                val paidAmountMap    = allLessons.groupBy { it.studentId }.mapValues { (_, ls) -> ls.filter { it.isPaid }.sumOf { it.fee } }
                val pendingAmountMap = allLessons.groupBy { it.studentId }.mapValues { (_, ls) -> ls.filter { !it.isPaid }.sumOf { it.fee } }
                val lastLessonDateMap= allLessons.groupBy { it.studentId }.mapValues { (_, ls) -> ls.maxByOrNull { it.date }?.date }

                StudentsUiState(
                    isLoading = false,
                    students  = students,
                    filteredStudents = filtered,
                    searchQuery      = query,
                    showOnlyActive   = activeOnly,
                    isPremium        = prefs.isPremium,
                    maxFreeStudents  = prefs.maxFreeStudents,
                    activeSeasonId   = seasonId,
                    activeSeasonName = "${seasonId.let { "2025-2026" }}",
                    currency         = prefs.currency,
                    lessonCountMap   = lessonCountMap,
                    paidAmountMap    = paidAmountMap,
                    pendingAmountMap = pendingAmountMap,
                    lastLessonDateMap = lastLessonDateMap,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StudentsUiState())

    fun onSearchQueryChange(q: String) { _searchQuery.value = q }
    fun toggleActiveFilter() { _showOnlyActive.update { !it } }
    fun deleteStudent(student: Student) { viewModelScope.launch { studentRepository.deleteStudent(student) } }
}
