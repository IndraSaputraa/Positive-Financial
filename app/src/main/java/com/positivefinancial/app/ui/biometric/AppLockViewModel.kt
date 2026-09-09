package com.positivefinancial.app.ui.biometric

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.positivefinancial.app.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AppLockUiState(
    val biometricRequired: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class AppLockViewModel @Inject constructor(
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val isUnlocked = MutableStateFlow(false)

    val uiState = combine(settingsRepository.settings, isUnlocked) { settings, unlocked ->
        AppLockUiState(
            biometricRequired = settings.biometricLockEnabled && !unlocked,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppLockUiState())

    fun onUnlocked() {
        isUnlocked.value = true
    }

    fun onAppBackgrounded() {
        isUnlocked.value = false
    }
}
