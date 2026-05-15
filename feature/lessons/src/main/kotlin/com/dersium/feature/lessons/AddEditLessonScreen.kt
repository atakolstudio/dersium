package com.dersium.feature.lessons

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dersium.core.domain.model.PaymentStatus
import com.dersium.core.ui.components.DersiumTextField
import com.dersium.core.ui.theme.DersiumColors
import java.time.LocalTime
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
        if (preSelectedStudentId != null) viewModel.preSelectStudent(preSelectedStudentId)
    }
    LaunchedEffect(state.isSaved) { if (state.isSaved) onBack() }

    Scaffold(
        containerColor = DersiumColors.Background,
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditMode) "Dersi Düzenle" else "Ders Ekle", color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DersiumColors.TextPrimary) } },
                actions = { TextButton(onClick = viewModel::save) { Text("Kaydet", color = DersiumColors.Primary, fontWeight = FontWeight.Bold) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DersiumColors.Background),
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Student picker
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
                    isError = state.studentError != null,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DersiumColors.Primary,
                        unfocusedBorderColor = DersiumColors.Outline,
                        focusedContainerColor = DersiumColors.SurfaceVariant,
                        unfocusedContainerColor = DersiumColors.SurfaceVariant,
                    ),
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    state.students.forEach { student ->
                        DropdownMenuItem(
                            text = { Text(student.fullName) },
                            onClick = { viewModel.onStudentSelected(student.id); expanded = false },
                        )
                    }
                }
            }
            if (state.studentError != null) {
                Text(state.studentError!!, style = MaterialTheme.typography.labelSmall, color = DersiumColors.Expense, modifier = Modifier.padding(start = 16.dp))
            }

            HorizontalDivider(color = DersiumColors.Outline)

            // Date — simplified text field (in prod: date picker dialog)
            DersiumTextField(
                value = state.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                onValueChange = {},
                label = "Tarih",
                leadingIcon = Icons.Default.CalendarToday,
                readOnly = true,
            )

            // Time
            DersiumTextField(
                value = state.startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                onValueChange = {},
                label = "Başlangıç Saati",
                leadingIcon = Icons.Default.AccessTime,
                readOnly = true,
            )

            // Duration
            Text("Süre", style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(45, 60, 90, 120).forEach { mins ->
                    FilterChip(
                        selected = state.durationMinutes == mins,
                        onClick = { viewModel.onDurationChange(mins) },
                        label = { Text("$mins dk") },
                    )
                }
            }

            HorizontalDivider(color = DersiumColors.Outline)

            // Topic + Notes
            DersiumTextField(value = state.topic, onValueChange = viewModel::onTopicChange, label = "Konu", leadingIcon = Icons.Default.Book)
            DersiumTextField(value = state.notes, onValueChange = viewModel::onNotesChange, label = "Notlar", singleLine = false, maxLines = 3, leadingIcon = Icons.Default.Notes)

            // Payment status
            Text("Ödeme Durumu", style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaymentStatus.entries.filter { it != PaymentStatus.CANCELLED }.forEach { s ->
                    FilterChip(
                        selected = state.paymentStatus == s,
                        onClick = { viewModel.onPaymentStatusChange(s) },
                        label = { Text(s.displayName) },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
