package com.dersium.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dersium.core.domain.model.Lesson
import com.dersium.core.domain.repository.LessonRepository
import com.dersium.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class CalendarUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val weekStart: LocalDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
    val lessonsOnSelectedDay: List<Lesson> = emptyList(),
    val lessonDates: Set<LocalDate> = emptySet(),
    val totalMinutes: Int = 0,
    val studentCount: Int = 0,
    val activeSeasonId: Long = 1L,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<CalendarUiState> = userPreferencesRepository.userPreferences
        .flatMapLatest { prefs ->
            val seasonId = prefs.activeSeasonId
            val start = LocalDate.now().minusMonths(6)
            val end = LocalDate.now().plusMonths(6)
            // Sadece aktif sezonun derslerini getir
            lessonRepository.getLessonsByDateRange(start, end)
                .combine(_selectedDate) { allLessons, selected ->
                    // Sezon filtresi uygula
                    val seasonLessons = allLessons.filter { it.seasonId == seasonId }
                    val day = seasonLessons.filter { it.date == selected }
                    CalendarUiState(
                        selectedDate = selected,
                        weekStart = selected.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                        lessonsOnSelectedDay = day,
                        lessonDates = seasonLessons.map { it.date }.toSet(),
                        totalMinutes = day.sumOf { it.durationMinutes },
                        studentCount = day.map { it.studentId }.distinct().size,
                        activeSeasonId = seasonId,
                    )
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState())

    fun selectDate(date: LocalDate) { _selectedDate.value = date }
    fun prevWeek() { _selectedDate.update { it.minusWeeks(1) } }
    fun nextWeek() { _selectedDate.update { it.plusWeeks(1) } }
}
