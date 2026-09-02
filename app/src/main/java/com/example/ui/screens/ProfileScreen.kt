package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Public
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.data.model.social.FollowerUser
import com.example.ui.components.FollowDialogTab
import com.example.ui.components.FollowersFollowingDialog
import com.example.ui.components.LikesAppreciationDialog
import com.example.ui.components.TripCard
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ceylonsteps.travelapp.data.model.TripWithStops
import com.example.data.model.SriLankaDestinations
import com.example.data.model.TripLocation
import com.example.data.model.UserProfile
import com.example.data.repository.AppThemeType
import com.example.data.repository.ThemeMode
import com.example.ui.components.RecycleBinDialog
import com.example.ui.theme.BentoAmberSecondary
import com.example.ui.theme.BentoGreenAccent
import com.example.ui.theme.BentoPrimary
import com.example.util.ExplorerRank
import com.example.util.ExplorerRankEngine
import com.example.util.GeoDistanceEngine
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.ceylonsteps.travelapp.auth.UserManager as AuthUserManager

enum class ProfileSubTab(val title: String, val icon: ImageVector) {
    FOOTPRINTS("Footprints", Icons.Default.Place),
    STORIES("My Stories", Icons.Default.Public),
    JOURNEYS("Journeys", Icons.Default.Timeline),
    CALENDAR("Calendar", Icons.Default.CalendarMonth),
    PROVINCES("Provinces", Icons.Default.Flag),
    METRICS("Metrics", Icons.Default.Speed),
    THEMES("Themes", Icons.Default.Palette),
    BACKUPS("Backups", Icons.Default.Backup),
    RECYCLE_BIN("Recycle Bin", Icons.Default.Delete)
}

enum class FootprintFilter {
    ALL, VISITED, UPCOMING
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    trips: List<TripLocation>,
    journeys: List<TripWithStops> = emptyList(),
    socialPosts: List<com.example.data.model.social.SocialPost> = emptyList(),
    onDeleteSocialPost: (String) -> Unit = {},
    onEditSocialPost: (com.example.data.model.social.SocialPost) -> Unit = {},
    onToggleLikeSocialPost: (String) -> Unit = {},
    totalDistanceKm: Double,
    roundTripFromHomeKm: Double,
    currentThemeMode: ThemeMode = ThemeMode.LIGHT,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    currentAppThemeType: AppThemeType = AppThemeType.DEFAULT,
    onAppThemeTypeChange: (AppThemeType) -> Unit = {},
    recycledTrips: List<TripLocation> = emptyList(),
    onRestoreTrip: (TripLocation) -> Unit = {},
    onPermanentlyDeleteTrip: (TripLocation) -> Unit = {},
    onEditProfileClick: () -> Unit,
    onBackupRestoreClick: () -> Unit = {},
    onGoogleSignInSuccess: ((GoogleSignInAccount) -> Unit)? = null,
    onUpdateAvatar: (String?) -> Unit = {},
    onUpdateCover: (String?) -> Unit = {},
    onLogout: () -> Unit = {},
    onTripClick: (TripLocation) -> Unit,
    onOpenMultiStopBuilder: () -> Unit = {},
    onSelectTripOnMap: (TripLocation) -> Unit = {},
    onEditTrip: (TripLocation) -> Unit = {},
    onDeleteTrip: (TripLocation) -> Unit = {},
    onCenterMap: (Double, Double) -> Unit = { _, _ -> },
    onOpenShareStoryDialog: () -> Unit = {},
    onOpenAddTrip: () -> Unit = {},
    followers: List<FollowerUser> = emptyList(),
    following: List<FollowerUser> = emptyList(),
    onToggleFollow: (String) -> Unit = {},
    onRemoveFollower: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var isRecycleBinOpen by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showFollowersDialog by remember { mutableStateOf(false) }
    var followersDialogInitialTab by remember { mutableStateOf(FollowDialogTab.FOLLOWERS) }
    var showLikesDialog by remember { mutableStateOf(false) }
    var postToEdit by remember { mutableStateOf<com.example.data.model.social.SocialPost?>(null) }
    var loggedInUser by remember { mutableStateOf(AuthUserManager.getLoggedInUser(context)) }
    val googleAccount = remember { GoogleSignIn.getLastSignedInAccount(context) }

    val myStories = remember(socialPosts, userProfile, googleAccount, loggedInUser) {
        val myIds = mutableSetOf<String>()
        if (userProfile.userEmail.isNotBlank()) myIds.add(userProfile.userEmail)
        if (userProfile.userName.isNotBlank()) myIds.add(userProfile.userName)
        googleAccount?.id?.let { if (it.isNotBlank()) myIds.add(it) }
        googleAccount?.email?.let { if (it.isNotBlank()) myIds.add(it) }
        loggedInUser?.email?.let { if (it.isNotBlank()) myIds.add(it) }
        loggedInUser?.googleId?.let { if (it.isNotBlank()) myIds.add(it) }
        myIds.add("user_local_explorer")

        val myNames = mutableSetOf<String>()
        if (userProfile.userName.isNotBlank()) myNames.add(userProfile.userName)
        googleAccount?.displayName?.let { if (it.isNotBlank()) myNames.add(it) }
        loggedInUser?.displayName?.let { if (it.isNotBlank()) myNames.add(it) }

        socialPosts.filter { post ->
            post.authorId in myIds ||
            myNames.any { it.equals(post.authorName, ignoreCase = true) } ||
            (userProfile.userEmail.isNotBlank() && post.authorId.contains(userProfile.userEmail, ignoreCase = true)) ||
            (userProfile.userName.isNotBlank() && post.authorId.contains(userProfile.userName, ignoreCase = true)) ||
            (loggedInUser?.email?.isNotBlank() == true && post.authorId.contains(loggedInUser!!.email, ignoreCase = true))
        }
    }

    val totalLikesEarned = remember(myStories) {
        myStories.sumOf { it.likeCount }
    }

    // Cover Photo Picker Launcher (Facebook Style)
    val coverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onUpdateCover(uri.toString())
            Toast.makeText(context, "Cover photo updated!", Toast.LENGTH_SHORT).show()
        }
    }

    // Avatar Photo Picker Launcher (Facebook Style)
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onUpdateAvatar(uri.toString())
            Toast.makeText(context, "Profile picture updated!", Toast.LENGTH_SHORT).show()
        }
    }

    // Google Sign-In Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                val appUser = AuthUserManager.autoRegisterFromGoogle(context, account)
                loggedInUser = appUser
                Toast.makeText(context, "Signed in as ${account.email}. Syncing backup from Google Drive...", Toast.LENGTH_SHORT).show()
                onGoogleSignInSuccess?.invoke(account)
            }
        } catch (e: ApiException) {
            Toast.makeText(context, "Google Sign-In failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
    }

    val pastTrips = remember(trips) {
        trips.filter { !it.isUpcoming }.sortedByDescending { it.dateEpochMillis }
    }
    val upcomingTrips = remember(trips) {
        trips.filter { it.isUpcoming }.sortedBy { it.dateEpochMillis }
    }

    val visitedProvinces = remember(pastTrips) {
        pastTrips.map { trip ->
            SriLankaDestinations.findMatchingProvince(trip.latitude, trip.longitude)
        }.distinct()
    }

    val explorationProgress = (visitedProvinces.size.toFloat() / 9f).coerceIn(0f, 1f)

    val furthestTripWithDistance = remember(pastTrips, userProfile) {
        pastTrips.map { trip ->
            Pair(trip, GeoDistanceEngine.calculateDistanceFromHomeKm(trip, userProfile))
        }.maxByOrNull { it.second }
    }

    val userRank = remember(pastTrips, visitedProvinces, journeys) {
        ExplorerRankEngine.calculateRank(
            visitedPlacesCount = pastTrips.size,
            uniqueProvincesCount = visitedProvinces.size,
            postsCount = 0,
            journeysCount = journeys.size
        )
    }

    val rankTitle = userRank.title

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("profile_screen_content"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ================= FACEBOOK-STYLE COVER PHOTO & PROFILE HEADER =================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_hero_card"),
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // COVER PHOTO CONTAINER (FB STYLE)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        if (userProfile.coverImageUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(userProfile.coverImageUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile Cover Photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { coverPickerLauncher.launch("image/*") },
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                BentoAmberSecondary,
                                                BentoGreenAccent
                                            )
                                        )
                                    )
                                    .clickable { coverPickerLauncher.launch("image/*") }
                            )
                        }

                        // Gradient protection overlay at bottom of cover
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                                    )
                                )
                        )

                        // Top Rank Pill
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Black.copy(alpha = 0.65f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                repeat(userRank.starCount) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = BentoAmberSecondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "Lv.${userRank.level} • ${userRank.title}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        // Camera Button to Update Cover Photo (Facebook Style)
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                                .clickable { coverPickerLauncher.launch("image/*") }
                                .testTag("btn_upload_cover_photo"),
                            shape = RoundedCornerShape(18.dp),
                            color = Color.Black.copy(alpha = 0.65f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Edit Cover",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Edit Cover",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    // PROFILE AVATAR (OVERLAPPING FB STYLE) & USER DETAILS
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // FB Overlapping Avatar
                            Box(
                                modifier = Modifier
                                    .offset(y = (-45).dp)
                                    .size(105.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .clickable { avatarPickerLauncher.launch("image/*") }
                                    .testTag("img_profile_avatar"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (userProfile.profileImageUri != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(userProfile.profileImageUri)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Profile Photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    val initials = userProfile.userName.split(" ")
                                        .filter { it.isNotBlank() }
                                        .take(2)
                                        .map { it.first().uppercaseChar() }
                                        .joinToString("")
                                        .ifBlank { "LF" }
                                    Text(
                                        text = initials,
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }

                                // Small Camera Badge on Avatar
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Change Avatar",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }

                            // Facebook-Style Action Buttons: Edit Profile & Logout
                            Row(
                                modifier = Modifier
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onEditProfileClick,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.testTag("btn_edit_profile_main")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Edit Profile", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = { showLogoutDialog = true },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    ),
                                    modifier = Modifier.testTag("btn_logout_fb_style")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Logout,
                                        contentDescription = "Log Out",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Log Out", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        // User Info
                        Column(
                            modifier = Modifier
                                .offset(y = (-30).dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = userProfile.userName,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.3).sp
                                )
                            )

                            if (userProfile.bio.isNotBlank()) {
                                Text(
                                    text = userProfile.bio,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = rankTitle.uppercase(),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = 0.8.sp
                                        )
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = userProfile.homeLocationName,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }

                            // ================= TIKTOK-STYLE INTERACTIVE STATS BAR =================
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .testTag("tiktok_profile_stats_bar")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Following (Clickable)
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clickable {
                                                followersDialogInitialTab = FollowDialogTab.FOLLOWING
                                                showFollowersDialog = true
                                            }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                            .testTag("btn_tiktok_following_count")
                                    ) {
                                        Text(
                                            text = "${following.size}",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = (-0.5).sp
                                            )
                                        )
                                        Text(
                                            text = "Following",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }

                                    // Divider
                                    Box(
                                        modifier = Modifier
                                            .height(24.dp)
                                            .width(1.dp)
                                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    )

                                    // Followers (Clickable - TikTok style highlight)
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clickable {
                                                followersDialogInitialTab = FollowDialogTab.FOLLOWERS
                                                showFollowersDialog = true
                                            }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                            .testTag("btn_tiktok_followers_count")
                                    ) {
                                        Text(
                                            text = "${followers.size}",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = (-0.5).sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                        Text(
                                            text = "Followers",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }

                                    // Divider
                                    Box(
                                        modifier = Modifier
                                            .height(24.dp)
                                            .width(1.dp)
                                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    )

                                    // Likes (Clickable)
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clickable { showLikesDialog = true }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                            .testTag("btn_tiktok_likes_count")
                                    ) {
                                        Text(
                                            text = "$totalLikesEarned",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = (-0.5).sp,
                                                color = androidx.compose.ui.graphics.Color(0xFFFF4757)
                                            )
                                        )
                                        Text(
                                            text = "Likes",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }

                                    // Divider
                                    Box(
                                        modifier = Modifier
                                            .height(24.dp)
                                            .width(1.dp)
                                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    )

                                    // Stories (Clickable - jump to stories tab)
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clickable { selectedTabIndex = ProfileSubTab.STORIES.ordinal }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                            .testTag("btn_tiktok_stories_count")
                                    ) {
                                        Text(
                                            text = "${myStories.size}",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = (-0.5).sp
                                            )
                                        )
                                        Text(
                                            text = "Stories",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ================= EXPLORER RANK & STAR LEVEL SHOWCASE CARD =================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("card_explorer_rank_showcase"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                border = BorderStroke(1.dp, BentoAmberSecondary.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = BentoAmberSecondary.copy(alpha = 0.2f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Rank Stars",
                                        tint = BentoAmberSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "EXPLORER RANK • LEVEL ${userRank.level}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = BentoAmberSecondary,
                                        letterSpacing = 1.sp
                                    )
                                )
                                Text(
                                    text = userRank.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        // Star ratings (1-5 stars)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(5) { index ->
                                val isFilled = index < userRank.starCount
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (isFilled) BentoAmberSecondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // XP Progress bar
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Rank Progress",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Text(
                                text = "${userRank.currentXp} / ${userRank.nextLevelXp} XP (${(userRank.progress * 100).toInt()}%)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        LinearProgressIndicator(
                            progress = { userRank.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = BentoAmberSecondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    Text(
                        text = userRank.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    )

                    // Stats Quick Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTabIndex = ProfileSubTab.FOOTPRINTS.ordinal }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${trips.size}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Footprints",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTabIndex = ProfileSubTab.PROVINCES.ordinal }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${visitedProvinces.size}/9",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Provinces",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTabIndex = ProfileSubTab.JOURNEYS.ordinal }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${journeys.size}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Journeys",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // ================= FACEBOOK-STYLE VERTICAL / HORIZONTAL CATEGORY TABS =================
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp)
                    .testTag("profile_category_tabs")
            ) {
                ProfileSubTab.values().forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = tab.title,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        modifier = Modifier.testTag("profile_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }

        // ================= DYNAMIC TAB CONTENT =================
        when (ProfileSubTab.values()[selectedTabIndex]) {
            ProfileSubTab.FOOTPRINTS -> {
                item {
                    FootprintsTabSection(
                        userProfile = userProfile,
                        trips = trips,
                        pastTrips = pastTrips,
                        upcomingTrips = upcomingTrips,
                        onTripClick = onTripClick,
                        onSelectTripOnMap = onSelectTripOnMap,
                        onEditTrip = onEditTrip,
                        onDeleteTrip = onDeleteTrip,
                        onOpenAddTrip = onOpenAddTrip
                    )
                }
            }

            ProfileSubTab.STORIES -> {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "My Stories (${myStories.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Button(
                            onClick = onOpenShareStoryDialog,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share Story", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (myStories.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Explore,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "No stories shared yet",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Share your Sri Lanka travel adventures, memories, and photos with the community!",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onOpenShareStoryDialog,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Post Your First Story 🇱🇰", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    items(myStories) { post ->
                        com.example.ui.components.SocialPostCard(
                            post = post,
                            onLikeToggle = { onToggleLikeSocialPost(post.postId) },
                            onFollowToggle = {},
                            onOpenComments = {},
                            onViewOnMap = { onCenterMap(post.latitude, post.longitude) },
                            onSaveToMyTrips = {},
                            onAuthorClick = {},
                            onMediaClick = { _, _ -> },
                            onEdit = { postToEdit = post },
                            onDelete = { onDeleteSocialPost(post.postId) }
                        )
                    }
                }
            }

            ProfileSubTab.THEMES -> {
                item {
                    ThemeTabSection(
                        currentThemeMode = currentThemeMode,
                        onThemeModeChange = onThemeModeChange,
                        currentAppThemeType = currentAppThemeType,
                        onAppThemeTypeChange = onAppThemeTypeChange
                    )
                }
            }

            ProfileSubTab.JOURNEYS -> {
                item {
                    JourneysTabSection(
                        journeys = journeys,
                        onOpenMultiStopBuilder = onOpenMultiStopBuilder
                    )
                }
            }

            ProfileSubTab.CALENDAR -> {
                item {
                    CalendarTabSection(
                        pastTrips = pastTrips,
                        upcomingTrips = upcomingTrips,
                        onTripClick = onTripClick
                    )
                }
            }

            ProfileSubTab.PROVINCES -> {
                item {
                    ProvinceExplorerTabSection(
                        visitedProvinces = visitedProvinces,
                        explorationProgress = explorationProgress
                    )
                }
            }

            ProfileSubTab.METRICS -> {
                item {
                    ExpeditionMetricsTabSection(
                        totalDistanceKm = totalDistanceKm,
                        roundTripFromHomeKm = roundTripFromHomeKm,
                        pastTrips = pastTrips,
                        upcomingTrips = upcomingTrips,
                        furthestTripWithDistance = furthestTripWithDistance
                    )
                }
            }

            ProfileSubTab.BACKUPS -> {
                item {
                    BackupsTabSection(
                        loggedInUser = loggedInUser,
                        onGoogleSignInClick = {
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestEmail()
                                .requestScopes(
                                    Scope(DriveScopes.DRIVE_APPDATA),
                                    Scope(DriveScopes.DRIVE_FILE)
                                )
                                .build()
                            val googleSignInClient = GoogleSignIn.getClient(context, gso)
                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                        },
                        onGoogleSignOutClick = {
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                            GoogleSignIn.getClient(context, gso).signOut().addOnCompleteListener {
                                AuthUserManager.signOut(context)
                                loggedInUser = null
                                Toast.makeText(context, "Google session disconnected", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onBackupRestoreClick = onBackupRestoreClick
                    )
                }
            }

            ProfileSubTab.RECYCLE_BIN -> {
                item {
                    RecycleBinTabSection(
                        recycledTrips = recycledTrips,
                        onRestoreTrip = onRestoreTrip,
                        onPermanentlyDeleteTrip = onPermanentlyDeleteTrip
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(90.dp))
        }
    }

    // Facebook-Style Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Log Out of CeylonSteps?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out? You can log back in anytime.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Log Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (isRecycleBinOpen) {
        RecycleBinDialog(
            recycledTrips = recycledTrips,
            onRestoreTrip = onRestoreTrip,
            onPermanentlyDeleteTrip = onPermanentlyDeleteTrip,
            onDismiss = { isRecycleBinOpen = false }
        )
    }

    postToEdit?.let { post ->
        com.example.ui.components.EditSocialPostDialog(
            post = post,
            onDismiss = { postToEdit = null },
            onSave = { updatedPost ->
                onEditSocialPost(updatedPost)
                postToEdit = null
            }
        )
    }

    // ================= TIKTOK-STYLE FOLLOWERS & FOLLOWING DIALOG =================
    if (showFollowersDialog) {
        FollowersFollowingDialog(
            initialTab = followersDialogInitialTab,
            followers = followers,
            following = following,
            onToggleFollow = onToggleFollow,
            onRemoveFollower = onRemoveFollower,
            onDismiss = { showFollowersDialog = false }
        )
    }

    // ================= LIKES APPRECIATION DIALOG =================
    if (showLikesDialog) {
        LikesAppreciationDialog(
            totalLikes = totalLikesEarned,
            sharedStoriesCount = myStories.size,
            onDismiss = { showLikesDialog = false }
        )
    }
}

// ================= TAB: FOOTPRINTS (MY MARKED LOCATIONS & ADVENTURES) =================
@Composable
fun FootprintsTabSection(
    userProfile: UserProfile,
    trips: List<TripLocation>,
    pastTrips: List<TripLocation>,
    upcomingTrips: List<TripLocation>,
    onTripClick: (TripLocation) -> Unit,
    onSelectTripOnMap: (TripLocation) -> Unit,
    onEditTrip: (TripLocation) -> Unit,
    onDeleteTrip: (TripLocation) -> Unit,
    onOpenAddTrip: () -> Unit
) {
    var footprintFilter by remember { mutableStateOf(FootprintFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    val displayedTrips = remember(trips, pastTrips, upcomingTrips, footprintFilter, searchQuery) {
        val baseList = when (footprintFilter) {
            FootprintFilter.ALL -> trips
            FootprintFilter.VISITED -> pastTrips
            FootprintFilter.UPCOMING -> upcomingTrips
        }
        if (searchQuery.isBlank()) {
            baseList
        } else {
            baseList.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.locationName.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true) ||
                SriLankaDestinations.findMatchingProvince(it.latitude, it.longitude).contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "My Footprints (${trips.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${pastTrips.size} Visited • ${upcomingTrips.size} Planned",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Button(
                onClick = onOpenAddTrip,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("btn_profile_add_footprint")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Pin", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search footprints or provinces...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_footprint_search_input")
        )

        // Filter chips (All, Visited, Planned)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = footprintFilter == FootprintFilter.ALL,
                onClick = { footprintFilter = FootprintFilter.ALL },
                label = { Text("All (${trips.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.testTag("filter_chip_all")
            )
            FilterChip(
                selected = footprintFilter == FootprintFilter.VISITED,
                onClick = { footprintFilter = FootprintFilter.VISITED },
                label = { Text("Visited (${pastTrips.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.testTag("filter_chip_visited")
            )
            FilterChip(
                selected = footprintFilter == FootprintFilter.UPCOMING,
                onClick = { footprintFilter = FootprintFilter.UPCOMING },
                label = { Text("Planned (${upcomingTrips.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.testTag("filter_chip_planned")
            )
        }

        if (displayedTrips.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (trips.isEmpty()) "No footprints added yet" else "No matching footprints found",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (trips.isEmpty())
                            "Pin the places, attractions, beaches, waterfalls, and cultural wonders you've visited in Sri Lanka!"
                        else
                            "Try clearing your search keyword or switching between Visited and Planned filters.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    if (trips.isEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onOpenAddTrip,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mark Your First Footprint 🇱🇰", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                displayedTrips.forEachIndexed { index, trip ->
                    val distFromHome = GeoDistanceEngine.calculateDistanceFromHomeKm(trip, userProfile)
                    TripCard(
                        trip = trip,
                        index = index + 1,
                        distanceFromHomeKm = distFromHome,
                        onClick = { onTripClick(trip) },
                        onMapClick = { onSelectTripOnMap(trip) },
                        onEditClick = { onEditTrip(trip) },
                        onDeleteClick = { onDeleteTrip(trip) }
                    )
                }
            }
        }
    }
}

// ================= TAB 1: THEMES & DISPLAY =================
@Composable
fun ThemeTabSection(
    currentThemeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    currentAppThemeType: AppThemeType,
    onAppThemeTypeChange: (AppThemeType) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("theme_selector_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Brightness4,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Theme & Display Mode",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = currentThemeMode == ThemeMode.SYSTEM,
                    onClick = { onThemeModeChange(ThemeMode.SYSTEM) },
                    label = { Text("System", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.BrightnessMedium, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    modifier = Modifier.weight(1f).testTag("theme_chip_system"),
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )

                FilterChip(
                    selected = currentThemeMode == ThemeMode.LIGHT,
                    onClick = { onThemeModeChange(ThemeMode.LIGHT) },
                    label = { Text("Light", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Brightness7, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    modifier = Modifier.weight(1f).testTag("theme_chip_light"),
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )

                FilterChip(
                    selected = currentThemeMode == ThemeMode.DARK,
                    onClick = { onThemeModeChange(ThemeMode.DARK) },
                    label = { Text("Dark", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Brightness4, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    modifier = Modifier.weight(1f).testTag("theme_chip_dark"),
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text("Color Palettes (Sri Lanka Aesthetics)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = currentAppThemeType == AppThemeType.DEFAULT,
                    onClick = { onAppThemeTypeChange(AppThemeType.DEFAULT) },
                    label = { Text("Sigiriya Sunset (Default)", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary)
                )
                FilterChip(
                    selected = currentAppThemeType == AppThemeType.OCEAN,
                    onClick = { onAppThemeTypeChange(AppThemeType.OCEAN) },
                    label = { Text("Mirissa Ocean", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary)
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = currentAppThemeType == AppThemeType.FOREST,
                    onClick = { onAppThemeTypeChange(AppThemeType.FOREST) },
                    label = { Text("Sinharaja Forest", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary)
                )
                FilterChip(
                    selected = currentAppThemeType == AppThemeType.SUNSET,
                    onClick = { onAppThemeTypeChange(AppThemeType.SUNSET) },
                    label = { Text("Galle Fort Sunset", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary)
                )
            }
        }
    }
}

// ================= TAB 2: JOURNEYS & MULTI-STOP EXPEDITIONS =================
@Composable
fun JourneysTabSection(
    journeys: List<TripWithStops>,
    onOpenMultiStopBuilder: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MULTI-STOP EXPEDITIONS & JOURNEYS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = "${journeys.size} custom routes created",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            Button(
                onClick = onOpenMultiStopBuilder,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BentoPrimary,
                    contentColor = Color.White
                )
            ) {
                Icon(imageVector = Icons.Default.Route, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Journey", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        if (journeys.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Route,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "No Multi-Stop Journeys Yet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Connect multiple stops (e.g. Colombo ➔ Kandy ➔ Ella) into single organized expeditions.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else {
            journeys.forEach { journeyWithStops ->
                val journey = journeyWithStops.trip
                val stops = journeyWithStops.stops.sortedBy { it.stopOrder }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = journey.tripTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BentoAmberSecondary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${stops.size} Stops • ${journey.totalDistanceKm} km",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = BentoAmberSecondary
                                    )
                                )
                            }
                        }

                        Text(
                            text = "From: ${journey.originName} @ ${journey.departureTime}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        // Stop sequence pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val displayStops = stops.take(4)
                            displayStops.forEachIndexed { idx, stop ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "${idx + 1}. ${stop.stopName.take(10)}",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                                    )
                                }
                                if (idx < displayStops.size - 1) {
                                    Text("➔", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================= TAB 3: CALENDAR & TIMELINE =================
@Composable
fun CalendarTabSection(
    pastTrips: List<TripLocation>,
    upcomingTrips: List<TripLocation>,
    onTripClick: (TripLocation) -> Unit
) {
    val dayFormat = remember { SimpleDateFormat("EEE, MMM dd", Locale.US) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "TRIP CALENDAR & TIMELINES",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.primary
            )
        )

        // Upcoming Calendar Events
        if (upcomingTrips.isNotEmpty()) {
            Text(
                text = "Upcoming Expeditions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            upcomingTrips.forEach { trip ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTripClick(trip) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = BentoAmberSecondary,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = trip.title,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${trip.locationName} • ${dayFormat.format(Date(trip.dateEpochMillis))}",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
            }
        }

        // Past Logged Calendar Timeline
        Text(
            text = "Completed Footprints History",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        if (pastTrips.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "No past trips logged yet. Mark trips as visited or log them from Map tab.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            pastTrips.forEach { trip ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTripClick(trip) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = BentoGreenAccent.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = BentoGreenAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = trip.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${trip.locationName} • ${dayFormat.format(Date(trip.dateEpochMillis))}",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ================= TAB 4: PROVINCE EXPLORER =================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProvinceExplorerTabSection(
    visitedProvinces: List<String>,
    explorationProgress: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("provinces_progress_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "9 Provinces of Sri Lanka",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${visitedProvinces.size} / 9 (${(explorationProgress * 100).toInt()}%)",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { explorationProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SriLankaDestinations.PROVINCES.forEach { province ->
                        val isVisited = visitedProvinces.contains(province)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isVisited) BentoGreenAccent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                if (isVisited) BentoGreenAccent else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isVisited) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = BentoGreenAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = province,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isVisited) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isVisited) BentoGreenAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================= TAB 5: EXPEDITION TRACKING METRICS =================
@Composable
fun ExpeditionMetricsTabSection(
    totalDistanceKm: Double,
    roundTripFromHomeKm: Double,
    pastTrips: List<TripLocation>,
    upcomingTrips: List<TripLocation>,
    furthestTripWithDistance: Pair<TripLocation, Double>?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "EXPEDITION TRACKING METRICS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.primary
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("card_total_traveled_stat"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(
                        imageVector = Icons.Default.Route,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "${String.format(Locale.US, "%,.0f", totalDistanceKm)} km",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "Total Traveled",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("card_roundtrip_stat"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = BentoAmberSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "${String.format(Locale.US, "%,.0f", roundTripFromHomeKm)} km",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "Home Base Loop",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("card_logged_places_stat"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = BentoGreenAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "${pastTrips.size} Visited",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "${upcomingTrips.size} in Wishlist",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("card_furthest_stat"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (furthestTripWithDistance != null) "${String.format(Locale.US, "%,.0f", furthestTripWithDistance.second)} km" else "0 km",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = if (furthestTripWithDistance != null) "Furthest: ${furthestTripWithDistance.first.title.take(10)}..." else "Furthest Reach",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }
    }
}

// ================= TAB 6: BACKUPS & GOOGLE DRIVE SYNC =================
@Composable
fun BackupsTabSection(
    loggedInUser: com.ceylonsteps.travelapp.auth.AppUser?,
    onGoogleSignInClick: () -> Unit,
    onGoogleSignOutClick: () -> Unit,
    onBackupRestoreClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "CLOUD BACKUPS & EXPORT",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.primary
            )
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Backup, contentDescription = null, tint = BentoPrimary)
                    Text(
                        text = "Google Drive Auto-Sync",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                if (loggedInUser == null) {
                    Text(
                        text = "Connect your Google account to automatically sync and restore footprints to your private Google Drive app storage.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Button(
                        onClick = onGoogleSignInClick,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                    ) {
                        Text(text = "Connect Google Account 🔑", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Connected Account:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = loggedInUser.email, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        OutlinedButton(
                            onClick = onGoogleSignOutClick,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Disconnect", fontSize = 11.sp)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Button(
                    onClick = onBackupRestoreClick,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "JSON Backup & Restore Hub", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

// ================= TAB 7: RECYCLE BIN =================
@Composable
fun RecycleBinTabSection(
    recycledTrips: List<TripLocation>,
    onRestoreTrip: (TripLocation) -> Unit,
    onPermanentlyDeleteTrip: (TripLocation) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TRIP RECYCLE BIN (30 DAYS RETENTION)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.error
                )
            )
            Text(
                text = "${recycledTrips.size} items",
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        if (recycledTrips.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "Recycle Bin is Empty",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Deleted footprint logs stay in the recycle bin for 30 days before permanent removal.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        } else {
            recycledTrips.forEach { trip ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = trip.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = trip.locationName,
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { onRestoreTrip(trip) }) {
                                Icon(
                                    imageVector = Icons.Default.Restore,
                                    contentDescription = "Restore",
                                    tint = BentoGreenAccent
                                )
                            }
                            IconButton(onClick = { onPermanentlyDeleteTrip(trip) }) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteForever,
                                    contentDescription = "Delete Forever",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
