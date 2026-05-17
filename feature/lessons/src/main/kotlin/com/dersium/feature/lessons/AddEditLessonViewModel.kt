package com.dersium.feature.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dersium.core.domain.model.Lesson
import com.dersium.core.domain.model.PaymentStatus
import com.dersium.core.domain.model.Student
import com.dersium.core.domain.repository.LessonRepository
import com.dersium.core.domain.repository.StudentRepository
import com.dersium.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class AddEditLessonUiState(
    val isLoading: Boolean = false,
    val students: List<Student> = emptyList(),
    val selectedStudentId: Long = 0L,
    val date: LocalDate = LocalDate.now(),
    val startTime: LocalTime = LocalTime.of(9, 0),
    val durationMinutes: Int = 60,
    val fee: String = "",
    val topic: String = "",
    val notes: String = "",
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val isEditMode: Boolean = false,
    val isSaved: Boolean = false,
    val activeSeasonId: Long = 1L,
)

@HiltViewModel
class AddEditLessonViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val studentRepository: StudentRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AddEditLessonUiState())
    val state: StateFlow<AddEditLessonUiState> = _state.asStateFlow()
    private var editingId: Long = 0L

    init {
        viewModelScope.launch {
            val prefs = userPreferencesRepository.userPreferences.first()
            _state.update { it.copy(activeSeasonId = prefs.activeSeasonId) }
            studentRepository.getActiveStudents(prefs.activeSeasonId).first().let { students ->
                _state.update { it.copy(students = students) }
            }
        }
    }

    fun loadLesson(lessonId: Long) {
        editingId = lessonId
        viewModelScope.launch {
            lessonRepository.getLessonById(lessonId).first()?.let { l ->
                _state.update {
                    it.copy(
                        isEditMode = true,
                        selectedStudentId = l.studentId,
                        date = l.date, startTime = l.startTime,
                        durationMinutes = l.durationMinutes,
                        fee = l.fee.toInt().toString(),
                        topic = l.topic, notes = l.notes,
                        paymentStatus = l.paymentStatus,
                    )
                }
            }
        }
    }

    fun onStudentSelected(studentId: Long) {
        val student = _state.value.students.find { it.id == studentId } ?: return
        // Otomatik saat ve süre doldur - öğrencinin bu günkü slotuna bak
        val today = _state.value.date.dayOfWeek
        val slot = student.scheduleSlots.find { it.dayOfWeek == today }
            ?: student.scheduleSlots.firstOrNull()
        _state.update {
            it.copy(
                selectedStudentId = studentId,
                fee = student.lessonFee.toInt().toString(),
                startTime = slot?.startTime ?: it.startTime,
                durationMinutes = slot?.durationMinutes ?: it.durationMinutes,
            )
        }
    }

    fun onDateChange(date: LocalDate) {
        // Tarih değişince o günün slotuna bak
        val student = _state.value.students.find { it.id == _state.value.selectedStudentId }
        val slot = student?.scheduleSlots?.find { it.dayOfWeek == date.dayOfWeek }
        _state.update {
            it.copy(
                date = date,
                startTime = slot?.startTime ?: it.startTime,
                durationMinutes = slot?.durationMinutes ?: it.durationMinutes,
            )
        }
    }

    fun onStartTimeChange(time: LocalTime) = _state.update { it.copy(startTime = time) }
    fun onDurationChange(minutes: Int) = _state.update { it.copy(durationMinutes = minutes) }
    fun onFeeChange(v: String) = _state.update { it.copy(fee = v) }
    fun onTopicChange(v: String) = _state.update { it.copy(topic = v) }
    fun onNotesChange(v: String) = _state.update { it.copy(notes = v) }
    fun onPaymentStatusChange(s: PaymentStatus) = _state.update { it.copy(paymentStatus = s) }

    fun save() {
        val s = _state.value
        if (s.selectedStudentId == 0L) return
        val fee = s.fee.toDoubleOrNull() ?: return
        viewModelScope.launch {
            val lesson = Lesson(
                id = if (s.isEditMode) editingId else 0L,
                studentId = s.selectedStudentId,
                studentName = s.students.find { it.id == s.selectedStudentId }?.fullName ?: "",
                studentAvatarColor = s.students.find { it.id == s.selectedStudentId }?.avatarColor ?: "#6366F1",
                date = s.date, startTime = s.startTime,
                durationMinutes = s.durationMinutes, fee = fee,
                topic = s.topic, notes = s.notes,
                paymentStatus = s.paymentStatus,
                seasonId = s.activeSeasonId,
            )
            if (s.isEditMode) lessonRepository.updateLesson(lesson)
            else lessonRepository.insertLesson(lesson)
            _state.update { it.copy(isSaved = true) }
        }
    }
}
