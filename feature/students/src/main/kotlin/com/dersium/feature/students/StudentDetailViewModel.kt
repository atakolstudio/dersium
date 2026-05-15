package com.dersium.feature.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dersium.core.domain.model.Lesson
import com.dersium.core.domain.model.PaymentStatus
import com.dersium.core.domain.model.Student
import com.dersium.core.domain.repository.LessonRepository
import com.dersium.core.domain.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentDetailUiState(
    val student: Student? = null,
    val lessons: List<Lesson> = emptyList(),
    val paidAmount: Double = 0.0,
    val pendingAmount: Double = 0.0,
)

@HiltViewModel
class StudentDetailViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val lessonRepository: LessonRepository,
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
                )
            }.collect { _uiState.value = it }
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
}
