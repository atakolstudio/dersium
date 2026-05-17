package com.dersium.feature.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dersium.core.domain.model.PaymentType
import com.dersium.core.domain.model.ScheduleSlot
import com.dersium.core.domain.model.Student
import com.dersium.core.domain.repository.StudentRepository
import com.dersium.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditStudentUiState(
    val isLoading: Boolean = false,
    val name: String = "",
    val surname: String = "",
    val school: String = "",
    val grade: String = "",
    val motherName: String = "",
    val motherPhone: String = "",
    val fatherName: String = "",
    val fatherPhone: String = "",
    val phone: String = "",
    val lessonFee: String = "",
    val paymentType: PaymentType = PaymentType.UPFRONT,
    val lessonCountForPayment: String = "4",
    val notes: String = "",
    val avatarColor: String = "#6366F1",
    val isActive: Boolean = true,
    val isEditMode: Boolean = false,
    val isSaved: Boolean = false,
    val nameError: String? = null,
    val feeError: String? = null,
    val activeSeasonId: Long = 1L,
    val scheduleSlots: List<ScheduleSlot> = emptyList(),
)

@HiltViewModel
class AddEditStudentViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AddEditStudentUiState())
    val state: StateFlow<AddEditStudentUiState> = _state.asStateFlow()
    private var editingId: Long = 0L

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferences.first().let { prefs ->
                _state.update { it.copy(activeSeasonId = prefs.activeSeasonId) }
            }
        }
    }

    fun loadStudent(studentId: Long) {
        editingId = studentId
        viewModelScope.launch {
            studentRepository.getStudentById(studentId).first()?.let { s ->
                _state.update {
                    it.copy(
                        isEditMode = true,
                        name = s.name, surname = s.surname,
                        school = s.school, grade = s.grade,
                        motherName = s.motherName, motherPhone = s.motherPhone,
                        fatherName = s.fatherName, fatherPhone = s.fatherPhone,
                        phone = s.phone, lessonFee = s.lessonFee.toInt().toString(),
                        paymentType = s.paymentType,
                        lessonCountForPayment = s.lessonCountForPayment.toString(),
                        notes = s.notes, avatarColor = s.avatarColor,
                        isActive = s.isActive,
                        scheduleSlots = s.scheduleSlots,
                    )
                }
            }
        }
    }

    fun onNameChange(v: String) = _state.update { it.copy(name = v, nameError = null) }
    fun onSurnameChange(v: String) = _state.update { it.copy(surname = v) }
    fun onSchoolChange(v: String) = _state.update { it.copy(school = v) }
    fun onGradeChange(v: String) = _state.update { it.copy(grade = v) }
    fun onMotherNameChange(v: String) = _state.update { it.copy(motherName = v) }
    fun onMotherPhoneChange(v: String) = _state.update { it.copy(motherPhone = v) }
    fun onFatherNameChange(v: String) = _state.update { it.copy(fatherName = v) }
    fun onFatherPhoneChange(v: String) = _state.update { it.copy(fatherPhone = v) }
    fun onPhoneChange(v: String) = _state.update { it.copy(phone = v) }
    fun onLessonFeeChange(v: String) = _state.update { it.copy(lessonFee = v, feeError = null) }
    fun onPaymentTypeChange(v: PaymentType) = _state.update { it.copy(paymentType = v) }
    fun onLessonCountChange(v: String) = _state.update { it.copy(lessonCountForPayment = v) }
    fun onNotesChange(v: String) = _state.update { it.copy(notes = v) }
    fun onIsActiveChange(v: Boolean) = _state.update { it.copy(isActive = v) }
    fun onScheduleSlotsChange(slots: List<ScheduleSlot>) = _state.update { it.copy(scheduleSlots = slots) }

    fun addSlot(slot: ScheduleSlot) {
        val current = _state.value.scheduleSlots.toMutableList()
        // Remove existing slot for same day
        current.removeAll { it.dayOfWeek == slot.dayOfWeek }
        current.add(slot)
        current.sortBy { it.dayOfWeek }
        _state.update { it.copy(scheduleSlots = current) }
    }

    fun removeSlot(slot: ScheduleSlot) {
        _state.update { it.copy(scheduleSlots = it.scheduleSlots.filter { s -> s != slot }) }
    }

    fun save() {
        val s = _state.value
        var hasError = false
        if (s.name.isBlank()) { _state.update { it.copy(nameError = "İsim zorunludur") }; hasError = true }
        val fee = s.lessonFee.toDoubleOrNull()
        if (fee == null || fee <= 0) { _state.update { it.copy(feeError = "Geçerli bir ücret girin") }; hasError = true }
        if (hasError) return
        viewModelScope.launch {
            val student = Student(
                id = if (s.isEditMode) editingId else 0L,
                name = s.name.trim(), surname = s.surname.trim(),
                school = s.school.trim(), grade = s.grade.trim(),
                motherName = s.motherName.trim(), motherPhone = s.motherPhone.trim(),
                fatherName = s.fatherName.trim(), fatherPhone = s.fatherPhone.trim(),
                phone = s.phone.trim(), lessonFee = fee!!,
                paymentType = s.paymentType,
                lessonCountForPayment = s.lessonCountForPayment.toIntOrNull() ?: 4,
                notes = s.notes.trim(), avatarColor = s.avatarColor,
                isActive = s.isActive, seasonId = s.activeSeasonId,
                scheduleSlots = s.scheduleSlots,
            )
            if (s.isEditMode) studentRepository.updateStudent(student)
            else studentRepository.insertStudent(student)
            _state.update { it.copy(isSaved = true) }
        }
    }
}
