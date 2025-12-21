package com.spectrum.phoenix.logic.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val registerRepository = RegisterRepository()

    private val _registrationState = MutableStateFlow<Result<Unit>?>(null)
    val registrationState: StateFlow<Result<Unit>?> = _registrationState

    fun onRegisterClicked(name: String, email: String, pass: String) {
        viewModelScope.launch {
            val result = registerRepository.createUser(name, email, pass, "user")
            _registrationState.value = result
        }
    }
}
