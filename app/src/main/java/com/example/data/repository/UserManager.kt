package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

class UserManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _userProfile = MutableStateFlow(loadProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun getUserProfile(): UserProfile {
        return _userProfile.value
    }

    fun getThemeMode(): ThemeMode {
        return _themeMode.value
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    fun saveUserProfile(profile: UserProfile) {
        prefs.edit()
            .putString(KEY_USER_NAME, profile.userName)
            .putString(KEY_PROFILE_IMAGE_URI, profile.profileImageUri)
            .putString(KEY_HOME_LOCATION_NAME, profile.homeLocationName)
            .putString(KEY_HOME_LATITUDE, profile.homeLatitude.toString())
            .putString(KEY_HOME_LONGITUDE, profile.homeLongitude.toString())
            .putBoolean(KEY_ONBOARDING_COMPLETED, profile.isOnboardingCompleted)
            .apply()

        _userProfile.value = profile
    }

    fun updateProfile(
        name: String,
        imageUri: String?,
        homeLocationName: String,
        homeLat: Double,
        homeLng: Double
    ) {
        val updated = UserProfile(
            userName = name.ifBlank { "Explorer" },
            profileImageUri = imageUri,
            homeLocationName = homeLocationName.ifBlank { "Sri Lanka" },
            homeLatitude = homeLat,
            homeLongitude = homeLng,
            isOnboardingCompleted = true
        )
        saveUserProfile(updated)
    }

    private fun loadThemeMode(): ThemeMode {
        val raw = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(raw)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    private fun loadProfile(): UserProfile {
        val name = prefs.getString(KEY_USER_NAME, "Sri Lanka Explorer") ?: "Sri Lanka Explorer"
        val imgUri = prefs.getString(KEY_PROFILE_IMAGE_URI, null)
        val homeName = prefs.getString(KEY_HOME_LOCATION_NAME, "Colombo, Western Province") ?: "Colombo, Western Province"
        val homeLat = prefs.getString(KEY_HOME_LATITUDE, "6.9271")?.toDoubleOrNull() ?: 6.9271
        val homeLng = prefs.getString(KEY_HOME_LONGITUDE, "79.8612")?.toDoubleOrNull() ?: 79.8612
        val completed = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)

        return UserProfile(
            userName = name,
            profileImageUri = imgUri,
            homeLocationName = homeName,
            homeLatitude = homeLat,
            homeLongitude = homeLng,
            isOnboardingCompleted = completed
        )
    }

    companion object {
        private const val PREFS_NAME = "lanka_footprints_user_prefs"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_PROFILE_IMAGE_URI = "profile_image_uri"
        private const val KEY_HOME_LOCATION_NAME = "home_location_name"
        private const val KEY_HOME_LATITUDE = "home_latitude"
        private const val KEY_HOME_LONGITUDE = "home_longitude"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_THEME_MODE = "app_theme_mode"

        @Volatile
        private var INSTANCE: UserManager? = null

        fun getInstance(context: Context): UserManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
