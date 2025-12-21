package com.spectrum.phoenix.logic.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spectrum.phoenix.logic.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UsuariosViewModel : ViewModel() {
    private val userRepository = UserRepository()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    init {
        userRepository.observeUsers { userList ->
            _users.value = userList
        }
    }

    fun updateUserRole(userId: String, newRole: String) {
        viewModelScope.launch {
            userRepository.updateUserRole(userId, newRole)
        }
    }
}
