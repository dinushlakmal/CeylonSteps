package com.ceylonsteps.travelapp.social.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ceylonsteps.travelapp.social.model.PublicTripPost
import com.ceylonsteps.travelapp.social.repository.SocialRepository
import com.example.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class SocialFeedFragment : Fragment() {

    private val socialRepository = SocialRepository()
    private var feedListener: ListenerRegistration? = null

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var rvSocialFeed: RecyclerView
    private lateinit var layoutEmptyFeed: View
    private lateinit var btnShareStoryTop: MaterialButton

    private lateinit var feedAdapter: SocialFeedAdapter

    private val likedPostIds = mutableSetOf<String>()
    private val followedUserIds = mutableSetOf<String>()
    private var currentUserId: String = "guest_explorer"

    var onNavigateToMap: ((lat: Double, lng: Double, title: String) -> Unit)? = null
    var onOpenShareDialog: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_social_feed, container, false)

        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
        currentUserId = account?.id ?: "local_explorer_${android.os.Build.MODEL}"

        swipeRefresh = view.findViewById(R.id.swipe_refresh_feed)
        rvSocialFeed = view.findViewById(R.id.rv_social_feed)
        layoutEmptyFeed = view.findViewById(R.id.layout_empty_feed)
        btnShareStoryTop = view.findViewById(R.id.btn_share_story_top)

        setupSwipeRefresh()
        setupRecyclerView()
        setupListeners()
        loadFeedRealtime()

        return view
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setColorSchemeColors(
            requireContext().getColor(android.R.color.holo_green_dark),
            requireContext().getColor(android.R.color.holo_blue_dark)
        )
        swipeRefresh.setOnRefreshListener {
            loadFeedRealtime()
        }
    }

    private fun setupRecyclerView() {
        feedAdapter = SocialFeedAdapter(
            currentUserId = currentUserId,
            followedUserIds = followedUserIds,
            likedPostIds = likedPostIds,
            onLikeClick = { post, isCurrentlyLiked ->
                handleLikeToggle(post, isCurrentlyLiked)
            },
            onCommentClick = { post ->
                val bottomSheet = CommentsBottomSheet.newInstance(post.postId, post.tripTitle)
                bottomSheet.show(parentFragmentManager, "CommentsBottomSheet_${post.postId}")
            },
            onFollowClick = { authorId, isCurrentlyFollowing ->
                handleFollowToggle(authorId, isCurrentlyFollowing)
            },
            onViewOnMapClick = { lat, lng, title ->
                if (onNavigateToMap != null) {
                    onNavigateToMap?.invoke(lat, lng, title)
                } else {
                    // Fallback to geo intent if no host callback attached
                    val geoUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($title)")
                    val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
                    if (mapIntent.resolveActivity(requireContext().packageManager) != null) {
                        startActivity(mapIntent)
                    } else {
                        Toast.makeText(requireContext(), "Coordinates: $lat, $lng", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onPhotoClick = { photoUrl, _, _ ->
                // Open browser/full view if needed
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(photoUrl))
                    startActivity(intent)
                } catch (e: Exception) {
                    // Ignore
                }
            }
        )

        rvSocialFeed.layoutManager = LinearLayoutManager(requireContext())
        rvSocialFeed.adapter = feedAdapter
    }

    private fun setupListeners() {
        btnShareStoryTop.setOnClickListener {
            onOpenShareDialog?.invoke()
        }
    }

    private fun loadFeedRealtime() {
        feedListener?.remove()
        swipeRefresh.isRefreshing = true

        feedListener = socialRepository.getGlobalFeedQuery()
            .addSnapshotListener { snapshot, error ->
                swipeRefresh.isRefreshing = false
                if (error != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val posts = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(PublicTripPost::class.java)?.copy(postId = doc.id)
                    }

                    feedAdapter.submitList(posts)
                    layoutEmptyFeed.visibility = if (posts.isEmpty()) View.VISIBLE else View.GONE
                }
            }
    }

    private fun handleLikeToggle(post: PublicTripPost, isCurrentlyLiked: Boolean) {
        if (isCurrentlyLiked) {
            likedPostIds.remove(post.postId)
        } else {
            likedPostIds.add(post.postId)
        }
        feedAdapter.notifyDataSetChanged()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                socialRepository.toggleLikePost(post.postId, currentUserId, isCurrentlyLiked)
            } catch (e: Exception) {
                // Rollback on error
                if (isCurrentlyLiked) {
                    likedPostIds.add(post.postId)
                } else {
                    likedPostIds.remove(post.postId)
                }
                feedAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun handleFollowToggle(authorId: String, isCurrentlyFollowing: Boolean) {
        if (isCurrentlyFollowing) {
            followedUserIds.remove(authorId)
        } else {
            followedUserIds.add(authorId)
        }
        feedAdapter.notifyDataSetChanged()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                socialRepository.toggleFollow(currentUserId, authorId, isCurrentlyFollowing)
                Toast.makeText(
                    requireContext(),
                    if (isCurrentlyFollowing) "Unfollowed explorer" else "Following explorer! 🎉",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                if (isCurrentlyFollowing) {
                    followedUserIds.add(authorId)
                } else {
                    followedUserIds.remove(authorId)
                }
                feedAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        feedListener?.remove()
        feedListener = null
    }
}
