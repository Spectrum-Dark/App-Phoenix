package com.spectrum.phoenix.logic.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spectrum.phoenix.logic.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val loginRepository = LoginRepository()

    private val _loginState = MutableStateFlow<Result<User>?>(null)
    val loginState: StateFlow<Result<User>?> = _loginState

    fun onLoginClicked(email: String, pass: String) {
        viewModelScope.launch {
            val result = loginRepository.login(email, pass)
            _loginState.value = result
        }
    }
}
