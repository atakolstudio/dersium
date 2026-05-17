package com.dersium.feature.lessons

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dersium.core.domain.model.PaymentStatus
import com.dersium.core.ui.components.DersiumTextField
import com.dersium.core.ui.theme.DersiumColors
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditLessonScreen(
    lessonId: Long?,
    preSelectedStudentId: Long?,
    onBack: () -> Unit,
    viewModel: AddEditLessonViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(lessonId, preSelectedStudentId) {
        if (lessonId != null) viewModel.loadLesson(lessonId)
        if (preSelectedStudentId != null) viewModel.onStudentSelected(preSelectedStudentId)
    }
    LaunchedEffect(state.isSaved) { if (state.isSaved) onBack() }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
    val timePickerState = rememberTimePickerState(
        initialHour = state.startTime.hour,
        initialMinute = state.startTime.minute,
        is24Hour = true,
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        viewModel.onDateChange(
                            Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        )
                    }
                    showDatePicker = false
                }) { Text("Tamam", color = DersiumColors.Primary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("İptal") }
            },
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = DersiumColors.Surface,
            title = { Text("Saat Seç", color = DersiumColors.TextPrimary) },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onStartTimeChange(LocalTime.of(timePickerState.hour, timePickerState.minute))
                    showTimePicker = false
                }) { Text("Tamam", color = DersiumColors.Primary) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("İptal") }
            },
        )
    }

    Scaffold(
        containerColor = DersiumColors.Background,
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditMode) "Dersi Düzenle" else "Ders Ekle", color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold) },
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
            Text("Öğrenci Seç *", style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextSecondary)
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = state.students.find { it.id == state.selectedStudentId }?.fullName ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Öğrenci") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    isError = state.selectedStudentId == 0L && false,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DersiumColors.Primary, unfocusedBorderColor = DersiumColors.Outline,
                        focusedContainerColor = DersiumColors.SurfaceVariant, unfocusedContainerColor = DersiumColors.SurfaceVariant,
                    ),
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    state.students.forEach { student ->
                        DropdownMenuItem(text = { Text(student.fullName) }, onClick = { viewModel.onStudentSelected(student.id); expanded = false })
                    }
                }
            }
            

            HorizontalDivider(color = DersiumColors.Outline)

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = DersiumColors.SurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, DersiumColors.Outline),
            ) {
                Icon(Icons.Default.CalendarToday, null, tint = DersiumColors.Primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(state.date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")), color = DersiumColors.TextPrimary, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowDropDown, null, tint = DersiumColors.TextSecondary)
            }

            OutlinedButton(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = DersiumColors.SurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, DersiumColors.Outline),
            ) {
                Icon(Icons.Default.AccessTime, null, tint = DersiumColors.Primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(state.startTime.format(DateTimeFormatter.ofPattern("HH:mm")), color = DersiumColors.TextPrimary, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowDropDown, null, tint = DersiumColors.TextSecondary)
            }

            Text("Süre", style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(45, 60, 90, 120).forEach { mins ->
                    FilterChip(
                        selected = state.durationMinutes == mins,
                        onClick = { viewModel.onDurationChange(mins) },
                        label = { Text("$mins dk") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = DersiumColors.Primary, selectedLabelColor = Color.White),
                    )
                }
            }

            HorizontalDivider(color = DersiumColors.Outline)

            DersiumTextField(value = state.topic, onValueChange = viewModel::onTopicChange, label = "Konu", leadingIcon = Icons.Default.Book)
            DersiumTextField(value = state.notes, onValueChange = viewModel::onNotesChange, label = "Notlar", singleLine = false, maxLines = 3, leadingIcon = Icons.Default.Notes)

            Text("Ödeme Durumu", style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(PaymentStatus.PENDING, PaymentStatus.PAID).forEach { s ->
                    FilterChip(
                        selected = state.paymentStatus == s,
                        onClick = { viewModel.onPaymentStatusChange(s) },
                        label = { Text(s.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (s == PaymentStatus.PAID) DersiumColors.Income else DersiumColors.Primary,
                            selectedLabelColor = Color.White,
                        ),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
