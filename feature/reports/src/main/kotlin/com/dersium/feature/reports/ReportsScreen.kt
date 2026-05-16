package com.dersium.feature.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import com.dersium.core.ui.components.*
import com.dersium.core.ui.theme.DersiumColors

private fun Double.fmt(c: String = "₺") = "$c${String.format("%,.0f", this)}"

@Composable
fun ReportsScreen(viewModel: ReportsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(modifier = Modifier.fillMaxSize().background(DersiumColors.Background)) {
        Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text("Raporlar", style = MaterialTheme.typography.headlineMedium, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
        }
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            items(ReportTab.entries) { tab ->
                FilterChip(selected = state.tab == tab, onClick = { viewModel.setTab(tab) }, label = { Text(tab.label) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = DersiumColors.Primary, selectedLabelColor = Color.White))
            }
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                when (state.tab) {
                    ReportTab.STUDENT -> StudentReport(state)
                    ReportTab.AVERAGE -> AverageReport(state)
                    ReportTab.MONTHLY -> MonthlyReport(state)
                    ReportTab.ACTIVE  -> ActiveReport(state)
                    ReportTab.PAYMENT -> PaymentReport(state)
                    ReportTab.PENDING -> PendingReport(state)
                    ReportTab.DAILY   -> DailyReport(state)
                    ReportTab.SEASON  -> SeasonReport(state)
                }
            }
        }
    }
}

@Composable
private fun StudentReport(state: ReportsUiState) {
    ReportCard("Öğrenci Bazlı Kazanç", "${state.studentIncomes.size} öğrenci · Toplam ${state.totalIncome.fmt(state.currency)}", Icons.Default.Person) {
        if (state.studentIncomes.isEmpty()) {
            DersiumEmptyState(icon = Icons.Default.Person, title = "Henüz öğrenci yok", subtitle = "Öğrenci ekleyerek başlayın")
        } else {
            state.studentIncomes.forEach { si ->
                val rate = if (si.totalIncome > 0) (si.paidAmount / si.totalIncome * 100).toInt() else 0
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = DersiumColors.SurfaceVariant) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                DersiumAvatar(initials = si.student.initials.ifEmpty { si.student.name.take(2).uppercase() }, colorHex = si.student.avatarColor, size = 40)
                                Column {
                                    Text(si.student.fullName, style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                                    Text("${si.lessonCount} ders · ${si.student.paymentType.displayName}", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(si.totalIncome.fmt(state.currency), style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                                Text("${si.student.lessonFee.toInt()}${state.currency}/ders", style = MaterialTheme.typography.labelSmall, color = DersiumColors.TextSecondary)
                            }
                        }
                        LinearProgressIndicator(progress = { rate / 100f }, modifier = Modifier.fillMaxWidth(), color = DersiumColors.Income, trackColor = DersiumColors.Outline)
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("✓ ${si.paidAmount.fmt(state.currency)}", style = MaterialTheme.typography.labelSmall, color = DersiumColors.Income)
                            Text("%$rate ödendi", style = MaterialTheme.typography.labelSmall, color = DersiumColors.TextSecondary)
                            StatusChip(if (si.student.isActive) "Aktif" else "Pasif", if (si.student.isActive) DersiumColors.Income else DersiumColors.TextTertiary)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun AverageReport(state: ReportsUiState) {
    ReportCard("Ders Başına Ortalama Kazanç", "Genel performans özeti", Icons.Default.Calculate) {
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = DersiumColors.PrimaryContainer) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Ders Başına Ortalama", style = MaterialTheme.typography.bodyMedium, color = DersiumColors.PrimaryLight)
                Text(state.averagePerLesson.fmt(state.currency), style = MaterialTheme.typography.displaySmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniStat("${state.totalLessons}", "Toplam Ders", Icons.Default.School, DersiumColors.Primary, Modifier.weight(1f))
            MiniStat(state.totalIncome.fmt(state.currency), "Toplam Kazanç", Icons.Default.AttachMoney, DersiumColors.Income, Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniStat(state.minIncome.fmt(state.currency), "En Düşük", Icons.Default.ArrowDownward, DersiumColors.TextSecondary, Modifier.weight(1f))
            MiniStat(state.maxIncome.fmt(state.currency), "En Yüksek", Icons.Default.ArrowUpward, DersiumColors.Income, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MonthlyReport(state: ReportsUiState) {
    val maxIncome = state.monthlyData.maxOfOrNull { it.income } ?: 1.0
    ReportCard("Aylık Kazanç Trendi", "${state.monthlyData.size} ay", Icons.AutoMirrored.Filled.TrendingUp) {
        if (state.monthlyData.isEmpty()) {
            DersiumEmptyState(icon = Icons.Default.BarChart, title = "Henüz veri yok", subtitle = "Ders ekleyerek başlayın")
        } else {
            Row(modifier = Modifier.fillMaxWidth().height(120.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                state.monthlyData.takeLast(6).forEach { m ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Bottom) {
                        val barH = ((m.income / maxIncome) * 100).dp.coerceAtLeast(4.dp)
                        Box(modifier = Modifier.fillMaxWidth().height(barH).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)).background(DersiumColors.Primary))
                        Text(m.month.takeLast(2), style = MaterialTheme.typography.labelSmall, color = DersiumColors.TextSecondary)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            state.monthlyData.reversed().take(6).forEach { m ->
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = DersiumColors.SurfaceVariant) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(m.month, style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                            StatusChip("${m.lessonCount} ders", DersiumColors.Primary)
                        }
                        Text(m.income.fmt(state.currency), style = MaterialTheme.typography.titleSmall, color = DersiumColors.Income, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun ActiveReport(state: ReportsUiState) {
    ReportCard("En Çok Ders Verilen Öğrenciler", "İlk ${state.studentIncomes.size} öğrenci", Icons.Default.EmojiEvents) {
        state.studentIncomes.forEachIndexed { i, si ->
            val medalColor = when (i) { 0 -> Color(0xFFF59E0B); 1 -> Color(0xFF94A3B8); 2 -> Color(0xFFCD7C39); else -> DersiumColors.TextTertiary }
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(medalColor), contentAlignment = Alignment.Center) {
                    Text("${i+1}", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                }
                DersiumAvatar(initials = si.student.initials.ifEmpty { si.student.name.take(2).uppercase() }, colorHex = si.student.avatarColor, size = 36)
                Column(modifier = Modifier.weight(1f)) {
                    Text(si.student.fullName, style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Medium)
                    Text("${si.lessonCount} ders", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
                }
                StatusChip(if (si.student.isActive) "Aktif" else "Pasif", if (si.student.isActive) DersiumColors.Income else DersiumColors.TextTertiary)
                Text(si.totalIncome.fmt(state.currency), style = MaterialTheme.typography.titleSmall, color = DersiumColors.Income, fontWeight = FontWeight.Bold)
            }
            if (i < state.studentIncomes.lastIndex) HorizontalDivider(color = DersiumColors.Outline)
        }
    }
}

@Composable
private fun PaymentReport(state: ReportsUiState) {
    val rc = if (state.collectionRate >= 75) DersiumColors.Income else if (state.collectionRate >= 50) DersiumColors.Pending else DersiumColors.Expense
    val rl = if (state.collectionRate >= 75) "İyi" else if (state.collectionRate >= 50) "Dikkat" else "Kritik"
    ReportCard("Ödeme Durumu", "Tahsilat analizi", Icons.Default.AccountBalance) {
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = DersiumColors.SurfaceVariant) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Tahsilat Oranı", style = MaterialTheme.typography.bodyMedium, color = DersiumColors.TextSecondary)
                    Text("${String.format("%.1f", state.collectionRate)}%", style = MaterialTheme.typography.titleLarge, color = rc, fontWeight = FontWeight.Bold)
                    StatusChip(rl, rc)
                }
                LinearProgressIndicator(progress = { (state.collectionRate / 100f).toFloat() }, modifier = Modifier.fillMaxWidth(), color = rc, trackColor = DersiumColors.Outline)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniStat("${state.paidLessons}/${state.totalLessons}", "Ödenen Ders", Icons.Default.CheckCircle, DersiumColors.Income, Modifier.weight(1f))
            MiniStat("${state.pendingLessons}", "Bekleyen Ders", Icons.Default.HourglassEmpty, DersiumColors.Pending, Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniStat(state.paidAmount.fmt(state.currency), "Ödenen Tutar", Icons.Default.AttachMoney, DersiumColors.Income, Modifier.weight(1f))
            MiniStat(state.pendingAmount.fmt(state.currency), "Bekleyen Tutar", Icons.Default.Warning, DersiumColors.Pending, Modifier.weight(1f))
        }
    }
}

@Composable
private fun PendingReport(state: ReportsUiState) {
    val pendingStudents = state.studentIncomes.filter { it.totalIncome - it.paidAmount > 0 }
    ReportCard("Bekleyen Ödemeler", "${pendingStudents.size} öğrenci · Toplam ${state.pendingAmount.fmt(state.currency)}", Icons.Default.HourglassEmpty) {
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = DersiumColors.PendingContainer) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Toplam Bekleyen", style = MaterialTheme.typography.bodySmall, color = DersiumColors.Pending)
                Text(state.pendingAmount.fmt(state.currency), style = MaterialTheme.typography.headlineMedium, color = DersiumColors.Pending, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(12.dp))
        if (pendingStudents.isEmpty()) {
            DersiumEmptyState(icon = Icons.Default.CheckCircle, title = "Bekleyen ödeme yok!", subtitle = "Tüm ödemeler tahsil edilmiş")
        } else {
            pendingStudents.forEach { si ->
                val pending = si.totalIncome - si.paidAmount
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = DersiumColors.SurfaceVariant) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DersiumAvatar(initials = si.student.initials.ifEmpty { si.student.name.take(2).uppercase() }, colorHex = si.student.avatarColor, size = 40)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(si.student.fullName, style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                            Text("${si.lessonCount} ders", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
                            Text("Ödenen: ${si.paidAmount.fmt(state.currency)}", style = MaterialTheme.typography.labelSmall, color = DersiumColors.Income)
                        }
                        Text(pending.fmt(state.currency), style = MaterialTheme.typography.titleMedium, color = DersiumColors.Pending, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DailyReport(state: ReportsUiState) {
    val maxL = state.dayData.maxOfOrNull { it.lessonCount } ?: 1
    ReportCard("Gün Bazlı Analiz", "Haftanın en verimli günleri", Icons.Default.CalendarViewDay) {
        Row(modifier = Modifier.fillMaxWidth().height(100.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            state.dayData.forEach { d ->
                val isMax = d.dayOfWeek == state.bestDay?.dayOfWeek
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Bottom) {
                    if (d.lessonCount > 0) Text("${d.lessonCount}", style = MaterialTheme.typography.labelSmall, color = DersiumColors.TextSecondary)
                    val barH = ((d.lessonCount.toFloat() / maxL) * 80).dp.coerceAtLeast(2.dp)
                    Box(modifier = Modifier.fillMaxWidth().height(barH).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)).background(if (isMax) DersiumColors.Income else DersiumColors.Primary.copy(0.6f)))
                    Spacer(Modifier.height(4.dp))
                    Text(d.day, style = MaterialTheme.typography.labelSmall, color = if (isMax) DersiumColors.Income else DersiumColors.TextSecondary, fontWeight = if (isMax) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        state.bestDay?.let { best ->
            Spacer(Modifier.height(12.dp))
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = DersiumColors.IncomeContainer) {
                Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Star, null, tint = DersiumColors.Pending, modifier = Modifier.size(20.dp))
                        Text("En verimli gün: ${best.day}", style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                    Text(best.income.fmt(state.currency), style = MaterialTheme.typography.titleSmall, color = DersiumColors.Income, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SeasonReport(state: ReportsUiState) {
    ReportCard("Sezon Karşılaştırması", "${state.allSeasonStats.size} sezon", Icons.AutoMirrored.Filled.CompareArrows) {
        if (state.allSeasonStats.isEmpty()) {
            DersiumEmptyState(icon = Icons.Default.CalendarMonth, title = "Henüz sezon yok", subtitle = "Ayarlar'dan sezon ekleyin")
        } else {
            state.allSeasonStats.forEach { ss ->
                val rc = when { ss.collectionRate >= 90 -> DersiumColors.Income; ss.collectionRate >= 60 -> DersiumColors.Pending; else -> DersiumColors.Expense }
                val rl = when { ss.collectionRate >= 90 -> "İyi"; ss.collectionRate >= 60 -> "Orta"; else -> "Düşük" }
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = if (ss.isActive) DersiumColors.PrimaryContainer else DersiumColors.SurfaceVariant) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.CalendarMonth, null, tint = if (ss.isActive) DersiumColors.PrimaryLight else DersiumColors.TextSecondary, modifier = Modifier.size(20.dp))
                                Column {
                                    Text(ss.season.displayName, style = MaterialTheme.typography.titleMedium, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                                    if (ss.isActive) Text("Aktif", style = MaterialTheme.typography.labelSmall, color = DersiumColors.PrimaryLight)
                                }
                            }
                            StatusChip(rl, rc)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Tahsilat Oranı", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
                            Text("%${ss.collectionRate.toInt()}", style = MaterialTheme.typography.bodySmall, color = rc, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(progress = { (ss.collectionRate / 100f).toFloat() }, modifier = Modifier.fillMaxWidth(), color = rc, trackColor = DersiumColors.Outline)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            StatusChip(ss.totalIncome.fmt(state.currency) + " Toplam", DersiumColors.Primary)
                            StatusChip(ss.paidAmount.fmt(state.currency) + " Ödenen", DersiumColors.Income)
                            if (ss.pendingAmount > 0) StatusChip(ss.pendingAmount.fmt(state.currency) + " Bekleyen", DersiumColors.Pending)
                        }
                        Text("${ss.lessonCount} ders · ${ss.studentCount} öğrenci · Ort. ${ss.avgPerLesson.fmt(state.currency)}/ders", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ReportCard(title: String, subtitle: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
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
private fun MiniStat(value: String, label: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = DersiumColors.SurfaceVariant) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
        }
    }
}
