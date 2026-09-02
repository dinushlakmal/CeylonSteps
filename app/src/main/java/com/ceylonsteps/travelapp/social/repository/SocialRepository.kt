package com.ceylonsteps.travelapp.social.repository

import com.ceylonsteps.travelapp.social.model.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class SocialRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    // 1. Sync / Create Social Profile on Sign-In
    suspend fun syncUserProfile(user: SocialUser) {
        val userRef = db.collection("users").document(user.userId)
        val doc = userRef.get().await()
        if (!doc.exists()) {
            userRef.set(user).await()
        }
    }

    // 2. Publish Public Post
    suspend fun publishTripPost(post: PublicTripPost): String {
        val docRef = db.collection("public_posts").document()
        val postWithId = post.copy(postId = docRef.id)
        docRef.set(postWithId).await()
        db.collection("users").document(post.authorId)
            .update("totalTripsPublished", FieldValue.increment(1))
        return docRef.id
    }

    // 3. Global & Following Feed Stream
    fun getGlobalFeedQuery(): Query =
        db.collection("public_posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(30)

    // 4. Atomic Toggle Like
    suspend fun toggleLikePost(postId: String, userId: String, isLiked: Boolean) {
        val postRef = db.collection("public_posts").document(postId)
        val likeRef = postRef.collection("likes").document(userId)

        db.runTransaction { transaction ->
            if (isLiked) {
                transaction.delete(likeRef)
                transaction.update(postRef, "likesCount", FieldValue.increment(-1))
            } else {
                transaction.set(likeRef, mapOf("timestamp" to FieldValue.serverTimestamp()))
                transaction.update(postRef, "likesCount", FieldValue.increment(1))
            }
        }.await()
    }

    // 5. Follow / Unfollow System
    suspend fun toggleFollow(currentUserId: String, targetUserId: String, isFollowing: Boolean) {
        val currentUserRef = db.collection("users").document(currentUserId)
        val targetUserRef = db.collection("users").document(targetUserId)
        val followingDoc = currentUserRef.collection("following").document(targetUserId)
        val followersDoc = targetUserRef.collection("followers").document(currentUserId)

        db.runTransaction { transaction ->
            if (isFollowing) {
                transaction.delete(followingDoc)
                transaction.delete(followersDoc)
                transaction.update(currentUserRef, "followingCount", FieldValue.increment(-1))
                transaction.update(targetUserRef, "followersCount", FieldValue.increment(-1))
            } else {
                val data = mapOf("timestamp" to FieldValue.serverTimestamp())
                transaction.set(followingDoc, data)
                transaction.set(followersDoc, data)
                transaction.update(currentUserRef, "followingCount", FieldValue.increment(1))
                transaction.update(targetUserRef, "followersCount", FieldValue.increment(1))
            }
        }.await()
    }

    // 6. Real-time Comments
    suspend fun addComment(postId: String, comment: PostComment) {
        val postRef = db.collection("public_posts").document(postId)
        val commentRef = postRef.collection("comments").document()

        db.runTransaction { transaction ->
            transaction.set(commentRef, comment.copy(commentId = commentRef.id))
            transaction.update(postRef, "commentsCount", FieldValue.increment(1))
        }.await()
    }

    fun getCommentsQuery(postId: String): Query =
        db.collection("public_posts").document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
}
