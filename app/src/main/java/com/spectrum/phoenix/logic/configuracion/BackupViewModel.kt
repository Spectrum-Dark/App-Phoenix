package com.spectrum.phoenix.logic.configuracion

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BackupViewModel : ViewModel() {
    private val repository = BackupRepository()

    private val _opResult = MutableStateFlow<Result<String>?>(null)
    val opResult: StateFlow<Result<String>?> = _opResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun exportDatabase(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _opResult.value = repository.exportData(context)
            _isLoading.value = false
        }
    }

    fun importDatabase(context: Context, uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.importData(context, uri)
            _opResult.value = if (result.isSuccess) Result.success("Base de datos restaurada con éxito") else Result.failure(result.exceptionOrNull()!!)
            _isLoading.value = false
        }
    }

    fun clearResult() { _opResult.value = null }
}
