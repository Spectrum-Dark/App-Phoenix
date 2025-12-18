package com.spectrum.phoenix.logic.clientes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spectrum.phoenix.logic.model.Client
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ClientViewModel : ViewModel() {
    private val repository = ClientRepository()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _result = MutableStateFlow<Result<Unit>?>(null)
    val result: StateFlow<Result<Unit>?> = _result

    private val _clients = repository.getClients()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val filteredClients: StateFlow<List<Client>> = combine(_searchQuery, _clients) { query, list ->
        if (query.isEmpty()) {
            list
        } else {
            list.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.lastName.contains(query, ignoreCase = true) 
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query.replace("\n", "")
    }

    fun addClient(name: String, lastName: String) {
        viewModelScope.launch {
            _result.value = repository.addClient(name, lastName)
        }
    }

    fun updateClient(client: Client) {
        viewModelScope.launch {
            _result.value = repository.updateClient(client)
        }
    }

    fun deleteClient(clientId: String) {
        viewModelScope.launch {
            _result.value = repository.deleteClient(clientId)
        }
    }

    fun clearResult() {
        _result.value = null
    }
}
