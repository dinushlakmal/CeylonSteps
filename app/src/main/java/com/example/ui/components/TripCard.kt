package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.SriLankaDestinations
import com.example.data.model.TripLocation
import com.example.data.repository.TripRepository
import com.example.ui.theme.BentoAmberSecondary
import com.example.ui.theme.BentoBorderLight
import com.example.ui.theme.BentoGreenAccent
import com.example.ui.theme.BentoLavenderContainer
import com.example.ui.theme.BentoOnPrimaryContainer
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoRoseContainer
import com.example.ui.theme.UpcomingBadgeColor
import com.example.ui.theme.VisitedBadgeColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TripCard(
    modifier: Modifier = Modifier,
    trip: TripLocation,
    index: Int,
    distanceToNextKm: Double? = null,
    distanceFromHomeKm: Double? = null,
    onClick: () -> Unit,
    onMapClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    val photoUrls = TripRepository.parseJsonArray(trip.imageUrisJson)
    val coverUrl = trip.coverImageUri ?: photoUrls.firstOrNull()
    val province = SriLankaDestinations.findMatchingProvince(trip.latitude, trip.longitude)

    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
    val dateString = dateFormat.format(Date(trip.dateEpochMillis))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("trip_card_${trip.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, BentoBorderLight.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Photo Header Banner with Overlaid Badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (!coverUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(coverUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = trip.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback artistic Bento gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        BentoPrimary,
                                        BentoAmberSecondary.copy(alpha = 0.8f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                // Top Gradient Scrim for contrast
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.65f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Top Badges: Stop Number & Visited/Upcoming Status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Stop Number / Plan Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (trip.isUpcoming) "Plan #$index" else "Stop #$index",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    // Visited vs Upcoming Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (trip.isUpcoming) UpcomingBadgeColor else VisitedBadgeColor
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (trip.isUpcoming) Icons.Default.Schedule else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (trip.isUpcoming) "Upcoming" else "Visited",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }

                // Bottom Photo Count Indicator (if multiple photos)
                if (photoUrls.size > 1) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Photos",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${photoUrls.size} Photos",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            // Card Body Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Title
                Text(
                    text = trip.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Location Subhead
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = BentoPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = trip.locationName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Chips Row: Date, Province, GPS Coordinates, Distance to Next
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Date Chip
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BentoLavenderContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = BentoOnPrimaryContainer,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = dateString,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = BentoOnPrimaryContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    // Province Chip
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BentoRoseContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "$province Province",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF672338)
                            )
                        )
                    }

                    // Coordinates Chip
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.3f°N, %.3f°E", trip.latitude, trip.longitude),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        )
                    }

                    // Distance to next stop
                    if (distanceToNextKm != null && distanceToNextKm > 0) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BentoAmberSecondary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = String.format(Locale.US, "➔ %.1f km to next", distanceToNextKm),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BentoAmberSecondary
                                )
                            )
                        }
                    }

                    // Distance from home
                    if (distanceFromHomeKm != null && distanceFromHomeKm > 0) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BentoPrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.1f km from Home", distanceFromHomeKm),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BentoPrimary
                                )
                            )
                        }
                    }
                }

                // Description Journal Snippet
                if (trip.description.isNotBlank()) {
                    Text(
                        text = trip.description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            lineHeight = 20.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Bottom Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Map Focus Button
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onMapClick)
                            .testTag("btn_map_focus_${trip.id}"),
                        shape = RoundedCornerShape(14.dp),
                        color = BentoPrimary.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Focus on Map",
                                tint = BentoPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "View on Map",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BentoPrimary
                                )
                            )
                        }
                    }

                    // Edit & Delete Icons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier.size(36.dp).testTag("btn_edit_${trip.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Footprint",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(36.dp).testTag("btn_delete_${trip.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Footprint",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

