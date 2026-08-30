package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.UserProfile
import com.example.util.GeoUtils
import com.lankafootprints.travelapp.data.model.StopType
import com.lankafootprints.travelapp.data.model.Trip
import com.lankafootprints.travelapp.data.model.TripStop
import com.lankafootprints.travelapp.data.model.TripWithStops
import com.lankafootprints.travelapp.data.repository.TripTimelineRepository
import com.lankafootprints.travelapp.data.seed.DestinationDataSeeder

data class EditableStop(
    val tempId: Long = System.currentTimeMillis() + (0..1000).random(),
    var stopName: String = "",
    var arrivalTime: String = "09:00 AM",
    var departureTime: String = "10:00 AM",
    var stopType: StopType = StopType.ATTRACTION,
    var latitude: Double = 7.8731,
    var longitude: Double = 80.7718,
    var notes: String = "",
    val mediaUris: MutableList<String> = mutableListOf()
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditMultiStopTripDialog(
    tripToEdit: TripWithStops? = null,
    userProfile: UserProfile,
    onDismissRequest: () -> Unit,
    onSaveTrip: (Trip, List<TripStop>) -> Unit,
    onPickCoordinatesOnMap: ((Double, Double) -> Unit)? = null
) {
    val context = LocalContext.current

    // Trip Header Fields
    var tripTitle by remember { mutableStateOf(tripToEdit?.trip?.tripTitle ?: "") }
    var originName by remember {
        mutableStateOf(
            tripToEdit?.trip?.originName ?: "Home (${userProfile.homeLocationName})"
        )
    }
    var originLat by remember {
        mutableStateOf(
            tripToEdit?.trip?.originLatitude ?: userProfile.homeLatitude
        )
    }
    var originLon by remember {
        mutableStateOf(
            tripToEdit?.trip?.originLongitude ?: userProfile.homeLongitude
        )
    }
    var departureTime by remember {
        mutableStateOf(tripToEdit?.trip?.departureTime ?: "06:30 AM")
    }
    var startDateEpoch by remember {
        mutableStateOf(tripToEdit?.trip?.startDateEpoch ?: System.currentTimeMillis())
    }

    // Dynamic Editable Stops
    val editableStops = remember {
        mutableStateListOf<EditableStop>().apply {
            if (tripToEdit != null && tripToEdit.stops.isNotEmpty()) {
                addAll(
                    tripToEdit.stops.sortedBy { it.stopOrder }.map { stop ->
                        EditableStop(
                            tempId = stop.stopId,
                            stopName = stop.stopName,
                            arrivalTime = stop.arrivalTime,
                            departureTime = stop.departureTime ?: "",
                            stopType = stop.stopType,
                            latitude = stop.latitude,
                            longitude = stop.longitude,
                            notes = stop.notes,
                            mediaUris = TripTimelineRepository.parseJsonArray(stop.mediaUrisJson).toMutableList()
                        )
                    }
                )
            } else {
                // Default first stop suggestion: Sigiriya Rock
                add(
                    EditableStop(
                        stopName = "Sigiriya Rock Fortress",
                        arrivalTime = "09:30 AM",
                        departureTime = "12:30 PM",
                        stopType = StopType.ATTRACTION,
                        latitude = 7.9570,
                        longitude = 80.7603,
                        notes = "Ancient rock palace & frescoes exploration",
                        mediaUris = mutableListOf("https://images.unsplash.com/photo-1586861635167-e5223aadc9fe?w=800")
                    )
                )
            }
        }
    }

    // Calculate live total distance
    val liveTotalDistanceKm by remember {
        derivedStateOf {
            if (editableStops.isEmpty()) 0.0
            var dist = 0.0
            var currLat = originLat
            var currLon = originLon
            for (st in editableStops) {
                dist += GeoUtils.calculateDistanceKm(currLat, currLon, st.latitude, st.longitude)
                currLat = st.latitude
                currLon = st.longitude
            }
            (dist * 10).toInt() / 10.0
        }
    }

    var showDestinationPickerForIndex by remember { mutableStateOf<Int?>(null) }
    var activeStopForMediaPick by remember { mutableStateOf<Int?>(null) }

    // Media Picker Launcher for attaching Photos & Videos
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        activeStopForMediaPick?.let { index ->
            if (index in 0..editableStops.lastIndex) {
                uris.forEach { uri ->
                    val uriStr = uri.toString()
                    if (!editableStops[index].mediaUris.contains(uriStr)) {
                        editableStops[index].mediaUris.add(uriStr)
                    }
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("multi_stop_builder_dialog"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.testTag("close_builder_button")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (tripToEdit == null) "Plan Multi-Stop Journey" else "Edit Journey Timeline",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Button(
                        onClick = {
                            if (tripTitle.isBlank()) return@Button
                            val trip = Trip(
                                tripId = tripToEdit?.trip?.tripId ?: 0L,
                                tripTitle = tripTitle.trim(),
                                startDateEpoch = startDateEpoch,
                                endDateEpoch = null,
                                originName = originName.trim(),
                                originLatitude = originLat,
                                originLongitude = originLon,
                                departureTime = departureTime.trim(),
                                totalDistanceKm = liveTotalDistanceKm
                            )
                            val stops = editableStops.mapIndexed { idx, st ->
                                TripStop(
                                    stopId = if (st.tempId < 1000000000000L) st.tempId else 0L,
                                    parentTripId = trip.tripId,
                                    stopName = st.stopName.ifBlank { "Stop ${idx + 1}" },
                                    arrivalTime = st.arrivalTime,
                                    departureTime = st.departureTime.ifBlank { null },
                                    stopType = st.stopType,
                                    latitude = st.latitude,
                                    longitude = st.longitude,
                                    notes = st.notes,
                                    mediaUrisJson = TripTimelineRepository.toJsonArray(st.mediaUris),
                                    stopOrder = idx + 1
                                )
                            }
                            onSaveTrip(trip, stops)
                        },
                        enabled = tripTitle.isNotBlank() && editableStops.isNotEmpty(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("save_journey_button")
                    ) {
                        Text("Save Journey", fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Section 1: Journey Details
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Journey Details",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = tripTitle,
                                    onValueChange = { tripTitle = it },
                                    label = { Text("Journey Name / Expedition Title") },
                                    placeholder = { Text("e.g., Cultural Triangle & Hill Country Tour") },
                                    modifier = Modifier.fillMaxWidth().testTag("journey_title_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = originName,
                                        onValueChange = { originName = it },
                                        label = { Text("Start Point (Origin)") },
                                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        modifier = Modifier.weight(1f).testTag("origin_name_input"),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = departureTime,
                                        onValueChange = { departureTime = it },
                                        label = { Text("Departure") },
                                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                                        modifier = Modifier.width(130.dp).testTag("departure_time_input"),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Distance & Stats Pill
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Route,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Estimated Journey Route: ",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "$liveTotalDistanceKm km (${editableStops.size} Stops)",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Section 2: Multi-Stop Stepper Builder
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Timeline Stops (${editableStops.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )

                            Button(
                                onClick = {
                                    editableStops.add(
                                        EditableStop(
                                            stopName = "",
                                            arrivalTime = "12:00 PM",
                                            departureTime = "01:00 PM",
                                            stopType = StopType.MEAL_BREAK,
                                            latitude = 7.8731,
                                            longitude = 80.7718
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("add_stop_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Stop", fontSize = 13.sp)
                            }
                        }
                    }

                    // Stop Cards List
                    itemsIndexed(editableStops) { index, stop ->
                        StopEditorCard(
                            index = index,
                            totalStops = editableStops.size,
                            stop = stop,
                            onUpdateStop = { updated ->
                                editableStops[index] = updated
                            },
                            onMoveUp = {
                                if (index > 0) {
                                    val item = editableStops.removeAt(index)
                                    editableStops.add(index - 1, item)
                                }
                            },
                            onMoveDown = {
                                if (index < editableStops.lastIndex) {
                                    val item = editableStops.removeAt(index)
                                    editableStops.add(index + 1, item)
                                }
                            },
                            onDelete = {
                                if (editableStops.size > 1) {
                                    editableStops.removeAt(index)
                                }
                            },
                            onSelectPresetDestination = {
                                showDestinationPickerForIndex = index
                            },
                            onPickMedia = {
                                activeStopForMediaPick = index
                                mediaPickerLauncher.launch("*/*")
                            }
                        )
                    }
                }
            }
        }
    }

    // Preset Destination Picker Dialog from 100+ Seeder
    if (showDestinationPickerForIndex != null) {
        val targetIdx = showDestinationPickerForIndex!!
        DestinationSelectionDialog(
            onDismissRequest = { showDestinationPickerForIndex = null },
            onDestinationSelected = { dest ->
                if (targetIdx in 0..editableStops.lastIndex) {
                    val cur = editableStops[targetIdx]
                    val updatedMedia = cur.mediaUris.toMutableList()
                    if (dest.imageUrl.isNotBlank() && !updatedMedia.contains(dest.imageUrl)) {
                        updatedMedia.add(dest.imageUrl)
                    }
                    editableStops[targetIdx] = cur.copy(
                        stopName = dest.name,
                        latitude = dest.latitude,
                        longitude = dest.longitude,
                        notes = dest.description,
                        mediaUris = updatedMedia
                    )
                }
                showDestinationPickerForIndex = null
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StopEditorCard(
    index: Int,
    totalStops: Int,
    stop: EditableStop,
    onUpdateStop: (EditableStop) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    onSelectPresetDestination: () -> Unit,
    onPickMedia: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth().testTag("stop_editor_card_$index")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row with Reorder & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${index + 1}",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Stop ${index + 1}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = index > 0,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = onMoveDown,
                        enabled = index < totalStops - 1,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        enabled = totalStops > 1,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Stop",
                            tint = if (totalStops > 1) MaterialTheme.colorScheme.error else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stop Name with quick selector
            OutlinedTextField(
                value = stop.stopName,
                onValueChange = { onUpdateStop(stop.copy(stopName = it)) },
                label = { Text("Stop / Attraction Name") },
                placeholder = { Text("e.g. Ambepussa Rest House / Sigiriya") },
                trailingIcon = {
                    TextButton(onClick = onSelectPresetDestination) {
                        Text("100+ Places", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Stop Type Chips
            Text(
                text = "Stop Category:",
                style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StopType.values().forEach { stType ->
                    FilterChip(
                        selected = stop.stopType == stType,
                        onClick = { onUpdateStop(stop.copy(stopType = stType)) },
                        label = { Text("${stType.emoji} ${stType.displayName}", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Arrival & Departure Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = stop.arrivalTime,
                    onValueChange = { onUpdateStop(stop.copy(arrivalTime = it)) },
                    label = { Text("Arrival Time") },
                    placeholder = { Text("08:15 AM") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = stop.departureTime,
                    onValueChange = { onUpdateStop(stop.copy(departureTime = it)) },
                    label = { Text("Departure Time") },
                    placeholder = { Text("09:00 AM") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Coordinates (Lat/Lng)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = stop.latitude.toString(),
                    onValueChange = { latStr ->
                        latStr.toDoubleOrNull()?.let { lat ->
                            onUpdateStop(stop.copy(latitude = lat))
                        }
                    },
                    label = { Text("Latitude") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = stop.longitude.toString(),
                    onValueChange = { lonStr ->
                        lonStr.toDoubleOrNull()?.let { lon ->
                            onUpdateStop(stop.copy(longitude = lon))
                        }
                    },
                    label = { Text("Longitude") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Notes / Travel Log
            OutlinedTextField(
                value = stop.notes,
                onValueChange = { onUpdateStop(stop.copy(notes = it)) },
                label = { Text("Stop Notes / Activity Log") },
                placeholder = { Text("Buffet breakfast, photo stops, ticket info...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Media (Photos & Videos) Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Media (${stop.mediaUris.size} Photos & Videos)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )

                OutlinedButton(
                    onClick = onPickMedia,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Media", fontSize = 12.sp)
                }
            }

            if (stop.mediaUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(stop.mediaUris) { uri ->
                        val isVid = isVideoMedia(uri)
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.DarkGray)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(uri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            if (isVid) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.35f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Videocam,
                                        contentDescription = "Video",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Delete Media Button
                            IconButton(
                                onClick = {
                                    stop.mediaUris.remove(uri)
                                    onUpdateStop(stop.copy(mediaUris = stop.mediaUris))
                                },
                                modifier = Modifier
                                    .size(22.dp)
                                    .align(Alignment.TopEnd)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.Close,
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
    }
}

@Composable
private fun DestinationSelectionDialog(
    onDismissRequest: () -> Unit,
    onDestinationSelected: (com.lankafootprints.travelapp.data.model.Destination) -> Unit
) {
    val destinations = remember { DestinationDataSeeder.get100PlusDestinations() }
    var searchQuery by remember { mutableStateOf("") }
    var selectedProvince by remember { mutableStateOf<String?>(null) }

    val filtered = remember(searchQuery, selectedProvince) {
        destinations.filter { dest ->
            val matchQuery = searchQuery.isBlank() ||
                    dest.name.contains(searchQuery, ignoreCase = true) ||
                    dest.sinhalaName.contains(searchQuery, ignoreCase = true) ||
                    dest.district.contains(searchQuery, ignoreCase = true) ||
                    dest.description.contains(searchQuery, ignoreCase = true)
            val matchProv = selectedProvince == null || dest.province.name.equals(selectedProvince, ignoreCase = true)
            matchQuery && matchProv
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select From 100+ Sri Lanka Destinations",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search by name, district, or keyword") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered) { dest ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDestinationSelected(dest) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(dest.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = dest.name,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dest.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${dest.sinhalaName} • ${dest.province.displayName} • ${dest.district}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Text(
                                        text = dest.description,
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
