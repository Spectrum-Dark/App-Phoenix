package com.spectrum.phoenix.logic.session

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import com.spectrum.phoenix.logic.dashboard.ActivityLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PerfilViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    // USANDO LA URL DIRECTA PARA MÁXIMA PRECISIÓN
    private val database = FirebaseDatabase.getInstance("https://phoenix-631f8133-default-rtdb.firebaseio.com/").getReference("Usuarios")
    private val logRepo = ActivityLogRepository()
    
    private val _userId = sessionManager.getUserId() ?: ""
    
    private val _userName = MutableStateFlow(sessionManager.getUserName() ?: "Usuario")
    val userName: StateFlow<String> = _userName

    private val _userEmail = MutableStateFlow(sessionManager.getUserEmail() ?: "")
    val userEmail: StateFlow<String> = _userEmail

    private val _updateResult = MutableStateFlow<Result<String>?>(null)
    val updateResult: StateFlow<Result<String>?> = _updateResult

    fun updateProfile(newName: String, newPass: String?) {
        if (_userId.isEmpty()) {
            _updateResult.value = Result.failure(Exception("Error: ID de usuario no encontrado en la sesión"))
            return
        }
        
        viewModelScope.launch {
            try {
                val updates = mutableMapOf<String, Any>("name" to newName)
                if (!newPass.isNullOrEmpty()) {
                    updates["password"] = newPass
                }
                
                // ACTUALIZACIÓN EN FIREBASE
                database.child(_userId).updateChildren(updates).await()
                
                // ACTUALIZAR SESIÓN LOCAL
                sessionManager.saveSession(_userId, newName, _userEmail.value)
                _userName.value = newName
                
                // REGISTRAR EN DASHBOARD
                logRepo.logAction("Perfil Actualizado", "El usuario cambió su información de perfil")
                
                _updateResult.value = Result.success("Perfil actualizado con éxito")
            } catch (e: Exception) {
                _updateResult.value = Result.failure(e)
            }
        }
    }

    fun clearResult() {
        _updateResult.value = null
    }
}
