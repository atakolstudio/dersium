package com.dersium.feature.calendar

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dersium.core.domain.model.Lesson
import com.dersium.core.ui.components.DersiumAvatar
import com.dersium.core.ui.components.DersiumEmptyState
import com.dersium.core.ui.components.DersiumFab
import com.dersium.core.ui.components.StatusChip
import com.dersium.core.ui.theme.DersiumColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    onAddLesson: () -> Unit,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(DersiumColors.Background)) {
        Column(Modifier.fillMaxSize()) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Haftalık Program", style = MaterialTheme.typography.headlineMedium, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                Text("Ders takvimi", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)

                // Week row
                WeekRow(
                    weekStart = state.weekStart,
                    selectedDate = state.selectedDate,
                    lessonDates = state.lessonDates,
                    onDateSelected = viewModel::selectDate,
                    onPrev = viewModel::prevWeek,
                    onNext = viewModel::nextWeek,
                )

                // Stats
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatBadge("${state.lessonsOnSelectedDay.sortedBy { it.startTime }.size} ders", Icons.Default.School)
                    StatBadge("${state.totalMinutes} dk toplam", Icons.Default.AccessTime)
                    StatBadge("${state.studentCount} öğrenci", Icons.Default.People)
                }

                Text("Günlük Program", style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextSecondary)
            }

            // Schedule
            if (state.lessonsOnSelectedDay.isEmpty()) {
                DersiumEmptyState(
                    icon = Icons.Outlined.CalendarToday,
                    title = "Bu gün ders yok",
                    subtitle = "Farklı bir gün seçin veya yeni ders ekleyin",
                    modifier = Modifier.weight(1f),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.lessonsOnSelectedDay, key = { it.id }) { lesson ->
                        ScheduleItem(lesson = lesson)
                    }
                }
            }
        }

        DersiumFab(label = "Ders Ekle", onClick = onAddLesson, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp))
    }
}

@Composable
private fun WeekRow(
    weekStart: LocalDate,
    selectedDate: LocalDate,
    lessonDates: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    Surface(shape = RoundedCornerShape(16.dp), color = DersiumColors.SurfaceVariant) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Day labels
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cts", "Paz").forEach {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = DersiumColors.TextTertiary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
            }
            // Day numbers
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                repeat(7) { idx ->
                    val date = weekStart.plusDays(idx.toLong())
                    val isSelected = date == selectedDate
                    val isToday = date == LocalDate.now()
                    val hasLesson = date in lessonDates
                    val bgColor by animateColorAsState(
                        if (isSelected) DersiumColors.Primary else Color.Transparent,
                        label = "dayBg",
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor)
                            .clickable { onDateSelected(date) }
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.titleSmall,
                            color = when {
                                isSelected -> Color.White
                                isToday -> DersiumColors.Primary
                                else -> DersiumColors.TextPrimary
                            },
                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                        )
                        if (hasLesson) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color.White.copy(0.8f) else DersiumColors.Primary),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBadge(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(shape = RoundedCornerShape(8.dp), color = DersiumColors.Primary.copy(alpha = 0.12f)) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, null, tint = DersiumColors.Primary, modifier = Modifier.size(13.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = DersiumColors.PrimaryLight)
        }
    }
}

@Composable
private fun ScheduleItem(lesson: Lesson) {
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = DersiumColors.SurfaceVariant,
    ) {
        Row(modifier = Modifier.padding(0.dp)) {
            // Time column
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(72.dp)
                    .background(
                        color = runCatching {
                            android.graphics.Color.parseColor(lesson.studentAvatarColor)
                        }.let { result ->
                            if (result.isSuccess) Color(result.getOrThrow()) else DersiumColors.Primary
                        },
                        shape = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp),
                    )
            )
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(lesson.startTime.format(timeFmt), style = MaterialTheme.typography.labelMedium, color = DersiumColors.Primary, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DersiumAvatar(
                        initials = lesson.studentName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                        colorHex = lesson.studentAvatarColor,
                        size = 32,
                    )
                    Text(lesson.studentName, style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.weight(1f))
                    Text("${lesson.durationMinutes} sa".let { if (lesson.durationMinutes >= 60) "${lesson.durationMinutes / 60.0} sa" else it }, style = MaterialTheme.typography.labelMedium, color = DersiumColors.TextSecondary)
                }
                Text(lesson.endTime.format(timeFmt), style = MaterialTheme.typography.labelSmall, color = DersiumColors.TextTertiary)
            }
        }
    }
}
