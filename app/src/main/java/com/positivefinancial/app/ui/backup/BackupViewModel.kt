package com.positivefinancial.app.ui.backup

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.positivefinancial.app.data.backup.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BackupEvent {
    data class ShareBackup(val uri: Uri) : BackupEvent()
    data class Error(val message: String) : BackupEvent()
}

data class BackupUiState(
    val isWorking: Boolean = false,
    val pendingRestoreUri: Uri? = null
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupManager: BackupManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<BackupEvent>()
    val events: SharedFlow<BackupEvent> = _events.asSharedFlow()

    fun createBackup() {
        if (_uiState.value.isWorking) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isWorking = true)
            try {
                val file = backupManager.createBackup()
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                _events.emit(BackupEvent.ShareBackup(uri))
            } catch (e: Exception) {
                _events.emit(BackupEvent.Error(e.message ?: "Backup failed"))
            } finally {
                _uiState.value = _uiState.value.copy(isWorking = false)
            }
        }
    }

    fun onRestoreFilePicked(uri: Uri) {
        _uiState.value = _uiState.value.copy(pendingRestoreUri = uri)
    }

    fun dismissRestoreConfirmation() {
        _uiState.value = _uiState.value.copy(pendingRestoreUri = null)
    }

    fun confirmRestore() {
        val uri = _uiState.value.pendingRestoreUri ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isWorking = true, pendingRestoreUri = null)
            val staged = backupManager.stagePendingRestore(uri)
            if (staged) {
                backupManager.restartApp()
            } else {
                _uiState.value = _uiState.value.copy(isWorking = false)
                _events.emit(BackupEvent.Error("This doesn't look like a Positive Financial backup file"))
            }
        }
    }
}
