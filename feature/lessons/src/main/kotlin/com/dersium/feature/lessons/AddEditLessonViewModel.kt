package com.dersium.feature.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dersium.core.domain.model.*
import com.dersium.core.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class AddEditLessonUiState(
    val isLoading: Boolean = false,
    val students: List<Student> = emptyList(),
    val selectedStudentId: Long? = null,
    val date: LocalDate = LocalDate.now(),
    val startTime: LocalTime = LocalTime.of(9, 0),
    val durationMinutes: Int = 60,
    val topic: String = "",
    val notes: String = "",
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val isEditMode: Boolean = false,
    val isSaved: Boolean = false,
    val studentError: String? = null,
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

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferences.first().let { prefs ->
                _state.update { it.copy(activeSeasonId = prefs.activeSeasonId) }
                studentRepository.getActiveStudents(prefs.activeSeasonId).first().let { students ->
                    _state.update { it.copy(students = students) }
                }
            }
        }
    }

    fun preSelectStudent(studentId: Long) { _state.update { it.copy(selectedStudentId = studentId) } }

    fun loadLesson(lessonId: Long) {
        viewModelScope.launch {
            lessonRepository.getLessonById(lessonId).first()?.let { l ->
                _state.update {
                    it.copy(
                        isEditMode = true,
                        selectedStudentId = l.studentId,
                        date = l.date, startTime = l.startTime,
                        durationMinutes = l.durationMinutes,
                        topic = l.topic, notes = l.notes,
                        paymentStatus = l.paymentStatus,
                    )
                }
            }
        }
    }

    fun onStudentSelected(id: Long) = _state.update { it.copy(selectedStudentId = id, studentError = null) }
    fun onDateChange(d: LocalDate) = _state.update { it.copy(date = d) }
    fun onTimeChange(t: LocalTime) = _state.update { it.copy(startTime = t) }
    fun onDurationChange(d: Int) = _state.update { it.copy(durationMinutes = d) }
    fun onTopicChange(v: String) = _state.update { it.copy(topic = v) }
    fun onNotesChange(v: String) = _state.update { it.copy(notes = v) }
    fun onPaymentStatusChange(s: PaymentStatus) = _state.update { it.copy(paymentStatus = s) }

    fun save() {
        val s = _state.value
        if (s.selectedStudentId == null) { _state.update { it.copy(studentError = "Öğrenci seçin") }; return }
        val student = s.students.find { it.id == s.selectedStudentId } ?: return
        viewModelScope.launch {
            val lesson = Lesson(
                studentId = student.id,
                date = s.date,
                startTime = s.startTime,
                durationMinutes = s.durationMinutes,
                fee = student.lessonFee,
                topic = s.topic.trim(),
                notes = s.notes.trim(),
                paymentStatus = s.paymentStatus,
                seasonId = s.activeSeasonId,
            )
            lessonRepository.insertLesson(lesson)
            _state.update { it.copy(isSaved = true) }
        }
    }
}
