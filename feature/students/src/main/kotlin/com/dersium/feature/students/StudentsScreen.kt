package com.dersium.feature.students

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.dersium.core.domain.model.Student
import com.dersium.core.ui.components.*
import com.dersium.core.ui.theme.DersiumColors
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun StudentsScreen(
    onStudentClick: (Long) -> Unit,
    onAddStudent: () -> Unit,
    viewModel: StudentsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(DersiumColors.Background)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        "Öğrenciler",
                        style = MaterialTheme.typography.headlineMedium,
                        color = DersiumColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        state.activeSeasonName.ifEmpty { "2025-2026" },
                        style = MaterialTheme.typography.bodySmall,
                        color = DersiumColors.TextSecondary,
                    )
                }
                IconButton(onClick = viewModel::toggleActiveFilter) {
                    Icon(Icons.Default.FilterList, null, tint = DersiumColors.TextSecondary)
                }
            }

            // ── Search bar ─────────────────────────────────────────────────────
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("İsim veya okul ara…", color = DersiumColors.TextTertiary) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = DersiumColors.TextSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = DersiumColors.Primary,
                    unfocusedBorderColor = DersiumColors.Outline,
                    focusedContainerColor   = DersiumColors.SurfaceVariant,
                    unfocusedContainerColor = DersiumColors.SurfaceVariant,
                ),
            )

            // ── Filter chips ───────────────────────────────────────────────────
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = !state.showOnlyActive,
                    onClick  = { if (state.showOnlyActive) viewModel.toggleActiveFilter() },
                    label    = { Text("Tümü (${state.students.size})") },
                    leadingIcon = { if (!state.showOnlyActive) Icon(Icons.Default.Check, null, Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DersiumColors.Income,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White,
                    ),
                )
                FilterChip(
                    selected = state.showOnlyActive,
                    onClick  = { if (!state.showOnlyActive) viewModel.toggleActiveFilter() },
                    label    = { Text("Aktif (${state.students.count { it.isActive }})") },
                    leadingIcon = { if (state.showOnlyActive) Icon(Icons.Default.Check, null, Modifier.size(16.dp)) },
                )
            }

            // ── Premium limit banner ───────────────────────────────────────────
            val atLimit = !state.isPremium && state.students.size >= state.maxFreeStudents
            if (atLimit) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = DersiumColors.ExpenseContainer,
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(Icons.Default.Lock, null, tint = DersiumColors.Expense, modifier = Modifier.size(22.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Öğrenci Limitine Ulaştınız", style = MaterialTheme.typography.titleSmall, color = DersiumColors.Expense, fontWeight = FontWeight.SemiBold)
                            Text("Premium'a geçerek sınırsız öğrenci ekleyin", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
                        }
                        Text("Premium", style = MaterialTheme.typography.labelMedium, color = DersiumColors.Primary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ── List ───────────────────────────────────────────────────────────
            if (state.isLoading) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(3) { ShimmerCard() }
                }
            } else if (state.filteredStudents.isEmpty()) {
                DersiumEmptyState(
                    icon = Icons.Outlined.People,
                    title = "Öğrenci bulunamadı",
                    subtitle = if (state.searchQuery.isNotEmpty()) "Arama kriterlerinizi değiştirin" else "Sağ alttaki butona basarak öğrenci ekleyin",
                    modifier = Modifier.weight(1f),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.filteredStudents, key = { it.id }) { student ->
                        // Use enriched student card that matches screenshot
                        EnrichedStudentCard(
                            student         = student,
                            lessonCount     = state.lessonCountMap[student.id] ?: 0,
                            paidAmount      = state.paidAmountMap[student.id] ?: 0.0,
                            pendingAmount   = state.pendingAmountMap[student.id] ?: 0.0,
                            lastLessonDate  = state.lastLessonDateMap[student.id],
                            currency        = state.currency,
                            onClick         = { onStudentClick(student.id) },
                            modifier        = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
            }
        }

        DersiumFab(
            label  = "Öğrenci Ekle",
            icon   = Icons.Default.PersonAdd,
            onClick = onAddStudent,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        )
    }
}

@Composable
private fun EnrichedStudentCard(
    student: com.dersium.core.domain.model.Student,
    lessonCount: Int,
    paidAmount: Double,
    pendingAmount: Double,
    lastLessonDate: java.time.LocalDate?,
    currency: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateFmt = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("tr"))
    Surface(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = DersiumColors.SurfaceVariant,
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Name row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DersiumAvatar(initials = student.initials, colorHex = student.avatarColor, size = 48)
                Column(modifier = Modifier.weight(1f)) {
                    Text(student.fullName, style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                    if (student.school.isNotEmpty())
                        Text(student.school, style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
                }
                StatusChip(
                    label = if (student.isActive) "Aktif" else "Pasif",
                    color = if (student.isActive) DersiumColors.Income else DersiumColors.TextTertiary,
                )
                Icon(Icons.Default.ChevronRight, null, tint = DersiumColors.TextTertiary, modifier = Modifier.size(20.dp))
            }

            // Stats row (matches Image 4: Ders | Ödendi | Bekleyen | Son Ders)
            if (lessonCount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    MiniStat(Icons.Default.School, lessonCount.toString(), "Ders")
                    MiniStat(Icons.Default.CheckCircle, paidAmount.formatCurrency(currency), "Ödendi", DersiumColors.Income)
                    MiniStat(Icons.Outlined.AccessTime, if (pendingAmount > 0) pendingAmount.formatCurrency(currency) else "—", "Bekleyen", DersiumColors.Pending)
                    MiniStat(Icons.Outlined.CalendarToday, lastLessonDate?.format(dateFmt) ?: "—", "Son Ders")
                }
            }

            // Fee chip
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                StatusChip(
                    label = "${student.lessonFee.toInt()}₺/ders",
                    color = DersiumColors.Primary,
                )
            }
        }
    }
}

@Composable
private fun MiniStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    valueColor: Color = DersiumColors.TextPrimary,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Icon(icon, null, tint = valueColor.let { if (it == DersiumColors.TextPrimary) DersiumColors.TextSecondary else it }, modifier = Modifier.size(16.dp))
        Text(value, style = MaterialTheme.typography.labelMedium, color = valueColor, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = DersiumColors.TextTertiary)
    }
}
