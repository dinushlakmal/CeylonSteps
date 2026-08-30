package com.example.data.model

/**
 * User Profile entity representing the traveler's identity, avatar,
 * and home base coordinates for distance calculation.
 */
data class UserProfile(
    val userName: String = "Sri Lanka Explorer",
    val profileImageUri: String? = null,
    val homeLocationName: String = "Colombo, Western Province",
    val homeLatitude: Double = 6.9271,
    val homeLongitude: Double = 79.8612,
    val isOnboardingCompleted: Boolean = false
)
