package com.dersium.feature.lessons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.dersium.core.ui.components.*
import com.dersium.core.ui.theme.DersiumColors
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

enum class LessonsViewMode { LIST, GRID }

@Composable
fun LessonsScreen(
    onAddLesson: () -> Unit,
    onEditLesson: (Long) -> Unit,
    viewModel: LessonsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var viewMode by remember { mutableStateOf(LessonsViewMode.LIST) }

    Box(modifier = Modifier.fillMaxSize().background(DersiumColors.Background)) {
        Column(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Dersler", style = MaterialTheme.typography.headlineMedium, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                    Row {
                        IconButton(onClick = { viewMode = LessonsViewMode.GRID }) {
                            Icon(Icons.Default.GridView, null, tint = if (viewMode == LessonsViewMode.GRID) DersiumColors.Primary else DersiumColors.TextTertiary)
                        }
                        IconButton(onClick = { viewMode = LessonsViewMode.LIST }) {
                            Icon(Icons.Default.ViewAgenda, null, tint = if (viewMode == LessonsViewMode.LIST) DersiumColors.Primary else DersiumColors.TextTertiary)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryBox(state.totalLessons.toString(), "Ders", DersiumColors.PrimaryContainer, DersiumColors.TextPrimary, Modifier.weight(1f))
                    SummaryBox(state.paidTotal.formatCurrency(state.currency), "Ödendi", DersiumColors.IncomeContainer, DersiumColors.Income, Modifier.weight(1f))
                    SummaryBox(if (state.pendingTotal == 0.0) "Tamam" else state.pendingTotal.formatCurrency(state.currency), "", DersiumColors.ExpenseContainer, DersiumColors.Expense, Modifier.weight(1f))
                }
                OutlinedTextField(
                    value = state.searchQuery, onValueChange = viewModel::onSearchQueryChange,
                    placeholder = { Text("Konu veya not ara…", color = DersiumColors.TextTertiary) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = DersiumColors.TextSecondary) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DersiumColors.Primary, unfocusedBorderColor = DersiumColors.Outline, focusedContainerColor = DersiumColors.SurfaceVariant, unfocusedContainerColor = DersiumColors.SurfaceVariant),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LessonFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = state.filter == filter, onClick = { viewModel.setFilter(filter) },
                            label = { Text("${filter.label} (${when(filter){ LessonFilter.ALL -> state.totalLessons; LessonFilter.PENDING -> state.pendingCount; LessonFilter.PAID -> state.paidCount }})") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = DersiumColors.Primary, selectedLabelColor = Color.White),
                        )
                    }
                }
            }

            if (state.isLoading) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { repeat(3) { ShimmerCard() } }
            } else if (state.lessons.isEmpty()) {
                DersiumEmptyState(icon = Icons.Outlined.School, title = "Ders bulunamadı", subtitle = "Yeni bir ders ekleyin", modifier = Modifier.weight(1f))
            } else when (viewMode) {
                LessonsViewMode.LIST -> {
                    val grouped = state.lessons.groupBy { "${it.date.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("tr")).replaceFirstChar { c -> c.uppercase() }} ${it.date.year}" }
                    LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(bottom = 100.dp)) {
                        grouped.forEach { (month, lessons) ->
                            item(key = month) {
                                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(month, style = MaterialTheme.typography.titleSmall, color = DersiumColors.Primary, fontWeight = FontWeight.Bold)
                                    Surface(shape = RoundedCornerShape(20.dp), color = DersiumColors.PrimaryContainer) {
                                        Text("${lessons.size} ders · ${lessons.sumOf { it.fee }.formatCurrency(state.currency)}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = DersiumColors.PrimaryLight)
                                    }
                                }
                            }
                            items(lessons, key = { it.id }) { lesson ->
                                LessonListItem(lesson, state.currency, { viewModel.markPaid(lesson.id) }, { onEditLesson(lesson.id) }, { viewModel.deleteLesson(lesson) }, Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                            }
                        }
                    }
                }
                LessonsViewMode.GRID -> {
                    LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.weight(1f), contentPadding = PaddingValues(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.lessons, key = { it.id }) { lesson ->
                            val fmt = DateTimeFormatter.ofPattern("d MMM", Locale("tr"))
                            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = DersiumColors.SurfaceVariant) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    DersiumAvatar(initials = lesson.studentName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""), colorHex = lesson.studentAvatarColor, size = 36)
                                    Text(lesson.studentName, style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text("${lesson.date.format(fmt)} · ${lesson.durationMinutes}dk", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(lesson.fee.formatCurrency(state.currency), style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                                        IconButton(onClick = { viewModel.deleteLesson(lesson) }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Delete, null, tint = DersiumColors.Expense, modifier = Modifier.size(14.dp)) }
                                    }
                                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), color = if (lesson.isPaid) DersiumColors.IncomeContainer else DersiumColors.PrimaryContainer, onClick = { if (lesson.isPaid) viewModel.markUnpaid(lesson.id) else viewModel.markPaid(lesson.id) }) {
                                        Text(if (lesson.isPaid) "✓ Ödendi" else "Tahsil Et", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = if (lesson.isPaid) DersiumColors.Income else DersiumColors.PrimaryLight, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
        DersiumFab(label = "Ders Ekle", onClick = onAddLesson, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp))
    }
}

@Composable
private fun SummaryBox(value: String, label: String, bgColor: Color, valColor: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(12.dp), color = bgColor) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(value, style = MaterialTheme.typography.titleSmall, color = valColor, fontWeight = FontWeight.Bold)
            if (label.isNotEmpty()) Text(label, style = MaterialTheme.typography.labelSmall, color = DersiumColors.TextSecondary)
        }
    }
}

@Composable
private fun LessonListItem(lesson: Lesson, currency: String, onMarkPaid: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    val fmt = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("tr"))
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = DersiumColors.SurfaceVariant) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DersiumAvatar(initials = lesson.studentName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""), colorHex = lesson.studentAvatarColor, size = 42)
                Column(Modifier.weight(1f)) {
                    Text(lesson.studentName, style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text("${lesson.date.format(fmt)} · ${lesson.durationMinutes} dk", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(lesson.fee.formatCurrency(currency), style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                    StatusChip(if (lesson.isPaid) "✓ Ödendi" else "Bekleyen", if (lesson.isPaid) DersiumColors.Income else DersiumColors.Pending)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                if (lesson.topic.isNotEmpty()) Text(lesson.topic, style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary, modifier = Modifier.weight(1f)) else Spacer(Modifier.weight(1f))
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Edit, null, tint = DersiumColors.Primary, modifier = Modifier.size(16.dp)) }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, null, tint = DersiumColors.Expense, modifier = Modifier.size(16.dp)) }
            }
        }
    }
}
