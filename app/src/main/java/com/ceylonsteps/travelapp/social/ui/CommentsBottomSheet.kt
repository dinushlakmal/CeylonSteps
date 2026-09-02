package com.ceylonsteps.travelapp.social.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.ceylonsteps.travelapp.social.model.PostComment
import com.ceylonsteps.travelapp.social.repository.SocialRepository
import com.example.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class CommentsBottomSheet : BottomSheetDialogFragment() {

    private var postId: String = ""
    private var postTitle: String = ""

    private val socialRepository = SocialRepository()
    private var snapshotListener: ListenerRegistration? = null

    private lateinit var rvComments: RecyclerView
    private lateinit var etCommentInput: EditText
    private lateinit var btnSendComment: ImageButton
    private lateinit var tvEmptyComments: TextView
    private lateinit var pbLoading: ProgressBar
    private lateinit var tvSheetTitle: TextView
    private lateinit var tvTotalCommentsBadge: TextView

    private lateinit var commentsAdapter: CommentsAdapter

    companion object {
        private const val ARG_POST_ID = "arg_post_id"
        private const val ARG_POST_TITLE = "arg_post_title"

        fun newInstance(postId: String, postTitle: String): CommentsBottomSheet {
            return CommentsBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_POST_ID, postId)
                    putString(ARG_POST_TITLE, postTitle)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postId = arguments?.getString(ARG_POST_ID) ?: ""
        postTitle = arguments?.getString(ARG_POST_TITLE) ?: "Post Discussion"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_comments, container, false)

        rvComments = view.findViewById(R.id.rv_comments)
        etCommentInput = view.findViewById(R.id.et_comment_input)
        btnSendComment = view.findViewById(R.id.btn_send_comment)
        tvEmptyComments = view.findViewById(R.id.tv_empty_comments)
        pbLoading = view.findViewById(R.id.pb_loading_comments)
        tvSheetTitle = view.findViewById(R.id.tv_sheet_title)
        tvTotalCommentsBadge = view.findViewById(R.id.tv_total_comments_badge)

        tvSheetTitle.text = if (postTitle.isNotBlank()) "Discussion: $postTitle" else "Travel Discussion"

        setupRecyclerView()
        setupListeners()
        startRealtimeCommentsListener()

        return view
    }

    private fun setupRecyclerView() {
        commentsAdapter = CommentsAdapter()
        rvComments.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        rvComments.adapter = commentsAdapter
    }

    private fun setupListeners() {
        btnSendComment.setOnClickListener {
            val text = etCommentInput.text.toString().trim()
            if (text.isEmpty()) {
                return@setOnClickListener
            }

            val account = GoogleSignIn.getLastSignedInAccount(requireContext())
            val authorId = account?.id ?: "user_local_explorer"
            val authorName = account?.displayName ?: "Ceylon Traveler"
            val authorPhoto = account?.photoUrl?.toString() ?: ""

            val comment = PostComment(
                authorId = authorId,
                authorName = authorName,
                authorPhotoUrl = authorPhoto,
                commentText = text
            )

            etCommentInput.setText("")
            btnSendComment.isEnabled = false

            lifecycleScope.launch {
                try {
                    socialRepository.addComment(postId, comment)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Failed to send comment: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    btnSendComment.isEnabled = true
                }
            }
        }
    }

    private fun startRealtimeCommentsListener() {
        if (postId.isBlank()) return

        pbLoading.visibility = View.VISIBLE
        snapshotListener = socialRepository.getCommentsQuery(postId)
            .addSnapshotListener { snapshot, error ->
                pbLoading.visibility = View.GONE
                if (error != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val comments = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(PostComment::class.java)?.copy(commentId = doc.id)
                    }

                    commentsAdapter.submitList(comments) {
                        if (comments.isNotEmpty()) {
                            rvComments.smoothScrollToPosition(comments.size - 1)
                        }
                    }

                    tvTotalCommentsBadge.text = "${comments.size} Comments"
                    tvEmptyComments.visibility = if (comments.isEmpty()) View.VISIBLE else View.GONE
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        snapshotListener?.remove()
        snapshotListener = null
    }

    class CommentsAdapter : ListAdapter<PostComment, CommentsAdapter.CommentViewHolder>(CommentDiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_post_comment, parent, false)
            return CommentViewHolder(view)
        }

        override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val imgAvatar: ShapeableImageView = itemView.findViewById(R.id.img_commenter_avatar)
            private val tvName: TextView = itemView.findViewById(R.id.tv_commenter_name)
            private val tvTime: TextView = itemView.findViewById(R.id.tv_comment_time)
            private val tvBody: TextView = itemView.findViewById(R.id.tv_comment_body)

            fun bind(comment: PostComment) {
                tvName.text = comment.authorName.ifBlank { "Explorer" }
                tvBody.text = comment.commentText

                val timeStr = comment.timestamp?.let {
                    val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.US)
                    sdf.format(it)
                } ?: "Just now"
                tvTime.text = timeStr

                if (comment.authorPhotoUrl.isNotBlank()) {
                    imgAvatar.load(comment.authorPhotoUrl) {
                        crossfade(true)
                        transformations(CircleCropTransformation())
                        placeholder(R.drawable.ic_footsteps)
                        error(R.drawable.ic_footsteps)
                    }
                } else {
                    imgAvatar.setImageResource(R.drawable.ic_footsteps)
                }
            }
        }

        class CommentDiffCallback : DiffUtil.ItemCallback<PostComment>() {
            override fun areItemsTheSame(oldItem: PostComment, newItem: PostComment): Boolean {
                return oldItem.commentId == newItem.commentId
            }

            override fun areContentsTheSame(oldItem: PostComment, newItem: PostComment): Boolean {
                return oldItem == newItem
            }
        }
    }
}
