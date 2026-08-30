package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.data.model.TripLocation
import com.example.data.repository.TripRepository
import com.example.ui.theme.BentoActiveIndicator
import com.example.ui.theme.BentoAmberSecondary
import com.example.ui.theme.BentoBorderLight
import com.example.ui.theme.BentoDeepPurple
import com.example.ui.theme.BentoGreenAccent
import com.example.ui.theme.BentoLavenderBorder
import com.example.ui.theme.BentoLavenderContainer
import com.example.ui.theme.BentoOnPrimaryContainer
import com.example.ui.theme.BentoOnRoseContainer
import com.example.ui.theme.BentoPinRed
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoRoseBorder
import com.example.ui.theme.BentoRoseContainer
import com.example.ui.theme.BentoSkyBlue
import com.example.ui.theme.BentoSlateMap
import com.example.ui.theme.BentoTextPrimaryLight
import com.example.ui.viewmodel.TripStats
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatsHeaderCard(
    modifier: Modifier = Modifier,
    stats: TripStats,
    latestTrip: TripLocation? = null,
    nextUpcomingTrip: TripLocation? = null,
    onQuickAdd: () -> Unit = {},
    onViewMap: () -> Unit = {}
) {
    val context = LocalContext.current
    val latestPhoto = latestTrip?.let { t ->
        t.coverImageUri ?: TripRepository.parseJsonArray(t.imageUrisJson).firstOrNull()
    } ?: "https://images.unsplash.com/photo-1546708973-b339540b5162?q=80&w=400&auto=format&fit=crop"

    val nextStopTitle = nextUpcomingTrip?.title ?: "Plan Your Next Stop"
    val nextStopDate = nextUpcomingTrip?.let {
        SimpleDateFormat("MMM d, hh:mm a", Locale.US).format(Date(it.dateEpochMillis))
    } ?: "Tap + to add destination"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stats_header_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Bento Section 1: Hero Map Preview Cell (rounded-28px, border-CAC4D0)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .testTag("bento_hero_cell"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = BentoSlateMap),
            border = BorderStroke(1.dp, BentoBorderLight.copy(alpha = 0.7f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onViewMap)
            ) {
                // Gradient Map Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    BentoSkyBlue.copy(alpha = 0.5f),
                                    BentoGreenAccent.copy(alpha = 0.25f),
                                    BentoLavenderContainer.copy(alpha = 0.35f)
                                )
                            )
                        )
                )

                // Center Pin & Location Tag
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Red Pin
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BentoPinRed)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }

                    // Frosted Destination Badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.92f),
                        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.08f)),
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            text = (latestTrip?.title ?: "SRI LANKA EXPEDITIONS").uppercase(Locale.US),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                fontSize = 10.sp,
                                color = BentoTextPrimaryLight
                            )
                        )
                    }
                }

                // Bottom Left & Right Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Current Altitude / Discovery Tag
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.92f),
                        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.08f)),
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (latestTrip != null) "LAST VISITED SPOT" else "EXPEDITION STATUS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = Color(0xFF64748B)
                                )
                            )
                            Text(
                                text = latestTrip?.locationName ?: "Ready to Explore",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1E293B)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Quick Map / Add Button
                    Surface(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onQuickAdd)
                            .testTag("bento_quick_add_btn"),
                        shape = RoundedCornerShape(14.dp),
                        color = BentoPrimary,
                        shadowElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add stop",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bento Section 2: Two-column grid (Stats Cell + Next Stop Cell)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left: Lavender Stats Bento Cell
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp)
                    .testTag("bento_stats_cell"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = BentoLavenderContainer),
                border = BorderStroke(1.dp, BentoLavenderBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BentoOnPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = "STATS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = BentoOnPrimaryContainer,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }

                    Column {
                        Text(
                            text = stats.totalFootprints.toString(),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = BentoOnPrimaryContainer,
                                letterSpacing = (-0.5).sp
                            )
                        )
                        Text(
                            text = "Active Footprints",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF49454F),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Right: White Next Stop Bento Cell
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp)
                    .testTag("bento_next_stop_cell"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BentoBorderLight.copy(alpha = 0.7f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "NEXT STOP",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 1.sp
                        )
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = nextStopTitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = nextStopDate,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        )
                    }

                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.67f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(BentoGreenAccent)
                        )
                    }
                }
            }
        }

        // Bento Section 3: Rose Latest Memory Bento Cell
        if (latestTrip != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bento_latest_memory_cell"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = BentoRoseContainer),
                border = BorderStroke(1.dp, BentoRoseBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Thumbnail image
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(latestPhoto)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Latest Memory",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .border(2.dp, Color.White, RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop
                    )

                    // Memory text details
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "LATEST MEMORY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = BentoOnRoseContainer.copy(alpha = 0.65f)
                            )
                        )
                        Text(
                            text = latestTrip.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = BentoOnRoseContainer
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = latestTrip.description.ifBlank { "Logged travel footprint with memorable moments." },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = BentoOnRoseContainer.copy(alpha = 0.8f),
                                lineHeight = 16.sp
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Bento Section 4: Secondary Progress & Island Exploration Bento Cell
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, BentoBorderLight.copy(alpha = 0.6f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            imageVector = Icons.Default.Route,
                            contentDescription = null,
                            tint = BentoPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Island Exploration: ${stats.uniqueProvinces}/9 Provinces",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    Text(
                        text = String.format(Locale.US, "%.0f km from Home (Total: %.0f km)", stats.roundTripFromHomeKm, stats.totalDistanceKm),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimary
                        )
                    )
                }

                LinearProgressIndicator(
                    progress = { (stats.uniqueProvinces / 9f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = BentoGreenAccent,
                    trackColor = Color(0xFFF1F5F9)
                )
            }
        }
    }
}

