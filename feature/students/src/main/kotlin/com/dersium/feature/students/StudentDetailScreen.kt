package com.dersium.feature.students

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dersium.core.domain.model.Lesson
import com.dersium.core.domain.model.PaymentType
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
    val context = LocalContext.current
    LaunchedEffect(studentId) { viewModel.loadStudent(studentId) }

    val student = state.student
    val dateFmt = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("tr"))

    var showParentDialog by remember { mutableStateOf(false) }
    var showMessageDialog by remember { mutableStateOf(false) }
    var selectedParent by remember { mutableStateOf("anne") }
    var editableMessage by remember { mutableStateOf("") }

    fun buildMessage(parentName: String, isMother: Boolean): String {
        if (student == null) return ""
        val salutation = if (isMother) "Hanım" else "Bey"
        val totalLessons = state.lessons.size
        val pendingAmt = "${String.format("%,.0f", state.pendingAmount)}₺"
        val dateLines = state.lessons.sortedBy { it.date }.joinToString("\n") { "- ${it.date.format(dateFmt)}" }
        val weekText = when {
            totalLessons == 1 -> "1 dersimiz oldu"
            totalLessons < 4  -> "$totalLessons dersimiz oldu"
            else -> "$totalLessons haftadır devam ediyor"
        }
        return "İyi günler $parentName $salutation,\n\n${student.fullName} ile dersimiz $weekText, acelesi yok sadece bilgilendirmek istedim.\n\nDers tarihleri:\n$dateLines\n\nTamamlanan ders: $totalLessons\nBekleyen tutar: $pendingAmt\n\nKolay gelsin."
    }

    fun openWhatsApp(phone: String, message: String) {
        val clean = phone.replace("[^0-9]".toRegex(), "")
        val formatted = if (clean.startsWith("0")) "90${clean.drop(1)}" else if (!clean.startsWith("90")) "90$clean" else clean
        val uri = Uri.parse("https://wa.me/$formatted?text=${Uri.encode(message)}")
        try { context.startActivity(Intent(Intent.ACTION_VIEW, uri).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (_: Exception) {}
    }

    // Parent selection dialog
    if (showParentDialog && student != null) {
        val hasMother = student.motherName.isNotEmpty() && student.motherPhone.isNotEmpty()
        val hasFather = student.fatherName.isNotEmpty() && student.fatherPhone.isNotEmpty()
        Dialog(onDismissRequest = { showParentDialog = false }) {
            Surface(shape = RoundedCornerShape(20.dp), color = DersiumColors.Surface, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Kime gönderilsin?", style = MaterialTheme.typography.titleLarge, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                    if (hasMother) {
                        OutlinedButton(
                            onClick = {
                                selectedParent = "anne"
                                editableMessage = buildMessage(student.motherName, true)
                                showParentDialog = false
                                showMessageDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Anne — ${student.motherName}  ${student.motherPhone}")
                        }
                    }
                    if (hasFather) {
                        OutlinedButton(
                            onClick = {
                                selectedParent = "baba"
                                editableMessage = buildMessage(student.fatherName, false)
                                showParentDialog = false
                                showMessageDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Baba — ${student.fatherName}  ${student.fatherPhone}")
                        }
                    }
                    if (!hasMother && !hasFather) {
                        Text("Veli telefon numarası eklenmemiş.\nÖğrenci düzenleme ekranından ekleyin.", style = MaterialTheme.typography.bodySmall, color = DersiumColors.Expense)
                    }
                    TextButton(onClick = { showParentDialog = false }, modifier = Modifier.align(Alignment.End)) {
                        Text("İptal", color = DersiumColors.TextSecondary)
                    }
                }
            }
        }
    }

    // Message preview dialog
    if (showMessageDialog && student != null) {
        val phone = if (selectedParent == "anne") student.motherPhone else student.fatherPhone
        Dialog(onDismissRequest = { showMessageDialog = false }) {
            Surface(shape = RoundedCornerShape(20.dp), color = DersiumColors.Surface, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Mesajı Düzenle", style = MaterialTheme.typography.titleLarge, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = editableMessage,
                        onValueChange = { editableMessage = it },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp, max = 320.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DersiumColors.Primary, unfocusedBorderColor = DersiumColors.Outline,
                            focusedContainerColor = DersiumColors.SurfaceVariant, unfocusedContainerColor = DersiumColors.SurfaceVariant,
                        ),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showMessageDialog = false }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                            Text("İptal")
                        }
                        Button(
                            onClick = { openWhatsApp(phone, editableMessage); showMessageDialog = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(Icons.Default.Send, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Gönder", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = DersiumColors.Background,
        topBar = {
            TopAppBar(
                title = { Text(student?.fullName ?: "", color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DersiumColors.TextPrimary) } },
                actions = { IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = DersiumColors.TextSecondary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DersiumColors.Background),
            )
        },
        floatingActionButton = { DersiumFab(label = "Ders Ekle", onClick = onAddLesson) },
    ) { padding ->
        if (student == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = DersiumColors.Primary) }
            return@Scaffold
        }
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(20.dp), color = DersiumColors.SurfaceVariant) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            DersiumAvatar(initials = student.initials, colorHex = student.avatarColor, size = 60)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(student.fullName, style = MaterialTheme.typography.titleLarge, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                                if (student.school.isNotEmpty()) Text("${student.school} · ${student.grade}", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
                            }
                            StatusChip(if (student.isActive) "Aktif" else "Pasif", if (student.isActive) DersiumColors.Income else DersiumColors.TextTertiary)
                        }
                        HorizontalDivider(color = DersiumColors.Outline)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatMini("${state.lessons.size}", "Ders", Modifier.weight(1f))
                            StatMini("₺${state.paidAmount.toInt()}", "Ödendi", Modifier.weight(1f))
                            StatMini("₺${state.pendingAmount.toInt()}", "Bekleyen", Modifier.weight(1f))
                            val lastDate = state.lessons.firstOrNull()?.date?.format(DateTimeFormatter.ofPattern("d MMM", Locale("tr"))) ?: "-"
                            StatMini(lastDate, "Son Ders", Modifier.weight(1f))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            StatusChip("₺${student.lessonFee.toInt()}/ders · ${student.paymentType.displayName}", DersiumColors.Primary)
                            if (student.paymentType == PaymentType.AFTER_CERTAIN_LESSONS)
                                StatusChip("${student.lessonCountForPayment} ders sonrası", DersiumColors.Pending)
                        }
                        if (student.motherName.isNotEmpty() || student.fatherName.isNotEmpty()) {
                            HorizontalDivider(color = DersiumColors.Outline)
                            if (student.motherName.isNotEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Anne:", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary, fontWeight = FontWeight.SemiBold)
                                    Text(student.motherName, style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
                                    if (student.motherPhone.isNotEmpty()) Text("· ${student.motherPhone}", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextTertiary)
                                }
                            }
                            if (student.fatherName.isNotEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Baba:", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary, fontWeight = FontWeight.SemiBold)
                                    Text(student.fatherName, style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
                                    if (student.fatherPhone.isNotEmpty()) Text("· ${student.fatherPhone}", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextTertiary)
                                }
                            }
                        }
                    }
                }
            }

            if (state.pendingAmount > 0) {
                item {
                    Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), color = Color(0xFF1A2E1A)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Notifications, null, tint = DersiumColors.Pending, modifier = Modifier.size(20.dp))
                                Text(
                                    when (student.paymentType) {
                                        PaymentType.MONTHLY -> "Bu ay ödeme zamanı!"
                                        PaymentType.AFTER_CERTAIN_LESSONS -> "${state.lessons.size} ders tamamlandı!"
                                        else -> "Bekleyen ödeme var"
                                    },
                                    style = MaterialTheme.typography.titleSmall, color = DersiumColors.Pending, fontWeight = FontWeight.Bold,
                                )
                            }
                            Text("Bekleyen tutar: ₺${state.pendingAmount.toInt()}", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
                            Button(
                                onClick = { showParentDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(Icons.Default.Send, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("WhatsApp ile Hatırlat", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item { SectionHeader(title = "Dersler", modifier = Modifier.padding(horizontal = 16.dp)) }
            if (state.lessons.isEmpty()) {
                item { DersiumEmptyState(icon = Icons.Default.School, title = "Henüz ders yok", subtitle = "FAB'a basarak ders ekleyin") }
            } else {
                items(state.lessons, key = { it.id }) { lesson ->
                    StudentLessonCard(lesson = lesson, onTogglePayment = { viewModel.togglePayment(lesson) }, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentLessonCard(lesson: Lesson, onTogglePayment: () -> Unit, modifier: Modifier = Modifier) {
    val fmt = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("tr"))
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = DersiumColors.SurfaceVariant) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(lesson.date.format(fmt), style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                if (lesson.topic.isNotEmpty()) Text(lesson.topic, style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
                Text("${lesson.durationMinutes} dk", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextTertiary)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("₺${lesson.fee.toInt()}", style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                Surface(shape = RoundedCornerShape(8.dp), color = if (lesson.isPaid) DersiumColors.IncomeContainer else DersiumColors.PendingContainer, onClick = onTogglePayment) {
                    Text(if (lesson.isPaid) "✓ Ödendi" else "Bekleyen", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = if (lesson.isPaid) DersiumColors.Income else DersiumColors.Pending, fontWeight = FontWeight.Bold)
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
