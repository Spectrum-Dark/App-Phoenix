package com.spectrum.phoenix.logic.session

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("PhoenixPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USER_EMAIL = "userEmail"
        private const val KEY_USER_ROLE = "userRole"
        private const val KEY_SEEN_NOTIFS = "seenNotifs"
        private const val KEY_DISMISSED_NOTIFS = "dismissedNotifs"
    }

    fun saveSession(userId: String, userName: String, email: String, role: String) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, userName)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_ROLE, role)
            apply()
        }
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)
    fun getUserRole(): String = prefs.getString(KEY_USER_ROLE, "user") ?: "user"

    fun getSeenNotifs(): Set<String> = prefs.getStringSet(KEY_SEEN_NOTIFS, emptySet()) ?: emptySet()
    fun saveSeenNotifs(ids: Set<String>) {
        prefs.edit().putStringSet(KEY_SEEN_NOTIFS, ids).apply()
    }

    fun getDismissedNotifs(): Set<String> = prefs.getStringSet(KEY_DISMISSED_NOTIFS, emptySet()) ?: emptySet()
    fun saveDismissedNotifs(ids: Set<String>) {
        prefs.edit().putStringSet(KEY_DISMISSED_NOTIFS, ids).apply()
    }

    fun clearSession() {
        // En lugar de clear() que borra todo, removemos solo los datos del usuario
        prefs.edit().apply {
            remove(KEY_IS_LOGGED_IN)
            remove(KEY_USER_ID)
            remove(KEY_USER_NAME)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_ROLE)
            apply()
        }
    }
}
