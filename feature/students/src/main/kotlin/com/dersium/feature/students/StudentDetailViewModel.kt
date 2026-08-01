package com.dersium.feature.students

import androidx.compose.runtime.Immutable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dersium.core.domain.model.Lesson
import com.dersium.core.domain.model.PaymentStatus
import com.dersium.core.domain.model.Student
import com.dersium.core.domain.repository.FinancialRepository
import com.dersium.core.domain.repository.LessonRepository
import com.dersium.core.domain.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@Immutable
data class StudentDetailUiState(
    val student: Student? = null,
    val lessons: List<Lesson> = emptyList(),
    val paidAmount: Double = 0.0,
    val pendingAmount: Double = 0.0,
    val pendingLessonCount: Int = 0,
    val generatedCount: Int? = null,
)

@HiltViewModel
class StudentDetailViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val lessonRepository: LessonRepository,
    private val financialRepository: FinancialRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentDetailUiState())
    val uiState: StateFlow<StudentDetailUiState> = _uiState.asStateFlow()

    fun loadStudent(studentId: Long) {
        viewModelScope.launch {
            combine(
                studentRepository.getStudentById(studentId),
                lessonRepository.getLessonsByStudent(studentId),
            ) { student, lessons ->
                StudentDetailUiState(
                    student = student,
                    lessons = lessons.sortedByDescending { it.date },
                    paidAmount = lessons.filter { it.isPaid }.sumOf { it.fee },
                    pendingAmount = lessons.filter { !it.isPaid }.sumOf { it.fee },
                    pendingLessonCount = lessons.count { !it.isPaid },
                )
            }.collect { s -> _uiState.update { s.copy(generatedCount = it.generatedCount) } }
        }
    }

    // Toggle: Ödendi → Bekleyen, Bekleyen → Ödendi
    fun togglePayment(lesson: Lesson) {
        viewModelScope.launch {
            val newStatus = if (lesson.isPaid) PaymentStatus.PENDING else PaymentStatus.PAID
            lessonRepository.updatePaymentStatus(lesson.id, newStatus)
        }
    }

    fun markLessonPaid(lessonId: Long) {
        viewModelScope.launch {
            lessonRepository.updatePaymentStatus(lessonId, PaymentStatus.PAID)
        }
    }

    /**
     * Uses the student's already-defined weekly schedule (set on their profile) to fill in
     * every remaining lesson of the active season in one tap — no need to re-enter the same
     * day/time/duration lesson by lesson. Skips any date that already has a lesson for this
     * student, so it's safe to tap again later in the season to just fill in the gap.
     */
    fun generateSeasonLessons() {
        val student = _uiState.value.student ?: return
        if (student.scheduleSlots.isEmpty()) return
        viewModelScope.launch {
            val season = financialRepository.getActiveSeason().first() ?: return@launch
            val existingDates = _uiState.value.lessons.map { it.date }.toSet()
            val today = LocalDate.now()
            var created = 0

            student.scheduleSlots.forEach { slot ->
                var date = if (today.dayOfWeek == slot.dayOfWeek) today
                    else today.with(TemporalAdjusters.next(slot.dayOfWeek))
                while (!date.isAfter(season.endDate)) {
                    if (date !in existingDates) {
                        lessonRepository.insertLesson(
                            Lesson(
                                studentId = student.id,
                                studentName = student.fullName,
                                studentAvatarColor = student.avatarColor,
                                date = date, startTime = slot.startTime,
                                durationMinutes = slot.durationMinutes,
                                fee = student.lessonFee,
                                paymentStatus = PaymentStatus.PENDING,
                                seasonId = student.seasonId,
                            ),
                        )
                        created++
                    }
                    date = date.plusWeeks(1)
                }
            }
            _uiState.update { it.copy(generatedCount = created) }
        }
    }

    fun clearGeneratedMessage() = _uiState.update { it.copy(generatedCount = null) }
}
