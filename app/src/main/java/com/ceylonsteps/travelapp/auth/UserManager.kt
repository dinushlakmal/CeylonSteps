package com.ceylonsteps.travelapp.auth

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

data class AppUser(
    val email: String,
    val displayName: String,
    val photoUrl: String?,
    val googleId: String
)

object UserManager {
    private const val PREF_USER_EMAIL = "pref_user_email"
    private const val PREF_USER_NAME = "pref_user_name"
    private const val PREF_USER_PHOTO = "pref_user_photo"
    private const val PREF_IS_LOGGED_IN = "pref_is_logged_in"

    fun autoRegisterFromGoogle(context: Context, account: GoogleSignInAccount): AppUser {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val user = AppUser(
            email = account.email ?: "",
            displayName = account.displayName ?: "Traveler",
            photoUrl = account.photoUrl?.toString(),
            googleId = account.id ?: ""
        )

        prefs.edit()
            .putString(PREF_USER_EMAIL, user.email)
            .putString(PREF_USER_NAME, user.displayName)
            .putString(PREF_USER_PHOTO, user.photoUrl)
            .putBoolean(PREF_IS_LOGGED_IN, true)
            .apply()

        return user
    }

    fun getLoggedInUser(context: Context): AppUser? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if (!prefs.getBoolean(PREF_IS_LOGGED_IN, false)) return null
        return AppUser(
            email = prefs.getString(PREF_USER_EMAIL, "") ?: "",
            displayName = prefs.getString(PREF_USER_NAME, "") ?: "",
            photoUrl = prefs.getString(PREF_USER_PHOTO, null),
            googleId = ""
        )
    }

    fun signOut(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit()
            .remove(PREF_USER_EMAIL)
            .remove(PREF_USER_NAME)
            .remove(PREF_USER_PHOTO)
            .putBoolean(PREF_IS_LOGGED_IN, false)
            .apply()
    }
}
