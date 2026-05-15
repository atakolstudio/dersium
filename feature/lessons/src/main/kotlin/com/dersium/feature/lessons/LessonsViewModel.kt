package com.dersium.feature.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dersium.core.domain.model.*
import com.dersium.core.domain.repository.*
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

    private val _filter      = MutableStateFlow(LessonFilter.ALL)
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<LessonsUiState> = userPreferencesRepository.userPreferences
        .flatMapLatest { prefs ->
            lessonRepository.getAllLessons(prefs.activeSeasonId).combine(
                _filter.combine(_searchQuery) { f, q -> Pair(f, q) }
            ) { allLessons, (filter, query) ->
                val byFilter = when (filter) {
                    LessonFilter.ALL     -> allLessons
                    LessonFilter.PENDING -> allLessons.filter { it.paymentStatus == PaymentStatus.PENDING }
                    LessonFilter.PAID    -> allLessons.filter { it.paymentStatus == PaymentStatus.PAID }
                }
                val lessons = if (query.isBlank()) byFilter
                    else byFilter.filter {
                        it.studentName.contains(query, true) ||
                        it.topic.contains(query, true) ||
                        it.notes.contains(query, true)
                    }
                LessonsUiState(
                    isLoading    = false,
                    lessons      = lessons,
                    filter       = filter,
                    searchQuery  = query,
                    totalLessons = allLessons.size,
                    paidCount    = allLessons.count { it.isPaid },
                    pendingCount = allLessons.count { !it.isPaid },
                    paidTotal    = allLessons.filter { it.isPaid }.sumOf { it.fee },
                    pendingTotal = allLessons.filter { !it.isPaid }.sumOf { it.fee },
                    currency     = prefs.currency,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LessonsUiState())

    fun setFilter(f: LessonFilter)       { _filter.value = f }
    fun onSearchQueryChange(q: String)   { _searchQuery.value = q }
    fun markPaid(id: Long)               { viewModelScope.launch { lessonRepository.updatePaymentStatus(id, PaymentStatus.PAID) } }
    fun deleteLesson(lesson: Lesson)     { viewModelScope.launch { lessonRepository.deleteLesson(lesson) } }
}
