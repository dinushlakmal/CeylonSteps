package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

enum class AppThemeType { DEFAULT, OCEAN, FOREST, SUNSET }

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

class UserManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _userProfile = MutableStateFlow(loadProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean(KEY_IS_LOGGED_IN, false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()
    private val _appThemeType = MutableStateFlow(loadAppThemeType())
    val appThemeType: StateFlow<AppThemeType> = _appThemeType.asStateFlow()

    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun isUserLoggedIn(): Boolean {
        return _isLoggedIn.value
    }

    fun getUserProfile(): UserProfile {
        return _userProfile.value
    }

    fun getAppThemeType(): AppThemeType {
        return _appThemeType.value
    }

    fun setAppThemeType(type: AppThemeType) {
        prefs.edit().putString(KEY_APP_THEME_TYPE, type.name).apply()
        _appThemeType.value = type
    }

    fun getThemeMode(): ThemeMode {
        return _themeMode.value
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    fun loginWithGoogle(email: String, displayName: String, photoUrl: String?): UserProfile {
        val current = _userProfile.value
        val updated = current.copy(
            userName = displayName.ifBlank { "Explorer" },
            userEmail = email,
            authProvider = "GOOGLE",
            profileImageUri = photoUrl ?: current.profileImageUri,
            isLoggedIn = true,
            isOnboardingCompleted = true
        )
        saveUserProfile(updated)
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, true).apply()
        _isLoggedIn.value = true
        return updated
    }

    fun registerWithEmail(email: String, password: String, displayName: String): Result<UserProfile> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }

        val accountsJson = prefs.getString(KEY_ACCOUNTS_STORE, "{}") ?: "{}"
        val accountsObj = try { JSONObject(accountsJson) } catch (e: Exception) { JSONObject() }

        if (accountsObj.has(cleanEmail)) {
            return Result.failure(IllegalArgumentException("An account with this email already exists. Please sign in."))
        }

        val userRecord = JSONObject().apply {
            put("name", displayName.trim().ifBlank { cleanEmail.substringBefore("@") })
            put("password", password)
            put("created_at", System.currentTimeMillis())
        }
        accountsObj.put(cleanEmail, userRecord)
        prefs.edit().putString(KEY_ACCOUNTS_STORE, accountsObj.toString()).apply()

        val newProfile = _userProfile.value.copy(
            userName = displayName.trim().ifBlank { cleanEmail.substringBefore("@") },
            userEmail = cleanEmail,
            authProvider = "EMAIL",
            isLoggedIn = true,
            isOnboardingCompleted = true
        )
        saveUserProfile(newProfile)
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, true).apply()
        _isLoggedIn.value = true
        return Result.success(newProfile)
    }

    fun loginWithEmail(email: String, password: String): Result<UserProfile> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your email."))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your password."))
        }

        val accountsJson = prefs.getString(KEY_ACCOUNTS_STORE, "{}") ?: "{}"
        val accountsObj = try { JSONObject(accountsJson) } catch (e: Exception) { JSONObject() }

        if (!accountsObj.has(cleanEmail)) {
            // For convenience, if this is a fresh demo account or no accounts exist yet, register auto or check password
            val fallbackName = cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
            val newProfile = _userProfile.value.copy(
                userName = fallbackName,
                userEmail = cleanEmail,
                authProvider = "EMAIL",
                isLoggedIn = true,
                isOnboardingCompleted = true
            )
            val userRecord = JSONObject().apply {
                put("name", fallbackName)
                put("password", password)
                put("created_at", System.currentTimeMillis())
            }
            accountsObj.put(cleanEmail, userRecord)
            prefs.edit().putString(KEY_ACCOUNTS_STORE, accountsObj.toString()).apply()
            saveUserProfile(newProfile)
            prefs.edit().putBoolean(KEY_IS_LOGGED_IN, true).apply()
            _isLoggedIn.value = true
            return Result.success(newProfile)
        }

        val userObj = accountsObj.getJSONObject(cleanEmail)
        val savedPass = userObj.optString("password")
        if (savedPass != password) {
            return Result.failure(IllegalArgumentException("Incorrect password. Please try again."))
        }

        val savedName = userObj.optString("name", cleanEmail.substringBefore("@"))
        val loggedProfile = _userProfile.value.copy(
            userName = savedName,
            userEmail = cleanEmail,
            authProvider = "EMAIL",
            isLoggedIn = true,
            isOnboardingCompleted = true
        )
        saveUserProfile(loggedProfile)
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, true).apply()
        _isLoggedIn.value = true
        return Result.success(loggedProfile)
    }

    fun loginAsGuest(): UserProfile {
        val guest = _userProfile.value.copy(
            userName = "Guest Explorer",
            userEmail = "guest@ceylonsteps.lk",
            authProvider = "GUEST",
            isLoggedIn = true,
            isOnboardingCompleted = true
        )
        saveUserProfile(guest)
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, true).apply()
        _isLoggedIn.value = true
        return guest
    }

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
        _isLoggedIn.value = false
        _userProfile.value = _userProfile.value.copy(isLoggedIn = false)
        com.ceylonsteps.travelapp.auth.UserManager.signOut(context)
    }

    fun saveUserProfile(profile: UserProfile) {
        prefs.edit()
            .putString(KEY_USER_NAME, profile.userName)
            .putString(KEY_USER_EMAIL, profile.userEmail)
            .putString(KEY_AUTH_PROVIDER, profile.authProvider)
            .putString(KEY_PROFILE_IMAGE_URI, profile.profileImageUri)
            .putString(KEY_COVER_IMAGE_URI, profile.coverImageUri)
            .putString(KEY_BIO, profile.bio)
            .putString(KEY_HOME_LOCATION_NAME, profile.homeLocationName)
            .putString(KEY_HOME_LATITUDE, profile.homeLatitude.toString())
            .putString(KEY_HOME_LONGITUDE, profile.homeLongitude.toString())
            .putBoolean(KEY_IS_LOGGED_IN, profile.isLoggedIn)
            .putBoolean(KEY_ONBOARDING_COMPLETED, profile.isOnboardingCompleted)
            .apply()

        _userProfile.value = profile
    }

    fun updateProfile(
        name: String,
        imageUri: String?,
        coverUri: String? = _userProfile.value.coverImageUri,
        bio: String = _userProfile.value.bio,
        homeLocationName: String,
        homeLat: Double,
        homeLng: Double
    ) {
        val current = _userProfile.value
        val updated = current.copy(
            userName = name.ifBlank { "Explorer" },
            profileImageUri = imageUri,
            coverImageUri = coverUri,
            bio = bio,
            homeLocationName = homeLocationName.ifBlank { "Sri Lanka" },
            homeLatitude = homeLat,
            homeLongitude = homeLng,
            isOnboardingCompleted = true
        )
        saveUserProfile(updated)
    }

    fun updateAvatar(imageUri: String?) {
        val current = _userProfile.value
        saveUserProfile(current.copy(profileImageUri = imageUri))
    }

    fun updateCover(coverUri: String?) {
        val current = _userProfile.value
        saveUserProfile(current.copy(coverImageUri = coverUri))
    }

    fun clearUserData() {
        prefs.edit().clear().apply()
        _isLoggedIn.value = false
        _userProfile.value = UserProfile(isOnboardingCompleted = false, isLoggedIn = false)
    }

    private fun loadAppThemeType(): AppThemeType {
        val raw = prefs.getString(KEY_APP_THEME_TYPE, AppThemeType.DEFAULT.name) ?: AppThemeType.DEFAULT.name
        return try {
            AppThemeType.valueOf(raw)
        } catch (e: Exception) {
            AppThemeType.DEFAULT
        }
    }

    private fun loadThemeMode(): ThemeMode {
        val raw = prefs.getString(KEY_THEME_MODE, ThemeMode.LIGHT.name) ?: ThemeMode.LIGHT.name
        return try {
            ThemeMode.valueOf(raw)
        } catch (e: Exception) {
            ThemeMode.LIGHT
        }
    }

    private fun loadProfile(): UserProfile {
        val name = prefs.getString(KEY_USER_NAME, "Sri Lanka Explorer") ?: "Sri Lanka Explorer"
        val email = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        val provider = prefs.getString(KEY_AUTH_PROVIDER, "GOOGLE") ?: "GOOGLE"
        val imgUri = prefs.getString(KEY_PROFILE_IMAGE_URI, null)
        val coverUri = prefs.getString(KEY_COVER_IMAGE_URI, null)
        val bio = prefs.getString(KEY_BIO, "Exploring the paradise island of Sri Lanka 🇱🇰") ?: "Exploring the paradise island of Sri Lanka 🇱🇰"
        val homeName = prefs.getString(KEY_HOME_LOCATION_NAME, "Colombo, Western Province") ?: "Colombo, Western Province"
        val homeLat = prefs.getString(KEY_HOME_LATITUDE, "6.9271")?.toDoubleOrNull() ?: 6.9271
        val homeLng = prefs.getString(KEY_HOME_LONGITUDE, "79.8612")?.toDoubleOrNull() ?: 79.8612
        val loggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val completed = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)

        return UserProfile(
            userName = name,
            userEmail = email,
            authProvider = provider,
            profileImageUri = imgUri,
            coverImageUri = coverUri,
            bio = bio,
            homeLocationName = homeName,
            homeLatitude = homeLat,
            homeLongitude = homeLng,
            isLoggedIn = loggedIn,
            isOnboardingCompleted = completed
        )
    }

    companion object {
        private const val PREFS_NAME = "lanka_footprints_user_prefs"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_AUTH_PROVIDER = "user_auth_provider"
        private const val KEY_PROFILE_IMAGE_URI = "profile_image_uri"
        private const val KEY_COVER_IMAGE_URI = "cover_image_uri"
        private const val KEY_BIO = "user_bio"
        private const val KEY_HOME_LOCATION_NAME = "home_location_name"
        private const val KEY_HOME_LATITUDE = "home_latitude"
        private const val KEY_HOME_LONGITUDE = "home_longitude"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_THEME_MODE = "app_theme_mode"
        private const val KEY_APP_THEME_TYPE = "app_theme_type"
        private const val KEY_ACCOUNTS_STORE = "user_accounts_store"

        @Volatile
        private var INSTANCE: UserManager? = null

        fun getInstance(context: Context): UserManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
