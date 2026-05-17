package com.dersium.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dersium.core.domain.model.Lesson
import com.dersium.core.domain.model.ScheduleSlot
import com.dersium.core.domain.model.Student
import com.dersium.core.domain.repository.LessonRepository
import com.dersium.core.domain.repository.StudentRepository
import com.dersium.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class CapacitySlot(
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val durationMinutes: Int,
    val studentName: String,
    val studentColor: String,
    val isBusy: Boolean,
)

data class WeekCapacity(
    val totalHours: Double,
    val busyHours: Double,
    val freeHours: Double,
    val slots: List<CapacitySlot>,
    val canTakeNewStudent: Boolean,
    val freeSlots: List<Pair<DayOfWeek, String>>,
)

data class CalendarUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val weekStart: LocalDate = LocalDate.now().with(java.time.DayOfWeek.MONDAY),
    val lessonsOnSelectedDay: List<Lesson> = emptyList(),
    val lessonDates: Set<LocalDate> = emptySet(),
    val totalLessonsThisWeek: Int = 0,
    val totalMinutesThisWeek: Int = 0,
    val studentsThisWeek: Int = 0,
    val weekCapacity: WeekCapacity? = null,
    val studentLessons: Map<Long, List<Lesson>> = emptyMap(),
    val students: List<Student> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val studentRepository: StudentRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<CalendarUiState> = userPreferencesRepository.userPreferences
        .flatMapLatest { prefs ->
            combine(
                lessonRepository.getAllLessons(prefs.activeSeasonId),
                studentRepository.getAllStudents(prefs.activeSeasonId),
                _selectedDate,
            ) { lessons, students, selected ->
                val weekStart = selected.with(DayOfWeek.MONDAY)
                val weekEnd   = weekStart.plusDays(6)

                val dayLessons = lessons.filter { it.date == selected }.sortedBy { it.startTime }
                val weekLessons = lessons.filter { !it.date.isBefore(weekStart) && !it.date.isAfter(weekEnd) }

                val lessonDates = lessons.map { it.date }.toSet()

                // Haftalık kapasite — öğrenci programlarından hesapla
                val allSlots = mutableListOf<CapacitySlot>()
                students.forEach { student ->
                    student.scheduleSlots.forEach { slot ->
                        allSlots.add(CapacitySlot(
                            dayOfWeek = slot.dayOfWeek,
                            startTime = slot.startTime,
                            durationMinutes = slot.durationMinutes,
                            studentName = student.fullName,
                            studentColor = student.avatarColor,
                            isBusy = true,
                        ))
                    }
                }

                val totalScheduledMinutes = allSlots.sumOf { it.durationMinutes }
                val totalWeekMinutes = 7 * 12 * 60 // 7 gün x 12 saat (08:00-20:00)
                val busyHours = totalScheduledMinutes / 60.0
                val freeHours = (totalWeekMinutes - totalScheduledMinutes) / 60.0

                // Boş gün-saat slotları bul
                val busyDays = allSlots.map { it.dayOfWeek }.toSet()
                val allDays = DayOfWeek.entries
                val freeDays = allDays.filter { it !in busyDays }
                    .map { Pair(it, "Tüm gün boş") }

                val weekCapacity = WeekCapacity(
                    totalHours = (totalWeekMinutes / 60.0),
                    busyHours = busyHours,
                    freeHours = freeHours,
                    slots = allSlots.sortedWith(compareBy({ it.dayOfWeek }, { it.startTime })),
                    canTakeNewStudent = freeHours >= 2.0,
                    freeSlots = freeDays,
                )

                // Öğrenci bazlı dersler
                val studentLessons = lessons.groupBy { it.studentId }

                CalendarUiState(
                    selectedDate = selected,
                    weekStart = weekStart,
                    lessonsOnSelectedDay = dayLessons,
                    lessonDates = lessonDates,
                    totalLessonsThisWeek = weekLessons.size,
                    totalMinutesThisWeek = weekLessons.sumOf { it.durationMinutes },
                    studentsThisWeek = weekLessons.map { it.studentId }.distinct().size,
                    weekCapacity = weekCapacity,
                    studentLessons = studentLessons,
                    students = students,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState())

    fun selectDate(date: LocalDate) { _selectedDate.value = date }
    fun previousWeek() { _selectedDate.value = _selectedDate.value.minusWeeks(1) }
    fun nextWeek() { _selectedDate.value = _selectedDate.value.plusWeeks(1) }
}
