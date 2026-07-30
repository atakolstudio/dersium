package com.dersium.feature.calendar

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dersium.core.domain.model.Lesson
import com.dersium.core.ui.components.*
import com.dersium.core.ui.theme.DersiumColors
import com.dersium.core.ui.theme.DersiumMotion
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
                val monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("tr"))
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
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) DersiumColors.Primary else DersiumColors.SurfaceVariant,
                        animationSpec = DersiumMotion.springSmooth(), label = "dayPillBg",
                    )
                    val interactionSource = remember { MutableInteractionSource() }
                    Column(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(bgColor)
                            .tappable(interactionSource) { viewModel.selectDate(day) }.padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("tr")).take(3), style = MaterialTheme.typography.labelSmall, color = if (isSelected) Color.White else DersiumColors.TextSecondary)
                        Text("${day.dayOfMonth}", style = MaterialTheme.typography.titleSmall, color = if (isSelected) Color.White else if (isToday) DersiumColors.Primary else DersiumColors.TextPrimary, fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal)
                        if (hasLesson) Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) Color.White else DersiumColors.Primary))
                        else Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatChip("${state.totalLessonsThisWeek} ders", Icons.Default.School, Modifier.weight(1f), accentColor = DersiumColors.Primary)
                StatChip("${state.totalMinutesThisWeek / 60}s ${state.totalMinutesThisWeek % 60}dk", Icons.Default.Timer, Modifier.weight(1f), accentColor = DersiumColors.Pending)
                StatChip("${state.studentsThisWeek} öğrenci", Icons.Default.People, Modifier.weight(1f), accentColor = DersiumColors.Income)
            }
        }
        item {
            Text("Günlük Program", style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }
        if (state.lessonsOnSelectedDay.isEmpty()) {
            item { DersiumEmptyState(icon = Icons.Default.EventAvailable, title = "Bu gün ders yok", subtitle = "Ders eklemek için + butonuna basın") }
        } else {
            itemsIndexed(state.lessonsOnSelectedDay.sortedBy { it.startTime }, key = { _, l -> l.id }) { index, lesson ->
                var visible by remember(lesson.id) { mutableStateOf(false) }
                LaunchedEffect(lesson.id) { visible = true }
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(200, delayMillis = (index * 40).coerceAtMost(200))) +
                        slideInVertically(tween(200, delayMillis = (index * 40).coerceAtMost(200))) { it / 4 },
                ) {
                    LessonTimeCard(lesson = lesson, timeFmt = timeFmt, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                }
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
        val progress = if (cap.totalHours > 0) (cap.busyHours / cap.totalHours).toFloat().coerceIn(0f, 1f) else 0f
        val ringColor = if (cap.canTakeNewStudent) DersiumColors.Income else DersiumColors.Expense

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = if (cap.canTakeNewStudent) DersiumColors.IncomeContainer else DersiumColors.ExpenseContainer,
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(if (cap.canTakeNewStudent) Icons.Default.CheckCircle else Icons.Default.Warning, null, tint = ringColor, modifier = Modifier.size(20.dp))
                        Text(
                            if (cap.canTakeNewStudent) "Yeni öğrenci alabilirsiniz!" else "Programınız dolu!",
                            style = MaterialTheme.typography.titleMedium, color = ringColor, fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        CapacityRing(progress = progress, ringColor = ringColor)
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            CapacityStatRow(label = "Dolu", value = "${String.format("%.1f", cap.busyHours)}s", color = DersiumColors.TextPrimary)
                            CapacityStatRow(label = "Boş", value = "${String.format("%.1f", cap.freeHours)}s", color = DersiumColors.Income)
                            CapacityStatRow(label = "Toplam", value = "${String.format("%.0f", cap.totalHours)}s", color = DersiumColors.TextSecondary)
                        }
                    }
                }
            }
        }

        val allFree = cap.slots.isEmpty() && cap.freeSlots.size >= 7
        if (allFree) {
            item {
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = DersiumColors.SurfaceVariant) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("🎉", style = MaterialTheme.typography.displaySmall)
                        Text("Bu hafta tamamen boş", style = MaterialTheme.typography.titleMedium, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                        Text("Henüz hiç ders programlanmamış — istediğin zaman yeni öğrenci ekleyebilirsin", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            item { Text("Haftalık Program", style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold) }
            val grouped = cap.slots.groupBy { it.dayOfWeek }
            val busyDays = DayOfWeek.entries.filter { !grouped[it].isNullOrEmpty() }
            if (busyDays.isEmpty()) {
                item { Text("Öğrencilere ders programı eklenmemiş.", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary) }
            } else {
                items(busyDays) { dow ->
                    val daySlots = grouped[dow].orEmpty()
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = DersiumColors.SurfaceVariant) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(dow.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("tr")), style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                            daySlots.forEach { slot ->
                                Row(modifier = Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier.size(28.dp).clip(CircleShape)
                                            .background(try { Color(android.graphics.Color.parseColor(slot.studentColor)).copy(alpha = 0.2f) } catch (_: Exception) { DersiumColors.PrimaryContainer }),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            slot.studentName.firstOrNull()?.uppercase() ?: "?",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = try { Color(android.graphics.Color.parseColor(slot.studentColor)) } catch (_: Exception) { DersiumColors.Primary },
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                    Column {
                                        Text(slot.studentName, style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Medium)
                                        Text("${slot.startTime.hour.toString().padStart(2,'0')}:${slot.startTime.minute.toString().padStart(2,'0')} · ${slot.durationMinutes}dk", style = MaterialTheme.typography.labelSmall, color = DersiumColors.TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (cap.freeSlots.isNotEmpty()) {
                item { Spacer(Modifier.height(4.dp)); Text("Boş Günler", style = MaterialTheme.typography.titleSmall, color = DersiumColors.Income, fontWeight = FontWeight.Bold) }
                item {
                    FlowRowFreeDays(cap.freeSlots)
                }
            }
        }
    }
}

@Composable
private fun CapacityRing(progress: Float, ringColor: Color, size: androidx.compose.ui.unit.Dp = 84.dp) {
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = DersiumMotion.springSmooth(), label = "capacityRing")
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 9.dp.toPx()
            drawArc(color = DersiumColors.Outline, startAngle = -90f, sweepAngle = 360f, useCenter = false, style = Stroke(stroke, cap = StrokeCap.Round))
            if (animatedProgress > 0f) {
                drawArc(color = ringColor, startAngle = -90f, sweepAngle = 360f * animatedProgress, useCenter = false, style = Stroke(stroke, cap = StrokeCap.Round))
            }
        }
        Text("${(animatedProgress * 100).toInt()}%", style = MaterialTheme.typography.titleMedium, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CapacityStatRow(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
    }
}

@Composable
private fun FlowRowFreeDays(freeSlots: List<Pair<DayOfWeek, String>>) {
    val rows = freeSlots.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { (dow, desc) ->
                    Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), color = DersiumColors.IncomeContainer) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.EventAvailable, null, tint = DersiumColors.Income, modifier = Modifier.size(14.dp))
                                Text(dow.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("tr")), style = MaterialTheme.typography.labelLarge, color = DersiumColors.Income, fontWeight = FontWeight.Bold)
                            }
                            Text(desc, style = MaterialTheme.typography.labelSmall, color = DersiumColors.TextSecondary, maxLines = 1)
                        }
                    }
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StudentCalendarTab(state: CalendarUiState) {
    val dateFmt = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("tr"))
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.students.isEmpty()) {
            item { DersiumEmptyState(icon = Icons.Default.People, title = "Öğrenci yok", subtitle = "Önce öğrenci ekleyin") }
            return@LazyColumn
        }
        itemsIndexed(state.students, key = { _, s -> s.id }) { index, student ->
            var visible by remember(student.id) { mutableStateOf(false) }
            LaunchedEffect(student.id) { visible = true }
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(200, delayMillis = (index * 35).coerceAtMost(180))) +
                    slideInVertically(tween(200, delayMillis = (index * 35).coerceAtMost(180))) { it / 4 },
            ) {
            val lessons = (state.studentLessons[student.id] ?: emptyList()).sortedByDescending { it.date }
            val totalLessons = lessons.size
            val firstLesson = lessons.minByOrNull { it.date }?.date
            // Beklenen ders = geçen hafta sayısı × haftadaki slot sayısı
            val slotsPerWeek = student.scheduleSlots.size.coerceAtLeast(1)
            val scheduleWeeks = if (firstLesson != null) {
                (java.time.temporal.ChronoUnit.WEEKS.between(firstLesson.with(DayOfWeek.MONDAY), LocalDate.now().with(DayOfWeek.MONDAY)).toInt() + 1).coerceAtLeast(1)
            } else 0
            val expectedLessons = scheduleWeeks * slotsPerWeek
            val regularity = if (expectedLessons > 0) (totalLessons * 100 / expectedLessons).coerceAtMost(100) else 0
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
                                        "${slot.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("tr"))} ${slot.startTime.hour.toString().padStart(2,'0')}:${slot.startTime.minute.toString().padStart(2,'0')}"
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
private fun StatChip(text: String, icon: ImageVector, modifier: Modifier = Modifier, accentColor: Color = DersiumColors.Primary) {
    Surface(modifier = modifier, shape = RoundedCornerShape(10.dp), color = DersiumColors.SurfaceVariant) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, tint = accentColor, modifier = Modifier.size(14.dp))
            Text(text, style = MaterialTheme.typography.labelSmall, color = DersiumColors.TextPrimary)
        }
    }
}
