package com.dersium.feature.export

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dersium.core.ui.theme.DersiumColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    onBack: () -> Unit,
    viewModel: ExportViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.importBackup(it) } }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = DersiumColors.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Yedekleme & Raporlar", color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DersiumColors.TextPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DersiumColors.Background),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SectionTitle("PDF Raporlar", Icons.Default.PictureAsPdf, DersiumColors.Expense)
            }
            item {
                ExportCard {
                    ExportRow(
                        icon = Icons.Default.PictureAsPdf,
                        iconColor = DersiumColors.Expense,
                        title = "Sezon Raporu",
                        subtitle = "Aktif sezonun tum ders ve ogrenci ozeti",
                        btnLabel = "PDF Olustur",
                        btnColor = DersiumColors.Expense,
                        isLoading = state.isLoading,
                        onClick = { viewModel.exportSeasonPdf() },
                    )
                }
            }

            item { Spacer(Modifier.height(4.dp)); SectionTitle("Veritabani Yedekleme", Icons.Default.Backup, DersiumColors.Income) }
            item {
                ExportCard {
                    ExportRow(
                        icon = Icons.Default.Upload,
                        iconColor = DersiumColors.Income,
                        title = "Yedek Al",
                        subtitle = "Downloads/Dersium/ klasorune kaydedilir",
                        btnLabel = "Yedekle",
                        btnColor = DersiumColors.Income,
                        isLoading = state.isLoading,
                        onClick = { viewModel.exportBackup() },
                    )
                    HorizontalDivider(color = DersiumColors.Outline)
                    ExportRow(
                        icon = Icons.Default.Download,
                        iconColor = DersiumColors.Primary,
                        title = "Yedek Geri Yukle",
                        subtitle = ".db dosyasi secin — mevcut veriler silinir!",
                        btnLabel = "Sec & Yukle",
                        btnColor = DersiumColors.Pending,
                        isLoading = state.isLoading,
                        onClick = { importLauncher.launch("*/*") },
                    )
                }
            }

            if (state.backupList.isNotEmpty()) {
                item { Spacer(Modifier.height(4.dp)); SectionTitle("Mevcut Yedekler", Icons.Default.Folder, DersiumColors.TextSecondary) }
                items(state.backupList) { file ->
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = DersiumColors.SurfaceVariant) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.Storage, null, tint = DersiumColors.TextSecondary, modifier = Modifier.size(20.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(file.name, style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Medium)
                                Text(
                                    "${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(file.lastModified()))} · ${file.length()/1024}KB",
                                    style = MaterialTheme.typography.labelSmall, color = DersiumColors.TextSecondary,
                                )
                            }
                        }
                    }
                }
            }

            item {
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = DersiumColors.PrimaryContainer) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Info, null, tint = DersiumColors.PrimaryLight, modifier = Modifier.size(18.dp))
                            Text("Bilgi", style = MaterialTheme.typography.titleSmall, color = DersiumColors.PrimaryLight, fontWeight = FontWeight.Bold)
                        }
                        listOf(
                            "PDF olusturulunca paylasma menusu acar",
                            "Yedekler Downloads/Dersium/ klasorune kaydedilir",
                            "Geri yukleme mevcut tum verilerin uzerine yazar",
                            "Duzenli yedek almayi unutmayin!",
                        ).forEach { Text("• $it", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, icon: ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ExportCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = DersiumColors.SurfaceVariant) {
        Column(content = content)
    }
}

@Composable
private fun ExportRow(icon: ImageVector, iconColor: Color, title: String, subtitle: String, btnLabel: String, btnColor: Color, isLoading: Boolean, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(shape = RoundedCornerShape(10.dp), color = iconColor.copy(alpha = 0.15f)) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.padding(8.dp).size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
        }
        Button(
            onClick = onClick, enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = btnColor),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
            else Text(btnLabel, style = MaterialTheme.typography.labelMedium, color = Color.White)
        }
    }
}
