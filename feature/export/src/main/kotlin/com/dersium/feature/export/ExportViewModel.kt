package com.dersium.feature.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dersium.core.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class ExportUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val backupList: List<File> = emptyList(),
)

@HiltViewModel
class ExportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lessonRepository: LessonRepository,
    private val studentRepository: StudentRepository,
    private val financialRepository: FinancialRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ExportUiState())
    val state: StateFlow<ExportUiState> = _state.asStateFlow()

    init { refreshBackupList() }

    private fun refreshBackupList() {
        _state.update { it.copy(backupList = BackupManager.listBackups(context)) }
    }

    fun exportSeasonPdf() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            try {
                val prefs    = userPreferencesRepository.userPreferences.first()
                val season   = financialRepository.getActiveSeason().first() ?: error("Aktif sezon yok")
                val lessons  = lessonRepository.getAllLessons(prefs.activeSeasonId).first()
                val students = studentRepository.getAllStudents(prefs.activeSeasonId).first()
                val file = withContext(Dispatchers.IO) {
                    PdfReportGenerator.generateSeasonReport(context, season, lessons, students, prefs.currency)
                }
                sharePdf(file)
                _state.update { it.copy(isLoading = false, message = "PDF olusturuldu!") }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, message = "Hata: ${e.message}") }
            }
        }
    }

    fun exportStudentPdf(studentId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            try {
                val prefs   = userPreferencesRepository.userPreferences.first()
                val student = studentRepository.getStudentById(studentId).first() ?: error("Ogrenci bulunamadi")
                val lessons = lessonRepository.getLessonsByStudent(studentId).first()
                val file = withContext(Dispatchers.IO) {
                    PdfReportGenerator.generateStudentReport(context, student, lessons, prefs.currency)
                }
                sharePdf(file)
                _state.update { it.copy(isLoading = false, message = "PDF olusturuldu!") }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, message = "Hata: ${e.message}") }
            }
        }
    }

    fun exportBackup() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            BackupManager.exportBackup(context)
                .onSuccess { file ->
                    refreshBackupList()
                    _state.update { it.copy(isLoading = false, message = "Yedek alindi: ${file.name}") }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, message = "Hata: ${e.message}") }
                }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            BackupManager.importBackup(context, uri)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, message = "Geri yuklendi! Uygulamayi yeniden baslatın.") }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, message = "Hata: ${e.message}") }
                }
        }
    }

    fun clearMessage() { _state.update { it.copy(message = null) } }

    private fun sharePdf(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "PDF Paylas").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}
