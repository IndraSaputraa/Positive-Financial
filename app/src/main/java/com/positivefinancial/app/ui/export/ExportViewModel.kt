package com.positivefinancial.app.ui.export

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.positivefinancial.app.data.repository.TransactionRepository
import com.positivefinancial.app.util.DateRanges
import com.positivefinancial.app.util.Formatters
import com.positivefinancial.app.util.export.CsvExporter
import com.positivefinancial.app.util.export.PdfExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.time.YearMonth
import javax.inject.Inject

sealed class ExportEvent {
    data class Success(val uri: Uri, val mimeType: String) : ExportEvent()
    data class Error(val message: String) : ExportEvent()
}

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow<YearMonth?>(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth?> = _selectedMonth.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    val transactionCount: StateFlow<Int> = _selectedMonth
        .flatMapLatest { month ->
            val (start, end) = month?.let { DateRanges.monthRange(it) } ?: (null to null)
            transactionRepository.observeFiltered(startDate = start, endDate = end).map { it.size }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _events = MutableSharedFlow<ExportEvent>()
    val events: SharedFlow<ExportEvent> = _events.asSharedFlow()

    fun onPreviousMonth() {
        _selectedMonth.value = (_selectedMonth.value ?: YearMonth.now()).minusMonths(1)
    }

    fun onNextMonth() {
        _selectedMonth.value = (_selectedMonth.value ?: YearMonth.now()).plusMonths(1)
    }

    fun onToggleAllTime() {
        _selectedMonth.value = if (_selectedMonth.value == null) YearMonth.now() else null
    }

    fun exportCsv() = export(isPdf = false)

    fun exportPdf() = export(isPdf = true)

    private fun export(isPdf: Boolean) {
        if (_isExporting.value) return
        viewModelScope.launch {
            _isExporting.value = true
            try {
                val month = _selectedMonth.value
                val (start, end) = month?.let { DateRanges.monthRange(it) } ?: (null to null)
                val transactions = transactionRepository.observeFiltered(startDate = start, endDate = end).first()
                val title = month?.let { Formatters.monthYear(it) } ?: "All Time"
                val fileLabel = month?.toString() ?: "all_time"
                val extension = if (isPdf) "pdf" else "csv"

                val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    ?: error("Storage is not available right now")
                downloadsDir.mkdirs()
                val file = File(downloadsDir, "PositiveFinancial_$fileLabel.$extension")

                if (isPdf) {
                    PdfExporter.write(file, title, transactions)
                } else {
                    CsvExporter.write(file, title, transactions)
                }

                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val mimeType = if (isPdf) "application/pdf" else "text/csv"
                _events.emit(ExportEvent.Success(uri, mimeType))
            } catch (e: Exception) {
                _events.emit(ExportEvent.Error(e.message ?: "Export failed"))
            } finally {
                _isExporting.value = false
            }
        }
    }
}
