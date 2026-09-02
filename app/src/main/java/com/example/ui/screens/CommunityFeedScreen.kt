package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.social.CommunityFeedFilter
import com.example.data.model.social.SocialComment
import com.example.data.model.social.SocialPost
import com.example.ui.components.PublicUserProfileDialog
import com.example.ui.components.SocialCommentsSheet
import com.example.ui.components.SocialPostCard
import com.example.ui.theme.BentoAmberSecondary
import com.example.ui.theme.BentoBorderLight
import com.example.ui.theme.BentoCyanAccent
import com.example.ui.theme.BentoGreenAccent
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryContainer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityFeedScreen(
    posts: List<SocialPost>,
    commentsMap: Map<String, List<SocialComment>>,
    currentAuthorId: String,
    selectedProvince: String?,
    onLikeToggle: (String) -> Unit,
    onFollowToggle: (String) -> Unit,
    onAddComment: (String, String) -> Unit,
    onListenToComments: (String) -> Unit,
    onViewOnMap: (Double, Double, String) -> Unit,
    onSaveTripToLocal: (SocialPost) -> Unit,
    onOpenShareDialog: () -> Unit,
    onOpenMediaViewer: (List<String>, Int) -> Unit,
    onDeletePost: (String) -> Unit = {},
    onEditPost: (SocialPost) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var activeFilter by remember { mutableStateOf(CommunityFeedFilter.ALL) }
    var selectedPostForComments by remember { mutableStateOf<SocialPost?>(null) }
    var selectedAuthorForProfile by remember { mutableStateOf<SocialPost?>(null) }
    var postToEdit by remember { mutableStateOf<SocialPost?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Filter posts
    val filteredPosts = remember(posts, activeFilter, selectedProvince) {
        posts.filter { post ->
            val matchPrivacy = post.isPublic
            val matchProvince = selectedProvince == null || post.province.equals(selectedProvince, ignoreCase = true)
            val matchFilter = when (activeFilter) {
                CommunityFeedFilter.ALL -> true
                CommunityFeedFilter.TRENDING -> post.likeCount >= 10
                CommunityFeedFilter.FOLLOWING -> post.isFollowingAuthor
                CommunityFeedFilter.MULTI_STOP -> post.isMultiStop
                CommunityFeedFilter.HERITAGE -> post.category.equals("HERITAGE", ignoreCase = true) || post.tags.any { it.contains("heritage", ignoreCase = true) || it.contains("ancient", ignoreCase = true) }
                CommunityFeedFilter.NATURE -> post.category.equals("WILDLIFE", ignoreCase = true) || post.category.equals("SCENIC", ignoreCase = true) || post.tags.any { it.contains("nature", ignoreCase = true) || it.contains("wildlife", ignoreCase = true) }
                CommunityFeedFilter.BEACHES -> post.category.equals("BEACH", ignoreCase = true) || post.tags.any { it.contains("beach", ignoreCase = true) || it.contains("coast", ignoreCase = true) }
            }
            matchPrivacy && matchProvince && matchFilter
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("community_feed_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. HERO BENTO COMMUNITY HEADER CARD
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, BentoBorderLight.copy(alpha = 0.6f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        BentoPrimary.copy(alpha = 0.12f),
                                        BentoAmberSecondary.copy(alpha = 0.08f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = BentoPrimary,
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Diversity3,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Ceylon Community Feed",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        Text(
                                            text = "Real-time travel stories & curated trails",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = BentoGreenAccent.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "⚡ Live Feed",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF2E7D32),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Stats Counter Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "${posts.size}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = BentoPrimary
                                            )
                                        )
                                        Text(
                                            text = "Stories Shared",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        val totalLikes = posts.sumOf { it.likeCount }
                                        Text(
                                            text = "$totalLikes",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = BentoAmberSecondary
                                            )
                                        )
                                        Text(
                                            text = "Hearts Given",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "\$0 Free",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = BentoGreenAccent
                                            )
                                        )
                                        Text(
                                            text = "Drive CDN",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Share Action Button
                            Button(
                                onClick = onOpenShareDialog,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BentoPrimary,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Share My Ceylon Adventure",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // 2. FILTER CHIPS ROW
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(CommunityFeedFilter.values()) { filter ->
                        val isSelected = activeFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { activeFilter = filter },
                            label = {
                                Text(
                                    text = filter.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BentoPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) BentoPrimary else BentoBorderLight
                            )
                        )
                    }
                }
            }

            // Province filter indicator
            if (selectedProvince != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BentoAmberSecondary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, BentoAmberSecondary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = BentoAmberSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Filtered by $selectedProvince Province",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BentoAmberSecondary
                                )
                            )
                        }
                    }
                }
            }

            // 3. POSTS FEED LIST
            if (filteredPosts.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, BentoBorderLight.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "🏝️ No stories published yet",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Be the first explorer to share an adventure with the CeylonSteps community! Use the 'Share My Ceylon Adventure' button above.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredPosts, key = { it.postId }) { post ->
                    SocialPostCard(
                        post = post,
                        onLikeToggle = { onLikeToggle(post.postId) },
                        onFollowToggle = { onFollowToggle(post.authorId) },
                        onOpenComments = {
                            selectedPostForComments = post
                            onListenToComments(post.postId)
                        },
                        onViewOnMap = {
                            onViewOnMap(post.latitude, post.longitude, post.title)
                        },
                        onSaveToMyTrips = {
                            onSaveTripToLocal(post)
                            Toast.makeText(
                                context,
                                "Saved \"${post.title}\" to your local CeylonSteps Journal!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onAuthorClick = {
                            selectedAuthorForProfile = post
                        },
                        onMediaClick = { mediaList, index ->
                            onOpenMediaViewer(mediaList, index)
                        },
                        onEdit = if (post.authorId == currentAuthorId) { { postToEdit = post } } else null,
                        onDelete = if (post.authorId == currentAuthorId) { { onDeletePost(post.postId) } } else null
                    )
                }
            }
        }
    }

    // Comments Sheet
    selectedPostForComments?.let { post ->
        val comments = commentsMap[post.postId] ?: emptyList()
        SocialCommentsSheet(
            post = post,
            comments = comments,
            sheetState = sheetState,
            onDismiss = { selectedPostForComments = null },
            onSendComment = { text ->
                onAddComment(post.postId, text)
            }
        )
    }

    // Author Profile Dialog
    selectedAuthorForProfile?.let { post ->
        val authorPosts = posts.filter { it.authorId == post.authorId && it.isPublic }
        PublicUserProfileDialog(
            authorId = post.authorId,
            authorName = post.authorName,
            authorAvatarUrl = post.authorAvatarUrl,
            authorBadge = post.authorBadge,
            authorRankLevel = post.authorRankLevel,
            isFollowing = post.isFollowingAuthor,
            authorPosts = authorPosts,
            onFollowToggle = { onFollowToggle(post.authorId) },
            onDismiss = { selectedAuthorForProfile = null },
            onSelectPost = { targetPost ->
                onViewOnMap(targetPost.latitude, targetPost.longitude, targetPost.title)
            }
        )
    }

    // Edit Post Dialog
    postToEdit?.let { post ->
        com.example.ui.components.EditSocialPostDialog(
            post = post,
            onDismiss = { postToEdit = null },
            onSave = { updatedPost ->
                onEditPost(updatedPost)
                postToEdit = null
            }
        )
    }
}
