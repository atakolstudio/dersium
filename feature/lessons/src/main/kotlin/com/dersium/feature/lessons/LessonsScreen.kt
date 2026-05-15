package com.dersium.feature.lessons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dersium.core.domain.model.Lesson
import com.dersium.core.domain.model.PaymentStatus
import com.dersium.core.ui.components.*
import com.dersium.core.ui.theme.DersiumColors
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun LessonsScreen(
    onAddLesson: () -> Unit,
    onEditLesson: (Long) -> Unit,
    viewModel: LessonsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(DersiumColors.Background)) {
        Column(Modifier.fillMaxSize()) {

            // ── Header ─────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Dersler",
                        style = MaterialTheme.typography.headlineMedium,
                        color = DersiumColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    // View mode icons (list / grid) matching screenshot
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.GridView, null, tint = DersiumColors.Primary, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.ViewAgenda, null, tint = DersiumColors.TextTertiary, modifier = Modifier.size(22.dp))
                    }
                }

                // Summary chips (3 colored boxes like Image 10)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LessonSummaryBox(
                        value    = state.totalLessons.toString(),
                        label    = "Ders",
                        bgColor  = DersiumColors.PrimaryContainer,
                        valColor = DersiumColors.TextPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    LessonSummaryBox(
                        value    = state.paidTotal.formatCurrency(state.currency),
                        label    = "Ödendi",
                        bgColor  = DersiumColors.IncomeContainer,
                        valColor = DersiumColors.Income,
                        modifier = Modifier.weight(1f),
                    )
                    LessonSummaryBox(
                        value    = if (state.pendingTotal == 0.0) "Tamam" else state.pendingTotal.formatCurrency(state.currency),
                        label    = if (state.pendingTotal == 0.0) "" else "₺${state.pendingTotal.toInt()}",
                        bgColor  = if (state.pendingTotal == 0.0) DersiumColors.ExpenseContainer.copy(alpha = 0.5f) else DersiumColors.ExpenseContainer,
                        valColor = DersiumColors.Expense,
                        modifier = Modifier.weight(1f),
                        isRed    = true,
                    )
                }

                // Search bar
                OutlinedTextField(
                    value          = state.searchQuery,
                    onValueChange  = viewModel::onSearchQueryChange,
                    placeholder    = { Text("Konu veya not ara…", color = DersiumColors.TextTertiary) },
                    leadingIcon    = { Icon(Icons.Default.Search, null, tint = DersiumColors.TextSecondary) },
                    modifier       = Modifier.fillMaxWidth(),
                    shape          = RoundedCornerShape(14.dp),
                    singleLine     = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor      = DersiumColors.Primary,
                        unfocusedBorderColor    = DersiumColors.Outline,
                        focusedContainerColor   = DersiumColors.SurfaceVariant,
                        unfocusedContainerColor = DersiumColors.SurfaceVariant,
                    ),
                )

                // Filter tabs
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LessonFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = state.filter == filter,
                            onClick  = { viewModel.setFilter(filter) },
                            label    = { Text("${filter.label} (${filter.count(state)})") },
                            leadingIcon = { if (state.filter == filter) Icon(Icons.Default.Check, null, Modifier.size(14.dp)) else when (filter) {
                                LessonFilter.ALL     -> {}
                                LessonFilter.PENDING -> Icon(Icons.Outlined.HourglassEmpty, null, Modifier.size(14.dp))
                                LessonFilter.PAID    -> Icon(Icons.Default.CheckCircle, null, Modifier.size(14.dp))
                            }},
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DersiumColors.Primary,
                                selectedLabelColor     = Color.White,
                                selectedLeadingIconColor = Color.White,
                            ),
                        )
                    }
                }
            }

            // ── Lesson list ────────────────────────────────────────────────────
            if (state.isLoading) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(3) { ShimmerCard() }
                }
            } else if (state.lessons.isEmpty()) {
                DersiumEmptyState(
                    icon = Icons.Outlined.School,
                    title = "Ders bulunamadı",
                    subtitle = "Yeni bir ders ekleyin",
                    modifier = Modifier.weight(1f),
                )
            } else {
                // Group by month (matching screenshot "Mart 2026 · 1 ders · ₺1200")
                val grouped = state.lessons.groupBy { lesson ->
                    "${lesson.date.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("tr")).replaceFirstChar { it.uppercase() }} ${lesson.date.year}"
                }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 100.dp),
                ) {
                    grouped.forEach { (month, lessons) ->
                        item(key = month) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    month,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = DersiumColors.Primary,
                                    fontWeight = FontWeight.Bold,
                                )
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = DersiumColors.PrimaryContainer,
                                ) {
                                    Text(
                                        "${lessons.size} ders · ${lessons.sumOf { it.fee }.formatCurrency(state.currency)}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DersiumColors.PrimaryLight,
                                    )
                                }
                            }
                        }
                        items(lessons, key = { it.id }) { lesson ->
                            LessonListItem(
                                lesson    = lesson,
                                currency  = state.currency,
                                onMarkPaid = { viewModel.markPaid(lesson.id) },
                                onEdit    = { onEditLesson(lesson.id) },
                                onDelete  = { viewModel.deleteLesson(lesson) },
                                modifier  = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }
        }

        DersiumFab(
            label   = "Ders Ekle",
            onClick = onAddLesson,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }
}

private fun LessonFilter.count(state: LessonsUiState): Int = when (this) {
    LessonFilter.ALL     -> state.totalLessons
    LessonFilter.PENDING -> state.pendingCount
    LessonFilter.PAID    -> state.paidCount
}

@Composable
private fun LessonSummaryBox(
    value: String,
    label: String,
    bgColor: Color,
    valColor: Color,
    modifier: Modifier = Modifier,
    isRed: Boolean = false,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, style = MaterialTheme.typography.titleSmall, color = valColor, fontWeight = FontWeight.Bold)
            if (label.isNotEmpty())
                Text(label, style = MaterialTheme.typography.labelSmall, color = DersiumColors.TextSecondary)
        }
    }
}

@Composable
private fun LessonListItem(
    lesson: Lesson,
    currency: String,
    onMarkPaid: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fmt = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("tr"))
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = DersiumColors.SurfaceVariant,
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                DersiumAvatar(
                    initials = lesson.studentName.split(" ")
                        .mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                    colorHex = lesson.studentAvatarColor,
                    size = 42,
                )
                Column(Modifier.weight(1f)) {
                    Text(lesson.studentName, style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.CalendarToday, null, tint = DersiumColors.TextTertiary, modifier = Modifier.size(11.dp))
                        Text("${lesson.date.format(fmt)} · ${lesson.durationMinutes} dk", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
                    }
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(lesson.fee.formatCurrency(currency), style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                    StatusChip(
                        label = if (lesson.isPaid) "✓ Ödendi" else "Bekleyen",
                        color = if (lesson.isPaid) DersiumColors.Income else DersiumColors.Pending,
                    )
                }
            }
            // Bottom: topic + action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (lesson.topic.isNotEmpty())
                    Text(lesson.topic, style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary, modifier = Modifier.weight(1f))
                else Spacer(Modifier.weight(1f))
                // Edit
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, null, tint = DersiumColors.Primary, modifier = Modifier.size(16.dp))
                }
                // Delete
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, null, tint = DersiumColors.Expense, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
