package com.dersium.feature.students

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dersium.core.domain.model.PaymentType
import com.dersium.core.domain.model.ScheduleSlot
import com.dersium.core.ui.components.DersiumAvatar
import com.dersium.core.ui.components.DersiumTextField
import com.dersium.core.ui.theme.DersiumColors
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditStudentScreen(
    studentId: Long?,
    onBack: () -> Unit,
    viewModel: AddEditStudentViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(studentId) { if (studentId != null) viewModel.loadStudent(studentId) }
    LaunchedEffect(state.isSaved) { if (state.isSaved) onBack() }

    var showSlotDialog by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableStateOf(DayOfWeek.MONDAY) }
    var selectedHour by remember { mutableIntStateOf(9) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var selectedDuration by remember { mutableIntStateOf(60) }

    // Slot ekleme diyalogu
    if (showSlotDialog) {
        Dialog(onDismissRequest = { showSlotDialog = false }) {
            Surface(shape = RoundedCornerShape(20.dp), color = DersiumColors.Surface, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Ders Saati Ekle", style = MaterialTheme.typography.titleLarge, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)

                    // Gün seçimi
                    Text("Gün", style = MaterialTheme.typography.labelMedium, color = DersiumColors.TextSecondary)
                    val days = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        days.forEach { day ->
                            val isSelected = selectedDay == day
                            Surface(
                                onClick = { selectedDay = day },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) DersiumColors.Primary else DersiumColors.SurfaceVariant,
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(
                                    day.getDisplayName(TextStyle.SHORT, Locale("tr")).take(3),
                                    modifier = Modifier.padding(vertical = 6.dp).wrapContentWidth(Alignment.CenterHorizontally),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Color.White else DersiumColors.TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                )
                            }
                        }
                    }

                    // Saat seçimi
                    Text("Başlangıç Saati", style = MaterialTheme.typography.labelMedium, color = DersiumColors.TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Saat
                        OutlinedTextField(
                            value = selectedHour.toString().padStart(2, '0'),
                            onValueChange = { v -> v.toIntOrNull()?.let { if (it in 0..23) selectedHour = it } },
                            label = { Text("Saat") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DersiumColors.Primary, unfocusedBorderColor = DersiumColors.Outline, focusedContainerColor = DersiumColors.SurfaceVariant, unfocusedContainerColor = DersiumColors.SurfaceVariant),
                        )
                        Text(":", style = MaterialTheme.typography.titleLarge, color = DersiumColors.TextPrimary)
                        // Dakika
                        OutlinedTextField(
                            value = selectedMinute.toString().padStart(2, '0'),
                            onValueChange = { v -> v.toIntOrNull()?.let { if (it in 0..59) selectedMinute = it } },
                            label = { Text("Dakika") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DersiumColors.Primary, unfocusedBorderColor = DersiumColors.Outline, focusedContainerColor = DersiumColors.SurfaceVariant, unfocusedContainerColor = DersiumColors.SurfaceVariant),
                        )
                    }

                    // Süre seçimi
                    Text("Ders Süresi", style = MaterialTheme.typography.labelMedium, color = DersiumColors.TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(45, 60, 90, 120).forEach { dur ->
                            FilterChip(
                                selected = selectedDuration == dur,
                                onClick = { selectedDuration = dur },
                                label = { Text("${dur}dk") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = DersiumColors.Primary, selectedLabelColor = Color.White),
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showSlotDialog = false }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                            Text("İptal")
                        }
                        Button(
                            onClick = {
                                viewModel.addSlot(ScheduleSlot(dayOfWeek = selectedDay, startTime = LocalTime.of(selectedHour, selectedMinute), durationMinutes = selectedDuration))
                                showSlotDialog = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = DersiumColors.Primary),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text("Ekle", color = Color.White)
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
                title = { Text(if (state.isEditMode) "Öğrenciyi Düzenle" else "Öğrenci Ekle", color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DersiumColors.TextPrimary) } },
                actions = { TextButton(onClick = viewModel::save) { Text("Kaydet", color = DersiumColors.Primary, fontWeight = FontWeight.Bold) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DersiumColors.Background),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                DersiumAvatar(initials = buildString { state.name.firstOrNull()?.let { append(it) }; state.surname.firstOrNull()?.let { append(it) } }.ifEmpty { "?" }, colorHex = state.avatarColor, size = 72)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DersiumTextField(value = state.name, onValueChange = viewModel::onNameChange, label = "İsim *", isError = state.nameError != null, errorMessage = state.nameError, modifier = Modifier.weight(1f))
                DersiumTextField(value = state.surname, onValueChange = viewModel::onSurnameChange, label = "Soyisim", modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DersiumTextField(value = state.school, onValueChange = viewModel::onSchoolChange, label = "Okul", modifier = Modifier.weight(1f))
                DersiumTextField(value = state.grade, onValueChange = viewModel::onGradeChange, label = "Sınıf", modifier = Modifier.weight(1f))
            }
            DersiumTextField(value = state.phone, onValueChange = viewModel::onPhoneChange, label = "Öğrenci Telefon", leadingIcon = Icons.Default.Phone, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))

            HorizontalDivider(color = DersiumColors.Outline)
            Text("Anne", style = MaterialTheme.typography.titleSmall, color = DersiumColors.Primary, fontWeight = FontWeight.SemiBold)
            DersiumTextField(value = state.motherName, onValueChange = viewModel::onMotherNameChange, label = "Anne Adı", leadingIcon = Icons.Default.Person)
            DersiumTextField(value = state.motherPhone, onValueChange = viewModel::onMotherPhoneChange, label = "Anne Telefon", leadingIcon = Icons.Default.Phone, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))

            HorizontalDivider(color = DersiumColors.Outline)
            Text("Baba", style = MaterialTheme.typography.titleSmall, color = DersiumColors.Primary, fontWeight = FontWeight.SemiBold)
            DersiumTextField(value = state.fatherName, onValueChange = viewModel::onFatherNameChange, label = "Baba Adı", leadingIcon = Icons.Default.Person)
            DersiumTextField(value = state.fatherPhone, onValueChange = viewModel::onFatherPhoneChange, label = "Baba Telefon", leadingIcon = Icons.Default.Phone, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))

            HorizontalDivider(color = DersiumColors.Outline)
            DersiumTextField(value = state.lessonFee, onValueChange = viewModel::onLessonFeeChange, label = "Ders Ücreti (₺) *", leadingIcon = Icons.Default.AttachMoney, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), isError = state.feeError != null, errorMessage = state.feeError)

            Text("Ödeme Tipi", style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextSecondary)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                PaymentType.entries.forEach { type ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = state.paymentType == type, onClick = { viewModel.onPaymentTypeChange(type) }, colors = RadioButtonDefaults.colors(selectedColor = DersiumColors.Primary))
                        Text(type.displayName, style = MaterialTheme.typography.bodyMedium, color = DersiumColors.TextPrimary)
                    }
                }
            }
            if (state.paymentType == PaymentType.AFTER_CERTAIN_LESSONS) {
                DersiumTextField(value = state.lessonCountForPayment, onValueChange = viewModel::onLessonCountChange, label = "Kaç Ders Sonrası?", leadingIcon = Icons.Default.Numbers, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }

            HorizontalDivider(color = DersiumColors.Outline)

            // Ders Programı
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Ders Programı", style = MaterialTheme.typography.titleSmall, color = DersiumColors.Primary, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = { showSlotDialog = true }) {
                    Icon(Icons.Default.Add, null, tint = DersiumColors.Primary)
                }
            }
            if (state.scheduleSlots.isEmpty()) {
                Text("Henüz program eklenmedi. + ile ekleyin.", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextTertiary)
            } else {
                state.scheduleSlots.forEach { slot ->
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = DersiumColors.SurfaceVariant) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Default.Schedule, null, tint = DersiumColors.Primary, modifier = Modifier.size(20.dp))
                                Column {
                                    Text(slot.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("tr")), style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                                    Text("${slot.startTime.hour.toString().padStart(2,'0')}:${slot.startTime.minute.toString().padStart(2,'0')} · ${slot.durationMinutes}dk", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
                                }
                            }
                            IconButton(onClick = { viewModel.removeSlot(slot) }) {
                                Icon(Icons.Default.Delete, null, tint = DersiumColors.Expense, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            DersiumTextField(value = state.notes, onValueChange = viewModel::onNotesChange, label = "Notlar", singleLine = false, maxLines = 4, leadingIcon = Icons.Default.Notes)

            if (state.isEditMode) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Aktif Öğrenci", style = MaterialTheme.typography.bodyMedium, color = DersiumColors.TextPrimary)
                    Switch(checked = state.isActive, onCheckedChange = viewModel::onIsActiveChange, colors = SwitchDefaults.colors(checkedThumbColor = DersiumColors.Primary, checkedTrackColor = DersiumColors.PrimaryContainer))
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
