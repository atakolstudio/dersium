package com.dersium.feature.students

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dersium.core.domain.model.PaymentType
import com.dersium.core.ui.components.DersiumAvatar
import com.dersium.core.ui.components.DersiumTextField
import com.dersium.core.ui.theme.DersiumColors

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

    Scaffold(
        containerColor = DersiumColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.isEditMode) "Öğrenciyi Düzenle" else "Öğrenci Ekle",
                        color = DersiumColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DersiumColors.TextPrimary)
                    }
                },
                actions = {
                    TextButton(onClick = viewModel::save) {
                        Text("Kaydet", color = DersiumColors.Primary, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DersiumColors.Background),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Avatar
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                DersiumAvatar(
                    initials = buildString {
                        state.name.firstOrNull()?.let { append(it) }
                        state.surname.firstOrNull()?.let { append(it) }
                    }.ifEmpty { "?" },
                    colorHex = state.avatarColor,
                    size = 72,
                )
            }

            // Ad / Soyad
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DersiumTextField(
                    value = state.name, onValueChange = viewModel::onNameChange,
                    label = "İsim *", isError = state.nameError != null,
                    errorMessage = state.nameError, modifier = Modifier.weight(1f),
                )
                DersiumTextField(
                    value = state.surname, onValueChange = viewModel::onSurnameChange,
                    label = "Soyisim", modifier = Modifier.weight(1f),
                )
            }

            // Okul / Sınıf
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DersiumTextField(value = state.school, onValueChange = viewModel::onSchoolChange, label = "Okul", modifier = Modifier.weight(1f))
                DersiumTextField(value = state.grade, onValueChange = viewModel::onGradeChange, label = "Sınıf", modifier = Modifier.weight(1f))
            }

            // Telefon
            DersiumTextField(
                value = state.phone, onValueChange = viewModel::onPhoneChange,
                label = "Öğrenci Telefon", leadingIcon = Icons.Default.Phone,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            )

            // Veli
            DersiumTextField(value = state.parentName, onValueChange = viewModel::onParentNameChange, label = "Veli Adı", leadingIcon = Icons.Default.Person)
            DersiumTextField(
                value = state.parentPhone, onValueChange = viewModel::onParentPhoneChange,
                label = "Veli Telefon", leadingIcon = Icons.Default.Phone,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            )

            HorizontalDivider(color = DersiumColors.Outline)

            // Ders ücreti
            DersiumTextField(
                value = state.lessonFee, onValueChange = viewModel::onLessonFeeChange,
                label = "Ders Ücreti (₺) *", leadingIcon = Icons.Default.AttachMoney,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = state.feeError != null, errorMessage = state.feeError,
            )

            // Ödeme tipi
            Text("Ödeme Tipi", style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextSecondary)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                PaymentType.entries.forEach { type ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = state.paymentType == type,
                            onClick = { viewModel.onPaymentTypeChange(type) },
                            colors = RadioButtonDefaults.colors(selectedColor = DersiumColors.Primary),
                        )
                        Text(type.displayName, style = MaterialTheme.typography.bodyMedium, color = DersiumColors.TextPrimary)
                    }
                }
            }

            // Belirli ders sonrası seçildiyse kaç ders sorusu
            if (state.paymentType == PaymentType.AFTER_CERTAIN_LESSONS) {
                DersiumTextField(
                    value = state.lessonCountForPayment,
                    onValueChange = viewModel::onLessonCountChange,
                    label = "Kaç Ders Sonrası?",
                    leadingIcon = Icons.Default.Numbers,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }

            // Notlar
            DersiumTextField(
                value = state.notes, onValueChange = viewModel::onNotesChange,
                label = "Notlar", singleLine = false, maxLines = 4,
                leadingIcon = Icons.Default.Notes,
            )

            // Aktif switch (düzenleme modunda)
            if (state.isEditMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Aktif Öğrenci", style = MaterialTheme.typography.bodyMedium, color = DersiumColors.TextPrimary)
                    Switch(
                        checked = state.isActive,
                        onCheckedChange = viewModel::onIsActiveChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DersiumColors.Primary,
                            checkedTrackColor = DersiumColors.PrimaryContainer,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
