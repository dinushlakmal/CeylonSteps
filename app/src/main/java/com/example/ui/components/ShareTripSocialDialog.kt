package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ceylonsteps.travelapp.auth.UserManager as AuthUserManager
import com.ceylonsteps.travelapp.data.model.TripWithStops
import com.ceylonsteps.travelapp.data.repository.TripTimelineRepository
import com.example.data.model.SriLankaDestinations
import com.example.data.model.TripLocation
import com.example.data.model.UserProfile
import com.example.data.model.social.SocialPost
import com.example.data.repository.FirestoreSocialEngine
import com.example.data.repository.TripRepository
import com.example.ui.theme.BentoAmberSecondary
import com.example.ui.theme.BentoBorderLight
import com.example.ui.theme.BentoPrimary
import com.example.util.ExplorerRankEngine
import com.example.util.GoogleDriveMediaEngine
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.coroutines.launch

data class DistrictPreset(
    val name: String,
    val sinhalaName: String,
    val province: String,
    val latitude: Double,
    val longitude: Double
)

val SRI_LANKA_DISTRICTS = listOf(
    DistrictPreset("Colombo", "කොළඹ", "Western", 6.9271, 79.8612),
    DistrictPreset("Gampaha", "ගම්පහ", "Western", 7.0840, 79.9943),
    DistrictPreset("Kalutara", "කළුතර", "Western", 6.5854, 79.9607),
    DistrictPreset("Kandy", "මහනුවර", "Central", 7.2906, 80.6337),
    DistrictPreset("Matale", "මාතලේ", "Central", 7.4675, 80.6234),
    DistrictPreset("Nuwara Eliya", "නුවරඑළිය", "Central", 6.9497, 80.7891),
    DistrictPreset("Galle", "ගාල්ල", "Southern", 6.0535, 80.2210),
    DistrictPreset("Matara", "මාතර", "Southern", 5.9549, 80.5550),
    DistrictPreset("Hambantota", "හම්බන්තොට", "Southern", 6.1429, 81.1212),
    DistrictPreset("Badulla", "බදුල්ල / ඇල්ල", "Uva", 6.9934, 81.0550),
    DistrictPreset("Monaragala", "මොණරාගල", "Uva", 6.8728, 81.3507),
    DistrictPreset("Ratnapura", "රත්නපුර", "Sabaragamuwa", 6.6828, 80.3992),
    DistrictPreset("Kegalle", "කෑගල්ල", "Sabaragamuwa", 7.2513, 80.3464),
    DistrictPreset("Kurunegala", "කුරුණෑගල", "North Western", 7.4863, 80.3623),
    DistrictPreset("Puttalam", "පුත්තලම", "North Western", 8.0408, 79.8394),
    DistrictPreset("Anuradhapura", "අනුරාධපුරය", "North Central", 8.3114, 80.4037),
    DistrictPreset("Polonnaruwa", "පොළොන්නරුව", "North Central", 7.9403, 81.0188),
    DistrictPreset("Trincomalee", "ත්රිකුණාමලය", "Eastern", 8.5874, 81.2152),
    DistrictPreset("Batticaloa", "මඩකලපුව", "Eastern", 7.7310, 81.6747),
    DistrictPreset("Ampara", "අම්පාර", "Eastern", 7.2974, 81.6747),
    DistrictPreset("Jaffna", "යාපනය", "Northern", 9.6615, 80.0255),
    DistrictPreset("Kilinochchi", "කිලිනොච්චිය", "Northern", 9.3803, 80.3770),
    DistrictPreset("Mannar", "මන්නාරම", "Northern", 8.9810, 79.9044),
    DistrictPreset("Mullaitivu", "මුලතිව්", "Northern", 9.2671, 80.8142),
    DistrictPreset("Vavuniya", "වවුනියාව", "Northern", 8.7542, 80.4982)
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ShareTripSocialDialog(
    userProfile: UserProfile,
    trips: List<TripLocation>,
    journeys: List<TripWithStops>,
    onDismiss: () -> Unit,
    onPostCreated: (SocialPost) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 0: Custom Place & Story (No GPS needed), 1: Single Trip (from Journal), 2: Multi-Stop Journey
    var shareMode by remember { mutableIntStateOf(0) }
    var selectedTrip by remember { mutableStateOf(trips.firstOrNull()) }
    var selectedJourney by remember { mutableStateOf(journeys.firstOrNull()) }

    // Form Fields
    var customTitle by remember { mutableStateOf("") }
    var customLocationName by remember { mutableStateOf("") }
    var selectedDistrict by remember { mutableStateOf(SRI_LANKA_DISTRICTS[3]) } // Default Kandy/Central
    var selectedProvince by remember { mutableStateOf("Central") }
    var customLat by remember { mutableStateOf(7.2906) }
    var customLng by remember { mutableStateOf(80.6337) }
    var story by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("SCENIC") }
    var mediaUris by remember { mutableStateOf<List<String>>(emptyList()) }
    var isPublicPost by remember { mutableStateOf(true) }

    // Dropdown state for District Selection
    var isDistrictDropdownOpen by remember { mutableStateOf(false) }

    // Landmark Quick Suggestions
    var isLocationSearchFocused by remember { mutableStateOf(false) }

    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableIntStateOf(0) }
    var uploadStatusText by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val stringUris = uris.map { it.toString() }
            mediaUris = (mediaUris + stringUris).distinct()
        }
    }

    val googleAccount = remember { GoogleSignIn.getLastSignedInAccount(context) }
    val loggedInAuthUser = remember { AuthUserManager.getLoggedInUser(context) }

    val categories = listOf("SCENIC", "HERITAGE", "BEACH", "HIKING", "WATERFALL", "WILDLIFE", "FOOD", "ROADTRIP", "CULTURAL")

    val userRank = remember(trips, journeys) {
        val uniqueProvinces = trips.map { SriLankaDestinations.findMatchingProvince(it.latitude, it.longitude) }.distinct().size
        ExplorerRankEngine.calculateRank(
            visitedPlacesCount = trips.size.coerceAtLeast(1),
            uniqueProvincesCount = uniqueProvinces.coerceAtLeast(1),
            postsCount = 1,
            journeysCount = journeys.size
        )
    }

    // Filter destination suggestions based on customLocationName
    val destinationSuggestions = remember(customLocationName) {
        if (customLocationName.length >= 2) {
            SriLankaDestinations.searchDestinations(customLocationName).take(5)
        } else {
            emptyList()
        }
    }

    Dialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp)
                .padding(vertical = 12.dp)
                .testTag("share_trip_social_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = BentoPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.TravelExplore,
                                    contentDescription = null,
                                    tint = BentoPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Share Your Story",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "Post to CeylonSteps Traveler Community",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isUploading,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Source Tabs (3 Options: Custom Story vs Visited Footprint vs Road Trip)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Option 0: Custom Place (No GPS needed)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (shareMode == 0) BentoPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, if (shareMode == 0) BentoPrimary else BentoBorderLight),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                shareMode = 0
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditLocation,
                                contentDescription = null,
                                tint = if (shareMode == 0) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Custom Place",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (shareMode == 0) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    // Option 1: Visited Spot (From Journal)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (shareMode == 1) BentoPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, if (shareMode == 1) BentoPrimary else BentoBorderLight),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                shareMode = 1
                                selectedTrip?.let {
                                    customTitle = it.title
                                    customLocationName = it.locationName.ifBlank { it.title }
                                    customLat = it.latitude
                                    customLng = it.longitude
                                    selectedProvince = SriLankaDestinations.findMatchingProvince(it.latitude, it.longitude)
                                    story = it.description.ifBlank { "Visited ${it.title} in Sri Lanka!" }
                                    selectedCategory = "SCENIC"
                                    mediaUris = TripRepository.parseJsonArray(it.imageUrisJson)
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = if (shareMode == 1) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Journal (${trips.size})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (shareMode == 1) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    // Option 2: Road Trip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (shareMode == 2) BentoPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, if (shareMode == 2) BentoPrimary else BentoBorderLight),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                shareMode = 2
                                selectedJourney?.let {
                                    customTitle = it.trip.tripTitle
                                    customLocationName = it.trip.originName.ifBlank { it.trip.tripTitle }
                                    story = "Road trip with ${it.stops.size} stops: " + it.stops.joinToString(" ➔ ") { s -> s.stopName }
                                    selectedCategory = "ROADTRIP"
                                    mediaUris = it.stops.flatMap { s -> TripTimelineRepository.parseJsonArray(s.mediaUrisJson) }
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Route,
                                contentDescription = null,
                                tint = if (shareMode == 2) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Road Trip (${journeys.size})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (shareMode == 2) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mode Specific Pickers
                if (shareMode == 1) {
                    // Pick from saved Journal Spots
                    if (trips.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "No recorded single trips in Journal yet. Switch to \"Custom Place\" above to type any place name directly!",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "Choose Visited Footprint:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        ) {
                            items(trips) { trip ->
                                val isSelected = selectedTrip?.id == trip.id
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) BentoPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(1.dp, if (isSelected) BentoPrimary else BentoBorderLight),
                                    modifier = Modifier.clickable {
                                        selectedTrip = trip
                                        customTitle = trip.title
                                        customLocationName = trip.locationName.ifBlank { trip.title }
                                        customLat = trip.latitude
                                        customLng = trip.longitude
                                        selectedProvince = SriLankaDestinations.findMatchingProvince(trip.latitude, trip.longitude)
                                        story = trip.description.ifBlank { "Visited ${trip.title} in Sri Lanka!" }
                                        mediaUris = TripRepository.parseJsonArray(trip.imageUrisJson)
                                    }
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                        Text(
                                            text = trip.title,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) BentoPrimary else MaterialTheme.colorScheme.onSurface
                                            ),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = trip.locationName.ifBlank { "Sri Lanka" },
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
                } else if (shareMode == 2) {
                    // Pick from saved Road Trips
                    if (journeys.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "No multi-stop road trips saved yet. Switch to \"Custom Place\" above to share your journey!",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "Choose Road Trip Itinerary:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        ) {
                            items(journeys) { journey ->
                                val isSelected = selectedJourney?.trip?.tripId == journey.trip.tripId
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) BentoPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(1.dp, if (isSelected) BentoPrimary else BentoBorderLight),
                                    modifier = Modifier.clickable {
                                        selectedJourney = journey
                                        customTitle = journey.trip.tripTitle
                                        customLocationName = journey.trip.originName.ifBlank { journey.trip.tripTitle }
                                        story = "Explored ${journey.stops.size} stops: " + journey.stops.joinToString(", ") { it.stopName }
                                        mediaUris = journey.stops.flatMap { TripTimelineRepository.parseJsonArray(it.mediaUrisJson) }
                                    }
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                        Text(
                                            text = journey.trip.tripTitle,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) BentoPrimary else MaterialTheme.colorScheme.onSurface
                                            ),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${journey.stops.size} stops • ${String.format(java.util.Locale.US, "%.1f", journey.trip.totalDistanceKm)} km",
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

                // ================= LOCATION & DISTRICT INPUT SECTION =================
                Text(
                    text = "Location & District",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = BentoPrimary)
                )
                Spacer(modifier = Modifier.height(4.dp))

                // 1. Location / Destination Name Field
                OutlinedTextField(
                    value = customLocationName,
                    onValueChange = {
                        customLocationName = it
                        if (customTitle.isBlank()) {
                            customTitle = "Trip to $it"
                        }
                    },
                    placeholder = { Text("e.g., Sigiriya, Ella Rock, Galle Fort, Mirissa Beach...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Place, contentDescription = null, tint = BentoPrimary)
                    },
                    trailingIcon = {
                        if (customLocationName.isNotBlank()) {
                            IconButton(onClick = { customLocationName = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoPrimary,
                        unfocusedBorderColor = BentoBorderLight
                    )
                )

                // Landmark Autocomplete / Suggestion Chips
                if (destinationSuggestions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Quick Match:",
                        style = MaterialTheme.typography.labelSmall.copy(color = BentoAmberSecondary, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(destinationSuggestions) { landmark ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BentoPrimary.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, BentoPrimary.copy(alpha = 0.3f)),
                                modifier = Modifier.clickable {
                                    customLocationName = "${landmark.title}, ${landmark.district}"
                                    if (customTitle.isBlank() || customTitle.startsWith("Trip to")) {
                                        customTitle = landmark.title
                                    }
                                    selectedProvince = landmark.province
                                    customLat = landmark.latitude
                                    customLng = landmark.longitude
                                    SRI_LANKA_DISTRICTS.firstOrNull { it.name.equals(landmark.district, ignoreCase = true) }?.let {
                                        selectedDistrict = it
                                    }
                                    if (story.isBlank()) {
                                        story = landmark.description
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = BentoPrimary, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${landmark.title} (${landmark.district})",
                                        style = MaterialTheme.typography.labelSmall.copy(color = BentoPrimary, fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. District & Province Selector Dropdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, BentoBorderLight),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { isDistrictDropdownOpen = true }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.LocationCity, contentDescription = null, tint = BentoPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "District / දිස්ත්‍රික්කය",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                        Text(
                                            text = "${selectedDistrict.name} (${selectedDistrict.sinhalaName})",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = if (isDistrictDropdownOpen) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = isDistrictDropdownOpen,
                            onDismissRequest = { isDistrictDropdownOpen = false },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .heightIn(max = 320.dp)
                        ) {
                            Text(
                                text = "Select Sri Lanka District",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = BentoPrimary),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            HorizontalDivider()
                            SRI_LANKA_DISTRICTS.forEach { district ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(text = "${district.name} • ${district.sinhalaName}", fontWeight = FontWeight.Bold)
                                                Text(text = "${district.province} Province", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            if (selectedDistrict.name == district.name) {
                                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = BentoPrimary, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedDistrict = district
                                        selectedProvince = district.province
                                        customLat = district.latitude
                                        customLng = district.longitude
                                        if (customLocationName.isBlank()) {
                                            customLocationName = district.name
                                        }
                                        isDistrictDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }

                    // Province Badge Display
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BentoPrimary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, BentoPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.padding(start = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Province",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = BentoPrimary)
                            )
                            Text(
                                text = selectedProvince,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = BentoPrimary)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ================= STORY TITLE & CONTENT =================
                Text(
                    text = "Story Title",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = customTitle,
                    onValueChange = { customTitle = it },
                    placeholder = { Text("e.g., Epic Sunrise Hike to Adam's Peak") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoPrimary,
                        unfocusedBorderColor = BentoBorderLight
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Story / Description Input
                Text(
                    text = "Your Experience & Tips",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = story,
                    onValueChange = { story = it },
                    placeholder = { Text("Share what you explored, road condition, entry tickets, food recommendations or travel tips...") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoPrimary,
                        unfocusedBorderColor = BentoBorderLight
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Chips
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BentoPrimary.copy(alpha = 0.15f),
                                selectedLabelColor = BentoPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Photos & Media Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Photos & Memories (${mediaUris.size})",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Add photos to inspire fellow travelers",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        )
                    }

                    OutlinedButton(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Photos", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (mediaUris.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(mediaUris) { uriStr ->
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(uriStr)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(72.dp)
                                )

                                Surface(
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.65f),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            mediaUris = mediaUris.filter { it != uriStr }
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Privacy / Visibility Selection (Public vs Private)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isPublicPost) BentoPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, if (isPublicPost) BentoPrimary.copy(alpha = 0.4f) else BentoBorderLight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { isPublicPost = !isPublicPost }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isPublicPost) BentoPrimary else MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isPublicPost) Icons.Default.Public else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isPublicPost) "Public Story" else "Private Story",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isPublicPost) BentoPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isPublicPost) BentoPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    ) {
                                        Text(
                                            text = if (isPublicPost) "COMMUNITY FEED" else "ONLY YOU",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (isPublicPost) BentoPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (isPublicPost)
                                        "Shared with all CeylonSteps explorers & profile visitors"
                                    else
                                        "Saved privately on your profile & journal only",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = isPublicPost,
                            onCheckedChange = { isPublicPost = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BentoPrimary
                            )
                        )
                    }
                }

                // Author Info Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BentoAmberSecondary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, BentoAmberSecondary.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = BentoAmberSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val displayName = userProfile.userName.ifBlank { googleAccount?.displayName ?: loggedInAuthUser?.displayName ?: "Ceylon Traveler" }
                            Text(
                                text = "Posting as $displayName (Level ${userRank.level})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                        Text(
                            text = "${userRank.currentXp} XP",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = BentoAmberSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Indicator
                if (isUploading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = uploadStatusText,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = BentoPrimary
                            )
                            Text(
                                text = "$uploadProgress%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = BentoPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { uploadProgress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = BentoPrimary
                        )
                    }
                }

                // Publish Action Button
                Button(
                    onClick = {
                        val titleToUse = customTitle.ifBlank {
                            if (customLocationName.isNotBlank()) "Exploring $customLocationName" else "Ceylon Adventure"
                        }
                        val finalLocation = customLocationName.ifBlank {
                            selectedDistrict.name + ", " + selectedProvince
                        }

                        val isMulti = shareMode == 2
                        val totalDist = if (shareMode == 2) selectedJourney?.trip?.totalDistanceKm ?: 0.0 else 0.0
                        val stopCount = if (shareMode == 2) selectedJourney?.stops?.size ?: 1 else 1
                        val stopNames = if (shareMode == 2) selectedJourney?.stops?.map { it.stopName } ?: listOf(finalLocation) else listOf(finalLocation)

                        isUploading = true
                        uploadProgress = 15
                        uploadStatusText = "Preparing story and photos..."

                        scope.launch {
                            try {
                                var directCdnUrls = mediaUris.filter { it.startsWith("http://") || it.startsWith("https://") }
                                val localMediaToUpload = mediaUris.filter { !it.startsWith("http://") && !it.startsWith("https://") }

                                if (localMediaToUpload.isNotEmpty()) {
                                    if (googleAccount != null) {
                                        uploadStatusText = "Uploading ${localMediaToUpload.size} media files..."
                                        uploadProgress = 35
                                        val uploadResult = try {
                                            GoogleDriveMediaEngine.uploadMediaList(
                                                context = context,
                                                account = googleAccount,
                                                mediaUris = localMediaToUpload,
                                                onProgress = { p, status ->
                                                    uploadProgress = (35 + (p * 0.45f)).toInt().coerceIn(35, 80)
                                                    uploadStatusText = status
                                                }
                                            )
                                        } catch (e: Exception) {
                                            null
                                        }

                                        if (uploadResult != null && uploadResult.success && uploadResult.directCdnUrls.isNotEmpty()) {
                                            directCdnUrls = directCdnUrls + uploadResult.directCdnUrls
                                        } else {
                                            directCdnUrls = directCdnUrls + localMediaToUpload
                                        }
                                    } else {
                                        uploadProgress = 60
                                        directCdnUrls = directCdnUrls + localMediaToUpload
                                    }
                                }

                                uploadStatusText = "Publishing story to CeylonSteps Community..."
                                uploadProgress = 85

                                val authorId = userProfile.userEmail.ifBlank {
                                    googleAccount?.email ?: googleAccount?.id ?: loggedInAuthUser?.email ?: loggedInAuthUser?.googleId ?: userProfile.userName.ifBlank { "user_local_explorer" }
                                }
                                val authorName = userProfile.userName.ifBlank {
                                    googleAccount?.displayName ?: loggedInAuthUser?.displayName ?: "Ceylon Traveler"
                                }

                                val post = SocialPost(
                                    authorId = authorId,
                                    authorName = authorName,
                                    authorAvatarUrl = googleAccount?.photoUrl?.toString() ?: loggedInAuthUser?.photoUrl ?: userProfile.profileImageUri,
                                    authorBadge = userRank.title,
                                    authorRankLevel = userRank.level,
                                    title = titleToUse,
                                    story = story.ifBlank { "Visited $finalLocation in $selectedProvince Province!" },
                                    locationName = finalLocation,
                                    latitude = customLat,
                                    longitude = customLng,
                                    province = selectedProvince,
                                    category = selectedCategory,
                                    mediaUrls = directCdnUrls,
                                    isMultiStop = isMulti,
                                    totalDistanceKm = totalDist,
                                    stopCount = stopCount,
                                    stopNames = stopNames,
                                    tags = listOf("#$selectedProvince", "#${selectedDistrict.name}", "#${selectedCategory.lowercase()}", "#CeylonSteps"),
                                    isPublic = isPublicPost
                                )

                                uploadProgress = 95
                                val socialEngine = FirestoreSocialEngine.getInstance(context)
                                socialEngine.publishPost(post)

                                uploadProgress = 100
                                uploadStatusText = "Published successfully!"

                                Toast.makeText(context, "🎉 Story shared with CeylonSteps Community!", Toast.LENGTH_LONG).show()
                                onPostCreated(post)
                                onDismiss()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Note: Saved to local community feed", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            } finally {
                                isUploading = false
                            }
                        }
                    },
                    enabled = !isUploading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BentoPrimary,
                        contentColor = Color.White
                    )
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Publishing...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Publish Story to Feed 🇱🇰", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
