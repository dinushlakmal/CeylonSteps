package com.ceylonsteps.travelapp.social.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.ceylonsteps.travelapp.social.model.PublicTripPost
import com.example.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import java.text.SimpleDateFormat
import java.util.Locale

class SocialFeedAdapter(
    private val currentUserId: String,
    private val followedUserIds: Set<String>,
    private val likedPostIds: Set<String>,
    private val onLikeClick: (post: PublicTripPost, isCurrentlyLiked: Boolean) -> Unit,
    private val onCommentClick: (post: PublicTripPost) -> Unit,
    private val onFollowClick: (authorId: String, isCurrentlyFollowing: Boolean) -> Unit,
    private val onViewOnMapClick: (lat: Double, lng: Double, title: String) -> Unit,
    private val onPhotoClick: (photoUrl: String, allPhotos: List<String>, position: Int) -> Unit
) : ListAdapter<PublicTripPost, SocialFeedAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_social_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgAuthorAvatar: ShapeableImageView = itemView.findViewById(R.id.img_author_avatar)
        private val tvAuthorName: TextView = itemView.findViewById(R.id.tv_author_name)
        private val tvPostTimestamp: TextView = itemView.findViewById(R.id.tv_post_timestamp)
        private val btnFollowToggle: MaterialButton = itemView.findViewById(R.id.btn_follow_toggle)
        private val layoutDestinationPill: LinearLayout = itemView.findViewById(R.id.layout_destination_pill)
        private val tvDestinationLabel: TextView = itemView.findViewById(R.id.tv_destination_label)
        private val tvPostTitle: TextView = itemView.findViewById(R.id.tv_post_title)
        private val tvStoryNotes: TextView = itemView.findViewById(R.id.tv_story_notes)
        private val rvPostPhotos: RecyclerView = itemView.findViewById(R.id.rv_post_photos)
        private val btnLike: LinearLayout = itemView.findViewById(R.id.btn_like)
        private val imgLikeIcon: ImageView = itemView.findViewById(R.id.img_like_icon)
        private val tvLikesCount: TextView = itemView.findViewById(R.id.tv_likes_count)
        private val btnComment: LinearLayout = itemView.findViewById(R.id.btn_comment)
        private val tvCommentsCount: TextView = itemView.findViewById(R.id.tv_comments_count)
        private val btnViewOnMap: MaterialButton = itemView.findViewById(R.id.btn_view_on_map)

        fun bind(post: PublicTripPost) {
            val context = itemView.context

            // Author details
            tvAuthorName.text = post.authorName.ifBlank { "Ceylon Explorer" }
            val timeText = post.createdAt?.let {
                val sdf = SimpleDateFormat("MMM d, yyyy", Locale.US)
                sdf.format(it)
            } ?: "Recent memory"
            tvPostTimestamp.text = "$timeText • CeylonSteps Community"

            // Avatar loading
            if (post.authorPhotoUrl.isNotBlank()) {
                imgAuthorAvatar.load(post.authorPhotoUrl) {
                    crossfade(true)
                    transformations(CircleCropTransformation())
                    placeholder(R.drawable.ic_footsteps)
                    error(R.drawable.ic_footsteps)
                }
            } else {
                imgAuthorAvatar.setImageResource(R.drawable.ic_footsteps)
            }

            // Follow button state
            val isAuthorSelf = post.authorId == currentUserId
            val isFollowing = followedUserIds.contains(post.authorId)
            if (isAuthorSelf) {
                btnFollowToggle.visibility = View.GONE
            } else {
                btnFollowToggle.visibility = View.VISIBLE
                if (isFollowing) {
                    btnFollowToggle.text = "Following"
                    btnFollowToggle.setBackgroundColor(context.getColor(android.R.color.transparent))
                    btnFollowToggle.strokeWidth = 2
                } else {
                    btnFollowToggle.text = "+ Follow"
                    btnFollowToggle.strokeWidth = 0
                }
                btnFollowToggle.setOnClickListener {
                    onFollowClick(post.authorId, isFollowing)
                }
            }

            // Destination Pill
            val locLabel = buildString {
                append("📍 ")
                append(post.locationName.ifBlank { "Sri Lanka Spot" })
                if (post.province.isNotBlank()) {
                    append(" • ")
                    append(post.province)
                    if (!post.province.contains("Province", ignoreCase = true)) {
                        append(" Province")
                    }
                }
            }
            tvDestinationLabel.text = locLabel

            // Title & Story Notes
            tvPostTitle.text = post.tripTitle.ifBlank { "Sri Lanka Travel Diary" }
            if (post.storyNotes.isNotBlank()) {
                tvStoryNotes.visibility = View.VISIBLE
                tvStoryNotes.text = post.storyNotes
            } else {
                tvStoryNotes.visibility = View.GONE
            }

            // Photos horizontal slider
            if (post.driveImageUrls.isNotEmpty()) {
                rvPostPhotos.visibility = View.VISIBLE
                val photoAdapter = PostPhotoSliderAdapter(post.driveImageUrls) { url, idx ->
                    onPhotoClick(url, post.driveImageUrls, idx)
                }
                rvPostPhotos.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                rvPostPhotos.adapter = photoAdapter
            } else {
                rvPostPhotos.visibility = View.GONE
            }

            // Likes
            val isLiked = likedPostIds.contains(post.postId)
            imgLikeIcon.setImageResource(if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline)
            tvLikesCount.text = post.likesCount.toString()

            btnLike.setOnClickListener {
                // Heart bounce animation
                animateHeart(imgLikeIcon)
                onLikeClick(post, isLiked)
            }

            // Comments
            tvCommentsCount.text = post.commentsCount.toString()
            btnComment.setOnClickListener {
                onCommentClick(post)
            }

            // View on Lanka Map
            btnViewOnMap.setOnClickListener {
                onViewOnMapClick(post.latitude, post.longitude, post.tripTitle)
            }
            layoutDestinationPill.setOnClickListener {
                onViewOnMapClick(post.latitude, post.longitude, post.tripTitle)
            }
        }

        private fun animateHeart(view: View) {
            val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.35f, 1f)
            val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.35f, 1f)
            val set = AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 350
                interpolator = OvershootInterpolator(3f)
            }
            set.start()
        }
    }

    class PostDiffCallback : DiffUtil.ItemCallback<PublicTripPost>() {
        override fun areItemsTheSame(oldItem: PublicTripPost, newItem: PublicTripPost): Boolean {
            return oldItem.postId == newItem.postId
        }

        override fun areContentsTheSame(oldItem: PublicTripPost, newItem: PublicTripPost): Boolean {
            return oldItem == newItem
        }
    }
}

class PostPhotoSliderAdapter(
    private val photoUrls: List<String>,
    private val onPhotoClick: (String, Int) -> Unit
) : RecyclerView.Adapter<PostPhotoSliderAdapter.PhotoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_social_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val url = photoUrls[position]
        holder.bind(url, position)
    }

    override fun getItemCount(): Int = photoUrls.size

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgPostPhoto: ImageView = itemView.findViewById(R.id.img_post_photo)

        fun bind(url: String, position: Int) {
            imgPostPhoto.load(url) {
                crossfade(true)
                transformations(RoundedCornersTransformation(14f))
                placeholder(R.drawable.ic_photo_placeholder)
                error(R.drawable.ic_photo_placeholder)
            }
            itemView.setOnClickListener {
                onPhotoClick(url, position)
            }
        }
    }
}
