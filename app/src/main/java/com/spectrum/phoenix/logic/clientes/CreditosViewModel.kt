package com.spectrum.phoenix.logic.clientes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spectrum.phoenix.logic.model.Credit
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CreditosViewModel : ViewModel() {
    private val repository = CreditRepository()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _allCredits = repository.getCredits()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredCredits: StateFlow<List<Credit>> = _searchQuery
        .combine(_allCredits) { query, list ->
            if (query.isEmpty()) list
            else list.filter { it.clientName.contains(query, ignoreCase = true) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _opResult = MutableStateFlow<Result<Unit>?>(null)
    val opResult: StateFlow<Result<Unit>?> = _opResult

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun clearAllCredits() {
        viewModelScope.launch {
            _opResult.value = repository.clearAll()
        }
    }

    fun clearResult() {
        _opResult.value = null
    }
}
