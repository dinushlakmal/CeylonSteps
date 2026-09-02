package com.example.data.model

/**
 * User Profile entity representing the traveler's identity, avatar,
 * authentication details, and home base coordinates for distance calculation.
 */
data class UserProfile(
    val userName: String = "Sri Lanka Explorer",
    val userEmail: String = "",
    val authProvider: String = "GOOGLE",
    val profileImageUri: String? = null,
    val coverImageUri: String? = null,
    val bio: String = "Exploring the paradise island of Sri Lanka 🇱🇰",
    val homeLocationName: String = "Colombo, Western Province",
    val homeLatitude: Double = 6.9271,
    val homeLongitude: Double = 79.8612,
    val isLoggedIn: Boolean = false,
    val isOnboardingCompleted: Boolean = false
)
