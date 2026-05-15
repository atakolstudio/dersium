package com.dersium.feature.students

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dersium.core.domain.model.Lesson
import com.dersium.core.domain.model.PaymentStatus
import com.dersium.core.ui.components.*
import com.dersium.core.ui.theme.DersiumColors
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailScreen(
    studentId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onAddLesson: () -> Unit,
    viewModel: StudentDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(studentId) { viewModel.loadStudent(studentId) }

    Scaffold(
        containerColor = DersiumColors.Background,
        topBar = {
            TopAppBar(
                title = { Text(state.student?.fullName ?: "", color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DersiumColors.TextPrimary) } },
                actions = { IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = DersiumColors.TextSecondary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DersiumColors.Background),
            )
        },
        floatingActionButton = { DersiumFab(label = "Ders Ekle", onClick = onAddLesson) },
    ) { padding ->
        val student = state.student
        if (student == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DersiumColors.Primary)
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = DersiumColors.SurfaceVariant,
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            DersiumAvatar(initials = student.initials, colorHex = student.avatarColor, size = 60)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(student.fullName, style = MaterialTheme.typography.titleLarge, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                                if (student.school.isNotEmpty())
                                    Text("${student.school} · ${student.grade}", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
                            }
                            StatusChip(if (student.isActive) "Aktif" else "Pasif", if (student.isActive) DersiumColors.Income else DersiumColors.TextTertiary)
                        }
                        HorizontalDivider(color = DersiumColors.Outline)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatMini("${state.lessons.size}", "Ders", Modifier.weight(1f))
                            StatMini(state.paidAmount.formatCurrency(), "Ödendi", Modifier.weight(1f))
                            StatMini(state.pendingAmount.formatCurrency(), "Bekleyen", Modifier.weight(1f))
                            val lastDate = state.lessons.firstOrNull()?.date?.format(DateTimeFormatter.ofPattern("d MMM", Locale("tr"))) ?: "-"
                            StatMini(lastDate, "Son Ders", Modifier.weight(1f))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            StatusChip("${student.lessonFee.toInt()}₺/ders · ${student.paymentType.displayName}", DersiumColors.Primary)
                        }
                    }
                }
            }
            item { SectionHeader(title = "Dersler", modifier = Modifier.padding(horizontal = 16.dp)) }
            if (state.lessons.isEmpty()) {
                item { DersiumEmptyState(icon = Icons.Default.School, title = "Henüz ders yok", subtitle = "FAB'a basarak ders ekleyin") }
            } else {
                items(state.lessons, key = { it.id }) { lesson ->
                    StudentLessonCard(
                        lesson = lesson,
                        onTogglePayment = { viewModel.togglePayment(lesson) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StudentLessonCard(
    lesson: Lesson,
    onTogglePayment: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fmt = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("tr"))
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = DersiumColors.SurfaceVariant) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(lesson.date.format(fmt), style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                if (lesson.topic.isNotEmpty())
                    Text(lesson.topic, style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
                Text("${lesson.durationMinutes} dk", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextTertiary)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(lesson.fee.formatCurrency(), style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                // Toggle butonu — hem ödeme hem de geri alma
                if (lesson.isPaid) {
                    OutlinedButton(
                        onClick = onTogglePayment,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DersiumColors.Income),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DersiumColors.Income.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Ödendi", style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    Button(
                        onClick = onTogglePayment,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DersiumColors.Primary),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("Tahsil Et", style = MaterialTheme.typography.labelSmall, color = androidx.compose.ui.graphics.Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatMini(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = DersiumColors.TextSecondary)
    }
}

private fun Double.formatCurrency(currency: String = "₺"): String =
    "$currency${String.format("%,.0f", this)}"
