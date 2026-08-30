package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.SriLankaDestinations
import com.example.data.model.TripLocation
import com.example.data.model.UserProfile
import com.example.data.repository.TripRepository
import com.example.ui.theme.BentoAmberSecondary
import com.example.ui.theme.BentoBorderLight
import com.example.ui.theme.BentoLavenderContainer
import com.example.ui.theme.BentoMintAccent
import com.example.ui.theme.BentoOnPrimaryContainer
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoRoseContainer
import com.example.ui.theme.UpcomingBadgeColor
import com.example.ui.theme.VisitedBadgeColor
import com.example.util.GeoDistanceEngine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TripDetailBottomSheet(
    trip: TripLocation,
    userProfile: UserProfile = UserProfile(),
    onDismiss: () -> Unit,
    onFocusMap: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleStatus: () -> Unit,
    onOpenMediaViewer: (mediaList: List<String>, initialIndex: Int) -> Unit = { _, _ -> }
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val photos = remember(trip.imageUrisJson) { TripRepository.parseJsonArray(trip.imageUrisJson) }
    var selectedPhotoUrl by remember(photos, trip.coverImageUri) {
        mutableStateOf(trip.coverImageUri ?: photos.firstOrNull())
    }

    val province = SriLankaDestinations.findMatchingProvince(trip.latitude, trip.longitude)
    val sinhalaProvince = SriLankaDestinations.PROVINCE_SINHALA[province] ?: ""
    val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US)
    val dateString = dateFormat.format(Date(trip.dateEpochMillis))
    val distanceFromHomeKm = GeoDistanceEngine.calculateDistanceFromHomeKm(trip, userProfile)
    val formattedHomeDist = GeoDistanceEngine.formatDistanceFromHome(distanceFromHomeKm)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null,
        modifier = Modifier.testTag("trip_detail_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Main Hero Image Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        if (photos.isNotEmpty()) {
                            val idx = photos.indexOf(selectedPhotoUrl).coerceAtLeast(0)
                            onOpenMediaViewer(photos, idx)
                        } else if (!selectedPhotoUrl.isNullOrBlank()) {
                            onOpenMediaViewer(listOf(selectedPhotoUrl!!), 0)
                        }
                    }
            ) {
                if (!selectedPhotoUrl.isNullOrBlank()) {
                    val isCurrentVideo = isVideoMedia(selectedPhotoUrl!!)
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(selectedPhotoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = trip.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    if (isCurrentVideo) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = BentoPrimary.copy(alpha = 0.9f),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play Video",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(BentoPrimary, BentoAmberSecondary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(72.dp)
                        )
                    }
                }

                // Top gradient scrim
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.65f), Color.Transparent)
                            )
                        )
                )

                // Top Controls (Status Badge, Share, Edit, Close)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (trip.isUpcoming) UpcomingBadgeColor else VisitedBadgeColor
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (trip.isUpcoming) Icons.Default.Schedule else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (trip.isUpcoming) "Upcoming Plan" else "Visited Location",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick Edit Icon
                        IconButton(
                            onClick = {
                                onEdit()
                                onDismiss()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Share Button
                        IconButton(
                            onClick = {
                                val shareText = buildString {
                                    append("🇱🇰 LankaFootprints Travel Memory:\n")
                                    append("📍 ${trip.title} - ${trip.locationName}\n")
                                    append("📅 $dateString\n")
                                    append("🗺️ Coordinates: ${trip.latitude}, ${trip.longitude}\n\n")
                                    if (trip.description.isNotBlank()) {
                                        append("Notes:\n${trip.description}\n\n")
                                    }
                                    append("Tracked with LankaFootprints (Sri Lanka Travel Journal)")
                                }
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Footprint"))
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Close Button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Fullscreen indicator button on hero image if photos available
                if (photos.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "View Full Screen",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Full Screen",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.White)
                            )
                        }
                    }
                }
            }

            // Thumbnail Strip if multiple photos
            if (photos.size > 1) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(photos) { idx, photoUrl ->
                        val isSelected = selectedPhotoUrl == photoUrl
                        val isVid = isVideoMedia(photoUrl)
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    BorderStroke(
                                        if (isSelected) 2.5.dp else 1.dp,
                                        if (isSelected) BentoPrimary else BentoBorderLight
                                    ),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    selectedPhotoUrl = photoUrl
                                }
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(photoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Gallery Thumbnail",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            if (isVid) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.4f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Video",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Content Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title & Location
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = trip.title.ifBlank { trip.locationName },
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.4).sp
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = BentoPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = trip.locationName,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                // Date, Province (English & Sinhala), Distance Badges
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Date Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BentoLavenderContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = BentoPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = dateString,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = BentoOnPrimaryContainer
                                )
                            )
                        }
                    }

                    // Province Badge with Sinhala Name
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BentoRoseContainer.copy(alpha = 0.55f)
                    ) {
                        Text(
                            text = if (sinhalaProvince.isNotBlank()) "$province Province ($sinhalaProvince)" else "$province Province",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF672338)
                            )
                        )
                    }

                    // Distance from Home Base Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BentoMintAccent.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, BentoMintAccent.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = BentoMintAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = formattedHomeDist,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BentoMintAccent
                                )
                            )
                        }
                    }
                }

                // Dedicated Media Gallery Grid Section (Photos & Videos)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    border = BorderStroke(1.dp, BentoBorderLight.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BentoPrimary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = null,
                                        tint = BentoPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Trip Media Gallery",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${photos.size} Photos & Videos attached",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }

                            // Edit Media Button
                            OutlinedButton(
                                onClick = {
                                    onEdit()
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, BentoPrimary.copy(alpha = 0.6f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoPrimary)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Media", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (photos.isNotEmpty()) {
                            // 2-Column Grid Layout for Attached Photos & Videos
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val chunkedMedia = photos.chunked(2)
                                chunkedMedia.forEachIndexed { rowIdx, rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowItems.forEachIndexed { colIdx, mediaUrl ->
                                            val overallIndex = rowIdx * 2 + colIdx
                                            val isVid = isVideoMedia(mediaUrl)
                                            val isCover = trip.coverImageUri == mediaUrl || (trip.coverImageUri == null && overallIndex == 0)

                                            Card(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1.25f)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .clickable {
                                                        onOpenMediaViewer(photos, overallIndex)
                                                    },
                                                shape = RoundedCornerShape(16.dp),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                                border = BorderStroke(1.dp, BentoBorderLight)
                                            ) {
                                                Box(modifier = Modifier.fillMaxSize()) {
                                                    AsyncImage(
                                                        model = ImageRequest.Builder(context)
                                                            .data(mediaUrl)
                                                            .crossfade(true)
                                                            .build(),
                                                        contentDescription = "Media item $overallIndex",
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )

                                                    // Video Indicator Overlay
                                                    if (isVid) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .background(Color.Black.copy(alpha = 0.35f)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Surface(
                                                                shape = CircleShape,
                                                                color = BentoPrimary.copy(alpha = 0.85f),
                                                                modifier = Modifier.size(36.dp)
                                                            ) {
                                                                Box(contentAlignment = Alignment.Center) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.PlayArrow,
                                                                        contentDescription = "Play Video",
                                                                        tint = Color.White,
                                                                        modifier = Modifier.size(20.dp)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }

                                                    // Cover badge
                                                    if (isCover) {
                                                        Surface(
                                                            modifier = Modifier
                                                                .align(Alignment.TopStart)
                                                                .padding(6.dp),
                                                            shape = RoundedCornerShape(8.dp),
                                                            color = BentoAmberSecondary
                                                        ) {
                                                            Text(
                                                                text = "Cover",
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                                style = MaterialTheme.typography.labelSmall.copy(
                                                                    fontSize = 9.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color.Black
                                                                )
                                                            )
                                                        }
                                                    }

                                                    // Tap to view full screen icon
                                                    Surface(
                                                        modifier = Modifier
                                                            .align(Alignment.BottomEnd)
                                                            .padding(6.dp),
                                                        shape = CircleShape,
                                                        color = Color.Black.copy(alpha = 0.6f)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Fullscreen,
                                                            contentDescription = "View",
                                                            tint = Color.White,
                                                            modifier = Modifier
                                                                .padding(4.dp)
                                                                .size(14.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // Balance row if odd count
                                        if (rowItems.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        } else {
                            // Empty Media Placeholder
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onEdit()
                                        onDismiss()
                                    },
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, BentoBorderLight)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        tint = BentoPrimary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Text(
                                        text = "No device media attached yet",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = "Tap to add photos and videos from your phone",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }
                        }
                    }
                }

                // Geospatial Coordinates & Map Trigger Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(1.dp, BentoBorderLight.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Geospatial Coordinates",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Text(
                                text = String.format(Locale.US, "%.5f° N, %.5f° E", trip.latitude, trip.longitude),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Copy Coordinates
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Coordinates", "${trip.latitude}, ${trip.longitude}")
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Coordinates copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Coordinates",
                                    tint = BentoPrimary
                                )
                            }

                            // Focus on OpenStreetMap
                            Button(
                                onClick = {
                                    onFocusMap()
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Map", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Travel Journal Reflection Notes
                if (trip.description.isNotBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Expedition Journal Notes",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = trip.description,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 24.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                            )
                        )
                    }
                }

                // Regional Travel & Weather Guidance Tip Bento Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BentoAmberSecondary.copy(alpha = 0.08f)
                    ),
                    border = BorderStroke(1.dp, BentoAmberSecondary.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Thermostat,
                            contentDescription = null,
                            tint = BentoAmberSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Sri Lanka Regional Insight ($province Province)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BentoAmberSecondary
                                )
                            )
                            Text(
                                text = getRegionalTip(province),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                )
                            )
                        }
                    }
                }

                // Bottom Action Buttons (Toggle Visited, Edit, Delete)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onToggleStatus,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BentoBorderLight)
                    ) {
                        Text(
                            text = if (trip.isUpcoming) "Mark Visited" else "Set Upcoming",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            onEdit()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit", fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = {
                            onDelete()
                            onDismiss()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

private fun getRegionalTip(province: String): String {
    return when (province) {
        "Central" -> "Central Highlands (Kandy, Nuwara Eliya) feature crisp mountain weather (12°C - 22°C). Carry warm layers and rain jacket for tea estate walks."
        "Southern" -> "Southern Coast (Galle, Mirissa) enjoys tropical coastal warmth (28°C - 32°C). Best whale watching & surf conditions occur from November through April."
        "Uva" -> "Uva Province (Ella, Badulla) offers panoramic mountain views and waterfalls. Early mornings are best for hiking Little Adam's Peak & Ella Rock."
        "Northern" -> "Northern Province (Jaffna) has warm dry climate. Sample authentic Jaffna crab curry and fresh palmyra fruit while exploring the islands."
        "Eastern" -> "Eastern Coast (Trincomalee, Nilaveli, Arugam Bay) boasts calm seas & prime surfing from May through September when southwest monsoons hit other coasts."
        "North Central" -> "Cultural Triangle (Sigiriya, Anuradhapura) can be hot around midday. Climb rocks and explore ancient stupas before 9:00 AM or after 4:00 PM."
        "Western" -> "Western Province (Colombo, Negombo) is warm and humid year-round. Perfect for street food safaris, colonial architecture, and seaside sunsets."
        else -> "Sri Lanka has two distinct monsoon seasons. When it rains on the southwest coast, the east coast is sunny and crystal clear!"
    }
}
