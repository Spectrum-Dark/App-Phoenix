package com.spectrum.phoenix.logic.user

import com.google.firebase.database.FirebaseDatabase
import com.spectrum.phoenix.logic.model.User
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.Flow
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class UserRepository {

    private val database = FirebaseDatabase.getInstance("https://phoenix-631f8133-default-rtdb.firebaseio.com/").getReference("Usuarios")

    suspend fun checkAndCreateAdmin() {
        try {
            val snapshot = database.orderByChild("email").equalTo("24phoenix99@gmail.com").get().await()
            if (!snapshot.exists()) {
                val newUserRef = database.push()
                val userId = newUserRef.key ?: return
                val admin = User(
                    userId = userId,
                    name = "Bryan Muñoz",
                    email = "24phoenix99@gmail.com",
                    password = "241299",
                    role = "admin"
                )
                newUserRef.setValue(admin).await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAllUsers(): Flow<List<User>> = flow {
        val snapshot = database.get().await()
        val users = snapshot.children.mapNotNull { it.getValue(User::class.java) }
        emit(users)
    }

    suspend fun updateUserRole(userId: String, newRole: String): Result<Unit> {
        return try {
            database.child(userId).child("role").setValue(newRole).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Para observar cambios en tiempo real si es necesario
    fun observeUsers(onUsersChanged: (List<User>) -> Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = snapshot.children.mapNotNull { it.getValue(User::class.java) }
                onUsersChanged(users)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
