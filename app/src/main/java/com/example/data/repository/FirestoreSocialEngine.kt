package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.model.social.CommunityFeedFilter
import com.example.data.model.social.FollowerUser
import com.example.data.model.social.SocialComment
import com.example.data.model.social.SocialPost
import com.example.data.model.social.SocialUserProfile
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class FirestoreSocialEngine private constructor(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs: SharedPreferences = context.getSharedPreferences("ceylon_social_prefs", Context.MODE_PRIVATE)

    private val firestore: FirebaseFirestore? by lazy {
        try {
            val existingApps = FirebaseApp.getApps(context)
            val currentApp = existingApps.firstOrNull { it.options.projectId == "gen-lang-client-0998550147" }
                ?: run {
                    existingApps.forEach { runCatching { it.delete() } }
                    val options = FirebaseOptions.Builder()
                        .setApplicationId("1:0998550147:android:ceylonsteps")
                        .setProjectId("gen-lang-client-0998550147")
                        .setApiKey("AIzaSyBsr3wDeiovyTYZmWipxTI1Kn8cB70z_G4")
                        .build()
                    FirebaseApp.initializeApp(context, options)
                }
            FirebaseFirestore.getInstance(currentApp).apply {
                firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firestore initialization error", e)
            try {
                FirebaseFirestore.getInstance()
            } catch (ex: Exception) {
                null
            }
        }
    }

    private val _posts = MutableStateFlow<List<SocialPost>>(emptyList())
    val posts: StateFlow<List<SocialPost>> = _posts.asStateFlow()

    private val _likedPostIds = MutableStateFlow<Set<String>>(loadLikedPostIds())
    val likedPostIds: StateFlow<Set<String>> = _likedPostIds.asStateFlow()

    private val _followingUserIds = MutableStateFlow<Set<String>>(loadFollowingUserIds())
    val followingUserIds: StateFlow<Set<String>> = _followingUserIds.asStateFlow()

    private val _followersList = MutableStateFlow<List<FollowerUser>>(loadFollowers())
    val followersList: StateFlow<List<FollowerUser>> = _followersList.asStateFlow()

    private val _followingList = MutableStateFlow<List<FollowerUser>>(emptyList())
    val followingList: StateFlow<List<FollowerUser>> = _followingList.asStateFlow()

    private val _commentsMap = MutableStateFlow<Map<String, List<SocialComment>>>(emptyMap())
    val commentsMap: StateFlow<Map<String, List<SocialComment>>> = _commentsMap.asStateFlow()

    private var postsListenerRegistration: ListenerRegistration? = null
    private val activeCommentListeners = mutableMapOf<String, ListenerRegistration>()

    init {
        // Initialize with user posts and start live updates
        loadInitialPosts()
        rebuildFollowingList()
        startPostsListener()
    }

    private fun loadInitialPosts() {
        val cached = loadCachedPosts().filter { !it.postId.startsWith("seed_post_") }
        saveCachedPosts(cached)
        _posts.value = enrichPostsWithLocalState(cached)
    }

    private fun enrichPostsWithLocalState(posts: List<SocialPost>): List<SocialPost> {
        val liked = _likedPostIds.value
        val following = _followingUserIds.value
        return posts.map { post ->
            post.copy(
                isLikedByMe = liked.contains(post.postId),
                isFollowingAuthor = following.contains(post.authorId)
            )
        }
    }

    fun startPostsListener() {
        val db = firestore ?: return
        postsListenerRegistration?.remove()
        try {
            postsListenerRegistration = db.collection(COLLECTION_POSTS)
                .orderBy("createdAtEpoch", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Posts listen error, trying basic query: ${error.message}")
                        // Fallback listener without orderBy in case index/rules issue
                        startFallbackPostsListener()
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val fetchedPosts = snapshot.documents.mapNotNull { doc ->
                            try {
                                val data = doc.data ?: return@mapNotNull null
                                parsePostFromMap(doc.id, data)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (fetchedPosts.isNotEmpty()) {
                            saveCachedPosts(fetchedPosts)
                            _posts.value = enrichPostsWithLocalState(fetchedPosts)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start posts listener", e)
            startFallbackPostsListener()
        }
    }

    private fun startFallbackPostsListener() {
        val db = firestore ?: return
        try {
            db.collection(COLLECTION_POSTS)
                .limit(50)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Fallback listen failed: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val fetchedPosts = snapshot.documents.mapNotNull { doc ->
                            try {
                                val data = doc.data ?: return@mapNotNull null
                                parsePostFromMap(doc.id, data)
                            } catch (e: Exception) {
                                null
                            }
                        }.sortedByDescending { it.createdAtEpoch }
                        if (fetchedPosts.isNotEmpty()) {
                            saveCachedPosts(fetchedPosts)
                            _posts.value = enrichPostsWithLocalState(fetchedPosts)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start fallback posts listener", e)
        }
    }

    suspend fun publishPost(post: SocialPost): Result<SocialPost> = withContext(Dispatchers.IO) {
        val postId = if (post.postId.isBlank()) UUID.randomUUID().toString() else post.postId
        val finalPost = post.copy(
            postId = postId,
            createdAtEpoch = System.currentTimeMillis()
        )

        // Update local reactive stream immediately (instant responsive UI)
        val current = _posts.value.toMutableList()
        current.removeAll { it.postId == postId }
        current.add(0, finalPost)
        _posts.value = enrichPostsWithLocalState(current)
        saveCachedPosts(current)

        // Asynchronously synchronize to Firestore in background with timeout
        scope.launch {
            val db = firestore ?: return@launch
            try {
                withTimeoutOrNull(15000L) {
                    val postMap = hashMapOf(
                        "authorId" to finalPost.authorId,
                        "authorName" to finalPost.authorName,
                        "authorAvatarUrl" to (finalPost.authorAvatarUrl ?: ""),
                        "authorBadge" to finalPost.authorBadge,
                        "authorRankLevel" to finalPost.authorRankLevel,
                        "title" to finalPost.title,
                        "story" to finalPost.story,
                        "locationName" to finalPost.locationName,
                        "latitude" to finalPost.latitude,
                        "longitude" to finalPost.longitude,
                        "province" to finalPost.province,
                        "category" to finalPost.category,
                        "mediaUrls" to finalPost.mediaUrls,
                        "isMultiStop" to finalPost.isMultiStop,
                        "totalDistanceKm" to finalPost.totalDistanceKm,
                        "stopCount" to finalPost.stopCount,
                        "stopNames" to finalPost.stopNames,
                        "tags" to finalPost.tags,
                        "likeCount" to 0,
                        "commentCount" to 0,
                        "createdAtEpoch" to finalPost.createdAtEpoch,
                        "travelDateEpoch" to finalPost.travelDateEpoch,
                        "isPublic" to finalPost.isPublic
                    )
                    db.collection(COLLECTION_POSTS).document(postId).set(postMap).await()

                    // Update author's tripsSharedCount in ceylon_users
                    if (finalPost.authorId.isNotBlank()) {
                        db.collection(COLLECTION_USERS).document(finalPost.authorId)
                            .update("tripsSharedCount", FieldValue.increment(1))
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Firestore publish post background sync note: ${e.message}")
            }
        }

        Result.success(finalPost)
    }

    suspend fun updatePost(post: SocialPost): Result<SocialPost> = withContext(Dispatchers.IO) {
        val postId = post.postId

        // Optimistic UI update
        val current = _posts.value.toMutableList()
        val index = current.indexOfFirst { it.postId == postId }
        if (index != -1) {
            current[index] = post
            _posts.value = enrichPostsWithLocalState(current)
            saveCachedPosts(current)
        }

        // Background sync
        scope.launch {
            val db = firestore ?: return@launch
            try {
                withTimeoutOrNull(15000L) {
                    val updateMap = mapOf(
                        "title" to post.title,
                        "story" to post.story,
                        "category" to post.category
                    )
                    db.collection(COLLECTION_POSTS).document(postId).update(updateMap).await()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Firestore update post background sync note: ${e.message}")
            }
        }
        Result.success(post)
    }

    suspend fun deletePost(postId: String, authorId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        // Optimistic UI update
        val current = _posts.value.toMutableList()
        current.removeAll { it.postId == postId }
        _posts.value = enrichPostsWithLocalState(current)
        saveCachedPosts(current)

        // Background sync
        scope.launch {
            val db = firestore ?: return@launch
            try {
                withTimeoutOrNull(15000L) {
                    db.collection(COLLECTION_POSTS).document(postId).delete().await()
                    if (authorId.isNotBlank()) {
                        db.collection(COLLECTION_USERS).document(authorId)
                            .update("tripsSharedCount", FieldValue.increment(-1))
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Firestore delete post background sync note: ${e.message}")
            }
        }
        Result.success(true)
    }

    suspend fun toggleLike(postId: String, userId: String) = withContext(Dispatchers.IO) {
        val currentLiked = _likedPostIds.value.toMutableSet()
        val isCurrentlyLiked = currentLiked.contains(postId)
        val newLikedState = !isCurrentlyLiked

        if (newLikedState) {
            currentLiked.add(postId)
        } else {
            currentLiked.remove(postId)
        }
        _likedPostIds.value = currentLiked
        saveLikedPostIds(currentLiked)

        // Update local post like count
        val updated = _posts.value.map { post ->
            if (post.postId == postId) {
                val newCount = if (newLikedState) post.likeCount + 1 else (post.likeCount - 1).coerceAtLeast(0)
                post.copy(likeCount = newCount, isLikedByMe = newLikedState)
            } else post
        }
        _posts.value = updated
        saveCachedPosts(updated)

        // Firestore atomic update
        val db = firestore ?: return@withContext
        try {
            val postRef = db.collection(COLLECTION_POSTS).document(postId)
            val likeRef = db.collection(COLLECTION_POSTS).document(postId)
                .collection("user_likes").document(userId.ifBlank { "anonymous" })

            db.runTransaction { transaction ->
                if (newLikedState) {
                    transaction.set(likeRef, hashMapOf("timestamp" to System.currentTimeMillis()))
                    transaction.update(postRef, "likeCount", FieldValue.increment(1))
                } else {
                    transaction.delete(likeRef)
                    transaction.update(postRef, "likeCount", FieldValue.increment(-1))
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore toggle like error", e)
        }
    }

    suspend fun toggleFollow(targetUserId: String, currentUserId: String) = withContext(Dispatchers.IO) {
        if (targetUserId.isBlank()) return@withContext
        val currentFollowing = _followingUserIds.value.toMutableSet()
        val isFollowing = currentFollowing.contains(targetUserId)
        val newFollowingState = !isFollowing

        if (newFollowingState) {
            currentFollowing.add(targetUserId)
        } else {
            currentFollowing.remove(targetUserId)
        }
        _followingUserIds.value = currentFollowing
        saveFollowingUserIds(currentFollowing)

        _posts.value = _posts.value.map { post ->
            if (post.authorId == targetUserId) {
                post.copy(isFollowingAuthor = newFollowingState)
            } else post
        }

        // Update followers list isFollowedByMe status
        _followersList.value = _followersList.value.map { follower ->
            if (follower.userId == targetUserId) {
                follower.copy(isFollowedByMe = newFollowingState)
            } else follower
        }
        saveFollowers(_followersList.value)
        rebuildFollowingList()

        val db = firestore ?: return@withContext
        try {
            val myFollowingRef = db.collection(COLLECTION_USERS)
                .document(currentUserId.ifBlank { "me" })
                .collection("following").document(targetUserId)

            val targetFollowersRef = db.collection(COLLECTION_USERS)
                .document(targetUserId)
                .collection("followers").document(currentUserId.ifBlank { "me" })

            db.runTransaction { transaction ->
                if (newFollowingState) {
                    transaction.set(myFollowingRef, hashMapOf("timestamp" to System.currentTimeMillis()))
                    transaction.set(targetFollowersRef, hashMapOf("timestamp" to System.currentTimeMillis()))
                    transaction.update(db.collection(COLLECTION_USERS).document(targetUserId), "followersCount", FieldValue.increment(1))
                } else {
                    transaction.delete(myFollowingRef)
                    transaction.delete(targetFollowersRef)
                    transaction.update(db.collection(COLLECTION_USERS).document(targetUserId), "followersCount", FieldValue.increment(-1))
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore toggle follow error", e)
        }
    }

    fun removeFollower(targetUserId: String) {
        val updated = _followersList.value.filter { it.userId != targetUserId }
        _followersList.value = updated
        saveFollowers(updated)
    }

    private fun rebuildFollowingList() {
        val followingIds = _followingUserIds.value
        val existingFollowers = _followersList.value.associateBy { it.userId }
        val postAuthors = _posts.value.associateBy { it.authorId }

        val list = followingIds.map { id ->
            val fromFollower = existingFollowers[id]
            val fromPost = postAuthors[id]
            FollowerUser(
                userId = id,
                displayName = fromFollower?.displayName ?: fromPost?.authorName ?: "Ceylon Explorer",
                email = fromFollower?.email ?: "",
                avatarUrl = fromFollower?.avatarUrl ?: fromPost?.authorAvatarUrl,
                badge = fromFollower?.badge ?: fromPost?.authorBadge ?: "Explorer",
                rankLevel = fromFollower?.rankLevel ?: fromPost?.authorRankLevel ?: 2,
                location = fromFollower?.location ?: fromPost?.province ?: "Sri Lanka",
                bio = fromFollower?.bio ?: "Sri Lanka travel enthusiast 🇱🇰",
                isFollowedByMe = true,
                followedSinceEpoch = fromFollower?.followedSinceEpoch ?: System.currentTimeMillis()
            )
        }
        _followingList.value = list
    }

    fun listenToComments(postId: String) {
        if (activeCommentListeners.containsKey(postId)) return
        val db = firestore ?: return

        try {
            val registration = db.collection(COLLECTION_POSTS).document(postId)
                .collection("comments")
                .orderBy("timestampEpoch", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Comments listen failed for $postId: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val commentList = snapshot.documents.mapNotNull { doc ->
                            try {
                                val d = doc.data ?: return@mapNotNull null
                                SocialComment(
                                    commentId = doc.id,
                                    postId = postId,
                                    authorId = d["authorId"] as? String ?: "",
                                    authorName = d["authorName"] as? String ?: "Explorer",
                                    authorAvatarUrl = d["authorAvatarUrl"] as? String,
                                    authorBadge = d["authorBadge"] as? String ?: "Traveler",
                                    text = d["text"] as? String ?: "",
                                    timestampEpoch = (d["timestampEpoch"] as? Number)?.toLong() ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        val map = _commentsMap.value.toMutableMap()
                        map[postId] = commentList
                        _commentsMap.value = map
                    }
                }
            activeCommentListeners[postId] = registration
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start comments listener", e)
        }
    }

    suspend fun addComment(postId: String, comment: SocialComment) = withContext(Dispatchers.IO) {
        val commentId = if (comment.commentId.isBlank()) UUID.randomUUID().toString() else comment.commentId
        val finalComment = comment.copy(
            commentId = commentId,
            postId = postId,
            timestampEpoch = System.currentTimeMillis()
        )

        // Optimistic local update
        val map = _commentsMap.value.toMutableMap()
        val list = (map[postId] ?: emptyList()).toMutableList()
        list.add(finalComment)
        map[postId] = list
        _commentsMap.value = map

        // Increment post comment count locally
        val updatedPosts = _posts.value.map {
            if (it.postId == postId) it.copy(commentCount = it.commentCount + 1) else it
        }
        _posts.value = updatedPosts
        saveCachedPosts(updatedPosts)

        val db = firestore ?: return@withContext
        try {
            val commentData = hashMapOf(
                "postId" to postId,
                "authorId" to finalComment.authorId,
                "authorName" to finalComment.authorName,
                "authorAvatarUrl" to (finalComment.authorAvatarUrl ?: ""),
                "authorBadge" to finalComment.authorBadge,
                "text" to finalComment.text,
                "timestampEpoch" to finalComment.timestampEpoch
            )
            val postRef = db.collection(COLLECTION_POSTS).document(postId)
            val commentRef = postRef.collection("comments").document(commentId)

            db.runTransaction { tx ->
                tx.set(commentRef, commentData)
                tx.update(postRef, "commentCount", FieldValue.increment(1))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore add comment error", e)
        }
    }

    suspend fun syncUserProfile(profile: SocialUserProfile) = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext
        if (profile.userId.isBlank()) return@withContext
        try {
            val userMap = hashMapOf(
                "userId" to profile.userId,
                "displayName" to profile.displayName,
                "email" to profile.email,
                "avatarUrl" to (profile.avatarUrl ?: ""),
                "bio" to profile.bio,
                "homeBase" to profile.homeBase,
                "badgeTitle" to profile.badgeTitle,
                "tripsSharedCount" to profile.tripsSharedCount,
                "followersCount" to profile.followersCount,
                "followingCount" to profile.followingCount,
                "joinedTimestampEpoch" to profile.joinedTimestampEpoch
            )
            db.collection(COLLECTION_USERS).document(profile.userId).set(userMap).await()
        } catch (e: Exception) {
            Log.w(TAG, "Sync user profile error", e)
        }
    }

    private fun parsePostFromMap(id: String, d: Map<String, Any>): SocialPost {
        @Suppress("UNCHECKED_CAST")
        val mediaList = (d["mediaUrls"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val stopList = (d["stopNames"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val tagList = (d["tags"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()

        return SocialPost(
            postId = id,
            authorId = d["authorId"] as? String ?: "",
            authorName = d["authorName"] as? String ?: "Explorer",
            authorAvatarUrl = (d["authorAvatarUrl"] as? String)?.takeIf { it.isNotBlank() },
            authorBadge = d["authorBadge"] as? String ?: "Lanka Explorer",
            authorRankLevel = (d["authorRankLevel"] as? Number)?.toInt() ?: 1,
            title = d["title"] as? String ?: "",
            story = d["story"] as? String ?: "",
            locationName = d["locationName"] as? String ?: "",
            latitude = (d["latitude"] as? Number)?.toDouble() ?: 0.0,
            longitude = (d["longitude"] as? Number)?.toDouble() ?: 0.0,
            province = d["province"] as? String ?: "Central",
            category = d["category"] as? String ?: "SCENIC",
            mediaUrls = mediaList,
            isMultiStop = d["isMultiStop"] as? Boolean ?: false,
            totalDistanceKm = (d["totalDistanceKm"] as? Number)?.toDouble() ?: 0.0,
            stopCount = (d["stopCount"] as? Number)?.toInt() ?: 1,
            stopNames = stopList,
            tags = tagList,
            likeCount = (d["likeCount"] as? Number)?.toInt() ?: 0,
            commentCount = (d["commentCount"] as? Number)?.toInt() ?: 0,
            createdAtEpoch = (d["createdAtEpoch"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            travelDateEpoch = (d["travelDateEpoch"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            isPublic = d["isPublic"] as? Boolean ?: true
        )
    }

    private fun saveCachedPosts(posts: List<SocialPost>) {
        try {
            val jsonArray = JSONArray()
            for (p in posts.take(50)) {
                val obj = JSONObject().apply {
                    put("postId", p.postId)
                    put("authorId", p.authorId)
                    put("authorName", p.authorName)
                    put("authorAvatarUrl", p.authorAvatarUrl ?: "")
                    put("authorBadge", p.authorBadge)
                    put("authorRankLevel", p.authorRankLevel)
                    put("title", p.title)
                    put("story", p.story)
                    put("locationName", p.locationName)
                    put("latitude", p.latitude)
                    put("longitude", p.longitude)
                    put("province", p.province)
                    put("category", p.category)
                    put("mediaUrls", JSONArray(p.mediaUrls))
                    put("isMultiStop", p.isMultiStop)
                    put("totalDistanceKm", p.totalDistanceKm)
                    put("stopCount", p.stopCount)
                    put("stopNames", JSONArray(p.stopNames))
                    put("tags", JSONArray(p.tags))
                    put("likeCount", p.likeCount)
                    put("commentCount", p.commentCount)
                    put("createdAtEpoch", p.createdAtEpoch)
                    put("travelDateEpoch", p.travelDateEpoch)
                    put("isPublic", p.isPublic)
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString(KEY_CACHED_POSTS, jsonArray.toString()).apply()
        } catch (e: Exception) {
            Log.w(TAG, "Save cached posts failed", e)
        }
    }

    private fun loadCachedPosts(): List<SocialPost> {
        val raw = prefs.getString(KEY_CACHED_POSTS, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<SocialPost>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val mediaJson = obj.optJSONArray("mediaUrls")
                val mediaUrls = mutableListOf<String>()
                if (mediaJson != null) {
                    for (j in 0 until mediaJson.length()) {
                        mediaUrls.add(mediaJson.getString(j))
                    }
                }
                val stopJson = obj.optJSONArray("stopNames")
                val stopNames = mutableListOf<String>()
                if (stopJson != null) {
                    for (k in 0 until stopJson.length()) {
                        stopNames.add(stopJson.getString(k))
                    }
                }
                val tagsJson = obj.optJSONArray("tags")
                val tags = mutableListOf<String>()
                if (tagsJson != null) {
                    for (m in 0 until tagsJson.length()) {
                        tags.add(tagsJson.getString(m))
                    }
                }

                list.add(
                    SocialPost(
                        postId = obj.getString("postId"),
                        authorId = obj.optString("authorId", ""),
                        authorName = obj.optString("authorName", "Explorer"),
                        authorAvatarUrl = obj.optString("authorAvatarUrl").takeIf { it.isNotBlank() },
                        authorBadge = obj.optString("authorBadge", "Lanka Explorer"),
                        authorRankLevel = obj.optInt("authorRankLevel", 1),
                        title = obj.getString("title"),
                        story = obj.optString("story", ""),
                        locationName = obj.getString("locationName"),
                        latitude = obj.optDouble("latitude", 0.0),
                        longitude = obj.optDouble("longitude", 0.0),
                        province = obj.optString("province", "Central"),
                        category = obj.optString("category", "SCENIC"),
                        mediaUrls = mediaUrls,
                        isMultiStop = obj.optBoolean("isMultiStop", false),
                        totalDistanceKm = obj.optDouble("totalDistanceKm", 0.0),
                        stopCount = obj.optInt("stopCount", 1),
                        stopNames = stopNames,
                        tags = tags,
                        likeCount = obj.optInt("likeCount", 0),
                        commentCount = obj.optInt("commentCount", 0),
                        createdAtEpoch = obj.optLong("createdAtEpoch", System.currentTimeMillis()),
                        travelDateEpoch = obj.optLong("travelDateEpoch", System.currentTimeMillis()),
                        isPublic = obj.optBoolean("isPublic", true)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveLikedPostIds(set: Set<String>) {
        prefs.edit().putStringSet(KEY_LIKED_POST_IDS, set).apply()
    }

    private fun loadLikedPostIds(): Set<String> {
        return prefs.getStringSet(KEY_LIKED_POST_IDS, emptySet()) ?: emptySet()
    }

    private fun saveFollowingUserIds(set: Set<String>) {
        prefs.edit().putStringSet(KEY_FOLLOWING_USER_IDS, set).apply()
    }

    private fun loadFollowingUserIds(): Set<String> {
        val raw = prefs.getStringSet(KEY_FOLLOWING_USER_IDS, emptySet()) ?: emptySet()
        val cleaned = raw.filter {
            !it.startsWith("user_kasun") &&
            !it.startsWith("user_nethmi") &&
            !it.startsWith("user_ruwan") &&
            !it.startsWith("user_dinithi") &&
            !it.startsWith("user_chaminda") &&
            !it.startsWith("user_sashika")
        }.toSet()
        if (cleaned.size != raw.size) {
            saveFollowingUserIds(cleaned)
        }
        return cleaned
    }

    private fun getCuratedSeedStories(): List<SocialPost> {
        return emptyList()
    }

    private fun saveFollowers(list: List<FollowerUser>) {
        try {
            val array = JSONArray()
            list.forEach { follower ->
                val obj = JSONObject().apply {
                    put("userId", follower.userId)
                    put("displayName", follower.displayName)
                    put("email", follower.email)
                    put("avatarUrl", follower.avatarUrl ?: "")
                    put("badge", follower.badge)
                    put("rankLevel", follower.rankLevel)
                    put("location", follower.location)
                    put("bio", follower.bio)
                    put("isFollowedByMe", follower.isFollowedByMe)
                    put("followedSinceEpoch", follower.followedSinceEpoch)
                }
                array.put(obj)
            }
            prefs.edit().putString(KEY_FOLLOWERS_LIST, array.toString()).apply()
        } catch (e: Exception) {
            Log.w(TAG, "Save followers failed", e)
        }
    }

    private fun loadFollowers(): List<FollowerUser> {
        val raw = prefs.getString(KEY_FOLLOWERS_LIST, null)
        if (!raw.isNullOrBlank()) {
            try {
                val array = JSONArray(raw)
                val list = mutableListOf<FollowerUser>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val userId = obj.optString("userId", "")
                    // Exclude any legacy dummy seeded users
                    if (userId.startsWith("user_kasun") ||
                        userId.startsWith("user_nethmi") ||
                        userId.startsWith("user_ruwan") ||
                        userId.startsWith("user_dinithi") ||
                        userId.startsWith("user_chaminda") ||
                        userId.startsWith("user_sashika")) {
                        continue
                    }
                    list.add(
                        FollowerUser(
                            userId = userId,
                            displayName = obj.optString("displayName", "Explorer"),
                            email = obj.optString("email", ""),
                            avatarUrl = obj.optString("avatarUrl", "").takeIf { it.isNotBlank() },
                            badge = obj.optString("badge", "Lanka Explorer"),
                            rankLevel = obj.optInt("rankLevel", 1),
                            location = obj.optString("location", "Sri Lanka"),
                            bio = obj.optString("bio", "Exploring Sri Lanka 🇱🇰"),
                            isFollowedByMe = obj.optBoolean("isFollowedByMe", false),
                            followedSinceEpoch = obj.optLong("followedSinceEpoch", System.currentTimeMillis())
                        )
                    )
                }
                saveFollowers(list)
                return list
            } catch (e: Exception) {
                Log.w(TAG, "Load followers failed", e)
            }
        }
        return emptyList()
    }

    companion object {
        private const val TAG = "FirestoreSocialEngine"
        private const val COLLECTION_USERS = "ceylon_users"
        private const val COLLECTION_POSTS = "ceylon_posts"
        private const val KEY_CACHED_POSTS = "key_cached_social_posts"
        private const val KEY_LIKED_POST_IDS = "key_liked_post_ids"
        private const val KEY_FOLLOWING_USER_IDS = "key_following_user_ids"
        private const val KEY_FOLLOWERS_LIST = "key_followers_list"

        @Volatile
        private var INSTANCE: FirestoreSocialEngine? = null

        fun getInstance(context: Context): FirestoreSocialEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirestoreSocialEngine(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
