package com.example.data.model.social

import com.example.data.model.TripLocation
import com.ceylonsteps.travelapp.data.model.TripWithStops

data class FollowerUser(
    val userId: String = "",
    val displayName: String = "Sri Lanka Explorer",
    val email: String = "",
    val avatarUrl: String? = null,
    val badge: String = "Lanka Explorer",
    val rankLevel: Int = 1,
    val location: String = "Colombo, Sri Lanka",
    val bio: String = "Exploring Sri Lanka's hidden gems 🇱🇰",
    val isFollowedByMe: Boolean = false,
    val followedSinceEpoch: Long = System.currentTimeMillis()
)

data class SocialUserProfile(
    val userId: String = "",
    val displayName: String = "Sri Lanka Explorer",
    val email: String = "",
    val avatarUrl: String? = null,
    val bio: String = "Passionate explorer traveling across the pearl of the Indian Ocean 🇱🇰",
    val homeBase: String = "Colombo, Western Province",
    val badgeTitle: String = "Lanka Trailblazer",
    val rankLevel: Int = 1,
    val rankTitle: String = "Island Wanderer",
    val rankStars: Int = 1,
    val tripsSharedCount: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val joinedTimestampEpoch: Long = System.currentTimeMillis()
)

data class SocialComment(
    val commentId: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "Explorer",
    val authorAvatarUrl: String? = null,
    val authorBadge: String = "Traveler",
    val text: String = "",
    val timestampEpoch: Long = System.currentTimeMillis()
)

data class SocialPost(
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "Explorer",
    val authorAvatarUrl: String? = null,
    val authorBadge: String = "Lanka Explorer",
    val authorRankLevel: Int = 1,
    val title: String = "",
    val story: String = "",
    val locationName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val province: String = "Central",
    val category: String = "SCENIC", // SCENIC, HERITAGE, BEACH, WILDLIFE, ADVENTURE, ROADTRIP
    val mediaUrls: List<String> = emptyList(), // Direct Google Drive CDN links: https://lh3.googleusercontent.com/d/{FILE_ID}
    val isMultiStop: Boolean = false,
    val totalDistanceKm: Double = 0.0,
    val stopCount: Int = 1,
    val stopNames: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val createdAtEpoch: Long = System.currentTimeMillis(),
    val travelDateEpoch: Long = System.currentTimeMillis(),
    val isLikedByMe: Boolean = false,
    val isFollowingAuthor: Boolean = false,
    val isPublic: Boolean = true
)

enum class CommunityFeedFilter(val title: String, val iconName: String) {
    ALL("All Stories", "Explore"),
    TRENDING("Trending", "Whatshot"),
    FOLLOWING("Following", "People"),
    MULTI_STOP("Road Trips", "Route"),
    HERITAGE("Heritage", "AccountBalance"),
    NATURE("Nature & Wildlife", "Forest"),
    BEACHES("Coast & Beaches", "BeachAccess")
}

data class ShareTripPayload(
    val selectedSingleTrip: TripLocation? = null,
    val selectedJourney: TripWithStops? = null,
    val customTitle: String = "",
    val story: String = "",
    val selectedMediaUris: List<String> = emptyList(),
    val category: String = "SCENIC",
    val tags: List<String> = emptyList(),
    val isPublic: Boolean = true
)
