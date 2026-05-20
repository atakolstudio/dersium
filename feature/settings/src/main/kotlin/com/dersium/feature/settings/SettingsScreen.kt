package com.dersium.feature.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dersium.core.domain.model.ThemeAccentColor
import com.dersium.core.ui.theme.DersiumColors

@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    onPrivacyPolicy: () -> Unit = {},
    onExport: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pinState by viewModel.pinState.collectAsStateWithLifecycle()

    // Yeni sezon dialog state
    var showNewSeasonDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(DersiumColors.Background),
        contentPadding = PaddingValues(bottom = 40.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DersiumColors.TextPrimary)
                }
                Text(
                    "Ayarlar",
                    style = MaterialTheme.typography.headlineMedium,
                    color = DersiumColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }

        // ── Güvenlik ───────────────────────────────────────────────────────────
        item { SectionHeader("Güvenlik", Icons.Default.Security) }
        item {
            SettingCard {
                SettingRow(
                    icon = Icons.Default.Lock,
                    iconColor = DersiumColors.Primary,
                    title = "PIN Kilidi",
                    subtitle = if (state.isPinEnabled) "PIN etkinleştirildi" else "Uygulamayı PIN ile kilitle",
                    trailing = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (state.isPinEnabled) {
                                TextButton(onClick = viewModel::disablePin) {
                                    Text("Kaldır", color = DersiumColors.Expense, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            Switch(
                                checked = state.isPinEnabled,
                                onCheckedChange = { if (it) viewModel.showPinSetup() else viewModel.disablePin() },
                                colors = SwitchDefaults.colors(checkedTrackColor = DersiumColors.Primary),
                            )
                        }
                    },
                )
                if (state.isPinEnabled) {
                    HorizontalDivider(color = DersiumColors.Outline)
                    SettingRow(
                        icon = Icons.Default.Fingerprint,
                        iconColor = DersiumColors.PrimaryLight,
                        title = "Biyometrik Giriş",
                        subtitle = "Parmak izi veya yüz tanıma",
                        trailing = {
                            Switch(
                                checked = state.isBiometricEnabled,
                                onCheckedChange = viewModel::setBiometric,
                                colors = SwitchDefaults.colors(checkedTrackColor = DersiumColors.Primary),
                            )
                        },
                    )
                }
            }
        }

        // ── Sezon Yönetimi ─────────────────────────────────────────────────────
        item { SectionHeader("Sezon Yönetimi", Icons.Default.CalendarMonth) }
        item {
            SettingCard {
                state.seasons.forEach { season ->
                    val isActive = season.id == state.activeSeasonId
                    SettingRow(
                        icon = Icons.Default.CalendarViewMonth,
                        iconColor = if (isActive) DersiumColors.Income else DersiumColors.TextTertiary,
                        title = season.displayName,
                        subtitle = if (isActive) "Aktif sezon" else "Geçiş yapmak için tıklayın",
                        onClick = if (!isActive) ({ viewModel.switchSeason(season.id) }) else null,
                        trailing = {
                            if (isActive) StatusBadge("Aktif", DersiumColors.Income)
                        },
                    )
                    if (season.id != state.seasons.lastOrNull()?.id) HorizontalDivider(color = DersiumColors.Outline)
                }
                if (state.seasons.isNotEmpty()) HorizontalDivider(color = DersiumColors.Outline)
                SettingRow(
                    icon = Icons.Default.Add,
                    iconColor = DersiumColors.Primary,
                    title = "Yeni Sezon Oluştur",
                    subtitle = "Örn: 2026-2027",
                    onClick = { showNewSeasonDialog = true },
                    trailing = {
                        Icon(Icons.Default.ChevronRight, null, tint = DersiumColors.TextTertiary, modifier = Modifier.size(18.dp))
                    },
                )
            }
        }

        // ── Görünüm ────────────────────────────────────────────────────────────
        item { SectionHeader("Görünüm", Icons.Default.Palette) }
        item {
            SettingCard {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Tema Rengi", style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ThemeAccentColor.entries.forEach { color ->
                            val selected = state.themeAccentColor == color
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(color.hex)))
                                    .border(
                                        width = if (selected) 3.dp else 0.dp,
                                        color = if (selected) Color.White else Color.Transparent,
                                        shape = CircleShape,
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                    ) { viewModel.setThemeAccent(color) },
                            )
                        }
                    }
                }
            }
        }

        // ── Premium ────────────────────────────────────────────────────────────
        if (!state.isPremium) {
            item { SectionHeader("Premium", Icons.Default.Star) }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = DersiumColors.PrimaryContainer,
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Star, null, tint = DersiumColors.Pending, modifier = Modifier.size(24.dp))
                            Text("Dersium Premium", style = MaterialTheme.typography.titleMedium, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                        }
                        listOf("Sınırsız öğrenci", "Tüm raporlar", "Veri dışa aktarma", "Öncelikli destek").forEach {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Check, null, tint = DersiumColors.Income, modifier = Modifier.size(16.dp))
                                Text(it, style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextPrimary)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Button(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DersiumColors.Primary),
                        ) {
                            Text("Premium'a Geç", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ── Hakkında ───────────────────────────────────────────────────────────
        item { SectionHeader("Yedekleme & Raporlar", Icons.Default.Backup) }
        item {
            SettingCard {
                SettingRow(
                    icon = Icons.Default.Backup,
                    iconColor = DersiumColors.Income,
                    title = "Yedekleme ve PDF Raporlar",
                    subtitle = "Veri yedekleme, geri yukleme ve PDF rapor",
                    onClick = onExport,
                    trailing = { Icon(Icons.Default.ChevronRight, null, tint = DersiumColors.TextTertiary) },
                )
            }
        }

        item { SectionHeader("Hakkında", Icons.Default.Info) }
        item {
            SettingCard {
                SettingRow(icon = Icons.Default.Info, iconColor = DersiumColors.TextSecondary, title = "Uygulama Versiyonu", subtitle = "1.0.0")
                HorizontalDivider(color = DersiumColors.Outline)
                SettingRow(icon = Icons.Default.Email, iconColor = DersiumColors.TextSecondary, title = "Destek", subtitle = "support@dersium.app")
            }
        }
    }

    // ── Yeni Sezon Dialog ──────────────────────────────────────────────────────
    if (showNewSeasonDialog) {
        NewSeasonDialog(
            onDismiss = { showNewSeasonDialog = false },
            onConfirm = { startYear, endYear ->
                viewModel.createNewSeason("$startYear-$endYear", startYear, endYear)
                showNewSeasonDialog = false
            },
        )
    }

    // ── PIN Setup ──────────────────────────────────────────────────────────────
    if (pinState.isVisible) {
        PinSetupDialog(
            pinState = pinState,
            onDigit = viewModel::onPinDigit,
            onBackspace = viewModel::onPinBackspace,
            onDismiss = viewModel::hidePinSetup,
        )
    }
}

// ── Yeni Sezon Dialog ──────────────────────────────────────────────────────────
@Composable
private fun NewSeasonDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
) {
    var startYear by remember { mutableStateOf("2025") }
    var endYear by remember { mutableStateOf("2026") }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DersiumColors.Surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("Yeni Sezon Oluştur", style = MaterialTheme.typography.titleLarge, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                Text("Sezon yıllarını girin (örn: 2026 - 2027)", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = startYear,
                        onValueChange = {
                            startYear = it
                            error = null
                            // Otomatik bitiş yılını hesapla
                            it.toIntOrNull()?.let { y -> endYear = (y + 1).toString() }
                        },
                        label = { Text("Başlangıç Yılı") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DersiumColors.Primary,
                            unfocusedBorderColor = DersiumColors.Outline,
                            focusedContainerColor = DersiumColors.SurfaceVariant,
                            unfocusedContainerColor = DersiumColors.SurfaceVariant,
                        ),
                    )
                    OutlinedTextField(
                        value = endYear,
                        onValueChange = { endYear = it; error = null },
                        label = { Text("Bitiş Yılı") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DersiumColors.Primary,
                            unfocusedBorderColor = DersiumColors.Outline,
                            focusedContainerColor = DersiumColors.SurfaceVariant,
                            unfocusedContainerColor = DersiumColors.SurfaceVariant,
                        ),
                    )
                }

                if (error != null) {
                    Text(error!!, style = MaterialTheme.typography.labelSmall, color = DersiumColors.Expense)
                }

                // Önizleme
                val sy = startYear.toIntOrNull()
                val ey = endYear.toIntOrNull()
                if (sy != null && ey != null) {
                    Surface(shape = RoundedCornerShape(10.dp), color = DersiumColors.PrimaryContainer) {
                        Text(
                            "$sy-$ey Sezonu",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = DersiumColors.PrimaryLight,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                    ) { Text("İptal") }

                    Button(
                        onClick = {
                            val sy2 = startYear.toIntOrNull()
                            val ey2 = endYear.toIntOrNull()
                            when {
                                sy2 == null || ey2 == null -> error = "Geçerli yıl girin"
                                ey2 <= sy2 -> error = "Bitiş yılı başlangıçtan büyük olmalı"
                                sy2 < 2000 || sy2 > 2100 -> error = "Geçerli bir yıl girin"
                                else -> onConfirm(sy2, ey2)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DersiumColors.Primary),
                    ) { Text("Oluştur", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, null, tint = DersiumColors.Primary, modifier = Modifier.size(18.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, color = DersiumColors.Primary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SettingCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = DersiumColors.SurfaceVariant,
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(shape = RoundedCornerShape(10.dp), color = iconColor.copy(alpha = 0.15f)) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.padding(8.dp).size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
        }
        trailing?.invoke()
    }
}

@Composable
private fun StatusBadge(label: String, color: Color) {
    Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.15f)) {
        Text(label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PinSetupDialog(
    pinState: PinSetupData,
    onDigit: (Int) -> Unit,
    onBackspace: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = DersiumColors.Surface) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp).navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                if (pinState.step == PinSetupStep.ENTER_NEW) "Yeni PIN Girin" else "PIN'i Doğrulayın",
                style = MaterialTheme.typography.titleLarge,
                color = DersiumColors.TextPrimary,
                fontWeight = FontWeight.Bold,
            )
            AnimatedVisibility(pinState.error != null) {
                Text(pinState.error ?: "", style = MaterialTheme.typography.bodySmall, color = DersiumColors.Expense)
            }
            val current = if (pinState.step == PinSetupStep.ENTER_NEW) pinState.newPin else pinState.confirmPin
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) { i ->
                    Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(if (i < current.length) DersiumColors.Primary else DersiumColors.Outline))
                }
            }
            listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9)).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    row.forEach { d ->
                        Box(
                            modifier = Modifier.size(64.dp).clip(CircleShape).background(DersiumColors.SurfaceVariant)
                                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDigit(d) },
                            contentAlignment = Alignment.Center,
                        ) { Text(d.toString(), fontSize = 22.sp, fontWeight = FontWeight.Medium, color = DersiumColors.TextPrimary) }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Spacer(Modifier.size(64.dp))
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(DersiumColors.SurfaceVariant)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDigit(0) },
                    contentAlignment = Alignment.Center,
                ) { Text("0", fontSize = 22.sp, fontWeight = FontWeight.Medium, color = DersiumColors.TextPrimary) }
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onBackspace),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Default.Backspace, null, tint = DersiumColors.TextSecondary, modifier = Modifier.size(24.dp)) }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
