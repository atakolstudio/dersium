package com.dersium.feature.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dersium.core.ui.components.DersiumAvatar
import com.dersium.core.ui.components.StatusChip
import com.dersium.core.ui.components.formatCurrency
import com.dersium.core.ui.theme.DersiumColors

@Composable
fun ReportsScreen(viewModel: ReportsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().background(DersiumColors.Background)) {
        // Title
        Text(
            "Raporlar",
            style = MaterialTheme.typography.headlineMedium,
            color = DersiumColors.TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.statusBarsPadding().padding(horizontal = 20.dp, vertical = 16.dp),
        )

        // Tab strip (horizontal scroll)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ReportTab.entries.forEach { t ->
                FilterChip(
                    selected = state.tab == t,
                    onClick = { viewModel.setTab(t) },
                    label = { Text(t.label, style = MaterialTheme.typography.labelMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DersiumColors.Primary,
                        selectedLabelColor = Color.White,
                    ),
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                when (state.tab) {
                    ReportTab.STUDENT -> StudentReport(state)
                    ReportTab.AVERAGE -> AverageReport(state)
                    ReportTab.MONTHLY -> MonthlyReport(state)
                    ReportTab.ACTIVE -> ActiveReport(state)
                    ReportTab.PAYMENT -> PaymentReport(state)
                    ReportTab.PENDING -> PendingReport(state)
                    ReportTab.DAILY -> DailyReport(state)
                    ReportTab.SEASON -> SeasonReport(state)
                }
            }
        }
    }
}

// ── Student Report ─────────────────────────────────────────────────────────────
@Composable
private fun StudentReport(state: ReportsUiState) {
    ReportCard(title = "Öğrenci Bazlı Kazanç", subtitle = "${state.studentIncomes.size} öğrenci · Toplam ${state.totalIncome.formatCurrency(state.currency)}", icon = Icons.Default.Person) {
        state.studentIncomes.forEachIndexed { idx, si ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DersiumAvatar(initials = si.student.initials, colorHex = si.student.avatarColor, size = 40)
                Column(modifier = Modifier.weight(1f)) {
                    Text(si.student.fullName, style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text("${si.lessonCount} ders · ${si.student.paymentType.displayName}", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
                    LinearProgressIndicator(
                        progress = { if (si.totalIncome > 0) (si.paidAmount / si.totalIncome).toFloat() else 0f },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        color = DersiumColors.Income,
                        trackColor = DersiumColors.Outline,
                    )
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("✓ ${si.paidAmount.formatCurrency(state.currency)}", style = MaterialTheme.typography.labelSmall, color = DersiumColors.Income)
                        Text("%${((si.paidAmount / si.totalIncome.coerceAtLeast(1.0)) * 100).toInt()} ödendi", style = MaterialTheme.typography.labelSmall, color = DersiumColors.TextTertiary)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(si.totalIncome.formatCurrency(state.currency), style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                    Text("${si.student.lessonFee.toInt()}₺/ders", style = MaterialTheme.typography.labelSmall, color = DersiumColors.TextSecondary)
                    StatusChip(if (si.student.isActive) "Aktif" else "Pasif", if (si.student.isActive) DersiumColors.Income else DersiumColors.TextTertiary)
                }
            }
            if (idx < state.studentIncomes.lastIndex) HorizontalDivider(color = DersiumColors.Outline, modifier = Modifier.padding(vertical = 4.dp))
        }
    }
}

// ── Average Report ─────────────────────────────────────────────────────────────
@Composable
private fun AverageReport(state: ReportsUiState) {
    ReportCard(title = "Ders Başına Ortalama Kazanç", subtitle = "Genel performans özeti", icon = Icons.Default.Calculate) {
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = DersiumColors.PrimaryContainer) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Ders Başına Ortalama", style = MaterialTheme.typography.bodyMedium, color = DersiumColors.PrimaryLight)
                Text(state.averagePerLesson.formatCurrency(state.currency), style = MaterialTheme.typography.displaySmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniStatCard("${state.totalLessons}", "Toplam Ders", Icons.Default.School, DersiumColors.Primary, Modifier.weight(1f))
            MiniStatCard(state.totalIncome.formatCurrency(state.currency), "Toplam Kazanç", Icons.Default.AttachMoney, DersiumColors.Income, Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniStatCard(state.minIncome.formatCurrency(state.currency), "En Düşük", Icons.Default.ArrowDownward, DersiumColors.TextSecondary, Modifier.weight(1f))
            MiniStatCard(state.maxIncome.formatCurrency(state.currency), "En Yüksek", Icons.Default.ArrowUpward, DersiumColors.Income, Modifier.weight(1f))
        }
    }
}

// ── Monthly Report ─────────────────────────────────────────────────────────────
@Composable
private fun MonthlyReport(state: ReportsUiState) {
    ReportCard(title = "Aylık Kazanç Trendi", subtitle = "${state.monthlyData.size} ay", icon = Icons.Default.TrendingUp) {
        // Simple bar chart using Box heights
        if (state.monthlyData.isNotEmpty()) {
            val maxIncome = state.monthlyData.maxOf { it.income }.coerceAtLeast(1.0)
            Row(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.Bottom,
            ) {
                state.monthlyData.forEach { m ->
                    val fraction = (m.income / maxIncome).toFloat().coerceIn(0.05f, 1f)
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.weight(1f).fillMaxHeight()) {
                        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(fraction).background(DersiumColors.Primary.copy(alpha = 0.7f), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)))
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)) {
                state.monthlyData.forEach { m ->
                    Text(m.month.takeLast(2), style = MaterialTheme.typography.labelSmall, color = DersiumColors.TextTertiary, modifier = Modifier.weight(1f))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        state.monthlyData.lastOrNull()?.let { last ->
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = DersiumColors.SurfaceElevated) {
                Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(last.month, style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                        StatusChip("${last.lessonCount} ders", DersiumColors.Primary)
                    }
                    Text(last.income.formatCurrency(state.currency), style = MaterialTheme.typography.titleSmall, color = DersiumColors.Income, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── Active students ────────────────────────────────────────────────────────────
@Composable
private fun ActiveReport(state: ReportsUiState) {
    ReportCard(title = "En Çok Ders Verilen Öğrenciler", subtitle = "İlk ${state.studentIncomes.size} öğrenci", icon = Icons.Default.EmojiEvents) {
        state.studentIncomes.forEachIndexed { i, si ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(shape = RoundedCornerShape(8.dp), color = if (i == 0) DersiumColors.Pending else DersiumColors.SurfaceElevated) {
                    Text("${i + 1}", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.titleSmall, color = if (i == 0) Color.Black else DersiumColors.TextSecondary, fontWeight = FontWeight.Bold)
                }
                DersiumAvatar(initials = si.student.initials, colorHex = si.student.avatarColor, size = 36)
                Column(modifier = Modifier.weight(1f)) {
                    Text(si.student.fullName, style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary)
                    Text("${si.lessonCount} ders", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
                }
                StatusChip("Aktif", DersiumColors.Income)
                Text(si.totalIncome.formatCurrency(state.currency), style = MaterialTheme.typography.titleSmall, color = DersiumColors.Income, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Payment status ─────────────────────────────────────────────────────────────
@Composable
private fun PaymentReport(state: ReportsUiState) {
    ReportCard(title = "Ödeme Durumu", subtitle = "Tahsilat analizi", icon = Icons.Default.AccountBalance) {
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = DersiumColors.SurfaceElevated) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tahsilat Oranı", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("%.1f%%".format(state.collectionRate), style = MaterialTheme.typography.headlineMedium, color = DersiumColors.Income, fontWeight = FontWeight.Bold)
                    StatusChip(if (state.collectionRate >= 90) "İyi" else "Dikkat", if (state.collectionRate >= 90) DersiumColors.Income else DersiumColors.Pending)
                }
                LinearProgressIndicator(progress = { (state.collectionRate / 100f).toFloat() }, modifier = Modifier.fillMaxWidth(), color = DersiumColors.Income, trackColor = DersiumColors.Outline)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniStatCard("${state.paidLessons}/${state.totalLessons}", "Ödenen Ders", Icons.Default.CheckCircle, DersiumColors.Income, Modifier.weight(1f))
            MiniStatCard("${state.pendingLessons}", "Bekleyen Ders", Icons.Default.HourglassEmpty, DersiumColors.Pending, Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniStatCard(state.paidAmount.formatCurrency(state.currency), "Ödenen Tutar", Icons.Default.AttachMoney, DersiumColors.Income, Modifier.weight(1f))
            MiniStatCard(state.pendingAmount.formatCurrency(state.currency), "Bekleyen Tutar", Icons.Default.Warning, DersiumColors.Pending, Modifier.weight(1f))
        }
    }
}

// ── Pending ────────────────────────────────────────────────────────────────────
@Composable
private fun PendingReport(state: ReportsUiState) {
    ReportCard(title = "Bekleyen Ödemeler", subtitle = "${state.pendingLessons} öğrenci · Toplam ${state.pendingAmount.formatCurrency(state.currency)}", icon = Icons.Default.HourglassEmpty) {
        if (state.pendingAmount == 0.0) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.CheckCircle, null, tint = DersiumColors.Income, modifier = Modifier.size(48.dp))
                Text("Bekleyen ödeme yok!", style = MaterialTheme.typography.titleMedium, color = DersiumColors.Income, fontWeight = FontWeight.Bold)
                Text("Tüm ödemeler tahsil edilmiş", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
            }
        } else {
            Text(state.pendingAmount.formatCurrency(state.currency), style = MaterialTheme.typography.headlineMedium, color = DersiumColors.Pending, fontWeight = FontWeight.Bold)
        }
    }
}

// ── Daily ──────────────────────────────────────────────────────────────────────
@Composable
private fun DailyReport(state: ReportsUiState) {
    ReportCard(title = "Gün Bazlı Analiz", subtitle = "Haftanın en verimli günleri", icon = Icons.Default.CalendarToday) {
        val maxIncome = state.dayData.maxOfOrNull { it.income }?.coerceAtLeast(1.0) ?: 1.0
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Haftalık Dağılım", style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary); Text("${state.totalLessons} toplam ders", style = MaterialTheme.typography.labelSmall, color = DersiumColors.TextSecondary) }
        Row(
            modifier = Modifier.fillMaxWidth().height(100.dp).padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
        ) {
            state.dayData.forEach { day ->
                val fraction = (day.income / maxIncome).toFloat().coerceIn(0.05f, 1f)
                val isTop = day == state.bestDay
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.weight(1f).fillMaxHeight()) {
                    if (day.lessonCount > 0) Text("${day.lessonCount}", style = MaterialTheme.typography.labelSmall, color = DersiumColors.TextTertiary)
                    Spacer(Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .fillMaxHeight(fraction)
                            .background(
                                if (isTop) DersiumColors.Primary else DersiumColors.Primary.copy(alpha = 0.4f),
                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                            )
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            state.dayData.forEach { day ->
                Text(day.day, style = MaterialTheme.typography.labelSmall, color = if (day == state.bestDay) DersiumColors.Primary else DersiumColors.TextTertiary)
            }
        }
        state.bestDay?.let { best ->
            Spacer(Modifier.height(12.dp))
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = DersiumColors.SurfaceElevated) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("⭐", style = MaterialTheme.typography.titleSmall)
                        Text("En verimli gün: ${best.day}", style = MaterialTheme.typography.bodyMedium, color = DersiumColors.Income)
                    }
                    Text(best.income.formatCurrency(state.currency), style = MaterialTheme.typography.titleSmall, color = DersiumColors.Income, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── Season ─────────────────────────────────────────────────────────────────────
@Composable
private fun SeasonReport(state: ReportsUiState) {
    ReportCard(title = "Sezon Karşılaştırması", subtitle = "1 sezon", icon = Icons.Default.CompareArrows) {
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = DersiumColors.SurfaceElevated) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CalendarMonth, null, tint = DersiumColors.Primary, modifier = Modifier.size(20.dp))
                        Text(state.seasonName, style = MaterialTheme.typography.titleMedium, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                    }
                    StatusChip("İyi", DersiumColors.Income)
                }
                Text("Tahsilat Oranı", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Tahsilat Oranı", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
                    Text("%${state.collectionRate.toInt()}", style = MaterialTheme.typography.bodySmall, color = DersiumColors.Income)
                }
                LinearProgressIndicator(progress = { (state.collectionRate / 100f).toFloat() }, modifier = Modifier.fillMaxWidth(), color = DersiumColors.Income, trackColor = DersiumColors.Outline)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip(state.totalIncome.formatCurrency(state.currency) + " Toplam", DersiumColors.Primary)
                    StatusChip(state.paidAmount.formatCurrency(state.currency) + " Ödenen", DersiumColors.Income)
                    StatusChip(state.pendingAmount.formatCurrency(state.currency) + " Bekleyen", DersiumColors.Pending)
                }
                Text("${state.totalLessons} ders · ${state.studentIncomes.size} öğrenci · Ort. ${state.averagePerLesson.formatCurrency(state.currency)}/ders", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
            }
        }
    }
}

// ── Shared UI helpers ──────────────────────────────────────────────────────────
@Composable
private fun ReportCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = RoundedCornerShape(12.dp), color = DersiumColors.PrimaryContainer) {
                Icon(icon, null, tint = DersiumColors.Primary, modifier = Modifier.padding(10.dp).size(22.dp))
            }
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
            }
        }
        content()
    }
}

@Composable
private fun MiniStatCard(value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = DersiumColors.SurfaceVariant) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
        }
    }
}
