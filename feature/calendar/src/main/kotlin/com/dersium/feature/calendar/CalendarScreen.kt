package com.dersium.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dersium.core.domain.model.Lesson
import com.dersium.core.ui.components.*
import com.dersium.core.ui.theme.DersiumColors
import java.time.DayOfWeek
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
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Takvim", "Kapasite", "Öğrenci")

    Box(modifier = Modifier.fillMaxSize().background(DersiumColors.Background)) {
        Column(Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text("Haftalık Program", style = MaterialTheme.typography.headlineMedium, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                Text("Ders takvimi", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DersiumColors.Background,
                contentColor = DersiumColors.Primary,
            ) {
                tabs.forEachIndexed { i, title ->
                    Tab(
                        selected = selectedTab == i,
                        onClick = { selectedTab = i },
                        text = { Text(title, color = if (selectedTab == i) DersiumColors.Primary else DersiumColors.TextSecondary) },
                    )
                }
            }

            when (selectedTab) {
                0 -> CalendarTab(state, viewModel)
                1 -> CapacityTab(state)
                2 -> StudentCalendarTab(state)
            }
        }
        DersiumFab(label = "Ders Ekle", onClick = onAddLesson, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp))
    }
}

@Composable
private fun CalendarTab(state: CalendarUiState, viewModel: CalendarViewModel) {
    val weekDays = (0..6).map { state.weekStart.plusDays(it.toLong()) }
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 100.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.previousWeek() }) { Icon(Icons.Default.ChevronLeft, null, tint = DersiumColors.TextPrimary) }
                val monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("tr"))
                Text(state.weekStart.format(monthFmt).replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                IconButton(onClick = { viewModel.nextWeek() }) { Icon(Icons.Default.ChevronRight, null, tint = DersiumColors.TextPrimary) }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                weekDays.forEach { day ->
                    val isSelected = day == state.selectedDate
                    val isToday = day == LocalDate.now()
                    val hasLesson = day in state.lessonDates
                    Column(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(if (isSelected) DersiumColors.Primary else DersiumColors.SurfaceVariant).clickable { viewModel.selectDate(day) }.padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("tr")).take(3), style = MaterialTheme.typography.labelSmall, color = if (isSelected) Color.White else DersiumColors.TextSecondary)
                        Text("${day.dayOfMonth}", style = MaterialTheme.typography.titleSmall, color = if (isSelected) Color.White else if (isToday) DersiumColors.Primary else DersiumColors.TextPrimary, fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal)
                        if (hasLesson) Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) Color.White else DersiumColors.Primary))
                        else Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatChip("${state.totalLessonsThisWeek} ders", Icons.Default.School, Modifier.weight(1f))
                StatChip("${state.totalMinutesThisWeek / 60}s ${state.totalMinutesThisWeek % 60}dk", Icons.Default.Timer, Modifier.weight(1f))
                StatChip("${state.studentsThisWeek} öğrenci", Icons.Default.People, Modifier.weight(1f))
            }
        }
        item {
            Text("Günlük Program", style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }
        if (state.lessonsOnSelectedDay.isEmpty()) {
            item { DersiumEmptyState(icon = Icons.Default.EventAvailable, title = "Bu gün ders yok", subtitle = "Ders eklemek için + butonuna basın") }
        } else {
            items(state.lessonsOnSelectedDay.sortedBy { it.startTime }) { lesson ->
                LessonTimeCard(lesson = lesson, timeFmt = timeFmt, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            }
        }
    }
}

@Composable
private fun CapacityTab(state: CalendarUiState) {
    val cap = state.weekCapacity
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (cap == null) {
            item { DersiumEmptyState(icon = Icons.Default.BarChart, title = "Kapasite bilgisi yok", subtitle = "Öğrencilere ders programı ekleyin") }
            return@LazyColumn
        }
        item {
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = if (cap.canTakeNewStudent) DersiumColors.IncomeContainer else DersiumColors.ExpenseContainer) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(if (cap.canTakeNewStudent) Icons.Default.CheckCircle else Icons.Default.Warning, null, tint = if (cap.canTakeNewStudent) DersiumColors.Income else DersiumColors.Expense, modifier = Modifier.size(24.dp))
                        Text(if (cap.canTakeNewStudent) "Yeni öğrenci alabilirsiniz!" else "Programınız dolu!", style = MaterialTheme.typography.titleMedium, color = if (cap.canTakeNewStudent) DersiumColors.Income else DersiumColors.Expense, fontWeight = FontWeight.Bold)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            Text("${String.format("%.1f", cap.busyHours)}s", style = MaterialTheme.typography.titleLarge, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                            Text("Dolu", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
                        }
                        Column {
                            Text("${String.format("%.1f", cap.freeHours)}s", style = MaterialTheme.typography.titleLarge, color = DersiumColors.Income, fontWeight = FontWeight.Bold)
                            Text("Boş", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
                        }
                    }
                    LinearProgressIndicator(progress = { (cap.busyHours / cap.totalHours).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth(), color = if (cap.canTakeNewStudent) DersiumColors.Pending else DersiumColors.Expense, trackColor = DersiumColors.Outline)
                }
            }
        }
        item { Text("Haftalık Program", style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold) }
        if (cap.slots.isEmpty()) {
            item { Text("Öğrencilere ders programı eklenmemiş.", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary) }
        } else {
            val grouped = cap.slots.groupBy { it.dayOfWeek }
            items(DayOfWeek.entries) { dow ->
                val daySlots = grouped[dow]
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = DersiumColors.SurfaceVariant) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(dow.getDisplayName(TextStyle.FULL, Locale("tr")), style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                        if (daySlots.isNullOrEmpty()) {
                            Text("Boş gün", style = MaterialTheme.typography.bodySmall, color = DersiumColors.Income)
                        } else {
                            daySlots.forEach { slot ->
                                Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(try { Color(android.graphics.Color.parseColor(slot.studentColor)) } catch (_: Exception) { DersiumColors.Primary }))
                                    Text("${slot.startTime.hour.toString().padStart(2,'0')}:${slot.startTime.minute.toString().padStart(2,'0')} · ${slot.durationMinutes}dk", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
                                    Text(slot.studentName, style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
        if (cap.freeSlots.isNotEmpty()) {
            item { Text("Boş Günler", style = MaterialTheme.typography.titleSmall, color = DersiumColors.Income, fontWeight = FontWeight.Bold) }
            items(cap.freeSlots) { (dow, desc) ->
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = DersiumColors.IncomeContainer) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.EventAvailable, null, tint = DersiumColors.Income, modifier = Modifier.size(18.dp))
                        Text(dow.getDisplayName(TextStyle.FULL, Locale("tr")), style = MaterialTheme.typography.titleSmall, color = DersiumColors.Income, fontWeight = FontWeight.SemiBold)
                        Text("· $desc", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentCalendarTab(state: CalendarUiState) {
    val dateFmt = DateTimeFormatter.ofPattern("d MMM", Locale("tr"))
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.students.isEmpty()) {
            item { DersiumEmptyState(icon = Icons.Default.People, title = "Öğrenci yok", subtitle = "Önce öğrenci ekleyin") }
            return@LazyColumn
        }
        items(state.students) { student ->
            val lessons = (state.studentLessons[student.id] ?: emptyList()).sortedByDescending { it.date }
            val totalLessons = lessons.size
            val attendedWeeks = lessons.map { it.date.with(DayOfWeek.MONDAY) }.distinct().size
            val firstLesson = lessons.minByOrNull { it.date }?.date
            val scheduleWeeks = if (firstLesson != null && student.scheduleSlots.isNotEmpty()) {
                (java.time.temporal.ChronoUnit.WEEKS.between(firstLesson.with(DayOfWeek.MONDAY), LocalDate.now().with(DayOfWeek.MONDAY)).toInt() + 1).coerceAtLeast(1)
            } else 0
            val regularity = if (scheduleWeeks > 0) (attendedWeeks * 100 / scheduleWeeks).coerceAtMost(100) else 0
            val regularityColor = if (regularity >= 80) DersiumColors.Income else if (regularity >= 50) DersiumColors.Pending else DersiumColors.Expense

            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = DersiumColors.SurfaceVariant) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DersiumAvatar(initials = student.initials, colorHex = student.avatarColor, size = 40)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(student.fullName, style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                            if (student.scheduleSlots.isNotEmpty()) {
                                Text(
                                    student.scheduleSlots.joinToString(", ") { slot ->
                                        "${slot.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("tr"))} ${slot.startTime.hour.toString().padStart(2,'0')}:${slot.startTime.minute.toString().padStart(2,'0')}"
                                    },
                                    style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary,
                                )
                            } else {
                                Text("Program eklenmemiş", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextTertiary)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("$totalLessons ders", style = MaterialTheme.typography.labelSmall, color = DersiumColors.TextSecondary)
                            Text("%$regularity", style = MaterialTheme.typography.titleSmall, color = regularityColor, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (scheduleWeeks > 0) {
                        LinearProgressIndicator(progress = { regularity / 100f }, modifier = Modifier.fillMaxWidth(), color = regularityColor, trackColor = DersiumColors.Outline)
                        Text("Düzenlilik: %$regularity", style = MaterialTheme.typography.labelSmall, color = DersiumColors.TextSecondary)
                    }
                    if (lessons.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            lessons.take(4).forEach { lesson ->
                                Surface(shape = RoundedCornerShape(6.dp), color = if (lesson.isPaid) DersiumColors.IncomeContainer else DersiumColors.PendingContainer) {
                                    Text(lesson.date.format(dateFmt), modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, color = if (lesson.isPaid) DersiumColors.Income else DersiumColors.Pending)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonTimeCard(lesson: Lesson, timeFmt: DateTimeFormatter, modifier: Modifier = Modifier) {
    val endTime = lesson.startTime.plusMinutes(lesson.durationMinutes.toLong())
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = DersiumColors.SurfaceVariant) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(lesson.startTime.format(timeFmt), style = MaterialTheme.typography.labelMedium, color = DersiumColors.Primary, fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.width(1.dp).height(20.dp).background(DersiumColors.Outline))
                Text(endTime.format(timeFmt), style = MaterialTheme.typography.labelSmall, color = DersiumColors.TextTertiary)
            }
            Box(modifier = Modifier.width(3.dp).height(50.dp).clip(RoundedCornerShape(2.dp)).background(try { Color(android.graphics.Color.parseColor(lesson.studentAvatarColor)) } catch (_: Exception) { DersiumColors.Primary }))
            Column(modifier = Modifier.weight(1f)) {
                Text(lesson.studentName, style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                if (lesson.topic.isNotEmpty()) Text(lesson.topic, style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
                Text("${lesson.durationMinutes} dk", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextTertiary)
            }
            Surface(shape = RoundedCornerShape(8.dp), color = if (lesson.isPaid) DersiumColors.IncomeContainer else DersiumColors.PendingContainer) {
                Text(if (lesson.isPaid) "Ödendi" else "Bekleyen", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = if (lesson.isPaid) DersiumColors.Income else DersiumColors.Pending, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StatChip(text: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(10.dp), color = DersiumColors.SurfaceVariant) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, tint = DersiumColors.Primary, modifier = Modifier.size(14.dp))
            Text(text, style = MaterialTheme.typography.labelSmall, color = DersiumColors.TextPrimary)
        }
    }
}
