package com.spectrum.phoenix.logic.register

import com.google.firebase.database.FirebaseDatabase
import com.spectrum.phoenix.logic.model.User
import kotlinx.coroutines.tasks.await

class RegisterRepository {

    private val database = FirebaseDatabase.getInstance("https://phoenix-631f8133-default-rtdb.firebaseio.com/").getReference("Usuarios")

    suspend fun createUser(name: String, email: String, pass: String, role: String): Result<Unit> {
        return try {
            val newUserRef = database.push()
            val userId = newUserRef.key ?: throw Exception("No se pudo generar el ID")

            val user = User(
                userId = userId,
                name = name,
                email = email,
                password = pass,
                role = role
            )

            newUserRef.setValue(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
