package com.ceylonsteps.travelapp.social.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class SocialUser(
    @DocumentId val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val bio: String = "Exploring Sri Lanka 🇱🇰",
    val followersCount: Long = 0,
    val followingCount: Long = 0,
    val totalTripsPublished: Long = 0
)

data class PublicTripPost(
    @DocumentId val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String = "",
    val tripTitle: String = "",
    val locationName: String = "",
    val province: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val storyNotes: String = "",
    val driveImageUrls: List<String> = emptyList(), // Direct Drive preview links
    val likesCount: Long = 0,
    val commentsCount: Long = 0,
    @ServerTimestamp val createdAt: Date? = null
)

data class PostComment(
    @DocumentId val commentId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String = "",
    val commentText: String = "",
    @ServerTimestamp val timestamp: Date? = null
)
