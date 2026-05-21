package com.dersium.feature.home

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dersium.core.common.DersiumAnalytics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dersium.core.domain.model.Lesson
import com.dersium.core.domain.model.PaymentStatus
import com.dersium.core.ui.components.*
import com.dersium.core.ui.theme.DersiumColors
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    onAddLesson: () -> Unit,
    onNavigateToStudents: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onStudentClick: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { DersiumAnalytics.logScreenView("home") }

    Box(modifier = Modifier.fillMaxSize().background(DersiumColors.Background)) {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 100.dp)) {
            item { HomeHeader(state = state, onNavigateToSettings = onNavigateToSettings) }

            // Paid / Pending
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    PaidPendingCard("Ödendi", state.paidAmount.formatCurrency(state.currency), Icons.Default.CheckCircle, DersiumColors.Income, Modifier.weight(1f))
                    PaidPendingCard("Bekleyen", state.pendingAmount.formatCurrency(state.currency), Icons.Outlined.AccessTime, DersiumColors.Pending, Modifier.weight(1f))
                }
            }

            // Financial section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Finansal", style = MaterialTheme.typography.titleLarge, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FinancialHomeCard(
                            label = "Ek Gelirler",
                            amount = state.extraIncome.formatCurrency(state.currency),
                            icon = Icons.Default.TrendingUp,
                            bgColor = Color(0xFF1A4D2E),
                            iconColor = DersiumColors.Income,
                            onClick = {},
                            modifier = Modifier.weight(1f),
                        )
                        FinancialHomeCard(
                            label = "Giderler",
                            amount = state.expenses.formatCurrency(state.currency),
                            icon = Icons.Default.TrendingDown,
                            bgColor = Color(0xFF4D1A1A),
                            iconColor = DersiumColors.Expense,
                            onClick = {},
                            modifier = Modifier.weight(1f),
                        )
                    }
                    // Net durum
                    val netColor = if (state.netAmount >= 0) DersiumColors.Income else DersiumColors.Expense
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable(onClick = onNavigateToReports),
                        shape = RoundedCornerShape(14.dp),
                        color = netColor.copy(alpha = 0.10f),
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.TrendingUp, null, tint = netColor, modifier = Modifier.size(18.dp))
                                Text("Genel Net Durum", style = MaterialTheme.typography.bodyMedium, color = DersiumColors.TextPrimary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(state.netAmount.formatCurrency(state.currency), style = MaterialTheme.typography.titleSmall, color = netColor, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.ChevronRight, null, tint = DersiumColors.TextTertiary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Son Dersler
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Son Dersler", style = MaterialTheme.typography.titleLarge, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onNavigateToStudents) {
                        Text("Tümünü Gör →", style = MaterialTheme.typography.labelMedium, color = DersiumColors.Primary)
                    }
                }
            }

            if (state.isLoading) {
                items(3) { ShimmerCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) }
            } else if (state.recentLessons.isEmpty()) {
                item {
                    DersiumEmptyState(
                        icon = Icons.Outlined.School,
                        title = "Henüz ders yok",
                        subtitle = "Sağ alttaki butona basarak ders ekleyin",
                        modifier = Modifier.padding(32.dp),
                    )
                }
            } else {
                items(state.recentLessons, key = { it.id }) { lesson ->
                    HomeLessonCard(
                        lesson = lesson,
                        currency = state.currency,
                        onMarkPaid = { viewModel.markLessonPaid(lesson.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).clickable { onStudentClick(lesson.studentId) },
                    )
                }
            }
        }

        DersiumFab(
            label = "Ders Ekle",
            icon = Icons.Default.Add,
            onClick = onAddLesson,
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 16.dp, end = 16.dp),
        )
    }
}

@Composable
private fun HomeHeader(state: HomeUiState, onNavigateToSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(colors = listOf(Color(0xFF1C1C40), DersiumColors.Background)))
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column {
                Text("Özel Ders", style = MaterialTheme.typography.headlineMedium, color = DersiumColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
                Text("Yönetim Paneli", style = MaterialTheme.typography.bodyMedium, color = DersiumColors.TextSecondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (state.activeSeasonName.isNotEmpty()) {
                    Surface(shape = RoundedCornerShape(10.dp), color = DersiumColors.Primary) {
                        Text(state.activeSeasonName, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
                IconButton(onClick = onNavigateToSettings, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Settings, null, tint = DersiumColors.TextSecondary, modifier = Modifier.size(22.dp))
                }
            }
        }

        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = DersiumColors.Primary.copy(alpha = 0.35f)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Bu Ay Kazancı", style = MaterialTheme.typography.bodyMedium, color = DersiumColors.TextSecondary)
                Text(state.thisMonthIncome.formatCurrency(state.currency), style = MaterialTheme.typography.displaySmall.copy(fontSize = 40.sp), color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickStatChip("${state.studentCount} Öğrenci", Icons.Filled.People)
                    QuickStatChip("${state.todayLessons} Bugün", Icons.Filled.Today)
                    QuickStatChip("${state.recentLessons.size} Son Ders", Icons.Filled.School)
                    
                }
            }
        }
    }
}

@Composable
private fun QuickStatChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(shape = RoundedCornerShape(20.dp), color = DersiumColors.SurfaceVariant.copy(alpha = 0.8f)) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, tint = DersiumColors.TextSecondary, modifier = Modifier.size(13.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = DersiumColors.TextSecondary)
        }
    }
}

@Composable
private fun PaidPendingCard(label: String, amount: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = DersiumColors.SurfaceVariant) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            Column {
                Text(amount, style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                Text(label, style = MaterialTheme.typography.labelSmall, color = DersiumColors.TextSecondary)
            }
        }
    }
}

@Composable
private fun FinancialHomeCard(label: String, amount: String, icon: androidx.compose.ui.graphics.vector.ImageVector, bgColor: Color, iconColor: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick), shape = RoundedCornerShape(16.dp), color = bgColor) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
                Icon(Icons.Default.ChevronRight, null, tint = iconColor.copy(0.5f), modifier = Modifier.size(16.dp))
            }
            Text(amount, style = MaterialTheme.typography.titleMedium, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = DersiumColors.TextSecondary)
        }
    }
}

@Composable
fun HomeLessonCard(lesson: Lesson, currency: String, onMarkPaid: () -> Unit, modifier: Modifier = Modifier) {
    val fmt = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("tr"))
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = DersiumColors.SurfaceVariant) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DersiumAvatar(
                initials = lesson.studentName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                colorHex = lesson.studentAvatarColor, size = 44,
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(lesson.studentName, style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                Text("${lesson.date.format(fmt)} · ${lesson.durationMinutes} dk", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(lesson.fee.formatCurrency(currency), style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                StatusChip(if (lesson.isPaid) "✓ Ödendi" else "Bekleyen", if (lesson.isPaid) DersiumColors.Income else DersiumColors.Pending)
            }
        }
    }
}
