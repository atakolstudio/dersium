package com.dersium.feature.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dersium.core.domain.model.Lesson
import com.dersium.core.domain.model.PaymentStatus
import com.dersium.core.domain.repository.LessonRepository
import com.dersium.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LessonFilter(val label: String) { ALL("Tümü"), PENDING("Bekleyen"), PAID("Ödendi") }

data class LessonsUiState(
    val isLoading: Boolean = true,
    val lessons: List<Lesson> = emptyList(),
    val filter: LessonFilter = LessonFilter.ALL,
    val searchQuery: String = "",
    val totalLessons: Int = 0,
    val paidCount: Int = 0,
    val pendingCount: Int = 0,
    val paidTotal: Double = 0.0,
    val pendingTotal: Double = 0.0,
    val currency: String = "₺",
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LessonsViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _filter = MutableStateFlow(LessonFilter.ALL)
    private val _search = MutableStateFlow("")

    val uiState: StateFlow<LessonsUiState> = userPreferencesRepository.userPreferences
        .flatMapLatest { prefs ->
            lessonRepository.getAllLessons(prefs.activeSeasonId)
                .combine(_filter) { lessons, filter -> Pair(lessons, filter) }
                .combine(_search) { (lessons, filter), search ->
                    val paid    = lessons.filter { it.isPaid }
                    val pending = lessons.filter { !it.isPaid }
                    val filtered = lessons
                        .filter { when (filter) { LessonFilter.ALL -> true; LessonFilter.PAID -> it.isPaid; LessonFilter.PENDING -> !it.isPaid } }
                        .filter { search.isBlank() || it.topic.contains(search, true) || it.notes.contains(search, true) || it.studentName.contains(search, true) }
                    LessonsUiState(
                        isLoading = false, lessons = filtered, filter = filter, searchQuery = search,
                        totalLessons = lessons.size, paidCount = paid.size, pendingCount = pending.size,
                        paidTotal = paid.sumOf { it.fee }, pendingTotal = pending.sumOf { it.fee },
                        currency = prefs.currency,
                    )
                }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LessonsUiState())

    fun setFilter(f: LessonFilter) { _filter.value = f }
    fun onSearchQueryChange(q: String) { _search.value = q }
    fun markPaid(id: Long)   { viewModelScope.launch { lessonRepository.updatePaymentStatus(id, PaymentStatus.PAID) } }
    fun markUnpaid(id: Long) { viewModelScope.launch { lessonRepository.updatePaymentStatus(id, PaymentStatus.PENDING) } }
    fun deleteLesson(lesson: Lesson) { viewModelScope.launch { lessonRepository.deleteLesson(lesson) } }
}
