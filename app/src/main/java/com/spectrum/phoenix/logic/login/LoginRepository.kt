package com.spectrum.phoenix.logic.login

import com.google.firebase.database.FirebaseDatabase
import com.spectrum.phoenix.logic.model.User
import kotlinx.coroutines.tasks.await

class LoginRepository {

    private val database = FirebaseDatabase.getInstance("https://phoenix-631f8133-default-rtdb.firebaseio.com/").getReference("Usuarios")

    suspend fun login(email: String, pass: String): Result<User> {
        return try {
            val snapshot = database.orderByChild("email").equalTo(email).get().await()
            if (snapshot.exists()) {
                var foundUser: User? = null
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user?.password == pass) {
                        foundUser = user
                        break
                    }
                }
                if (foundUser != null) {
                    Result.success(foundUser)
                } else {
                    Result.failure(Exception("Contraseña incorrecta"))
                }
            } else {
                Result.failure(Exception("El usuario no existe"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
